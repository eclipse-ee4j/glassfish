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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.admin.cli.CLIProcessExecutor;
import com.sun.enterprise.universal.glassfish.GFLauncherUtils;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.OS;


/**
 *  start-database command
 *  This command class will invoke DerbyControl to first ping
 *  if the database is running.  If not then it will start
 *  the database.  If the database is already started, then
 *  a message will be displayed to the user.
 *
 *  @author <a href="mailto:jane.young@sun.com">Jane Young</a>
 *  @author Bill Shannon
 */
@Service(name = "start-database")
@PerLookup
public final class StartDatabaseCommand extends DatabaseCommand {
    private final static String DATABASE_DIR_NAME = "databases";

    @Param(name = "dbhome", optional = true)
    private String dbHome;

    @Param(name = "jvmoptions", optional = true, separator=' ')
    private String[] jvmoptions;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(StartDatabaseCommand.class);

    /**
     *  defines the command to start the derby database
     *  Note that when using Darwin (Mac), the property,
     *  "-Dderby.storage.fileSyncTransactionLog=True" is defined.
     */
    public String[] startDatabaseCmd() {
        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add(sJavaHome + File.separator + "bin" + File.separator + "java");
        cmd.add("-Djava.library.path=" + sInstallRoot + File.separator + "lib");
        if (OS.isDarwin()) {
            cmd.add("-Dderby.storage.fileSyncTransactionLog=True");
        }
        cmd.add("-cp");
        cmd.add(sClasspath + File.pathSeparator + sDatabaseClasspath);
        if (jvmoptions != null) {
            cmd.addAll(Arrays.asList(jvmoptions));
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
     *  defines the command to print out the database sysinfo
     *  Note that when using Darwin (Mac), the property,
     *  "-Dderby.storage.fileSyncTransactionLog=True" is defined.
     */
    public String[] sysinfoCmd() throws Exception {
        if (OS.isDarwin()) {
            return new String [] {
                sJavaHome+File.separator+"bin"+File.separator+"java",
                "-Djava.library.path="+sInstallRoot+File.separator+"lib",
                "-Dderby.storage.fileSyncTransactionLog=True",
                "-cp",
                sClasspath + File.pathSeparator + sDatabaseClasspath,
                "com.sun.enterprise.admin.cli.optional.DerbyControl",
                "sysinfo",
               dbHost, dbPort, "false"
            };
        } else {
            return new String [] {
                sJavaHome+File.separator+"bin"+File.separator+"java",
                "-Djava.library.path="+sInstallRoot+File.separator+"lib",
                "-cp",
                sClasspath + File.pathSeparator + sDatabaseClasspath,
                "com.sun.enterprise.admin.cli.optional.DerbyControl",
                "sysinfo",
                dbHost, dbPort, "false"
            };
        }
    }

    /**
     *  This method returns dbhome.
     *  If dbhome option is specified, then the option value is returned.
     *  If not, then go through series of conditions to determine the
     *  default dbhome directory.
     *  The conditions are as follow:
     *    1.  if derby.log exists in the current directory, then that is
     *        the default dbhome directory.
     *    2.  if derby.log does not exist in the current directory, then
     *        create a "databases" in <parent directory of domains>.  This
     *        is usually <install-dir> in filebased installation.  In
     *        package based installation this directory is /var/SUNWappserver.
     */
    private String getDatabaseHomeDir() {
        // dbhome is specified then return the dbhome option value
        if (dbHome != null)
            return dbHome;

        // check if current directory contains derby.log
        // for now we are going to rely on derby.log file to ascertain
        // whether the current directory is where databases were created.
        // However, this may not always be right.
        // The reason for this behavior is so that it is
        // compatible with 8.2PE and 9.0 release.
        // In 8.2PE and 9.0, the current directory is the
        // default dbhome.
        final String currentDir = System.getProperty("user.dir");
        if ((new File(currentDir, DerbyControl.DB_LOG_FILENAME)).exists())
            return currentDir;
        // the default dbhome is <AS_INSTALL>/databases
        final File installPath = GFLauncherUtils.getInstallDir();

        if (installPath != null) {
            final File dbDir = new File(installPath, DATABASE_DIR_NAME);
            if (!dbDir.isDirectory() && !dbDir.mkdir())
                logger.warning(strings.get("CantCreateDatabaseDir", dbDir));
            try {
                return dbDir.getCanonicalPath();
            } catch (IOException ioe) {
                // if unable to get canonical path, then return absolute path
                return dbDir.getAbsolutePath();
            }
        }
        // hopefully it'll never get here.  if installPath is null then
        // asenv.conf is incorrect.
        return null;
    }

    /**
     *  method that execute the command
     *  @throws CommandException
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {
        final CLIProcessExecutor cpe = new CLIProcessExecutor();
        String dbLog = "";
        int exitCode = 0;
        try {
            prepareProcessExecutor();
            dbHome = getDatabaseHomeDir();
            if (dbHome != null)
                dbLog = dbHome + File.separator + DerbyControl.DB_LOG_FILENAME;

            logger.finer("Ping Database");
            cpe.execute("pingDatabaseCmd", pingDatabaseCmd(true), true);
            // if ping is unsuccesfull then database is not up and running
            if (cpe.exitValue() > 0) {
                logger.finer("Start Database");
                cpe.execute("startDatabaseCmd", startDatabaseCmd(), false);
                if (cpe.exitValue() != 0) {
                    throw new CommandException(strings.get("UnableToStartDatabase", dbLog));
                }
            } else if (cpe.exitValue() < 0) {
                // Something terribly wrong!
                throw new CommandException(strings.get("CommandUnSuccessful", name));
            } else {
                // database already started
                logger.info(strings.get("StartDatabaseStatus", dbHost, dbPort));
            }
        } catch (IllegalThreadStateException ite) {
            // IllegalThreadStateException is thrown if the
            // process has not yet teminated and is still running.
            // see http://java.sun.com/j2se/1.5.0/docs/api/java/lang/Process.html#exitValue()
            // This is good since that means the database is up and running.
            CLIProcessExecutor cpePing = new CLIProcessExecutor();
            CLIProcessExecutor cpeSysInfo = new CLIProcessExecutor();
            try {
                if (!programOpts.isTerse()) {
                    // try getting sysinfo
                    logger.fine(strings.get("database.info.msg", dbHost, dbPort));
                }
                cpePing.execute("pingDatabaseCmd", pingDatabaseCmd(true), true);
                int counter = 0;
                //give time for the database to be started
                while (cpePing.exitValue() != 0 && counter < 10) {
                    cpePing.execute("pingDatabaseCmd", pingDatabaseCmd(true), true);
                    Thread.sleep(500);
                    counter++;
                    //break out if start-database failed
                    try {
                        cpe.exitValue();
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
