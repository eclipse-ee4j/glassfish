/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli;

import com.sun.appserv.server.util.Version;
import com.sun.enterprise.admin.cli.remote.RemoteCLICommand;

import java.util.logging.Level;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.jvnet.hk2.annotations.Service;

/**
 * A local version command. Prints the version of the server, if running. Prints the version from locally available
 * Version class if server is not running, if the --local flag is passed or if the version could not be obtained from a
 * server for some reason. The idea is to get the version of server software, the server process need not be running.
 * This command does not return the version of local server installation if its options (host, port, user, passwordfile)
 * identify a running server.
 *
 * @author km@dev.java.net
 * @author Bill Shannon
 */
@Service(name = "version")
@PerLookup
public class VersionCommand extends CLICommand {

    @Param(optional = true, shortName = "v")
    private boolean verbose;

    @Param(optional = true)
    private boolean local;

    @Param(optional = true)
    private boolean terse;

    private static final LocalStringsImpl strings = new LocalStringsImpl(VersionCommand.class);

    @Override
    protected int executeCommand() throws CommandException {
        if (local) {
            invokeLocal();
            return 0;
        }
        try {
            RemoteCLICommand cmd = new RemoteCLICommand("version", programOpts, env);
            String version;
            if (verbose) {
                version = cmd.executeAndReturnOutput("version", "--verbose");
            } else {
                version = cmd.executeAndReturnOutput("version");
            }
            version = version.trim(); // get rid of gratuitous newlines
            logger.info(terse ? version : strings.get("version.remote", version));
        } catch (Exception e) {
            // suppress all output and infer that the server is not running
            printRemoteException(e);
            invokeLocal();
        }
        return 0; // always succeeds
    }

    private void invokeLocal() {
        String fv = Version.getProductIdInfo();

        logger.info(terse ? fv : strings.get("version.local", fv));
        if (verbose) {
            logger.info(strings.get("version.local.java", System.getProperty("java.version")));
        }
    }

    private void printRemoteException(Exception e) {
        logger.info(strings.get("remote.version.failed", programOpts.getHost(), programOpts.getPort() + ""));
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(e.getMessage());
        } else {
            logger.info(strings.get("remote.version.failed.debug", Environment.getDebugVar()));
        }
    }
}
