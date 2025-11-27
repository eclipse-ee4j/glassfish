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

import com.sun.enterprise.admin.cli.CLICommand;
import com.sun.enterprise.admin.cli.remote.RemoteCLICommand;
import com.sun.enterprise.util.HostAndPort;

import jakarta.inject.Inject;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.admin.cli.CLIConstants.DEATH_TIMEOUT_MS;
import static com.sun.enterprise.admin.cli.CLIConstants.WAIT_FOR_DAS_TIME_MS;
import static com.sun.enterprise.admin.servermgmt.cli.StartServerHelper.parseCustomEndpoints;
import static com.sun.enterprise.admin.servermgmt.util.CommandAction.step;

/**
 * THe restart-domain command. The local portion of this command is only used to block until:
 * <ul>
 * <li>the old server dies
 * <li>the new server starts
 * </ul>
 * Tactics:
 * <ul>
 * <li>Get the uptime for the current server
 * <li>start the remote Restart command
 * <li>Call uptime in a loop until the uptime number is less than the original uptime
 *
 * @author bnevins
 * @author Bill Shannon
 */
@Service(name = "restart-domain")
@PerLookup
public class RestartDomainCommand extends StopDomainCommand {

    @Param(name = "debug", optional = true)
    private Boolean debug;

    // Cannot be disabled, we use it to get PID.
    private boolean checkPidFile = true;

    @Param(name = "check-process-alive", optional = true, defaultValue = "true")
    private boolean checkProcessAlive;

    @Param(name = "check-admin-port", optional = true, defaultValue = "true")
    private boolean checkAdminEndpoint;

    @Param(name = "server-output", shortName = "o",  optional = true)
    private Boolean printServerOutput;

    @Param(optional = true)
    private String customEndpoints;

    @Inject
    private ServiceLocator serviceLocator;

    /**
     * Execute the restart-domain command.
     */
    @Override
    protected void doCommand() throws CommandException {

        if (!isRestartable()) {
            throw new CommandException("The domain reports that it is not restartable.\n\n"
                + "This usually means that the password file that was originally used"
                + " to start the server has been deleted or is not readable now.\n"
                + "Please stop and then restart the server - or fix the password file.");
        }

        // oldPid is received from the running server.
        final Long oldPid = getServerPid();
        final HostAndPort oldAdminAddress = getReachableAdminAddress();
        final boolean printDots = !programOpts.isTerse();
        final PortWatcher portWatcher = oldAdminAddress == null ? null : PortWatcher.watch(oldAdminAddress, printDots);
        final RemoteCLICommand cmd = new RemoteCLICommand("restart-domain", programOpts, env);
        if (debug == null) {
            cmd.executeAndReturnOutput("restart-domain");
        } else {
            cmd.executeAndReturnOutput("restart-domain", "--debug", debug.toString());
        }

        final Duration timeout = getRestartTimeout();
        final Duration startTimeout;
        if (isLocal()) {
            startTimeout = step(null, timeout, () -> waitForStop(oldPid, null, timeout));
        } else {
            startTimeout = timeout;
        }

        // Well, this looks duplicit, but it is not. The port watcher starts before we run the command
        // on the instance. It will attach to the admin endpoint and wait for the disconnection.
        // Meanwhile we ask the instance for restart.
        // After we are sure that the server stopped, we will monitor its start.
        if (portWatcher != null && !portWatcher.get(startTimeout)) {
            logger.warning("The endpoint is still listening after timeout: " + oldAdminAddress);
        }

        final List<HostAndPort> userEndpoints = parseCustomEndpoints(customEndpoints);
        final ServerLifeSignCheck lifeSignCheck = new ServerLifeSignCheck("domain " + getDomainName(),
            printServerOutput, checkPidFile, checkProcessAlive, checkAdminEndpoint, userEndpoints);
        final Supplier<List<HostAndPort>> adminEndpointsSupplier = () -> List.of(getReachableAdminAddress());
        final String report = waitForStart(oldPid, lifeSignCheck, adminEndpointsSupplier, startTimeout);
        logger.info(report);
    }


    /**
     * If the server isn't running, try to start it.
     */
    @Override
    protected int dasNotRunning() throws CommandException {
        if (!isLocal()) {
            throw new CommandException("Remote server is not running, can not restart it");
        }
        logger.warning("Server is not running, will attempt to start it...");
        CLICommand cmd = serviceLocator.getService(CLICommand.class, "start-domain");
        /*
         * Collect the arguments that also apply to start-domain.
         * The start-domain CLICommand object will already have the
         * ProgramOptions injected into it so we don't need to worry
         * about them here.
         *
         * Usage: asadmin [asadmin-utility-options] start-domain
         *      [-v|--verbose[=<verbose(default:false)>]]
         *      [--upgrade[=<upgrade(default:false)>]]
         *      [--debug[=<debug(default:false)>]] [--domaindir <domaindir>]
         *      [-?|--help[=<help(default:false)>]] [domain_name]
         *
         * Only --debug, --domaindir, and the operand apply here.
         */
        List<String> opts = new ArrayList<>();
        opts.add("start-domain");
        if (debug != null) {
            opts.add("--debug");
            opts.add(debug.toString());
        }
        if (domainDirParam != null) {
            opts.add("--domaindir");
            opts.add(domainDirParam);
        }
        final Duration startTimeout = getStartTimeout();
        if (startTimeout != null) {
            opts.add("--timeout");
            opts.add(Long.toString(startTimeout.toSeconds()));
        }
        opts.add("--check-pid-file");
        opts.add(Boolean.toString(checkPidFile));
        opts.add("--check-process-alive");
        opts.add(Boolean.toString(checkProcessAlive));
        opts.add("--check-admin-port");
        opts.add(Boolean.toString(checkAdminEndpoint));
        if (printServerOutput != null) {
            // TODO: At this moment works just when the domain was not running
            opts.add("--server-output");
            opts.add(Boolean.toString(printServerOutput));
        }
        if (customEndpoints != null) {
            opts.add("--custom-endpoints");
            opts.add(customEndpoints);
        }
        if (getDomainName() != null) {
            opts.add(getDomainName());
        }
        return cmd.execute(opts.toArray(String[]::new));
    }

    /**
     * @return timeout for the start-domain command.
     */
    protected Duration getStartTimeout() {
        final Duration parameter = getTimeout();
        return parameter == null ? WAIT_FOR_DAS_TIME_MS : parameter;
    }

    /**
     * @return timeout set as a parameter
     */
    private Duration getRestartTimeout() {
        final Duration paramTimeout = getTimeout();
        if (paramTimeout != null) {
            return paramTimeout;
        }
        final Duration startTimeout = getStartTimeout();
        if (startTimeout == null && DEATH_TIMEOUT_MS == null) {
            return null;
        }
        if (startTimeout == null) {
            return DEATH_TIMEOUT_MS.plusSeconds(5L);
        }
        if (DEATH_TIMEOUT_MS == null) {
            return startTimeout.plusSeconds(5L);
        }
        return WAIT_FOR_DAS_TIME_MS.plus(DEATH_TIMEOUT_MS).plusSeconds(5);
    }
}
