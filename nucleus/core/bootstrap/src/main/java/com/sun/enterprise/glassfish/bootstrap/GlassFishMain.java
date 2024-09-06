/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.glassfish.bootstrap.cfg.AsenvConf;
import com.sun.enterprise.glassfish.bootstrap.cfg.OsgiPlatform;
import com.sun.enterprise.glassfish.bootstrap.cfg.ServerFiles;
import com.sun.enterprise.glassfish.bootstrap.cfg.StartupContextCfg;
import com.sun.enterprise.glassfish.bootstrap.cfg.StartupContextUtil;
import com.sun.enterprise.glassfish.bootstrap.cp.ClassLoaderBuilder;
import com.sun.enterprise.glassfish.bootstrap.cp.GlassfishBootstrapClassLoader;
import com.sun.enterprise.glassfish.bootstrap.log.LogFacade;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;

import static com.sun.enterprise.glassfish.bootstrap.StartupContextCfgFactory.createStartupContextCfg;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.PLATFORM_PROPERTY_KEY;
import static com.sun.enterprise.glassfish.bootstrap.log.LogFacade.BOOTSTRAP_LOGGER;
import static com.sun.enterprise.module.bootstrap.ArgumentManager.argsToMap;
import static java.util.logging.Level.SEVERE;
import static org.glassfish.main.jul.cfg.GlassFishLoggingConstants.CLASS_INITIALIZER;
import static org.glassfish.main.jul.cfg.GlassFishLoggingConstants.KEY_TRACING_ENABLED;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author David Matejcek
 */
public class GlassFishMain {

    /**
     * true enable 'logging of logging' so you can watch the order of actions in standard outputs.
     */
    private static final String ENV_AS_TRACE_LOGGING = "AS_TRACE_LOGGING";
    /**
     * <ul>
     * <li>true defers log record resolution to a moment when logging configuration is loaded from
     * logging.properties.
     * <li>false means that log record's level is compared with default logger settings which is
     * usually INFO/WARNING. Records with FINE, FINER, FINEST will be lost.
     * </ul>
     */
    private static final String ENV_AS_TRACE_BOOTSTRAP = "AS_TRACE_BOOTSTRAP";

    private static final String DEFAULT_DOMAINS_DIR_PROPNAME = "AS_DEF_DOMAINS_PATH";

    // logging system may override original output streams.
    private static final PrintStream STDOUT = System.out;

    public static void main(final String[] args) throws Exception {
        final File installRoot = StartupContextUtil.detectInstallRoot();
        final ClassLoader jdkExtensionCL = ClassLoader.getSystemClassLoader().getParent();
        final GlassfishBootstrapClassLoader gfBootCL = new GlassfishBootstrapClassLoader(installRoot, jdkExtensionCL);
        initializeLogManager(gfBootCL);

        checkJdkVersion();

        final Properties argsAsProps = argsToMap(args);
        final OsgiPlatform platform = OsgiPlatform.valueOf(whichPlatform());
        STDOUT.println("Launching GlassFish on " + platform + " platform");

        final Path instanceRoot = findInstanceRoot(installRoot, argsAsProps);
        final ServerFiles files = new ServerFiles(installRoot.toPath(), instanceRoot);
        final StartupContextCfg startupContextCfg = createStartupContextCfg(platform, files, args);
        final ClassLoader launcherCL = ClassLoaderBuilder.createLauncherCL(startupContextCfg, gfBootCL);

        final Class<?> launcherClass = launcherCL.loadClass(Launcher.class.getName());
        final Object launcher = launcherClass.getDeclaredConstructor().newInstance();
        final Method method = launcherClass.getMethod("launch", Properties.class);

        // launcherCL is used only to load the RuntimeBuilder service.
        // on all other places is used classloader which loaded the GlassfishRuntime class
        // -> it must not be loaded by any parent classloader, it's children would be ignored.
        method.invoke(launcher, startupContextCfg.toProperties());

        // also note that debugging is not possible until the debug port is open.
    }


    /**
     * The GlassFishLogManager must be set before the first usage of any JUL component,
     * it would be replaced by another implementation otherwise.
     */
    private static void initializeLogManager(final GlassfishBootstrapClassLoader gfMainCL) throws Exception {
        final Class<?> loggingInitializer = gfMainCL.loadClass(CLASS_INITIALIZER);
        final Properties loggingCfg = createDefaultLoggingProperties();
        loggingInitializer.getMethod("tryToSetAsDefault", Properties.class).invoke(loggingInitializer, loggingCfg);
    }


    private static void checkJdkVersion() {
        int version = Runtime.version().feature();
        if (version < 11) {
            BOOTSTRAP_LOGGER.log(SEVERE, LogFacade.BOOTSTRAP_INCORRECT_JDKVERSION, new Object[] {11, version});
            System.exit(1);
        }
    }


    private static String whichPlatform() {
        final String platformSysOption = System.getProperty(PLATFORM_PROPERTY_KEY);
        if (platformSysOption != null && !platformSysOption.isBlank()) {
            return platformSysOption.trim();
        }
        final String platformEnvOption = System.getenv(PLATFORM_PROPERTY_KEY);
        if (platformEnvOption != null && !platformEnvOption.isBlank()) {
            return platformEnvOption.trim();
        }
        return OsgiPlatform.Felix.name();
    }


    /**
     * IMPORTANT - check for instance BEFORE domain.  We will always come up
     * with a default domain but there is no such thing as a default instance.
     */
    private static Path findInstanceRoot(File installRoot, Properties args) {
        File instanceDir = getInstanceRoot(args);
        if (instanceDir == null) {
            // that means that this is a DAS.
            instanceDir = getDomainRoot(args, installRoot);
        }
        verifyDomainRoot(instanceDir);
        return instanceDir.toPath();
    }


    private static File getInstanceRoot(Properties args) {
        String instanceDir = getParam(args, "instancedir");
        if (isSet(instanceDir)) {
            return new File(instanceDir);
        }
        return null;
    }


    /**
     * Determines the root directory of the domain that we'll start.
     *
     * @param installRoot
     */
    private static File getDomainRoot(Properties args, File installRoot) {
        // first see if it is specified directly
        String domainDir = getParam(args, "domaindir");
        if (isSet(domainDir)) {
            return new File(domainDir);
        }

        // now see if they specified the domain name -- we will look in the default domains-dir
        File defDomainsRoot = getDefaultDomainsDir(installRoot);
        String domainName = getParam(args, "domain");

        if (isSet(domainName)) {
            return new File(defDomainsRoot, domainName);
        }

        // OK - they specified nothing.  Get the one-and-only domain in the domains-dir
        return getDefaultDomain(defDomainsRoot);
    }


    private static File getDefaultDomainsDir(File installRoot) {
        AsenvConf asEnv = AsenvConf.parseAsEnv(installRoot);
        String dirname = asEnv.getProperty(DEFAULT_DOMAINS_DIR_PROPNAME);
        if (!isSet(dirname)) {
            throw new RuntimeException(DEFAULT_DOMAINS_DIR_PROPNAME + " is not set.");
        }

        File domainsDir = absolutize(new File(dirname));
        if (!domainsDir.isDirectory()) {
            throw new RuntimeException(DEFAULT_DOMAINS_DIR_PROPNAME + "[" + dirname + "]"
                + " is specifying a file that is NOT a directory.");
        }
        return domainsDir;
    }


    private static File getDefaultDomain(File domainsDir) {
        File[] domains = domainsDir.listFiles(File::isDirectory);

        // By default we will start an unspecified domain if it is the only
        // domain in the default domains dir

        if (domains == null || domains.length == 0) {
            throw new RuntimeException("no domain directories found under " + domainsDir);
        }

        if (domains.length > 1) {
            throw new RuntimeException("Multiple domains[" + domains.length + "] found under "
                    + domainsDir + " -- you must specify a domain name as -domain <name>");
        }

        return domains[0];
    }


    /**
     * Verifies correctness of the root directory of the domain that we'll start.
     *
     * @param domainRoot
     */
    private static void verifyDomainRoot(File domainRoot) {
        if (domainRoot == null) {
            throw new RuntimeException("Internal Error: The domain dir is null.");
        } else if (!domainRoot.exists()) {
            throw new RuntimeException("the domain directory does not exist");
        } else if (!domainRoot.isDirectory()) {
            throw new RuntimeException("the domain directory is not a directory.");
        } else if (!domainRoot.canWrite()) {
            throw new RuntimeException("the domain directory is not writable.");
        } else if (!new File(domainRoot, "config").isDirectory()) {
            throw new RuntimeException("the domain directory is corrupt - there is no config subdirectory.");
        }
    }


    private static Properties createDefaultLoggingProperties() {
        final Properties cfg = new Properties();
        cfg.setProperty("handlers",
            "org.glassfish.main.jul.handler.SimpleLogHandler,org.glassfish.main.jul.handler.GlassFishLogHandler");
        cfg.setProperty("org.glassfish.main.jul.handler.SimpleLogHandler.formatter",
            "org.glassfish.main.jul.formatter.UniformLogFormatter");
        // useful to track any startup race conditions etc. Logging is always in game.
        if ("true".equals(System.getenv(ENV_AS_TRACE_LOGGING))) {
            cfg.setProperty(KEY_TRACING_ENABLED, "true");
        }
        cfg.setProperty("systemRootLogger.level", Level.INFO.getName());
        cfg.setProperty(".level", Level.INFO.getName());
        // better startup performance vs. losing log records.
        if ("true".equals(System.getenv(ENV_AS_TRACE_BOOTSTRAP))) {
            cfg.setProperty("org.glassfish.main.jul.record.resolveLevelWithIncompleteConfiguration", "false");
        } else {
            cfg.setProperty("org.glassfish.main.jul.record.resolveLevelWithIncompleteConfiguration", "true");
        }

        return cfg;
    }


    private static String getParam(Properties map, String name) {
        // allow both "-" and "--"
        String val = map.getProperty("-" + name);
        if (val == null) {
            val = map.getProperty("--" + name);
        }
        return val;
    }


    private static File absolutize(File f) {
        try {
            return f.getCanonicalFile();
        } catch (Exception e) {
            return f.getAbsoluteFile();
        }
    }


    private static boolean isSet(String s) {
        return s != null && !s.isEmpty();
    }
}
