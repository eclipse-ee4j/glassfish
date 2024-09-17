/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.glassfish.runnablejar;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.glassfish.bootstrap.embedded.EmbeddedGlassFishRuntimeBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.runnablejar.commandline.Arguments;
import org.glassfish.runnablejar.commandline.CommandLineParser;

import static java.lang.System.exit;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static org.glassfish.embeddable.CommandResult.ExitStatus.SUCCESS;

/**
 * This is main class for the uber jars viz., glassfish-embedded-all.jar and
 * glassfish-embedded-web.jar, to be able to do:
 * <p/>
 * <p/>
 * {@code java -jar glassfish-embedded-all.jar}
 * <p/>
 * {@code java -jar glassfish-embedded-web.jar}
 * <p/>
 * <h3>Example of running an app from command line</h3>
 * <p/>
 * On port 8080 and root context by default
 * <p/>
 * {@code java -jar glassfish-embedded-all.jar app.war}
 * <p/>
 * <h3>Example of running an app on a different port</h3>
 * <p/>
 * {@code java -jar glassfish-embedded-all.jar --httpPort=8090 app.war}
 * <p/>
 * <h3>Example with a custom deploy command</h3>
 * <p/>
 * Sets a custom root context (custom commands need to be enclosed in quotes
 * because they usually contain spaces)
 * <p/>
 * {@code java -jar glassfish-embedded-all.jar "deploy --contextroot=/app app.war"}
 *
 * @author Ondro Mihalyi
 * @author bhavanishankar@dev.java.net
 */
public class UberMain {

    private static final Logger logger = Logger.getLogger(UberMain.class.getName());
    private static final String SERVER_NAME = "server";

    GlassFish glassFish;
    CommandRunner commandRunner;

    public static void main(String... args) throws IOException, GlassFishException {
        final Arguments arguments = new CommandLineParser().parse(args);
        if (arguments.askedForHelp) {
            arguments.printHelp();
        } else {
            // When running off the uber jar don't add extras module URLs to classpath.
            EmbeddedGlassFishRuntimeBuilder.addModuleJars = false;
            new UberMain().run(arguments);
        }
    }

    public void run(Arguments arguments) throws GlassFishException {
        addShutdownHook(); // handle Ctrt-C.

        GlassFishProperties gfProps = arguments.glassFishProperties;
        setFromSystemProperty(gfProps, "org.glassfish.embeddable.autoDelete", "true");

        glassFish = GlassFishRuntime.bootstrap().newGlassFish(gfProps);
        glassFish.start();
        commandRunner = glassFish.getCommandRunner();

        for (String command : arguments.commands) {
            executeCommandFromString(command);
        }

        if (!arguments.deployables.isEmpty()) {
            if (glassFish.getDeployer().getDeployedApplications().isEmpty() && arguments.deployables.size() == 1) {
                final String deployable = arguments.deployables.get(0);
                executeCommandFromString("deploy --contextroot=/ " + deployable);
                logger.log(INFO, () -> "Application " + Path.of(deployable).getFileName() + " deployed at context root \"/\"");
            } else {
                arguments.deployables.forEach(deployable -> {
                    executeCommandFromString("deploy " + deployable);
                    logger.log(INFO, () -> "Application " + Path.of(deployable).getFileName() + " deployed");
                });
            }
        }

        if (arguments.shutdown) {
            logger.log(INFO, () -> "Shutting down after startup as requested");
            exit(0);
        }

        switch (glassFish.getStatus()) {
            case INIT:
            case STARTING:
            case STARTED:
                if (!arguments.noInfo) {
                    printInfoAfterStartup();
                }

                if (glassFish.getDeployer().getDeployedApplications().isEmpty()) {
                    runCommandPromptLoop();
                    exit(0);
                }
                break;
            case STOPPING:
                logger.log(INFO, () -> "GlassFish is shutting down...");
                break;
            default:
                logger.log(INFO, () -> "GlassFish is shut down");
        }

    }

    protected void printInfoAfterStartup() throws GlassFishException {
        final Level LOG_LEVEL = INFO;
        if (logger.isLoggable(LOG_LEVEL)) {
            final Domain domain = glassFish.getService(Domain.class);
            final List<Application> applications = domain.getApplicationsInTarget(SERVER_NAME);
            final List<NetworkListener> listeners = domain.getServers().getServer(SERVER_NAME).getConfig()
                    .getNetworkConfig().getNetworkListeners().getNetworkListener();
            logger.log(LOG_LEVEL, "\n\n" + new InfoPrinter().getInfoAfterStartup(applications, listeners) + "\n");
        }
    }

    private void runCommandPromptLoop() throws GlassFishException {
        while (true) {
            System.out.print("\n\nGlassFish $ ");
            String str = null;
            try {
                str = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset())).readLine();
            } catch (IOException | RuntimeException e) {
                logger.log(SEVERE, e.getMessage(), e);
            }
            if (str != null && str.trim().length() != 0) {
                if ("exit".equalsIgnoreCase(str) || "quit".equalsIgnoreCase(str)) {
                    break;
                }
                executeCommandFromString(str);
            }
        }
    }

    private void executeCommandFromString(String stringCommand) {
        logger.log(FINE, () -> "Executing command: " + stringCommand);
        String[] split = stringCommand.split(" ");
        String command = split[0].trim();
        String[] commandParams = null;
        if (split.length > 1) {
            commandParams = new String[split.length - 1];
            for (int i = 1; i < split.length; i++) {
                commandParams[i - 1] = split[i].trim();
            }
        }
        try {
            CommandResult result = commandParams == null
                    ? commandRunner.run(command) : commandRunner.run(command, commandParams);
            switch (result.getExitStatus()) {
                case SUCCESS:
                    logger.log(FINE, () -> "SUCCESS: " + result.getOutput());
                    break;
                default:
                    if (result.getFailureCause() != null) {
                        throw result.getFailureCause();
                    } else {
                        throw new RuntimeException("Command completed with " + result.getExitStatus() + ": "
                                + result.getOutput() + ". Command was: " + stringCommand);
                    }
            }
        } catch (Throwable ex) {
            logger.log(SEVERE, ex.getMessage());
            logger.log(FINE, ex.getMessage(), ex);
        }
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread("GlassFish Shutdown Hook") {

            @Override
            public void run() {
                if (glassFish != null) {
                    stopGlassFish();
                }
            }
        });
    }

    private void stopGlassFish() {
        try {
            glassFish.stop();
        } catch (GlassFishException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                glassFish.dispose();
            } catch (GlassFishException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    // Preference: System property > GlassFish property > default value
    private void setFromSystemProperty(GlassFishProperties gfProps, String propertyName, String defaultValue) {
        final String systemProperty = System.getProperty(propertyName);
        if (systemProperty != null) {
            gfProps.setProperty(propertyName, systemProperty);
        } else if (!gfProps.getProperties().containsKey(propertyName)) {
            gfProps.setProperty(propertyName, defaultValue);
        }
    }

}
