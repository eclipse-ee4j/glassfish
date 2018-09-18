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

package com.sun.enterprise.admin.cli.optional;

import java.io.File;
import java.util.HashMap;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.admin.cli.CLIProcessExecutor;
import com.sun.enterprise.admin.cli.CLIUtil;
import com.sun.enterprise.admin.cli.Environment;
import com.sun.enterprise.admin.cli.ProgramOptions;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.OS;

/**
 *  stop-database command
 *  This command class will invoke DerbyControl to stop
 *  the database.
 *
 *  @author <a href="mailto:jane.young@sun.com">Jane Young</a>
 *  @author Bill Shannon
 */
@Service(name = "stop-database")
@PerLookup
public final class StopDatabaseCommand extends DatabaseCommand {

    @Param(name = "dbuser", optional = true)
    private String dbUser;

    private File dbPasswordFile;
    private String dbPassword;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(StopDatabaseCommand.class);

    /**
     * Defines the command to stop the derby database.
     * Note that when using Darwin (Mac), the property,
     * "-Dderby.storage.fileSyncTransactionLog=True" is defined.
     */
    public String[] stopDatabaseCmd() throws Exception {
        passwords = new HashMap<String, String>();
        String passwordfile = this.getOption(ProgramOptions.PASSWORDFILE);
        if (passwordfile != null) {
            dbPasswordFile = new File(passwordfile);
            dbPasswordFile = SmartFile.sanitize(dbPasswordFile);
        }
        if (dbPasswordFile != null) {
            passwords =
                CLIUtil.readPasswordFileOptions(dbPasswordFile.getPath(), true);
            dbPassword = passwords.get(Environment.getPrefix() + "DBPASSWORD");
        }
        if (dbUser == null && dbPassword == null) {
            if (OS.isDarwin()) {
                return new String[]{
                            sJavaHome + File.separator + "bin" + File.separator + "java",
                            "-Djava.library.path=" + sInstallRoot + File.separator + "lib",
                            "-Dderby.storage.fileSyncTransactionLog=True",
                            "-cp",
                            sClasspath + File.pathSeparator + sDatabaseClasspath,
                            "com.sun.enterprise.admin.cli.optional.DerbyControl",
                            "shutdown",
                            dbHost, dbPort, "false"
                        };
            }
            return new String[]{
                        sJavaHome + File.separator + "bin" + File.separator + "java",
                        "-Djava.library.path=" + sInstallRoot + File.separator + "lib",
                        "-cp",
                        sClasspath + File.pathSeparator + sDatabaseClasspath,
                        "com.sun.enterprise.admin.cli.optional.DerbyControl",
                        "shutdown",
                        dbHost, dbPort, "false"
                    };
        } else {
            if (OS.isDarwin()) {
                return new String[]{
                            sJavaHome + File.separator + "bin" + File.separator + "java",
                            "-Djava.library.path=" + sInstallRoot + File.separator + "lib",
                            "-Dderby.storage.fileSyncTransactionLog=True",
                            "-cp",
                            sClasspath + File.pathSeparator + sDatabaseClasspath,
                            "com.sun.enterprise.admin.cli.optional.DerbyControl",
                            "shutdown",
                            dbHost, dbPort, "false", dbUser, dbPassword
                        };
            }
            return new String[]{
                        sJavaHome + File.separator + "bin" + File.separator + "java",
                        "-Djava.library.path=" + sInstallRoot + File.separator + "lib",
                        "-cp",
                        sClasspath + File.pathSeparator + sDatabaseClasspath,
                        "com.sun.enterprise.admin.cli.optional.DerbyControl",
                        "shutdown",
                        dbHost, dbPort, "false", dbUser, dbPassword
                    };
        }
    }

    /**
     *  Method that Executes the command
     *  @throws CommandException
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {
        try {
            prepareProcessExecutor();
            CLIProcessExecutor cpe = new CLIProcessExecutor();
            cpe.execute("pingDatabaseCmd", pingDatabaseCmd(false), true);
            if (cpe.exitValue() > 0) {
                // if ping is unsuccesfull then database is not up and running
                throw new CommandException(
                    strings.get("StopDatabaseStatus", dbHost, dbPort));
            } else if (cpe.exitValue() < 0) {
                // Something terribly wrong!
                throw new CommandException(
                    strings.get("UnableToStopDatabase", "derby.log"));
            } else {
                // database is running so go ahead and stop the database
                cpe.execute("stopDatabaseCmd", stopDatabaseCmd(), true);
                if (cpe.exitValue() > 0) {
                    throw new CommandException(
                        strings.get("UnableToStopDatabase", "derby.log"));
                }
            }
        } catch (Exception e) {
            throw new CommandException(
                strings.get("UnableToStopDatabase", "derby.log"), e);
        }
        return 0;
    }
}
