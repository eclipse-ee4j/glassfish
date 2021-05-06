/*
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

package admin.monitoring;

import java.io.*;
import java.sql.*;
import static admin.monitoring.Constants.*;

/**
 * Tests JDBC monitoring.  Note that this requires a running JavaDB database.
 * @author Jennifer Chou
 */
public class Jdbc extends MonTest {
    @Override
    void runTests(TestDriver driver) {
        setDriver(driver);
        report(true, "Hello from JDBC Monitoring Tests!");
        createJdbcPool();
        createJdbcResource();
        deploy("server", connApp);
        setupSQL();
        createTable();

        report(wget(8080, "onlygetconnectionservlet/onlygetconnectionservlet"), "hit 1 onlygetconnectionservlet on 8080-");

        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.numconncreated-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.numconncreated-count = 8"),
                "jdbc-check-getm-numconncreated-count");
        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.numconnfree-current"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.numconnfree-current = 8"),
                "jdbc-check-getm-numconnfree-count");

        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.numconnacquired-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.numconnacquired-count = 1"),
                "jdbc-check-getm-numconnaquired-count-1");
        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.jdbc-onlygetconnection-servletApp.numconnacquired-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.jdbc-onlygetconnection-servletApp.numconnacquired-count = 1"),
                "jdbc-check-getm-pool-app-numconnaquired-count-1");
        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.numconnreleased-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.numconnreleased-count = 1"),
                "jdbc-check-getm-numconnreleased-count-1");

        report(wget(8080, "onlygetconnectionservlet/onlygetconnectionservlet"), "hit 2 onlygetconnectionservlet on 8080-");

        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.numconnacquired-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.numconnacquired-count = 2"),
                "jdbc-check-getm-numconnaquired-count-2");
        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.jdbc-onlygetconnection-servletApp.numconnacquired-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.jdbc-onlygetconnection-servletApp.numconnacquired-count = 2"),
                "jdbc-check-getm-pool-app-numconnaquired-count-2");
        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.numconnreleased-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.numconnreleased-count = 2"),
                "jdbc-check-getm-numconnreleased-count-2");

        report(wget(8080, "onlygetconnectionservlet/onlygetconnectionservlet"), "hit 3 onlygetconnectionservlet on 8080-");

        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.numconnacquired-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.numconnacquired-count = 3"),
                "jdbc-check-getm-numconnaquired-count-3");
        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.jdbc-onlygetconnection-servletApp.numconnacquired-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.jdbc-onlygetconnection-servletApp.numconnacquired-count = 3"),
                "jdbc-check-getm-pool-app-numconnaquired-count-3");
        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.numconnreleased-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.numconnreleased-count = 3"),
                "jdbc-check-getm-numconnreleased-count-3");

        report(asadmin("set", "configs.config.server-config.monitoring-service.module-monitoring-levels.jdbc-connection-pool=OFF"),
                "jdbc-set-jdbc-connection-pool-OFF");
        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-only*"),
                "No monitoring data to report"),
                "jdbc-check-getm-no-monitoring-data");

        report(asadmin("set", "configs.config.server-config.monitoring-service.module-monitoring-levels.jdbc-connection-pool=HIGH"),
                "jdbc-set-jdbc-connection-pool-HIGH");


        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.numconncreated-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.numconncreated-count = 0"),
                "jdbc-check-getm-numconncreated-count");



                /* After grizzly 2.0 integration numconfree changed from 5 to 7.  The onlygetconnectionservlet is being invoked
                 in the *same* thread and com.sun.enterprise.resource.pool.AssocWithThreadResourcePool in this case doesn't allocate
                 new resource, but uses the one already associated with the thread. */
        //Comment out - this number seems upredictable to test - sometimes 6, sometimes 7 - JC
        //report(checkForString(
        //        asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.numconnfree-current"),
        //        "server.resources.jdbc-onlygetconnectionservlet-pool.numconnfree-current = 7"),
        //        "jdbc-check-getm-numconnfree-count");



        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.numconnacquired-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.numconnacquired-count = 0"),
                "jdbc-check-getm-numconnaquired-count-0-reset");
        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.jdbc-onlygetconnection-servletApp.numconnacquired-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.jdbc-onlygetconnection-servletApp.numconnacquired-count = 0"),
                "jdbc-check-getm-pool-app-numconnaquired-count-0-reset");
        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.numconnreleased-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.numconnreleased-count = 0"),
                "jdbc-check-getm-numconnreleased-count-0-reset");

        report(wget(8080, "onlygetconnectionservlet/onlygetconnectionservlet"), "hit 4 onlygetconnectionservlet on 8080-");

        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.numconnacquired-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.numconnacquired-count = 1"),
                "jdbc-check-getm-numconnaquired-count-1-reset");
        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.jdbc-onlygetconnection-servletApp.numconnacquired-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.jdbc-onlygetconnection-servletApp.numconnacquired-count = 1"),
                "jdbc-check-getm-pool-app-numconnaquired-count-1-reset");
        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.numconnreleased-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.numconnreleased-count = 1"),
                "jdbc-check-getm-numconnreleased-count-1-reset");

        report(wget(8080, "onlygetconnectionservlet/onlygetconnectionservlet"), "hit 5 onlygetconnectionservlet on 8080-");

        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.numconnacquired-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.numconnacquired-count = 2"),
                "jdbc-check-getm-numconnaquired-count-2-reset");
        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.jdbc-onlygetconnection-servletApp.numconnacquired-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.jdbc-onlygetconnection-servletApp.numconnacquired-count = 2"),
                "jdbc-check-getm-pool-app-numconnaquired-count-2-reset");
        report(checkForString(
                asadminWithOutput("get", "-m", "server.resources.jdbc-onlygetconnectionservlet-pool.numconnreleased-count"),
                "server.resources.jdbc-onlygetconnectionservlet-pool.numconnreleased-count = 2"),
                "jdbc-check-getm-numconnreleased-count-2-reset");

        dropTable();
    }

    private void createJdbcPool() {
        report(asadmin("create-jdbc-connection-pool", "--datasourceclassname", "org.apache.derby.jdbc.ClientDataSource",
                "--restype", "javax.sql.DataSource", "--target", "server", "jdbc-onlygetconnectionservlet-pool"),
                "createJdbcPool");
        report(asadmin("set", "domain.resources.jdbc-connection-pool.jdbc-onlygetconnectionservlet-pool.property.User=dbuser"),
                "setUser");
        report(asadmin("set", "domain.resources.jdbc-connection-pool.jdbc-onlygetconnectionservlet-pool.property.Password=dbpassword"),
                "setPassword");
        report(asadmin("set", "domain.resources.jdbc-connection-pool.jdbc-onlygetconnectionservlet-pool.property.driverType=4"),
                "setDriverType");
        report(asadmin("set", "domain.resources.jdbc-connection-pool.jdbc-onlygetconnectionservlet-pool.property.portNumber=1527"),
                "setPortNumber");
        report(asadmin("set", "domain.resources.jdbc-connection-pool.jdbc-onlygetconnectionservlet-pool.property.dataBaseName=testdb"),
                "setDataBaseName");
        report(asadmin("set", "domain.resources.jdbc-connection-pool.jdbc-onlygetconnectionservlet-pool.property.retrieveMessagesFromServerOnGetMessage=true"),
                "setRetrieveMessage");
        report(asadmin("set", "domain.resources.jdbc-connection-pool.jdbc-onlygetconnectionservlet-pool.property.create=true"),
                "setCreate");
        report(asadmin("set", "domain.resources.jdbc-connection-pool.jdbc-onlygetconnectionservlet-pool.property.serverName=localhost"),
                "setServerName");
        report(asadmin("set", "domain.resources.jdbc-connection-pool.jdbc-onlygetconnectionservlet-pool.property.associatewiththread=true"),
                "setAssociateWithThread");
    }

    private void createJdbcResource() {
        report(asadmin("create-jdbc-resource", "--connectionpoolid", "jdbc-onlygetconnectionservlet-pool",
                "--target", "server", "jdbc/onlygetconnectionservlet"),
                "createJdbcResource");
    }

    private void setupSQL() {
        try {
            Class.forName(dbDriver).newInstance();
            connection = DriverManager.getConnection(connectionURL, "dbuser", "dbpassword");
            statement = connection.createStatement();
        } catch (Exception ex) {
            report(false, "Got Exception-setupSQL-"+ ex.toString());
        }
    }

    private void createTable() {
       try {
            String QueryString = "CREATE TABLE ONLYGETCONNECTION(name char(20), num integer)";
            updateQuery = statement.executeUpdate(QueryString);
            QueryString = "INSERT INTO ONLYGETCONNECTION values ('abcd', 120)";
            updateQuery = statement.executeUpdate(QueryString);
        } catch (Exception ex) {
            report(false, "Got Exception-createTable-"+ ex.toString());
        }

    }

    private void dropTable() {
       try {
            String QueryString = "drop table ONLYGETCONNECTION";
            updateQuery = statement.executeUpdate(QueryString);
            statement.close();
            connection.close();
        } catch (SQLException ex) {
            report(false, "Got Exception-dropTable-"+ ex.toString());
        }

    }

    private static final File connApp = new File(RESOURCES_DIR, "jdbc-onlygetconnection-servletApp.ear");
    private static final String connectionURL = "jdbc:derby://localhost:1527/testdb;create=true;";
    private static final String dbDriver ="org.apache.derby.jdbc.ClientDriver";
    private static Connection connection = null;
    private static Statement statement = null;
    private static int updateQuery = 0;
}
