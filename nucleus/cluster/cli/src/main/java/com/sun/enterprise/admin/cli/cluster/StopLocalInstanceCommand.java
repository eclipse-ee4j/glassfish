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

package com.sun.enterprise.admin.cli.cluster;

import com.sun.enterprise.admin.cli.remote.RemoteCLICommand;
import com.sun.enterprise.universal.process.KillNotPossibleException;
import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.enterprise.util.HostAndPort;

import java.io.File;
import java.net.ConnectException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.logging.Level;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.admin.cli.CLIConstants.DEATH_TIMEOUT_MS;

/**
 * Stop a local server instance.
 *
 * @author Bill Shannon
 * @author Byron Nevins
 */
@Service(name = "stop-local-instance")
@PerLookup
public class StopLocalInstanceCommand extends LocalInstanceCommand {
    @Param(optional = true, defaultValue = "true")
    private Boolean force;
    @Param(name = "instance_name", primary = true, optional = true)
    private String userArgInstanceName;
    @Param(optional = true, defaultValue = "false")
    private Boolean kill;
    @Param(optional = true)
    private Integer timeout;


    @Override
    protected void validate() throws CommandException, CommandValidationException {
        instanceName = userArgInstanceName;
        super.validate();
    }

    @Override
    protected boolean mkdirs(File f) {
        // we definitely do NOT want dirs created for this instance if
        // they don't exist!
        return false;
    }

    /**
     * @return timeout set as a parameter
     */
    protected Duration getTimeout() {
        return timeout == null ?  null : Duration.ofSeconds(timeout);
    }

    /**
     * Big trouble if you allow the super implementation to run
     * because it creates directories.  If this command is called with
     * an instance that doesn't exist -- new dirs will be created which
     * can cause other problems.
     */
    @Override
    protected void initInstance() throws CommandException {
        super.initInstance();
    }

    @Override
    protected int executeCommand() throws CommandException, CommandValidationException {
        // if the local password isn't available, the instance isn't running
        // (localPassword is set by initInstance)
        File serverDir = getServerDirs().getServerDir();
        if (serverDir == null || !serverDir.isDirectory()) {
            return noSuchInstance();
        }

        if (getServerDirs().getLocalPassword() == null) {
            return instanceNotRunning();
        }

        HostAndPort addr = getReachableAdminAddress();
        if (addr == null) {
            return instanceNotRunning();
        }
        programOpts.setHostAndPort(addr);
        logger.log(Level.FINER, "Stopping server at {0}", addr);

        if (!ProcessUtils.isAlive(getServerDirs().getPidFile())) {
            return instanceNotRunning();
        }

        logger.finer("It's the correct Instance");
        doCommand();
        return 0;
    }

    /**
     * Print message and return exit code when
     * we detect that the DAS is not running.
     */
    protected int instanceNotRunning() throws CommandException {
        logger.log(Level.FINE, "instanceNotRunning()");
        if (kill) {
            try {
                File lastPid = getServerDirs().getLastPidFile();
                ProcessUtils.kill(lastPid, getStopTimeout(), !programOpts.isTerse());
            } catch (KillNotPossibleException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                return -1;
            }
        }
        // by definition this is not an error
        logger.warning(Strings.get("StopInstance.instanceNotRunning"));
        return 0;
    }

    /**
     * Print message and return exit code when
     * we detect that there is no such instance
     */
    private int noSuchInstance() {
        // by definition this is not an error
        // https://glassfish.dev.java.net/issues/show_bug.cgi?id=8387
        logger.warning(Strings.get("Instance.noSuchInstance"));
        return 0;
    }

    /**
     * Execute the actual stop-domain command.
     */
    protected void doCommand() throws CommandException {

        // put the local-password for the instance  in programOpts
        // we don't do this for ALL local-instance commands because if they call
        // DAS with the instance's local-password it will cause BIG trouble...
        setLocalPassword();

        /*
         * If we're using the local password, we don't want to prompt
         * for a new password.  If the local password doesn't work it
         * most likely means we're talking to the wrong server.
         */
        programOpts.setInteractive(false);

        final Long pid = getServerPid();
        final boolean printDots = !programOpts.isTerse();
        final RemoteCLICommand cmd = new RemoteCLICommand("_stop-instance", programOpts, env);
        try {
            try {
                cmd.executeAndReturnOutput("_stop-instance", "--force", force.toString());
            } catch (CommandException e) {
                if (e.getCause() instanceof ConnectException) {
                    logger.log(Level.FINE,
                        "Remote _stop-instance call thrown a ConnectException."
                            + " It is usual on Windows, where immediately after port closes, firewalls break"
                            + " any connection even before we can process the response."
                            + " However it is not critical, we will still monitor the PID.",
                        e);
                } else {
                    throw e;
                }
            }
            if (printDots) {
                System.out.print(Strings.get("StopInstance.waitForDeath") + " ");
            }
            final boolean dead = pid == null || ProcessUtils.waitWhileIsAlive(pid, getStopTimeout(), printDots);
            if (!dead) {
                throw new CommandException(MessageFormat
                    .format("Timed out {0} seconds waiting for the instance to stop.", getStopTimeout().toSeconds()));
            }
        } catch (Exception e) {
            // The server may have died so fast we didn't have time to
            // get the (always successful!!) return data.  This is NOT AN ERROR!
            logger.log(Level.CONFIG, "Remote stop-instance call failed.", e);
            if (kill) {
                try {
                    File prevPid = getServerDirs().getLastPidFile();
                    ProcessUtils.kill(prevPid, getStopTimeout(), printDots);
                    return;
                } catch (Exception ex) {
                    e.addSuppressed(ex);
                }
            }
            throw e;
        }
    }

    /**
     * @return timeout for the command
     */
    private Duration getStopTimeout() {
        final Duration parameter = getTimeout();
        return parameter == null ? DEATH_TIMEOUT_MS : parameter;
    }
}
