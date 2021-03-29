/*
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
import java.io.*;
import java.util.*;
import org.glassfish.api.Param;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.admin.*;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;

import com.sun.enterprise.admin.cli.remote.*;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

import jakarta.inject.Inject;

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

    @Inject
    private ServiceLocator habitat;
    private static final LocalStringsImpl strings = new LocalStringsImpl(RestartDomainCommand.class);

    /**
     * Execute the restart-domain command.
     */
    @Override
    protected void doCommand() throws CommandException {

        if (!isRestartable())
            throw new CommandException(Strings.get("restartDomain.notRestartable"));

        int oldServerPid = getServerPid(); // might be < 0

        // run the remote restart-domain command and throw away the output
        RemoteCLICommand cmd = new RemoteCLICommand("restart-domain", programOpts, env);

        if (debug != null)
            cmd.executeAndReturnOutput("restart-domain", "--debug", debug.toString());
        else
            cmd.executeAndReturnOutput("restart-domain");

        waitForRestart(oldServerPid);

        logger.info(strings.get("restartDomain.success"));
    }

    /**
     * If the server isn't running, try to start it.
     */
    @Override
    protected int dasNotRunning() throws CommandException {
        if (!isLocal())
            throw new CommandException(Strings.get("restart.dasNotRunningNoRestart"));
        logger.warning(strings.get("restart.dasNotRunning"));
        CLICommand cmd = habitat.getService(CLICommand.class, "start-domain");
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
        List<String> opts = new ArrayList<String>();
        opts.add("start-domain");
        if (debug != null) {
            opts.add("--debug");
            opts.add(debug.toString());
        }
        if (domainDirParam != null) {
            opts.add("--domaindir");
            opts.add(domainDirParam);
            // XXX - would this be better?
            //opts.add(getDomainRootDir().toString());
        }
        if (getDomainName() != null)
            opts.add(getDomainName());

        return cmd.execute(opts.toArray(new String[opts.size()]));
    }
}
