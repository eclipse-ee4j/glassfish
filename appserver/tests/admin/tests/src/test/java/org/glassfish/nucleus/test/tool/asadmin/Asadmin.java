/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.nucleus.test.tool.asadmin;

import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.universal.process.ProcessManagerTimeoutException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Arrays.asList;

/**
 * Tool for executing asadmin/asadmin.bat commands.
 * The tool is stateless.
 *
 * @author David Matejcek
 */
public class Asadmin {
    private static final Logger LOG = Logger.getLogger(Asadmin.class.getName());

    private static final int DEFAULT_TIMEOUT_MSEC = 10 * 1000;

    private final File asadmin;
    private final String adminUser;
    private final File adminPasswordFile;


    /**
     * Creates a stateless instance of the tool.
     *
     * @param asadmin - executable file
     * @param adminUser - username authorized to use the domain
     * @param adminPasswordFile - a file containing admin's password set as <code>AS_ADMIN_PASSWORD=...</code>
     */
    public Asadmin(final File asadmin, final String adminUser, final File adminPasswordFile) {
        this.asadmin = asadmin;
        this.adminUser = adminUser;
        this.adminPasswordFile = adminPasswordFile;
    }


    /**
     * @return asadmin command file name
     */
    public String getCommandName() {
        return asadmin.getName();
    }


    /**
     * Executes the command with arguments asynchronously with {@value #DEFAULT_TIMEOUT_MSEC} ms timeout.
     * The command can be attached by the attach command.
     * You should find the job id in the {@link AsadminResult#getStdOut()} as <code>Job ID: [0-9]+</code>
     *
     * @param args
     * @return {@link AsadminResult} never null.
     */
    public AsadminResult execDetached(final String... args) {
        return exec(DEFAULT_TIMEOUT_MSEC, true, args);
    }

    /**
     * Executes the command with arguments synchronously with {@value #DEFAULT_TIMEOUT_MSEC} ms timeout.
     *
     * @param args
     * @return {@link AsadminResult} never null.
     */
    public AsadminResult exec(final String... args) {
        return exec(DEFAULT_TIMEOUT_MSEC, false, args);
    }


    /**
     * Executes the command with arguments.
     *
     * @param timeout timeout in millis
     * @param detached - detached command is executed asynchronously, can be attached later by the attach command.
     * @param args - command and arguments.
     * @return {@link AsadminResult} never null.
     */
    public AsadminResult exec(final int timeout, final boolean detached, final String... args) {
        final List<String> parameters = asList(args);
        LOG.log(Level.INFO, "exec(timeout={0}, detached={1}, args={2})", new Object[] {timeout, detached, parameters});
        final List<String> command = new ArrayList<>();
        command.add(asadmin.getAbsolutePath());
        command.add("--user");
        command.add(adminUser);
        command.add("--passwordfile");
        command.add(adminPasswordFile.getAbsolutePath());
        if (detached) {
            command.add("--detach");
        }
        command.addAll(parameters);

        final ProcessManager pm = new ProcessManager(command);
        pm.setTimeoutMsec(timeout);
        pm.setEcho(false);

        int exitCode;
        String asadminErrorMessage = "";
        try {
            exitCode = pm.execute();
        } catch (final ProcessManagerTimeoutException e) {
            asadminErrorMessage = "ProcessManagerTimeoutException: command timed stdOut after " + timeout + " ms.\n";
            exitCode = 1;
        } catch (final ProcessManagerException e) {
            LOG.log(Level.SEVERE, "The execution failed.", e);
            asadminErrorMessage = e.getMessage();
            exitCode = 1;
        }

        final String stdErr = pm.getStderr() + '\n' + asadminErrorMessage;
        final AsadminResult ret = new AsadminResult(args[0], exitCode, pm.getStdout(), stdErr);
        writeToStdOut(ret.getOutput());
        return ret;
    }


    private static void writeToStdOut(final String text) {
        if (!text.isEmpty()) {
            System.out.print(text);
        }
    }
}
