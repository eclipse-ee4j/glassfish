/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.admin.cli.CLICommand;
import com.sun.enterprise.admin.cli.ClassPathBuilder;
import com.sun.enterprise.util.OS;

import java.io.File;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.DERBY_ROOT_PROP_NAME;
import static com.sun.enterprise.util.SystemPropertyConstants.INSTALL_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.JAVA_ROOT_PROPERTY;

/**
 * This is an abstract class to be inherited by StartDatabaseCommand and StopDatabaseCommand. This classes prepares the
 * variables that are used to to invoke DerbyControl. It also contains a pingDatabase method that is used by both
 * start/stop database command.
 *
 * @author <a href="mailto:jane.young@sun.com">Jane Young</a>
 * @author Bill Shannon
 */
public abstract class DatabaseCommand extends CLICommand {

    private static final LocalStringsImpl strings = new LocalStringsImpl(DatabaseCommand.class);

    protected static final String DB_HOST_DEFAULT = "0.0.0.0";
    protected static final String DB_PORT_DEFAULT = "1527";
    protected final static String DB_USER = "dbuser";
    protected final static String DB_PASSWORDFILE = "dbpasswordfile";

    @Param(name = "dbhost", optional = true, defaultValue = DB_HOST_DEFAULT)
    protected String dbHost;

    @Param(name = "dbport", optional = true, defaultValue = DB_PORT_DEFAULT)
    protected String dbPort;

    protected final ClassPathBuilder sClasspath = new ClassPathBuilder();
    protected final ClassPathBuilder sDatabaseClasspath = new ClassPathBuilder();

    protected File dbLocation;
    protected File javaHome;
    protected File installRoot;

    /**
     * Prepare variables to invoke start/ping database command.
     */
    protected void prepareProcessExecutor() throws Exception {
        installRoot = new File(getSystemProperty(INSTALL_ROOT_PROPERTY));
        if (dbHost == null) {
            dbHost = DB_HOST_DEFAULT;
        }
        if (dbPort == null) {
            dbPort = DB_PORT_DEFAULT;
        } else {
            checkIfPortIsValid(dbPort);
        }

        javaHome = new File(getSystemProperty(JAVA_ROOT_PROPERTY));
        dbLocation = new File(getSystemProperty(DERBY_ROOT_PROP_NAME));
        checkIfDbInstalled(dbLocation);

        sClasspath.add(new File(installRoot, "lib/asadmin/cli-optional.jar"));
        sDatabaseClasspath.add(dbLocation, "lib", "derby.jar")
                          .add(dbLocation, "lib", "derbyshared.jar")
                          .add(dbLocation, "lib", "derbytools.jar")
                          .add(dbLocation, "lib", "derbynet.jar")
                          .add(dbLocation, "lib", "derbyclient.jar");
    }

    /**
     * Check if database port is valid. Derby does not check this so need to add code to check the port number.
     */
    private void checkIfPortIsValid(final String port) throws CommandValidationException {
        try {
            Integer.parseInt(port);
        } catch (NumberFormatException e) {
            throw new CommandValidationException(strings.get("InvalidPortNumber", port));
        }
    }

    /**
     * Check if database is installed.
     */
    private void checkIfDbInstalled(final File dblocation) throws CommandException {
        if (!dblocation.exists()) {
            logger.info(strings.get("DatabaseNotInstalled", dblocation));
            throw new CommandException("dblocation not found: " + dblocation);
        }

        File derbyJar = new File(new File(dbLocation, "lib"), "derbyclient.jar");
        if (!derbyJar.exists()) {
            logger.info(strings.get("DatabaseNotInstalled", dblocation));
            throw new CommandException("derbyclient.jar not found in " + dblocation);
        }

    }

    /**
     * Defines the command to ping the derby database.
     *
     * <p>
     * Note that when using Darwin (Mac), the property,
     * "-Dderby.storage.fileSyncTransactionLog=True" is defined.
     *
     * See:
     * http://www.jasonbrome.com/blog/archives/2004/12/05/apache_derby_on_mac_os_x.html
     * https://issues.apache.org/jira/browse/DERBY-1
     */
    protected String[] pingDatabaseCmd(boolean bRedirect) throws Exception {
        if (OS.isDarwin()) {
            return new String[] {
                getJavaExe().toString(),
                "-Djava.library.path=" + installRoot + File.separator + "lib",
                "-Dderby.storage.fileSyncTransactionLog=True",
                "-cp", sClasspath + File.pathSeparator + sDatabaseClasspath,

                "com.sun.enterprise.admin.cli.optional.DerbyControl",
                "ping",
                dbHost, dbPort, Boolean.toString(bRedirect) };

        }

        return new String[] {
                getJavaExe().toString(),
                "-Djava.library.path=" + installRoot + File.separator + "lib",
                "-cp", sClasspath + File.pathSeparator + sDatabaseClasspath,

                "com.sun.enterprise.admin.cli.optional.DerbyControl",
                "ping",
                dbHost, dbPort, Boolean.toString(bRedirect) };
    }

    /**
     * Computes the java executable location from {@link #javaHome}.
     */
    protected final File getJavaExe() {
        return new File(new File(javaHome, "bin"), "java");
    }
}
