/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.boot.osgi;

import com.sun.enterprise.glassfish.bootstrap.launch.Launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Properties;

import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;

// must be public to be accessible for reflection
public class GlassFishOsgiLauncher implements Launcher {

    // logging system may override original output streams.
    private static final PrintStream STDOUT = System.out;
    private static final PrintStream STDERR = System.err;

    private volatile GlassFish gf;
    private volatile GlassFishRuntime gfr;

    public GlassFishOsgiLauncher(ClassLoader classloader) {
        // Why this - it is too easy to load this class by wrong classloader.
        // That has consequences (CNFE for other required classes) and here it is
        // quite easy to check.
        if (getClass().getClassLoader() != classloader) {
            throw new IllegalStateException(
                "The class " + getClass() + " was not loaded by the same classloader as given in constructor!");
        }
    }

    @Override
    public void launch(final Properties properties) throws Exception {
        addShutdownHook();
        gfr = GlassFishRuntime.bootstrap(new BootstrapProperties(properties), getClass().getClassLoader());
        gf = gfr.newGlassFish(new GlassFishProperties(properties));
        if (Boolean.parseBoolean(getPropertyOrSystemProperty(properties, "GlassFish_Interactive", "false"))) {
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

    private void runCommand(final CommandRunner cmdRunner, final String command) {
        String[] tokens = command.split("\\s");
        CommandResult result = cmdRunner.run(tokens[0], Arrays.copyOfRange(tokens, 1, tokens.length));
        System.out.println(result.getExitStatus());
        System.out.println(result.getOutput());
        if (result.getFailureCause() != null) {
            result.getFailureCause().printStackTrace(STDERR);
        }
    }

    private static String getPropertyOrSystemProperty(Properties properties, String name, String defaultValue) {
        String value = properties.getProperty(name);
        return value == null ? System.getProperty(name, defaultValue) : value;
    }
}