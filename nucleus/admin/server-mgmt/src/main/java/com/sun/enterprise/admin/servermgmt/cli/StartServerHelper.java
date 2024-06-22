/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.process.ProcessStreamDrainer;
import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.enterprise.util.HostAndPort;
import com.sun.enterprise.util.io.ServerDirs;
import com.sun.enterprise.util.net.NetUtils;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import org.glassfish.api.admin.CommandException;
import static com.sun.enterprise.admin.cli.CLIConstants.DEATH_TIMEOUT_MS;
import static com.sun.enterprise.admin.cli.CLIConstants.MASTER_PASSWORD;
import static com.sun.enterprise.admin.cli.CLIConstants.WAIT_FOR_DAS_TIME_MS;
import static com.sun.enterprise.util.StringUtils.ok;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Java does not allow multiple inheritance. Both StartDomainCommand and StartInstanceCommand have common code but they
 * are already in a different hierarchy of classes. The first common baseclass is too far away -- e.g. no "launcher"
 * variable, etc.
 *
 * Instead -- put common code in here and call it as common utilities This class is designed to be thread-safe and
 * IMMUTABLE
 *
 * @author bnevins
 */
public class StartServerHelper {
    private static final LocalStringsImpl I18N = new LocalStringsImpl(StartServerHelper.class);
    private static final Logger LOG = System.getLogger(StartServerHelper.class.getName(), I18N.getBundle());

    private final boolean terse;
    private final GFLauncher launcher;
    private final File pidFile;
    private final GFLauncherInfo info;
    private final List<HostAndPort> addresses;
    private final ServerDirs serverDirs;
    private final String masterPassword;
    private final String serverOrDomainName;
    private final int debugPort;

    public StartServerHelper(boolean terse, ServerDirs serverDirs, GFLauncher launcher, String masterPassword) {
        this(terse, serverDirs, launcher, masterPassword, false);
    }

    public StartServerHelper(boolean terse, ServerDirs serverDirs, GFLauncher launcher, String masterPassword,
            boolean debug) {
        this.terse = terse;
        this.launcher = launcher;
        info = launcher.getInfo();

        if (info.isDomain()) {
            serverOrDomainName = info.getDomainName();
        } else {
            serverOrDomainName = info.getInstanceName();
        }

        addresses = info.getAdminAddresses();
        this.serverDirs = serverDirs;
        pidFile = serverDirs.getPidFile();
        this.masterPassword = masterPassword;

        // it will be < 0 if both --debug is false and debug-enabled=false in jvm-config
        debugPort = launcher.getDebugPort();
    }


    public void waitForServerStart() throws CommandException {
        if (!terse) {
            // use stdout because logger always appends a newline
            System.out.print(I18N.get("WaitServer", serverOrDomainName) + " ");
        }

        final Process glassFishProcess;
        try {
            glassFishProcess = launcher.getProcess();
        } catch (GFLauncherException e) {
            throw new IllegalStateException("Could not access the server process!", e);
        }
        final Supplier<Boolean> signOfFinishedStartup = () -> {
            if (pidFile == null) {
                if (isListeningOnAnyEndpoint()) {
                    return true;
                }
            } else {
                if (pidFile.exists()) {
                    LOG.log(Level.TRACE, "The pid file {0} has been created.", pidFile);
                    return true;
                }
            }
            // Don't wait if the process died.
            return !glassFishProcess.isAlive();
        };
        final Duration timeout = Duration.ofMillis(WAIT_FOR_DAS_TIME_MS);
        if (!ProcessUtils.waitFor(signOfFinishedStartup, timeout, !terse)) {
            final String msg;
            if (info.isDomain()) {
                msg = I18N.get("serverNoStart", I18N.get("DAS"), info.getDomainName(), timeout.toSeconds());
            } else {
                msg = I18N.get("serverNoStart", I18N.get("INSTANCE"), info.getInstanceName(), timeout.toSeconds());
            }
            throw new CommandException(msg);
        }

        if (glassFishProcess.isAlive()) {
            // Ok, server is running.
            return;
        }

        // Now try to throw some comprehensible report about what happened.
        final int exitCode = glassFishProcess.exitValue();
        final String output;
        try {
            ProcessStreamDrainer psd = launcher.getProcessStreamDrainer();
            output = psd.getOutErrString();
        } catch (GFLauncherException e) {
            throw new IllegalStateException("Could not access the output of the server process!", e);
        }
        final String serverName = info.isDomain()
            ? "domain " + info.getDomainName()
            : "instance " + info.getInstanceName();
        if (ok(output)) {
            throw new CommandException(I18N.get("serverDiedOutput", serverName, exitCode, output));
        }
        throw new CommandException(I18N.get("serverDied", serverName, exitCode));
    }

    /**
     * Run a series of commands to prepare for a launch.
     *
     * @return false if there was a problem.
     */
    public boolean prepareForLaunch() throws CommandException {
        waitForParentToDie();
        setSecurity();
        if (!checkPorts()) {
            return false;
        }
        deletePidFile();
        return true;
    }


    public void report() {
        final String logfile;
        try {
            logfile = launcher.getLogFilename();
        } catch (GFLauncherException e) {
            throw new IllegalStateException(e);
        }

        final Integer adminPort;
        if (addresses == null || addresses.isEmpty()) {
            adminPort = null;
        } else {
            adminPort = addresses.get(0).getPort();
        }
        LOG.log(Level.INFO, "ServerStart.SuccessMessage", info.isDomain() ? "domain " : "instance",
            serverDirs.getServerName(), serverDirs.getServerDir(), logfile, adminPort);
    }


    /**
     * If the parent is a GF server -- then wait for it to die. This is part of the Client-Server Restart Dance! THe dying
     * server called us with the system property AS_RESTART set to its pid
     *
     * @throws CommandException if we timeout waiting for the parent to die or if the admin ports never free up
     */
    private void waitForParentToDie() throws CommandException {
        // we also come here with just a regular start in which case there is
        // no parent, and the System Property is NOT set to anything...
        final Integer pid = getParentPid();
        if (pid == null) {
            return;
        }
        LOG.log(DEBUG, "Waiting for death of the parent process with pid={0}", pid);
        final Supplier<Boolean> parentDeathSign = () -> {
            if (pid != null && ProcessUtils.isAlive(pid)) {
                return false;
            }
            return !isListeningOnAnyEndpoint();
        };
        if (!ProcessUtils.waitFor(parentDeathSign, Duration.ofMillis(DEATH_TIMEOUT_MS), false)) {
            throw new CommandException(I18N.get("deathwait_timeout", DEATH_TIMEOUT_MS));
        }
        LOG.log(DEBUG, "Parent process with PID={0} is dead and all admin endpoints are free.", pid);
    }


    private Integer getParentPid() {
        String pid = System.getProperty("AS_RESTART");
        if (!ok(pid)) {
            return null;
        }
        try {
            return Integer.valueOf(pid);
        } catch (NumberFormatException e) {
            LOG.log(WARNING, "Cannot parse pid {0} required for waiting for the death of the parent process.", pid);
            return null;
        }
    }


    private boolean isListeningOnAnyEndpoint() {
        for (HostAndPort address : addresses) {
            if (ProcessUtils.isListening(address)) {
                LOG.log(Level.TRACE, "Server is listening on {0}.", address);
                return true;
            }
        }
        return false;
    }


    private boolean checkPorts() {
        String err = adminPortInUse();
        if (err == null) {
            return true;
        }
        LOG.log(WARNING, err);
        return false;
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

    private void setSecurity() {
        info.addSecurityToken(MASTER_PASSWORD, masterPassword);
    }

    private String adminPortInUse() {
        return adminPortInUse(info.getAdminAddresses());
    }

    private static String adminPortInUse(List<HostAndPort> adminAddresses) {
        // it returns a String for logging --- if desired
        for (HostAndPort addr : adminAddresses) {
            if (!NetUtils.isPortFree(addr.getHost(), addr.getPort())) {
                return I18N.get("ServerRunning", addr.getPort());
            }
        }

        return null;
    }
}
