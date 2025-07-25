/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.appclient.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.glassfish.appclient.client.jws.boot.ErrorDisplayDialog;
import org.glassfish.appclient.client.jws.boot.LaunchSecurityHelper;
import org.glassfish.embeddable.client.UserError;

/**
 *
 * @author tjquinn
 */
public class JWSAppClientContainerMain {

    public static final String SECURITY_CONFIG_PATH_PLACEHOLDER = "security.config.path";

    private static final Logger logger = Logger.getLogger(JWSAppClientContainerMain.class.getName());

    private static final String ENDORSED_PACKAGE_PROPERTY_NAME = "endorsed-standard-packages";

    /** localizable strings */
    private static final ResourceBundle rb = ResourceBundle
            .getBundle(JWSAppClientContainerMain.class.getPackage().getName().replaceAll("\\.", "/") + ".LocalStrings");

    /** unpublished command-line argument conveying jwsacc information */
    private static final Map<JWSACCSetting, String> jwsaccSettings = new EnumMap<JWSACCSetting, String>(JWSACCSetting.class);

    private static final String JWSACC_PROPERTY_NAME_PREFIX = "javaws.acc.";

    /**
     * request to exit the JVM upon return from the client - should be set (via the -jwsacc command-line argument value)
     * only for command-line clients; otherwise it can prematurely end the JVM when the GUI and other user work is
     * continuing
     */
    private static final String JWSACC_EXIT_AFTER_RETURN = "exitAfterReturn";

    private static final String JWSACC_FORCE_ERROR = "forceError";

//    private static final String JWSACC_KEEP_JWS_CLASS_LOADER = "keepJWSClassLoader";

    private static final String JWSACC_RUN_ON_SWING_THREAD = "runOnSwingThread";

    private static final String JWSACC_TEST_OUTPUT = "testOutput";

    private static ExitManager exitManager = null;

    private static long now;

    private static boolean isTestMode() {
        return exitManager != null;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            now = System.currentTimeMillis();
            // Process any arguments (conveyed as properties in the JNLP) intended for the JWS-aware ACC.
            processJWSArgs();

            final String agentArgsText = System.getProperty("agent.args");
            LaunchSecurityHelper.setPermissions();

            // Prevent the Java Web Start class loader from delegating to its parent when resolving
            // classes and resources that should come from the GlassFish-provided endorsed JARs.
            insertMaskingLoader();

            final ClientRunner runner = new ClientRunner(agentArgsText, args);
            if (runOnSwingThread()) {
                SwingUtilities.invokeAndWait(runner);
            } else {
                runner.run();
            }
        } catch (Throwable thr) {
            if (isTestMode()) {
                exitManager.recordFailure(thr);
            }
            throw new RuntimeException(rb.getString("jwsacc.errorLaunch"), thr);
        }

    }

    /*
     * Launches the client. This is in its own class so we can either run it directly or run it on the Swing EDT, if
     * requested by a jwsacc command line argument.
     */
    private static class ClientRunner implements Runnable {
        private final String agentArgsText;
        private final String[] args;

        private ClientRunner(final String agentArgsText, final String[] args) {
            this.agentArgsText = agentArgsText;
            this.args = args;
        }

        @Override
        public void run() {
            try {
                AppClientContainerHolder.init(agentArgsText, null);
                AppClientContainerHolder.getInstance().launch(args);
                logger.log(Level.FINE, "JWSAppClientContainer finished after {0} ms", (System.currentTimeMillis() - now));
            } catch (UserError ue) {
                if (isTestMode()) {
                    throw new RuntimeException(ue);
                }
                ErrorDisplayDialog.showUserError(ue, rb);
            } catch (Throwable thr) {
                throw new RuntimeException(thr);
            }
        }
    }

    private static void insertMaskingLoader() throws IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        final String loaderConfig = System.getProperty("loader.config");
        StringReader sr = new StringReader(loaderConfig);
        final Properties props = new Properties();
        props.load(sr);

        final ClassLoader jwsLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader mcl = getMaskingClassLoader(jwsLoader.getParent(), props);
        final Field jwsLoaderParentField = ClassLoader.class.getDeclaredField("parent");
        jwsLoaderParentField.setAccessible(true);
        jwsLoaderParentField.set(jwsLoader, mcl);
    }

    private static ClassLoader getMaskingClassLoader(final ClassLoader parent, final Properties props) {

        final Collection<String> endorsedPackagesToMask = prepareEndorsedPackages(props.getProperty(ENDORSED_PACKAGE_PROPERTY_NAME));
        return new JWSACCMaskingClassLoader(parent, endorsedPackagesToMask);
    }

    private static Collection<String> prepareEndorsedPackages(final String packageNames) {
        final Collection<String> result = new HashSet<String>();
        for (String s : packageNames.split(",")) {
            s = s.trim();
            if (s.length() > 0) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Interpret the JWSACC arguments (if any) supplied on the command line.
     *
     * @param args the JWSACC arguments
     */
    private static void processJWSArgs() {

        String propValue;
        for (int i = 0; (propValue = System.getProperty(JWSACC_PROPERTY_NAME_PREFIX + i)) != null; i++) {
            final int equals = propValue.indexOf('=');
            final JWSACCSetting setting = JWSACCSetting.find((equals == -1 ? propValue : propValue.substring(0, equals)));
            if (setting != null) {
                final String settingValue = (equals == -1 ? "" : propValue.substring(equals + 1));
                jwsaccSettings.put(setting, settingValue);
                setting.run(settingValue);
            }
        }
    }

    private static boolean runOnSwingThread() {
        return jwsaccSettings.containsKey(JWSACCSetting.RUN_ON_SWING_THREAD);
    }

//    private static boolean forceError() {
//        return jwsaccSettings.containsKey(JWSACCSetting.FORCE_ERROR);
//    }
//
//    private static String testOutput() {
//        return jwsaccSettings.get(JWSACCSetting.TEST_OUTPUT);
//    }

    private enum JWSACCSetting {
        EXIT_AFTER_RETURN(JWSACC_EXIT_AFTER_RETURN), FORCE_ERROR(JWSACC_FORCE_ERROR), RUN_ON_SWING_THREAD(JWSACC_RUN_ON_SWING_THREAD),
        TEST_OUTPUT(JWSACC_TEST_OUTPUT, new Runner() {

            @Override
            public void run(final String testOutputFile) {
                prepareTestMode(testOutputFile);
            }

        });

        private static class Runner {
            protected void run(final String info) {
            }
        }

        private final String propertyNameSuffix;
        private final Runner action;

        private JWSACCSetting(final String propertyNameSuffix) {
            this(propertyNameSuffix, new Runner());
        }

        private JWSACCSetting(final String propertyNameSuffix, final Runner action) {
            this.propertyNameSuffix = propertyNameSuffix;
            this.action = action;
        }

        private void run(final String runInfo) {
            if (action != null) {
                action.run(runInfo);
            }
        }

        private static JWSACCSetting find(final String targetSuffix) {
            for (JWSACCSetting candidate : values()) {
                if (candidate.propertyNameSuffix.equals(targetSuffix)) {
                    return candidate;
                }
            }
            return null;
        }
    }

    private static void prepareTestMode(final String testReportLocation) {
        exitManager = new ExitManager(testReportLocation);
        try {
            final SplitPrintStream splitPS = new SplitPrintStream(System.out, new File(testReportLocation));
            System.setOut(splitPS);
            logger.log(Level.FINE, "Also sending output to {0}", testReportLocation);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Sends output to two streams, one an original one and one to a new File.
     */
    private static class SplitPrintStream extends PrintStream {

        private final PrintStream originalPS;

        public SplitPrintStream(final PrintStream originalPS, final File newOutputFile) throws FileNotFoundException {
            super(newOutputFile);
            this.originalPS = originalPS;
        }

        @Override
        public void write(byte[] b) throws IOException {
            super.write(b);
            originalPS.write(b);
        }

        @Override
        public void write(int b) {
            super.write(b);
            originalPS.write(b);
        }

        @Override
        public void write(byte[] buf, int off, int len) {
            super.write(buf, off, len);
            originalPS.write(buf, off, len);
        }
    }
}
