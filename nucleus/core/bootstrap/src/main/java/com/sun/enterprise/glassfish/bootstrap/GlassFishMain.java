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

import com.sun.enterprise.glassfish.bootstrap.cfg.GFBootstrapProperties;
import com.sun.enterprise.module.bootstrap.Which;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.logging.Level;

import static com.sun.enterprise.module.bootstrap.ArgumentManager.argsToMap;
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

    // logging system may override original output streams.
    private static final PrintStream STDOUT = System.out;

    public static void main(final String[] args) throws Exception {
        final File installRoot = getInstallRoot();
        final ClassLoader jdkExtensionCL = ClassLoader.getSystemClassLoader().getParent();
        final GlassfishBootstrapClassLoader gfBootCL = new GlassfishBootstrapClassLoader(installRoot, jdkExtensionCL);
        initializeLogManager(gfBootCL);

        MainHelper.checkJdkVersion();

        final Properties argsAsProps = argsToMap(args);
        final String platform = MainHelper.whichPlatform();
        STDOUT.println("Launching GlassFish on " + platform + " platform");

        final File instanceRoot = MainHelper.findInstanceRoot(installRoot, argsAsProps);
        final GFBootstrapProperties startupCtx = MainHelper.buildStartupContext(platform, installRoot, instanceRoot, args);
        final ClassLoader launcherCL = MainHelper.createLauncherCL(startupCtx, gfBootCL);
        final Class<?> launcherClass = launcherCL.loadClass(Launcher.class.getName());
        final Object launcher = launcherClass.getDeclaredConstructor().newInstance();
        final Method method = launcherClass.getMethod("launch", Properties.class);

        // launcherCL is used only to load the RuntimeBuilder service.
        // on all other places is used classloader which loaded the GlassfishRuntime class
        // -> it must not be loaded by any parent classloader, it's children would be ignored.
        method.invoke(launcher, startupCtx.toProperties());

        // also note that debugging is not possible until the debug port is open.
    }


    /**
     * @return autodetected glassfish directory based on where usually is this class.
     */
    public static File getInstallRoot() {
        // glassfish/modules/glassfish.jar
        File bootstrapFile = findBootstrapFile();
        // glassfish/
        return bootstrapFile.getParentFile().getParentFile();
    }


    private static File findBootstrapFile() {
        try {
            return Which.jarFile(GlassFishMain.class);
        } catch (IOException e) {
            throw new Error("Cannot get bootstrap path from " + GlassFishMain.class + " class location, aborting", e);
        }
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
}
