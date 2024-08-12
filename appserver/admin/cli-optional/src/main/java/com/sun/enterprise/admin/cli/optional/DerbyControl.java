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

import com.sun.enterprise.util.i18n.StringManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;

import org.glassfish.api.admin.CommandException;

import static com.sun.enterprise.util.Utility.isAllNull;
import static com.sun.enterprise.util.Utility.isEmpty;

/**
 * This class uses Java reflection to invoke Derby NetworkServerControl class.
 *
 * <p>
 * This class is used to start/stop/ping derby database. The reason for creating this class instead of directly invoking NetworkServerControl from the
 * StartDatabaseCommand class is so that a separate JVM is launched when starting the database and the control is return
 * to CLI.
 *
 * @author <a href="mailto:jane.young@sun.com">Jane Young</a>
 * @version $Revision: 1.13 $
 */
public final class DerbyControl {
    final public static String DB_LOG_FILENAME = "derby.log";

    final private String derbyCommand;
    final private String derbyHost;
    final private String derbyPort;
    final private String derbyHome;
    final private boolean redirect;
    final private String derbyUser;
    final private String derbyPassword;

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("paramters not specified.");
            System.out.println("DerbyControl <derby command> <derby host> <derby port> <derby home> <redirect output>");
            System.exit(1);
        }

        DerbyControl derbyControl = null;
        if (args.length == 3) {
            derbyControl = new DerbyControl(args[0], args[1], args[2]);
        } else if (args.length == 4) {
            derbyControl = new DerbyControl(args[0], args[1], args[2], args[3]);
        } else if (args.length == 5) {
            derbyControl = new DerbyControl(args[0], args[1], args[2], args[3], args[4]);
        } else {
            derbyControl = new DerbyControl(args[0], args[1], args[2], args[3], args[4], args[5]);
        }

        derbyControl.invokeNetworkServerControl();
    }

    public DerbyControl(final String dc, final String dht, final String dp, final String redirect, final String dhe, final String duser, final String dpwd) {
        this.derbyCommand = dc;
        this.derbyHost = dht;
        this.derbyPort = dp;
        this.derbyHome = dhe;
        this.redirect = Boolean.valueOf(redirect).booleanValue();
        this.derbyUser = duser;
        this.derbyPassword = dpwd;

        if (this.redirect) {

            try {
                String dbLog = "";
                if (derbyHome == null) {
                    // If derbyHome is null then redirect the output to a temporary file
                    // which gets deleted after the jvm exists.
                    dbLog = createTempLogFile();
                } else {
                    dbLog = createDBLog(derbyHome);
                }

                // Redirect stdout and stderr to a file
                try (PrintStream printStream = new PrintStream(new FileOutputStream(dbLog, true), true)) {
                    System.setOut(printStream);
                    System.setErr(printStream);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                // exit with an error code of 2
                Runtime.getRuntime().exit(2);
            }
        }

        // Do not set derby.system.home if derbyHome is empty
        if (!isEmpty(derbyHome)) {
            System.setProperty("derby.system.home", derbyHome);
        }
        // Set the property to not overwrite log file
        System.setProperty("derby.infolog.append", "true");
    }

    // constructor
    public DerbyControl(final String dc, final String dht, final String dp) {
        this(dc, dht, dp, "true", null, null, null);
    }

    // constructor
    public DerbyControl(final String dc, final String dht, final String dp, final String redirect) {
        this(dc, dht, dp, redirect, null, null, null);
    }

    // constructor
    public DerbyControl(final String dc, final String dht, final String dp, final String redirect, final String dhe) {
        this(dc, dht, dp, redirect, dhe, null, null);
    }

    public DerbyControl(final String dc, final String dht, final String dp, final String redirect, final String duser, final String dpassword) {
        this(dc, dht, dp, redirect, null, duser, dpassword);
    }

    /**
     * This method invokes the Derby's NetworkServerControl to start/stop/ping the database.
     */
    private void invokeNetworkServerControl() {
        try {
            Class<?> networkServer = Class.forName("org.apache.derby.drda.NetworkServerControl");
            Method networkServerMethod = networkServer.getDeclaredMethod("main", String[].class);
            Object[] parameters = null;
            if (isAllNull(derbyUser, derbyPassword)) {
                parameters = new Object[] {
                    new String[] { derbyCommand, "-h", derbyHost, "-p", derbyPort, "-noSecurityManager" } };
            } else {
                parameters = new Object[] {
                    new String[] { derbyCommand, "-h", derbyHost, "-p", derbyPort, "-noSecurityManager", "-user", derbyUser, "-password", derbyPassword} };
            }

            networkServerMethod.invoke(networkServer, parameters);
        } catch (Throwable t) {
            t.printStackTrace();
            Runtime.getRuntime().exit(2);
        }
    }

    /**
     * create a db.log file that stdout/stderr will redirect to.
     *
     * <p>
     * dbhome is the derby.system.home directory where derb.log
     * gets created. if user specified --dbhome options, derby.log will be created there.
     **/
    private String createDBLog(final String dbHome) throws Exception {
        // dbHome must exist and have write permission
        final File fDBHome = new File(dbHome);
        String dbLogFileName = "";

        final StringManager lsm = StringManager.getManager(DerbyControl.class);
        if (fDBHome.isDirectory() && fDBHome.canWrite()) {
            final File fDBLog = new File(dbHome, DB_LOG_FILENAME);
            dbLogFileName = fDBLog.toString();

            // If the file exists, check if it is writeable
            if (fDBLog.exists() && !fDBLog.canWrite()) {
                System.out.println(lsm.getString("UnableToAccessDatabaseLog", dbLogFileName));
                System.out.println(lsm.getString("ContinueStartingDatabase"));

                // If exist but not able to write then create a temporary
                // log file and persist on starting the database
                dbLogFileName = createTempLogFile();
            } else if (!fDBLog.exists()) {
                // create log file
                if (!fDBLog.createNewFile()) {
                    System.out.println(lsm.getString("UnableToCreateDatabaseLog", dbLogFileName));
                }
            }
        } else {
            System.out.println(lsm.getString("InvalidDBDirectory", dbHome));
            System.out.println(lsm.getString("ContinueStartingDatabase"));
            // if directory does not exist then create a temporary log file
            // and persist on starting the database
            dbLogFileName = createTempLogFile();
        }
        return dbLogFileName;
    }

    /**
     * creates a temporary log file.
     */
    private String createTempLogFile() throws CommandException {
        String tempFileName = "";
        try {
            final File fTemp = File.createTempFile("foo", null);
            fTemp.deleteOnExit();
            tempFileName = fTemp.toString();
        } catch (IOException ioe) {
            final StringManager lsm = StringManager.getManager(DerbyControl.class);
            throw new CommandException(lsm.getString("UnableToAccessDatabaseLog", tempFileName));
        }
        return tempFileName;
    }

}
