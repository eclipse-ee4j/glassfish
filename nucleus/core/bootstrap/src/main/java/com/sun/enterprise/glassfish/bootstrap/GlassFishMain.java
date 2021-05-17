/*
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

import org.glassfish.embeddable.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Properties;

import static com.sun.enterprise.module.bootstrap.ArgumentManager.argsToMap;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class GlassFishMain {

    // TODO(Sahoo): Move the code to ASMain once we are ready to phase out ASMain

    public static void main(final String args[]) throws Exception {
        MainHelper.checkJdkVersion();

        final Properties argsAsProps = argsToMap(args);

        String platform = MainHelper.whichPlatform();

        System.out.println("Launching GlassFish on " + platform + " platform");

        // Set the system property if downstream code wants to know about it
        System.setProperty(Constants.PLATFORM_PROPERTY_KEY, platform); // TODO(Sahoo): Why is this a system property?

        File installRoot = MainHelper.findInstallRoot();

        // domainDir can be passed as argument, so pass the agrgs as well.
        File instanceRoot = MainHelper.findInstanceRoot(installRoot, argsAsProps);

        Properties ctx = MainHelper.buildStartupContext(platform, installRoot, instanceRoot, args);
        /*
         * We have a tricky class loading issue to solve. GlassFishRuntime looks for an implementation of RuntimeBuilder.
         * In case of OSGi, the implementation class is OSGiGlassFishRuntimeBuilder. OSGiGlassFishRuntimeBuilder has
         * compile time dependency on OSGi APIs, which are unavoidable. More over, OSGiGlassFishRuntimeBuilder also
         * needs to locate OSGi framework factory using some class loader and that class loader must share same OSGi APIs
         * with the class loader of OSGiGlassFishRuntimeBuilder. Since we don't have the classpath for OSGi framework
         * until main method is called (note, we allow user to select what OSGi framework to use at runtime without
         * requiring them to add any extra jar in system classpath), we can't assume that everything is correctly set up
         * in system classpath. So, we create a class loader which can load GlassFishRuntime, OSGiGlassFishRuntimebuilder
         * and OSGi framework classes.
         */
        final ClassLoader launcherCL = MainHelper.createLauncherCL(ctx,
                ClassLoader.getSystemClassLoader().getParent());
        Class launcherClass = launcherCL.loadClass(GlassFishMain.Launcher.class.getName());
        Object launcher = launcherClass.newInstance();
        Method method = launcherClass.getMethod("launch", Properties.class);
        method.invoke(launcher, ctx);
    }

    public static class Launcher {
        /*
         * Only this class has compile time dependency on glassfishapi.
         */
        private volatile GlassFish gf;
        private volatile GlassFishRuntime gfr;

        public Launcher() {
        }

        public void launch(Properties ctx) throws Exception {
            addShutdownHook();
            gfr = GlassFishRuntime.bootstrap(new BootstrapProperties(ctx), getClass().getClassLoader());
            gf = gfr.newGlassFish(new GlassFishProperties(ctx));
            if (Boolean.valueOf(Util.getPropertyOrSystemProperty(ctx, "GlassFish_Interactive", "false"))) {
                startConsole();
            } else {
                gf.start();
            }
        }

        private void startConsole() throws IOException {
            String command;
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while ((command = readCommand(reader)) != null) {
                try {
                    System.out.println("command = " + command);
                    if ("start".equalsIgnoreCase(command)) {
                        if (gf.getStatus() != GlassFish.Status.STARTED || gf.getStatus() == GlassFish.Status.STOPPING || gf.getStatus() == GlassFish.Status.STARTING)
                            gf.start();
                        else System.out.println("Already started or stopping or starting");
                    } else if ("stop".equalsIgnoreCase(command)) {
                        if (gf.getStatus() != GlassFish.Status.STARTED) {
                            System.out.println("GlassFish is not started yet. Please execute start first.");
                            continue;
                        }
                        gf.stop();
                    } else if (command.startsWith("deploy")) {
                        if (gf.getStatus() != GlassFish.Status.STARTED) {
                            System.out.println("GlassFish is not started yet. Please execute start first.");
                            continue;
                        }
                        Deployer deployer = gf.getService(Deployer.class, null);
                        String[] tokens = command.split("\\s");
                        if (tokens.length < 2) {
                            System.out.println("Syntax: deploy <options> file");
                            continue;
                        }
                        final URI uri = URI.create(tokens[tokens.length -1]);
                        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length-1);
                        String name = deployer.deploy(uri, params);
                        System.out.println("Deployed = " + name);
                    } else if (command.startsWith("undeploy")) {
                        if (gf.getStatus() != GlassFish.Status.STARTED) {
                            System.out.println("GlassFish is not started yet. Please execute start first.");
                            continue;
                        }
                        Deployer deployer = gf.getService(Deployer.class, null);
                        String name = command.substring(command.indexOf(" ")).trim();
                        deployer.undeploy(name);
                        System.out.println("Undeployed = " + name);
                    } else if ("quit".equalsIgnoreCase(command)) {
                        System.exit(0);
                    } else {
                        if (gf.getStatus() != GlassFish.Status.STARTED) {
                            System.out.println("GlassFish is not started yet. Please execute start first.");
                            continue;
                        }
                        CommandRunner cmdRunner = gf.getCommandRunner();
                        String[] tokens = command.split("\\s");
                        CommandResult result = cmdRunner.run(tokens[0], Arrays.copyOfRange(tokens, 1, tokens.length));
                        System.out.println(result.getExitStatus());
                        System.out.println(result.getOutput());
                        if (result.getFailureCause() != null) {
                            result.getFailureCause().printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        private String readCommand(BufferedReader reader) throws IOException {
            prompt();
            String command = null;
            while((command = reader.readLine()) != null && command.isEmpty()) {
                // loop until a non empty command or Ctrl-D is inputted.
            }
            return command;
        }

        private void prompt() {
            System.out.print("Enter any of the following commands: start, stop, quit, deploy <path to file>, undeploy <name of app>\n" +
                    "glassfish$ ");
            System.out.flush();
        }

        private void addShutdownHook() {
            Runtime.getRuntime().addShutdownHook(new Thread("GlassFish Shutdown Hook") {
                public void run() {
                    try {
                        gfr.shutdown();
                    }
                    catch (Exception ex) {
                        System.err.println("Error stopping framework: " + ex);
                        ex.printStackTrace();
                    }
                }
            });

        }

    }

}
