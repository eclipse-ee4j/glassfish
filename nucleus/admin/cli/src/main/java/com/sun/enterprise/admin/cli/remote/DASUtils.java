/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli.remote;

import com.sun.enterprise.admin.cli.Environment;
import com.sun.enterprise.admin.cli.ProgramOptions;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ConnectException;
import java.net.UnknownHostException;

import org.glassfish.api.admin.AuthenticationException;
import org.glassfish.api.admin.CommandException;

import static java.lang.System.Logger.Level.WARNING;

/**
 * Domain Admin Server utility method.
 */
public class DASUtils {
    private static final Logger LOG = System.getLogger(DASUtils.class.getName());

    public enum Error {
        NONE, AUTHENTICATION, CONNECTION, IO, UNKNOWN
    }

    private DASUtils() {
        // can't instantiate
    }

    /**
     * See if DAS is alive. Do not print out the results of the version command from the server.
     *
     * @return true if DAS can be reached and can handle commands, otherwise false.
     */
    public static boolean pingDASQuietly(ProgramOptions programOpts, Environment env) {
        try {
            RemoteCLICommand cmd = new RemoteCLICommand("version", programOpts, env);
            cmd.executeAndReturnOutput(new String[] { "version" });
            return true;
        } catch (AuthenticationException aex) {
            return true;
        } catch (Exception ex) {
            LOG.log(Level.DEBUG, "Communication with the server failed. " + ex.getMessage());
            ExceptionAnalyzer ea = new ExceptionAnalyzer(ex);
            if (ea.getFirstInstanceOf(ConnectException.class) != null) {
                return false; // this definitely means server is not up
            } else if (ea.getFirstInstanceOf(UnknownHostException.class) != null) {
                return false; // this definitely means server is not up
            } else if (ea.getFirstInstanceOf(IOException.class) != null) {
                return true;
            } else {
                return false; // unknown error, shouldn't really happen
            }
        }
    }

    /**
     * See if DAS is alive, but insist that athentication is correct.
     * Do not print out the results of the version command from the server.
     *
     * @return Error code indicating status
     */
    public static Error pingDASWithAuth(ProgramOptions programOpts, Environment env) throws CommandException {
        try {
            RemoteCLICommand cmd = new RemoteCLICommand("version", programOpts, env);
            cmd.executeAndReturnOutput(new String[] { "version" });
        } catch (AuthenticationException aex) {
            return Error.AUTHENTICATION;
        } catch (Exception ex) {
            LOG.log(WARNING, "Communication with the server failed. " + ex.getMessage());
            ExceptionAnalyzer ea = new ExceptionAnalyzer(ex);
            if (ea.getFirstInstanceOf(ConnectException.class) != null) {
                return Error.CONNECTION;
            } else if (ea.getFirstInstanceOf(UnknownHostException.class) != null) {
                return Error.CONNECTION;
            } else if (ea.getFirstInstanceOf(IOException.class) != null) {
                return Error.IO;
            } else {
                return Error.UNKNOWN;
            }
        }
        return Error.NONE;
    }
}
