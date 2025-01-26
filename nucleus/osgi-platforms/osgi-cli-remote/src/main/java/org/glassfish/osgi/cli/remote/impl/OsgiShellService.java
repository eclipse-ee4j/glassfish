/*
 * Copyright (c) 2022, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.osgi.cli.remote.impl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Common parent of OSGi Shell service wrappers.
 *
 * @author David Matejcek
 */
public abstract class OsgiShellService {
    /** Used by LocalOSGiShellCommand */
    public static final String ASADMIN_OSGI_SHELL = "asadmin-osgi-shell";
    private static final Logger LOG = Logger.getLogger(OsgiShellService.class.getName());

    private final ActionReport report;
    private final ByteArrayOutputStream stdoutBytes;
    private final ByteArrayOutputStream stderrBytes;
    /** Can be used to send response to user */
    protected final PrintStream stdout;
    /** Error output for user */
    protected final PrintStream stderr;


    /**
     * Initializes streams collecting output,
     *
     * @param report
     */
    protected OsgiShellService(final ActionReport report) {
        this.report = report;
        this.stdoutBytes = new ByteArrayOutputStream(8192);
        this.stdout = new PrintStream(stdoutBytes, false, UTF_8);
        this.stderrBytes = new ByteArrayOutputStream(8192);
        this.stderr = new PrintStream(stderrBytes, false, UTF_8);
    }


    /**
     * Executes the command.
     *
     * @param cmdName - command name. Can be {@value #ASADMIN_OSGI_SHELL} or the first command of cmd.
     * @param cmd - command and arguments.
     * @return updated {@link ActionReport} given in constructor
     * @throws Exception
     */
    public ActionReport exec(final String cmdName, final String cmd) throws Exception {
        LOG.log(Level.FINE, "exec: {0}", cmd);
        execCommand(cmdName, cmd);
        return generateReport();
    }


    /**
     * Calls the real implementation of the OSGI shell.
     *
     * @param cmdName - command name. Can be {@value #ASADMIN_OSGI_SHELL} or the first command of cmd.
     * @param cmd - command and arguments.
     * @throws Exception
     */
    protected abstract void execCommand(String cmdName, String cmd) throws Exception;


    private ActionReport generateReport() {
        stdout.flush();
        final String output = stdoutBytes.toString(UTF_8);
        report.setMessage(output);

        stderr.flush();
        final String errors = stderrBytes.toString(UTF_8);
        if (errors.isEmpty()) {
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } else {
            report.setMessage(errors);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
        return report;
    }
}
