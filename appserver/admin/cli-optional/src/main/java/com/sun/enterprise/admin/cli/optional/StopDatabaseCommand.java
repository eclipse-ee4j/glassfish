/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.cli.optional;

import com.sun.enterprise.admin.cli.CLIProcessExecutor;
import com.sun.enterprise.admin.cli.Environment;
import com.sun.enterprise.util.OS;

import java.io.File;
import java.util.HashMap;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.admin.cli.CLIUtil.readPasswordFileOptions;
import static com.sun.enterprise.admin.cli.ProgramOptions.PASSWORDFILE;
import static com.sun.enterprise.universal.io.SmartFile.sanitize;
import static com.sun.enterprise.util.Utility.isAllNull;
import static java.io.File.pathSeparator;
import static java.io.File.separator;

/**
 * stop-database command This command class will not invoke DerbyControl to stop the database.
 *
 * @author <a href="mailto:jane.young@sun.com">Jane Young</a>
 * @author Bill Shannon
 */
@Service(name = "stop-database")
@PerLookup
public final class StopDatabaseCommand extends DatabaseCommand {

    private static final LocalStringsImpl strings = new LocalStringsImpl(StopDatabaseCommand.class);

    @Param(name = "dbuser", optional = true)
    private String dbUser;

    private File dbPasswordFile;
    private String dbPassword;

    /**
     * Defines the command to stop the derby database.
     *
     * <p>
     * Note that when using Darwin (Mac), the property,
     * "-Dderby.storage.fileSyncTransactionLog=True" is defined.
     */
    public String[] stopDatabaseCmd() throws Exception {
        passwords = new HashMap<>();
        String passwordfile = this.getOption(PASSWORDFILE);
        if (passwordfile != null) {
            dbPasswordFile = new File(passwordfile);
            dbPasswordFile = sanitize(dbPasswordFile);
        }
        if (dbPasswordFile != null) {
            passwords = readPasswordFileOptions(dbPasswordFile.getPath(), true);
            dbPassword = passwords.get(Environment.getPrefix() + "DBPASSWORD");
        }

        if (isAllNull(dbUser, dbPassword)) {
            if (OS.isDarwin()) {
                return new String[] {
                    javaHome + separator + "bin" + separator + "java",
                    "-Djava.library.path=" + installRoot + separator + "lib",
                    "-Dderby.storage.fileSyncTransactionLog=True",
                    "-cp", sClasspath + pathSeparator + sDatabaseClasspath,

                    "com.sun.enterprise.admin.cli.optional.DerbyControl",
                    "shutdown",
                    dbHost, dbPort, "false" };
            }

            return new String[] {
                javaHome + separator + "bin" + separator + "java",
                "-Djava.library.path=" + installRoot + separator + "lib",
                "-cp", sClasspath + pathSeparator + sDatabaseClasspath,

                "com.sun.enterprise.admin.cli.optional.DerbyControl",
                "shutdown",
                dbHost, dbPort, "false" };
        }

        if (OS.isDarwin()) {
            return new String[] { javaHome + separator + "bin" + separator + "java",
                "-Djava.library.path=" + installRoot + separator +
                "lib", "-Dderby.storage.fileSyncTransactionLog=True",
                "-cp", sClasspath + pathSeparator + sDatabaseClasspath,

                "com.sun.enterprise.admin.cli.optional.DerbyControl",
                "shutdown",
                dbHost, dbPort, "false", dbUser, dbPassword };
        }

        return new String[] {
            javaHome + separator + "bin" + separator + "java",
            "-Djava.library.path=" + installRoot + separator + "lib",
            "-cp", sClasspath + pathSeparator + sDatabaseClasspath,

            "com.sun.enterprise.admin.cli.optional.DerbyControl",
            "shutdown",
            dbHost, dbPort, "false", dbUser, dbPassword };

    }

    /**
     * Method that Executes the command
     *
     * @throws CommandException
     */
    @Override
    protected int executeCommand() throws CommandException, CommandValidationException {
        try {
            prepareProcessExecutor();
            CLIProcessExecutor cliProcessExecutor = new CLIProcessExecutor();
            cliProcessExecutor.execute("pingDatabaseCmd", pingDatabaseCmd(false), true);

            if (cliProcessExecutor.exitValue() > 0) {
                // If ping is unsuccessful then database is not up and running
                throw new CommandException(strings.get("StopDatabaseStatus", dbHost, dbPort));
            }

            if (cliProcessExecutor.exitValue() < 0) {
                // Something terribly wrong!
                throw new CommandException(strings.get("UnableToStopDatabase", "derby.log"));
            }

            // Database is running so go ahead and stop the database
            cliProcessExecutor.execute("stopDatabaseCmd", stopDatabaseCmd(), true);
            if (cliProcessExecutor.exitValue() > 0) {
                throw new CommandException(strings.get("UnableToStopDatabase", "derby.log"));
            }

        } catch (Exception e) {
            throw new CommandException(strings.get("UnableToStopDatabase", "derby.log"), e);
        }

        return 0;
    }
}
