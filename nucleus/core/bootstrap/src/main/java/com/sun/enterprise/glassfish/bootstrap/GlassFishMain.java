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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;

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

    private static final Pattern COMMAND_PATTERN = Pattern.compile("([^\"']\\S*|\".*?\"|'.*?')\\s*");
    // logging system may override original output streams.
    private static final PrintStream STDOUT = System.out;
    private static final PrintStream STDERR = System.err;


    public static void main(final String[] args) throws Exception {
        final File installRoot = MainHelper.findInstallRoot();
        final ClassLoader jdkExtensionCL = ClassLoader.getSystemClassLoader().getParent();
        final GlassfishBootstrapClassLoader gfBootCL = new GlassfishBootstrapClassLoader(installRoot, jdkExtensionCL);
        initializeLogManager(gfBootCL);

        MainHelper.checkJdkVersion();

        final Properties argsAsProps = argsToMap(args);
        final String platform = MainHelper.whichPlatform();
        STDOUT.println("Launching GlassFish on " + platform + " platform");

        final File instanceRoot = MainHelper.findInstanceRoot(installRoot, argsAsProps);
        final Properties startupCtx = MainHelper.buildStartupContext(platform, installRoot, instanceRoot, args);
        final ClassLoader launcherCL = MainHelper.createLauncherCL(startupCtx, gfBootCL);
        final Class<?> launcherClass = launcherCL.loadClass(GlassFishMain.Launcher.class.getName());
        final Object launcher = launcherClass.getDeclaredConstructor().newInstance();
        final Method method = launcherClass.getMethod("launch", Properties.class);

        // launcherCL is used only to load the RuntimeBuilder service.
        // on all other places is used classloader which loaded the GlassfishRuntime class
        // -> it must not be loaded by any parent classloader, it's children would be ignored.
        method.invoke(launcher, startupCtx);

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

    // must be public to be accessible via reflection
    public static class Launcher {
        private volatile GlassFish gf;
        private volatile GlassFishRuntime gfr;

        public void launch(final Properties ctx) throws Exception {
            addShutdownHook();
            gfr = GlassFishRuntime.bootstrap(new BootstrapProperties(ctx), getClass().getClassLoader());
            gf = gfr.newGlassFish(new GlassFishProperties(ctx));
            if (Boolean.parseBoolean(Util.getPropertyOrSystemProperty(ctx, "GlassFish_Interactive", "false"))) {
                startConsole();
            } else {
                gf.start();
            }
        }

        private void startConsole() throws IOException {
            String command;
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset()));
            while ((command = readCommand(reader)) != null) {
                try {
                    STDOUT.println("command = " + command);
                    if ("start".equalsIgnoreCase(command)) {
                        if (gf.getStatus() != GlassFish.Status.STARTED || gf.getStatus() == GlassFish.Status.STOPPING
                            || gf.getStatus() == GlassFish.Status.STARTING) {
                            gf.start();
                        } else {
                            STDOUT.println("Already started or stopping or starting");
                        }
                    } else if ("stop".equalsIgnoreCase(command)) {
                        if (gf.getStatus() != GlassFish.Status.STARTED) {
                            STDOUT.println("GlassFish is not started yet. Please execute start first.");
                            continue;
                        }
                        gf.stop();
                    } else if (command.startsWith("deploy")) {
                        if (gf.getStatus() != GlassFish.Status.STARTED) {
                            STDOUT.println("GlassFish is not started yet. Please execute start first.");
                            continue;
                        }
                        final Deployer deployer = gf.getService(Deployer.class, null);
                        final String[] tokens = command.split("\\s");
                        if (tokens.length < 2) {
                            STDOUT.println("Syntax: deploy <options> file");
                            continue;
                        }
                        final URI uri = URI.create(tokens[tokens.length -1]);
                        final String[] params = Arrays.copyOfRange(tokens, 1, tokens.length-1);
                        final String name = deployer.deploy(uri, params);
                        STDOUT.println("Deployed = " + name);
                    } else if (command.startsWith("undeploy")) {
                        if (gf.getStatus() != GlassFish.Status.STARTED) {
                            STDOUT.println("GlassFish is not started yet. Please execute start first.");
                            continue;
                        }
                        final Deployer deployer = gf.getService(Deployer.class, null);
                        final String name = command.substring(command.indexOf(' ')).trim();
                        deployer.undeploy(name);
                        STDOUT.println("Undeployed = " + name);
                    } else if ("quit".equalsIgnoreCase(command)) {
                        System.exit(0);
                    } else {
                        if (gf.getStatus() != GlassFish.Status.STARTED) {
                            STDOUT.println("GlassFish is not started yet. Please execute start first.");
                            continue;
                        }
                        final CommandRunner cmdRunner = gf.getCommandRunner();
                        runCommand(cmdRunner, command);
                    }
                } catch (final Exception e) {
                    e.printStackTrace(STDERR);
                }
            }
        }

        private String readCommand(final BufferedReader reader) throws IOException {
            prompt();
            String command = null;
            while((command = reader.readLine()) != null && command.isEmpty()) {
                // loop until a non empty command or Ctrl-D is inputted.
            }
            return command;
        }

        private void prompt() {
            STDOUT.print("Enter any of the following commands: start, stop, quit, deploy <path to file>, undeploy <name of app>\n" +
                    "glassfish$ ");
            STDOUT.flush();
        }

        private void addShutdownHook() {
            Runtime.getRuntime().addShutdownHook(new Thread("GlassFish Shutdown Hook") {
                @Override
                public void run() {
                    try {
                        if (gfr != null) {
                            gfr.shutdown();
                        }
                    }
                    catch (final Exception ex) {
                        STDERR.println("Error stopping framework: " + ex);
                        ex.printStackTrace(STDERR);
                    }
                }
            });

        }

        /**
         * Runs a command read from a string
         *
         * @param cmdRunner
         * @param command
         * @throws GlassFishException
         */
        private void runCommand(final CommandRunner cmdRunner, final String command) throws GlassFishException {
            String[] tokens = command.split("\\s");
            CommandResult result = cmdRunner.run(tokens[0], Arrays.copyOfRange(tokens, 1, tokens.length));
            System.out.println(result.getExitStatus());
            System.out.println(result.getOutput());
            if (result.getFailureCause() != null) {
                result.getFailureCause().printStackTrace(STDERR);
            }
        }
    }

}
