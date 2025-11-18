/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation
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

import java.io.Console;
import java.lang.System.Logger;
import java.nio.file.Path;
import java.util.List;

import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFish.Status;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.main.boot.embedded.EmbeddedGlassFishRuntimeBuilder;
import org.glassfish.runnablejar.commandline.Arguments;
import org.glassfish.runnablejar.commandline.CommandLineParser;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

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
public class UberMain implements AutoCloseable {

    private static final Logger LOG = System.getLogger(UberMain.class.getName());
    private static final String SERVER_NAME = "server";
    private static volatile String goodByeMessage;

    private GlassFish glassFish;
    private CommandRunner commandRunner;
    private boolean shutdownRequested;

    /**
     * Main will start the server and finish itself.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String... args) throws Exception {
        final Arguments arguments = new CommandLineParser().parse(args);
        if (arguments.askedForHelp) {
            arguments.printHelp();
        } else {
            // When running off the uber jar don't add extras module URLs to classpath.
            EmbeddedGlassFishRuntimeBuilder.addModuleJars = false;
            System.out.println("GlassFish will start now. Welcome!");
            @SuppressWarnings("resource")
            UberMain uberMain = new UberMain();
            try {
                uberMain.run(arguments);
                if (uberMain.isShutdownRequested()) {
                    exit(0, "GlassFish shut down after startup as requested.");
                }
            } catch (GlassFishException e) {
                LOG.log(ERROR, "Exit code 1, execution failed.", e);
                exit(1, "Exit code 1, execution failed.");
            } catch (Throwable e) {
                LOG.log(ERROR, "Exit code 100, execution failed.", e);
                exit(100, "Exit code 100, execution failed.");
            }
        }
    }

    private static void exit(int exitCode, String goodByeMessage) {
        UberMain.goodByeMessage = goodByeMessage;
        System.exit(exitCode);
    }

    public UberMain() {
        addShutdownHook();
    }

    public boolean isShutdownRequested() {
        return shutdownRequested;
    }

    private void run(Arguments arguments) throws GlassFishException {
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
                LOG.log(INFO, () -> "Application " + Path.of(deployable).getFileName() + " deployed at context root \"/\"");
            } else {
                for (String deployable : arguments.deployables) {
                    executeCommandFromString("deploy " + deployable);
                    LOG.log(INFO, () -> "Application " + Path.of(deployable).getFileName() + " deployed");
                }
            }
        }

        if (arguments.shutdown) {
            LOG.log(INFO, "Shutting down after startup as requested.");
            shutdownRequested = true;
            return;
        }

        final Status status = glassFish.getStatus();
        switch (status) {
            case INIT:
            case STARTING:
            case STARTED:
                if (!arguments.noInfo) {
                    printInfoAfterStartup();
                }
                if (arguments.prompt) {
                    runCommandPromptLoop();
                }
                break;
            case STOPPING:
                LOG.log(INFO, "GlassFish is shutting down...");
                break;
            case STOPPED:
            case DISPOSED:
                LOG.log(INFO, "GlassFish is shut down.");
            default:
                throw new IllegalArgumentException("Unknown status of the GlassFish runtime: " + status);
        }

    }

    protected void printInfoAfterStartup() throws GlassFishException {
        final Domain domain = glassFish.getService(Domain.class);
        final List<Application> applications = domain.getApplicationsInTarget(SERVER_NAME);
        final List<NetworkListener> listeners = domain.getServers().getServer(SERVER_NAME).getConfig()
                .getNetworkConfig().getNetworkListeners().getNetworkListener();
        System.out.println("\n\n" + new InfoPrinter().getInfoAfterStartup(applications, listeners));
    }

    private void runCommandPromptLoop() {
        final Console console = System.console();
        if (console == null) {
            throw new Error("System.console() is not supported in this shell.");
        }
        while (true) {
            String str = console.readLine("\nGlassFish $ ").strip();
            if (str != null && !str.isEmpty()) {
                if ("exit".equalsIgnoreCase(str) || "quit".equalsIgnoreCase(str)) {
                    exit(0, "GlassFish shut down. See you soon!");
                }
                try {
                    executeCommandFromString(str);
                } catch (GlassFishException e) {
                    System.out.println(e.getLocalizedMessage());
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    private void executeCommandFromString(String stringCommand) throws GlassFishException {
        LOG.log(DEBUG, () -> "Executing command: " + stringCommand);
        // Split according to empty space but not if empty space is escaped by \
        String[] split = stringCommand.split("(?<!\\\\)\\s+");
        String command = split[0].trim();
        String[] commandParams = null;
        if (split.length > 1) {
            commandParams = new String[split.length - 1];
            for (int i = 1; i < split.length; i++) {
                commandParams[i - 1] = split[i].trim();
            }
        }
        CommandResult result = commandParams == null
            ? commandRunner.run(command)
            : commandRunner.run(command, commandParams);

        switch (result.getExitStatus()) {
            case SUCCESS:
                LOG.log(INFO, () -> "SUCCESS: " + result.getOutput());
                break;
            case WARNING:
                LOG.log(WARNING, () -> "WARNING: " + result.getOutput());
                break;
            case FAILURE:
                throw new GlassFishException("Command completed with " + result.getExitStatus() + ": "
                    + result.getOutput() + ". Command was: " + stringCommand, result.getFailureCause());
            default:
                throw new IllegalArgumentException(
                    "Unknwown command exit status: " + result.getExitStatus() + ". Output: " + result.getOutput());
        }
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread("GlassFish UberMain Shutdown Hook") {

            @Override
            public void run() {
                try {
                    close();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }

    @Override
    public void close() throws GlassFishException {
        if (glassFish == null) {
            // nothing to do
            return;
        }
        final GlassFish instance = glassFish;
        glassFish = null;
        try {
            instance.stop();
        } finally {
            instance.dispose();
            // goodbye message can be null - anything can initiate exit for any reason.
            if (goodByeMessage != null) {
                System.out.println(goodByeMessage);
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
