/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.cli;

import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.admin.servermgmt.cli.ServerLifeSignChecker.GlassFishProcess;
import com.sun.enterprise.admin.servermgmt.cli.ServerLifeSignChecker.ServerLifeSigns;
import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import com.sun.enterprise.util.HostAndPort;
import com.sun.enterprise.util.io.ServerDirs;
import com.sun.enterprise.util.net.NetUtils;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.glassfish.api.admin.CommandException;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.glassfish.main.jul.formatter.OneLineFormatter;
import org.glassfish.main.jul.handler.GlassFishLogHandler;
import org.glassfish.main.jul.handler.GlassFishLogHandlerConfiguration;

import static com.sun.enterprise.admin.cli.CLIConstants.RESTART_DEBUG_OFF;
import static com.sun.enterprise.admin.cli.CLIConstants.RESTART_DEBUG_ON;
import static com.sun.enterprise.admin.cli.CLIConstants.RESTART_NORMAL;
import static com.sun.enterprise.admin.cli.CLIConstants.WALL_CLOCK_START_PROP;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.jdke.props.SystemProperties.setProperty;

/**
 * Manager of starting process.
 * While {@link GFLauncher} manages the launch of the new server, this controls also user interaction.
 *
 * @author bnevins
 * @author David Matejcek
 */
public final class StartServerHelper {
    private static final LocalStringsImpl I18N = new LocalStringsImpl(StartServerHelper.class);
    private static final Logger LOG = System.getLogger(StartServerHelper.class.getName(), I18N.getBundle());

    private final boolean terse;
    private final GFLauncher launcher;
    private final File pidFile;
    private final GFLauncherInfo launchParams;
    private final List<HostAndPort> adminAddresses;
    private final ServerDirs serverDirs;
    private final String serverTitleAndName;
    private final ServerLifeSignCheck lifeSignCheck;

    public StartServerHelper(boolean terse, Duration timeout, ServerDirs serverDirs, GFLauncher launcher,
        ServerLifeSignCheck lifeSignCheck) throws GFLauncherException {
        this.terse = terse;
        this.launcher = launcher;
        this.launchParams = launcher.getParameters();
        this.serverTitleAndName = (launchParams.isDomain() ? "domain " : "instance ") + serverDirs.getServerName();
        this.adminAddresses = launchParams.getAdminAddresses();
        this.serverDirs = serverDirs;
        this.pidFile = serverDirs.getPidFile();
        this.lifeSignCheck = lifeSignCheck;

        // This means we are running restart.
        // Restart has a problem, the start is initiate d by the running server.
        // That means we cannot watch its output. So we have to write it down.
        if (launcher.getPidBeforeRestart() != null) {
            waitForParentToDie(launcher.getPidBeforeRestart(), timeout);
            configureLoggingOfRestart(serverDirs.getRestartLogFile());
        }
        checkFreeDebugPort(launcher.getDebugPort(), Duration.ofSeconds(10L), terse);
        checkFreeAdminPorts(launchParams.getAdminAddresses());
        deletePidFile();
    }

    /**
     * Blocks and communicates with the user using console.
     *
     * @return launcher exit code
     * @throws GFLauncherException
     * @throws MiniXmlParserException
     */
    public int talkWithUser() throws GFLauncherException, MiniXmlParserException {
        while (true) {
            int returnValue = launcher.getExitValue();
            switch (returnValue) {
                case RESTART_NORMAL:
                    LOG.log(INFO, "restart");
                    break;
                case RESTART_DEBUG_ON:
                    LOG.log(INFO, "restartChangeDebug", "on");
                    launchParams.setDebug(true);
                    break;
                case RESTART_DEBUG_OFF:
                    LOG.log(INFO, "restartChangeDebug", "off");
                    launchParams.setDebug(false);
                    break;
                default:
                    return returnValue;
            }
            setProperty(WALL_CLOCK_START_PROP, Instant.now().toString(), true);
            launcher.setup();
            launcher.launch();
        }
    }

    public String waitForServerStart(Duration timeout) throws CommandException {
        if (!terse) {
            if (launcher.isSuspendEnabled()) {
                // If the server starts suspended, user needs to see this before it happens.
                System.out.print("Debugging is configured to listen on port " + launcher.getDebugPort()
                    + ". Server's JVM is set to suspend.");
            }
        }
        final GlassFishProcess glassFishProcess = GlassFishProcess.of(launcher.getProcess());
        final ServerLifeSignChecker checker = new ServerLifeSignChecker(lifeSignCheck, pidFile, () -> adminAddresses, !terse);
        final ServerLifeSigns signs = checker.watchStartup(glassFishProcess, timeout);
        final String report = report(signs);
        if (signs.isError()) {
            throw new CommandException(report);
        }
        return report;
    }


    private String report(ServerLifeSigns signs) {
        final StringBuilder report = new StringBuilder(2048);
        report.append('\n').append(signs.getSummary());
        if (signs.getSuggestion() != null) {
            report.append('\n').append(signs.getSuggestion());
        }
        report.append("\n  Location: ").append(serverDirs.getServerDir());
        report.append("\n  Log File: ").append(launcher.getLogFile());
        if (launcher.getDebugPort() != null) {
            report.append("\n  Debugging is configured to listen on port " + launcher.getDebugPort() + ".");
            if (launcher.isSuspendEnabled()) {
                report.append(" Server's JVM is set to suspend.");
            }
        }

        final String situationReport = signs.getSituationReport();
        if (situationReport != null) {
            report.append(signs.getSituationReport());
        }
        // Print output just if user explicitly asked
        // or start failed and user did not explicitly forbid the print.
        if (launcher.getPidBeforeRestart() != null || lifeSignCheck.isPrintServerOutput(signs.isError())) {
            report.append("\n\n").append(getProcessOutput());
        }
        return report.append('\n').toString();
    }


    private String getProcessOutput() {
        final String output = launcher.getProcessStreamDrainer().getOutErrString();
        if (output == null) {
            return "Unfortunately the new process did not produce any output.";
        }
        return "The output of the process until we stopped watching:\n\n**********\n" + output + "\n**********";
    }

    private void configureLoggingOfRestart(File logFile) {
        GlassFishLogHandlerConfiguration cfg = new GlassFishLogHandlerConfiguration();
        cfg.setFormatterConfiguration(new OneLineFormatter());
        if (logFile.isFile()) {
            logFile.renameTo(new File(logFile.getParent(), "restart.log_" + LocalDateTime.now()));
        }
        cfg.setLogFile(logFile);
        cfg.setLevel(Level.ALL);
        cfg.setFlushFrequency(1);
        GlassFishLogHandler handler = new GlassFishLogHandler(cfg);
        java.util.logging.Logger.getLogger("").addHandler(handler);
        // see AdminMain
        java.util.logging.Logger.getLogger("com.sun.enterprise.admin.cli").addHandler(handler);
    }

    /**
     * If the parent is a GF server -- then wait for it to die.
     * This is part of the Client-Server Restart Dance!
     * The dying server called us with the system property AS_RESTART_PREVIOUS_PID set to its pid
     *
     * @throws CommandException if we timeout waiting for the parent to die or if the admin ports never free up
     */
    private void waitForParentToDie(Long pid, Duration timeout) throws GFLauncherException {
        LOG.log(INFO, () -> "Waiting for death of the parent process with the pid " + pid);
        if (!ProcessUtils.waitWhileIsAlive(pid, timeout, false)) {
            throw new GFLauncherException("Waited " + timeout.toSeconds()
                + " s for the server to die. Restart is not possible unless you kill it manually.");
        }
        LOG.log(DEBUG, () -> "Parent process with PID " + pid + " is dead.");
    }

    private void deletePidFile() {
        if (pidFile.exists() && !pidFile.isFile()) {
            throw new IllegalStateException("The pid file " + pidFile + " is not a file!");
        }
        try {
            Files.delete(pidFile.toPath());
        } catch (NoSuchFileException e) {
            // ok, another delete was faster.
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't remove the pid file " + pidFile, e);
        }
        LOG.log(DEBUG, "The pid file {0} has been deleted.", pidFile);
    }


    /**
     * @param endpoints
     * @return space separated list of endpoints including HTTP/HTTPS protocol prefix.
     */
    public static String toHttpList(List<HostAndPort> endpoints) {
        return endpoints.stream()
        .map(h -> (h.isSecure() ? "https://" : "http://") + h.getHost() + ':' + h.getPort())
        .collect(Collectors.joining(" "));
    }


    /**
     * @param customEndpoints value provided by the user
     * @return parsed list of endpoints to check
     * @throws CommandException if anything goes wrong
     */
    public static List<HostAndPort> parseCustomEndpoints(String customEndpoints) throws CommandException {
        if (customEndpoints == null || customEndpoints.isBlank()) {
            return List.of();
        }
        final String[] strings = customEndpoints.strip().split(",");
        final List<HostAndPort> endpoints = new ArrayList<>(strings.length);
        for (String string : strings) {
            try {
                final boolean secure = string.startsWith("https");
                final boolean http = string.startsWith("http");
                final String[] pair = string.replaceFirst("[a-z]+\\://", "").split(":");
                final HostAndPort endpoint;
                if (pair.length == 1) {
                    final int port;
                    if (http) {
                        port = secure ? 443 : 80;
                    } else {
                        throw new CommandException("Port is mandatory endpoints without explicit protocol: " + string);
                    }
                    endpoint = new HostAndPort(pair[0], port, secure);
                } else if (pair.length == 2) {
                    endpoint = new HostAndPort(pair[0], Integer.parseInt(pair[1]), secure);
                } else {
                    throw new CommandException("Invalid customEndpoints value: " + string);
                }
                endpoints.add(endpoint);
            } catch (CommandException e) {
                throw e;
            } catch (Exception e) {
                throw new CommandException("Invalid customEndpoints value: " + string, e);
            }
        }
        return endpoints;
    }

    /**
     * Fast respawn can meet with previous JVM on ports, despite the JVM is already dead.
     * So we have to wait a bit.
     */
    private static void checkFreeDebugPort(Integer debugPort, Duration timeout, boolean terse) {
        if (debugPort == null || NetUtils.isPortFree(debugPort)) {
            return;
        }
        ProcessUtils.waitFor(() -> NetUtils.isPortFree(debugPort), Duration.ofSeconds(10L), terse);
    }

    private static void checkFreeAdminPorts(List<HostAndPort> endpoints) throws GFLauncherException {
        LOG.log(DEBUG, "Checking if all admin ports are free.");
        for (HostAndPort endpoint : endpoints) {
            if (!NetUtils.isPortFree(endpoint.getHost(), endpoint.getPort())) {
                throw new GFLauncherException("There is a process already using the admin port " + endpoint.getPort()
                    + " - it might be another instance of a GlassFish server.");
            }
        }
    }
}
