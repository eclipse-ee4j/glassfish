/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.paas.mydbplugin;

import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.util.OS;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.internal.api.ServerContext;
import org.glassfish.paas.javadbplugin.DerbyPlugin;
import org.glassfish.virtualization.spi.VirtualMachine;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

/**
 * @author Sandhya Kripalani K
  */
@PerLookup
@Service
public class MyDBPlugin extends DerbyPlugin {

    private static Logger logger = LogDomains.getLogger(MyDBPlugin.class, LogDomains.PAAS_LOGGER);

    private static final String DERBY_USERNAME = "APP";
    private static final String DERBY_PASSWORD = "APP";
    // TODO :: grab the actual port.
    private static final String DERBY_PORT = "1528";
    private static final int DERBY_TIMEOUT = 60_000;

    @Inject
    private ServerContext serverContext;

    private String derbyDatabaseName = "sample-db";

    public String getDefaultServiceName() {
        return "default-myderby-db-service";
    }

    /*@Override
    public void executeInitSql(Properties dbProps, String sqlFile) {
        try {
            logger.log(Level.INFO, "javadb.spe.init_sql.exec.start", sqlFile);
            String url = "jdbc:derby://" + dbProps.getProperty(HOST) + ":" +
                    dbProps.getProperty(PORT) + "/" +
                    dbProps.getProperty(DATABASENAME) + ";create=true";
            executeAntTask(dbProps, "org.apache.derby.jdbc.ClientDriver", url, sqlFile, true);
            logger.log(Level.INFO, "javadb.spe.init_sql.exec.stop", sqlFile);
        } catch (Exception ex) {
            Object[] args = new Object[]{sqlFile, ex};
            logger.log(Level.WARNING, "javadb.spe.init_sql.fail.ex", args);
        }
    }

    @Override
    public void createDatabase(Properties dbProps) {
        try {
            logger.log(Level.INFO, "javadb.spe.custom_db_creation.exec.start", dbProps.getProperty(DATABASENAME));
            String url = "jdbc:derby://" + dbProps.getProperty(HOST) + ":" +
                    dbProps.getProperty(PORT) + "/" +
                    dbProps.getProperty(DATABASENAME) + ";create=true";
            String sql = "VALUES(1)";
            executeAntTask(dbProps, "org.apache.derby.jdbc.ClientDriver", url, sql, false);
            logger.log(Level.INFO, "javadb.spe.custom_db_creation.exec.stop", dbProps.getProperty(DATABASENAME));
        } catch (Exception ex) {
            Object[] args = new Object[]{dbProps.getProperty(DATABASENAME), ex};
            logger.log(Level.WARNING, "javadb.spe.custom_db_creation.fail.ex", args);
        }
    } */

    @Override
    protected Properties getServiceProperties(String ipAddress) {
        Properties serviceProperties = new Properties();
        serviceProperties.put(USER, DERBY_USERNAME);
        serviceProperties.put(PASSWORD, DERBY_PASSWORD);
        serviceProperties.put(HOST, ipAddress);
        serviceProperties.put(PORT, DERBY_PORT);
        serviceProperties.put(DATABASENAME, getDatabaseName());
        serviceProperties.put("CONNECTIONATTRIBUTES", ";create=true");
        serviceProperties.put(RESOURCE_TYPE, "javax.sql.XADataSource");
        serviceProperties.put(CLASSNAME, "org.apache.derby.jdbc.ClientXADataSource");
        return serviceProperties;
    }

    /*protected void setDatabaseName(String databaseName) {
        derbyDatabaseName = databaseName;
    }

    protected String getDatabaseName() {
        return derbyDatabaseName;
    }*/

    public void startDatabase(VirtualMachine virtualMachine) {
        //Non native mode
        if (virtualMachine.getMachine() != null) {
            runAsadminCommand(virtualMachine, "start-database", "--dbport", "1528");
        } else { //Native mode
            start(virtualMachine, false);
        }
    }

    public void stopDatabase(VirtualMachine virtualMachine) {
        //Non native mode
        if (virtualMachine.getMachine() != null) {
            runAsadminCommand(virtualMachine, "stop-database", "--dbport", "1528");
        } else {   //Native mode
            stop(virtualMachine);
        }
    }

    public void start(VirtualMachine virtualMachine, boolean firstStart) {

        String[] startdbArgs = {serverContext.getInstallRoot().getAbsolutePath() +
                File.separator + "bin" + File.separator + "asadmin" + (OS.isWindows() ? ".bat" : ""), "start-database", "--dbport", "1528"};
        ProcessManager startDatabase = new ProcessManager(startdbArgs);
        startDatabase.setTimeoutMsec(DERBY_TIMEOUT);

        try {
            startDatabase.execute();
        } catch (ProcessManagerException e) {
            e.printStackTrace();
        }
    }

    public void stop(VirtualMachine virtualMachine) {

        String[] stopdbArgs = {serverContext.getInstallRoot().getAbsolutePath() +
                File.separator + "bin" + File.separator + "asadmin" + (OS.isWindows() ? ".bat" : ""), "stop-database", "--dbport", "1528"};
        ProcessManager stopDatabase = new ProcessManager(stopdbArgs);
        stopDatabase.setTimeoutMsec(DERBY_TIMEOUT);

        try {
            stopDatabase.execute();
        } catch (ProcessManagerException e) {
            e.printStackTrace();
        }
    }

    public void runAsadminCommand(VirtualMachine virtualMachine, String... parameters) {
        if (virtualMachine.getMachine() == null) {
            return;
        }
        String installDir = virtualMachine.getProperty(VirtualMachine.PropertyName.INSTALL_DIR);
        List<String> args = new ArrayList<String>();
        String asadmin = installDir + File.separator + "glassfish" +
                File.separator + "bin" + File.separator + "asadmin ";
        args.add(asadmin);
        args.addAll(Arrays.asList(parameters));

        try {
            String output = virtualMachine.executeOn(args.toArray(new String[args.size()]));
            Object[] params = new Object[]{virtualMachine.getName(), output};
            logger.log(Level.INFO, "javadb.spe.asadmin_cmd_exec", params);
        } catch (IOException e) {
            Object[] params = new Object[]{parameters.toString(), e};
            logger.log(Level.WARNING, "javadb.spe.command_execution.fail.ex", params);
        } catch (InterruptedException e) {
            Object[] params = new Object[]{parameters.toString(), e};
            logger.log(Level.WARNING, "javadb.spe.command_execution.fail.ex", params);
        }
    }
}
