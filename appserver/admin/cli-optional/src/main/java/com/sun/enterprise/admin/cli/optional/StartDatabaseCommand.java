/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.universal.glassfish.GFLauncherUtils;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.OS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.admin.cli.optional.DerbyControl.DB_LOG_FILENAME;
import static com.sun.enterprise.util.Utility.isAnyNull;
import static java.io.File.pathSeparator;
import static java.io.File.separator;
import static java.util.Arrays.asList;

/**
 * start-database command This command class will invoke DerbyControl to first ping if the database is running. If not
 * then it will start the database. If the database is already started, then a message will be displayed to the user.
 *
 * @author <a href="mailto:jane.young@sun.com">Jane Young</a>
 * @author Bill Shannon
 */
@Service(name = "start-database")
@PerLookup
public final class StartDatabaseCommand extends DatabaseCommand {

    private static final LocalStringsImpl strings = new LocalStringsImpl(StartDatabaseCommand.class);
    private final static String DATABASE_DIR_NAME = "databases";

    @Param(name = "dbhome", optional = true)
    private String dbHome;

    @Param(optional = true)
    private String dbname;

    @Param(optional = true)
    private String sqlfilename;

    @Param(name = "jvmoptions", optional = true, separator = ' ')
    private String[] jvmoptions;

    /**
     * Starts the database server in debug mode with suspend on
     */
    @Param(optional = true, shortName = "s", defaultValue = "false")
    private boolean suspend;

    /**
     * defines the command to start the derby database Note that when using Darwin (Mac), the property,
     * "-Dderby.storage.fileSyncTransactionLog=True" is defined.
     */
    public String[] startDatabaseCmd() {
        List<String> cmd = new ArrayList<>();

        cmd.add(javaHome + separator + "bin" + separator + "java");
        cmd.add("-Djava.library.path=" + installRoot + separator + "lib");
        if (OS.isDarwin()) {
            cmd.add("-Dderby.storage.fileSyncTransactionLog=True");
        }
        if (suspend) {
            cmd.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=localhost:9011");
        }

        cmd.add("-cp");
        cmd.add(sClasspath + pathSeparator + sDatabaseClasspath);
        if (jvmoptions != null) {
            cmd.addAll(asList(jvmoptions));
        }
        cmd.add("com.sun.enterprise.admin.cli.optional.DerbyControl");
        cmd.add("start");
        cmd.add(dbHost);
        cmd.add(dbPort);
        cmd.add("true");
        cmd.add(dbHome);

        return cmd.toArray(new String[cmd.size()]);
    }

    /**
     * defines the command to print out the database sysinfo Note that when using Darwin (Mac), the property,
     * "-Dderby.storage.fileSyncTransactionLog=True" is defined.
     */
    public String[] sysinfoCmd() throws Exception {
        if (OS.isDarwin()) {
            return new String[] {
                javaHome + separator + "bin" + separator + "java",
                "-Djava.library.path=" + installRoot + separator + "lib",
                "-Dderby.storage.fileSyncTransactionLog=True",
                "-cp", sClasspath + pathSeparator + sDatabaseClasspath,

                "com.sun.enterprise.admin.cli.optional.DerbyControl",
                "sysinfo",
                dbHost, dbPort, "false" };
        }

        return new String[] {
            javaHome + separator + "bin" + separator + "java",
            "-Djava.library.path=" + installRoot + separator + "lib",
            "-cp", sClasspath + pathSeparator + sDatabaseClasspath,

            "com.sun.enterprise.admin.cli.optional.DerbyControl",
            "sysinfo",
            dbHost, dbPort, "false" };

    }

    public String[] executeSQLCmd() throws Exception {
        List<String> cmd = new ArrayList<>();

        cmd.add(javaHome + separator + "bin" + separator + "java");
        cmd.add("-Djava.library.path=" + installRoot + separator + "lib");
        if (suspend) {
            cmd.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=localhost:9012");
        }

        cmd.add("-cp");
        cmd.add(sClasspath + pathSeparator + sDatabaseClasspath);

        cmd.add("com.sun.enterprise.admin.cli.optional.DerbyExecuteSQL");
        cmd.add(dbHost);
        cmd.add(dbPort);
        cmd.add(dbname);
        cmd.add(sqlfilename);

        return cmd.toArray(new String[cmd.size()]);
    }

    /**
     * This method returns dbhome.
     *
     * <p>
     * If dbhome option is specified, then the option value is returned. If not, then go through
     * series of conditions to determine the default dbhome directory.
     *
     * The conditions are as follow:
     * 1. if derby.log exists in the current directory, then that is the default dbhome directory.
     * 2. if derby.log does not exist in the current directory, then create a "databases" in <parent directory of domains>. This is usually <install-dir> in filebased
     * installation. In package based installation this directory is /var/SUNWappserver.
     */
    private String getDatabaseHomeDir() {
        // dbhome is specified then return the dbhome option value
        if (dbHome != null) {
            return dbHome;
        }

        // Check if current directory contains derby.log
        // for now we are going to rely on derby.log file to ascertain
        // whether the current directory is where databases were created.
        // However, this may not always be right.
        //
        // The reason for this behavior is so that it is compatible with 8.2PE and 9.0 release.
        // In 8.2PE and 9.0, the current directory is the default dbhome.
        final String currentDir = System.getProperty("user.dir");
        if ((new File(currentDir, DB_LOG_FILENAME)).exists()) {
            return currentDir;
        }

        // the default dbhome is <AS_INSTALL>/databases
        final File installPath = GFLauncherUtils.getInstallDir();

        if (installPath != null) {
            final File dbDir = new File(installPath, DATABASE_DIR_NAME);
            if (!dbDir.isDirectory() && !dbDir.mkdir()) {
                logger.warning(strings.get("CantCreateDatabaseDir", dbDir));
            }
            try {
                return dbDir.getCanonicalPath();
            } catch (IOException ioe) {
                // If unable to get canonical path, then return absolute path
                return dbDir.getAbsolutePath();
            }
        }

        // Hopefully it'll never get here. if installPath is null then
        // asenv.conf is incorrect.
        return null;
    }

    /**
     * method that execute the command
     *
     * @throws CommandException
     */
    @Override
    protected int executeCommand() throws CommandException, CommandValidationException {
        final CLIProcessExecutor cliProcessExecutor = new CLIProcessExecutor();
        String dbLog = "";
        int exitCode = 0;

        try {
            prepareProcessExecutor();
            dbHome = getDatabaseHomeDir();
            if (dbHome != null) {
                dbLog = dbHome + separator + DB_LOG_FILENAME;
            }

            logger.finer("Ping Database");
            cliProcessExecutor.execute("pingDatabaseCmd", pingDatabaseCmd(true), true);

            // If ping is unsuccessful then database is not up and running
            if (cliProcessExecutor.exitValue() > 0) {
                logger.finer("Start Database");
                cliProcessExecutor.execute("startDatabaseCmd", startDatabaseCmd(), false);
                if (cliProcessExecutor.exitValue() != 0) {
                    throw new CommandException(strings.get("UnableToStartDatabase", dbLog));
                }
            } else if (cliProcessExecutor.exitValue() < 0) {
                // Something terribly wrong!
                throw new CommandException(strings.get("CommandUnSuccessful", name));
            } else {
                // database already started
                logger.info(strings.get("StartDatabaseStatus", dbHost, dbPort));
            }
        } catch (IllegalThreadStateException ite) {

            // IllegalThreadStateException is thrown if the
            // process has not yet terminated and is still running.
            //
            // see http://java.sun.com/j2se/1.5.0/docs/api/java/lang/Process.html#exitValue()
            // This is good since that means the database is up and running.
            CLIProcessExecutor cpePing = new CLIProcessExecutor();
            CLIProcessExecutor cpeSysInfo = new CLIProcessExecutor();
            try {
                if (!programOpts.isTerse()) {
                    // Try getting sysinfo
                    logger.fine(strings.get("database.info.msg", dbHost, dbPort));
                }
                cpePing.execute("pingDatabaseCmd", pingDatabaseCmd(true), true);
                int counter = 0;

                // Give time for the database to be started
                while (cpePing.exitValue() != 0 && (suspend || counter < 10)) {
                    cpePing.execute("pingDatabaseCmd", pingDatabaseCmd(true), true);
                    Thread.sleep(500);
                    counter++;
                    // Break out if start-database failed
                    try {
                        cliProcessExecutor.exitValue();
                        break;
                    } catch (IllegalThreadStateException itse) {
                        continue;
                    }
                }

                if (!programOpts.isTerse()) {
                    logger.finer("Database SysInfo");
                    if (cpePing.exitValue() == 0) {
                        cpeSysInfo.execute("sysinfoCmd", sysinfoCmd(), true);
                        if (cpeSysInfo.exitValue() != 0) {
                            logger.info(strings.get("CouldNotGetSysInfo"));
                        }
                    }
                }

                if (cpePing.exitValue() == 0 && !isAnyNull(dbname, sqlfilename)) {
                    cpeSysInfo.execute("executeSQLCmd", executeSQLCmd(), true);
                    if (cpeSysInfo.exitValue() != 0) {
                        logger.info("Failed to execute SQL to init DB.");
                    }
                }

            } catch (Exception e) {
                throw new CommandException(strings.get("CommandUnSuccessful", name), e);
            }

            if (cpePing.exitValue() == 0) {
                logger.info(strings.get("DatabaseStartMsg"));
                if ((new File(dbLog)).canWrite()) {
                    logger.info(strings.get("LogRedirectedTo", dbLog));
                }
            } else {
                throw new CommandException(strings.get("UnableToStartDatabase", dbLog));
            }
        } catch (Exception e) {
            throw new CommandException(strings.get("CommandUnSuccessful", name), e);
        }

        return exitCode;
    }
}
