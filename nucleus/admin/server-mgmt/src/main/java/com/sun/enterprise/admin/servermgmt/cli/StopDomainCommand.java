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

import com.sun.enterprise.admin.cli.CLIConstants;
import com.sun.enterprise.admin.cli.remote.DASUtils;
import com.sun.enterprise.admin.cli.remote.RemoteCLICommand;
import com.sun.enterprise.universal.process.KillNotPossibleException;
import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.enterprise.util.HostAndPort;

import java.io.File;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.time.Duration;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.admin.cli.CLIConstants.DEATH_TIMEOUT_MS;

/**
 * The stop-domain command.
 *
 * @author Byron Nevins
 * @author Bill Shannon
 */
@Service(name = "stop-domain")
@PerLookup
public class StopDomainCommand extends LocalDomainCommand {
    private static final Logger LOG = System.getLogger(StopDomainCommand.class.getName());

    @Param(name = "domain_name", primary = true, optional = true)
    private String userArgDomainName;
    @Param(name = "force", optional = true, defaultValue = "true")
    private Boolean force;
    @Param(optional = true, defaultValue = "false")
    private Boolean kill;

    private HostAndPort addr;

    @Override
    protected void validate() throws CommandException {
        setDomainName(userArgDomainName);
        super.validate(); // which calls initDomain() !!
    }

    /**
     * Override initDomain in LocalDomainCommand to only initialize the local domain information (name, directory) in the
     * local case, when no --host has been specified.
     */
    @Override
    protected void initDomain() throws CommandException {
        // only initialize local domain information if it's a local operation

        // TODO Byron said in April 2013 that we should probably just check if
        // NetUtils says that the getHost() --> isThisMe() rather than merely
        // checking for the magic "localhost" string.  Too risky to fool with it today.
        // FIXME: Not every operating system uses localhost
        // FIXME: When explicitly used --host argument, should use the remote variant.
        if (programOpts.getHost().equals(CLIConstants.DEFAULT_HOSTNAME)) {
            super.initDomain();
        } else if (userArgDomainName != null) { // remote case
            throw new CommandException(Strings.get("StopDomain.noDomainNameAllowed"));
        }
    }

    @Override
    protected int executeCommand() throws CommandException {

        if (isLocal()) {
            // if the local password isn't available, the domain isn't running
            // (localPassword is set by initDomain)
            if (getServerDirs().getLocalPassword() == null) {
                return dasNotRunning();
            }

            // cmd line has higher priority, but if not set, use domain.xml or defaults
            addr = getAdminAddress();
            programOpts.setHostAndPort(addr);
            LOG.log(Level.DEBUG, "Stopping local domain on port {0}", programOpts.getPort());

            /*
             * If we're using the local password, we don't want to prompt
             * for a new password.  If the local password doesn't work it
             * most likely means we're talking to the wrong server.
             */
            programOpts.setInteractive(false);

            // in the local case, make sure we're talking to the correct DAS
            if (!isThisDAS(getDomainRootDir())) {
                return dasNotRunning();
            }

            LOG.log(Level.DEBUG, "It's the correct DAS");
        } else {
            // remote
            // Verify that the DAS is running and reachable
            if (!DASUtils.pingDASQuietly(programOpts, env)) {
                return dasNotRunning();
            }

            LOG.log(Level.DEBUG, "DAS is running");
            programOpts.setInteractive(false);
        }

        /*
         * At this point any options will have been prompted for, and
         * the password will have been prompted for by pingDASQuietly,
         * so even if the password is wrong we don't want any more
         * prompting here.
         */

        doCommand();
        return 0;
    }


    /**
     * Print message and return exit code when we detect that the DAS is not running.
     */
    protected int dasNotRunning() throws CommandException {
        LOG.log(Level.DEBUG, "dasNotRunning()");
        if (kill) {
            if (isLocal()) {
                try {
                    File prevPid = getServerDirs().getLastPidFile();
                    ProcessUtils.kill(prevPid, Duration.ofMillis(DEATH_TIMEOUT_MS), !programOpts.isTerse());
                } catch (KillNotPossibleException e) {
                    throw new CommandException(e.getMessage(), e);
                }
            } else {
                throw new CommandException(Strings.get("StopDomain.dasNotRunningRemotely"));
            }
        }

        // by definition this is not an error
        // https://glassfish.dev.java.net/issues/show_bug.cgi?id=8387
        if (isLocal()) {
            LOG.log(Level.WARNING, "CLI306: Warning - The server located at {0} is not running.", getDomainRootDir());
        } else {
            LOG.log(Level.WARNING, "CLI307: Warning - remote server is not running, unable to force it to stop.\n"
                + "Try running stop-domain on the remote server.");
        }
        return 0;
    }

    /**
     * Execute the actual stop-domain command.
     */
    protected void doCommand() throws CommandException {
        // run the remote stop-domain command and throw away the output
        final RemoteCLICommand cmd = new RemoteCLICommand(getName(), programOpts, env);
        final File watchedPid = isLocal() ? getServerDirs().getPidFile() : null;
        final Long oldPid = ProcessUtils.loadPid(watchedPid);
        final Duration timeout = Duration.ofMillis(DEATH_TIMEOUT_MS);
        final boolean printDots = !programOpts.isTerse();
        try {
            cmd.executeAndReturnOutput("stop-domain", "--force", force.toString());
            if (printDots) {
                // use stdout because logger always appends a newline
                System.out.print(Strings.get("StopDomain.WaitDASDeath") + " ");
            }
            final boolean dead;
            if (isLocal()) {
                dead = ProcessUtils.waitWhileIsAlive(oldPid, timeout, printDots);
            } else {
                dead = ProcessUtils.waitWhileListening(addr, timeout, printDots);
            }
            if (!dead) {
                throw new CommandException(Strings.get("StopDomain.DASNotDead", timeout.toSeconds()));
            }
        } catch (Exception e) {
            // The domain server may have died so fast we didn't have time to
            // get the (always successful!!) return data.  This is NOT AN ERROR!
            LOG.log(Level.DEBUG, "Remote stop-domain call failed.", e);
            if (kill && isLocal()) {
                try {
                    File prevPid = getServerDirs().getLastPidFile();
                    ProcessUtils.kill(prevPid, timeout, printDots);
                    return;
                } catch (Exception ex) {
                    e.addSuppressed(ex);
                }
            }
            throw e;
        }
    }
}
