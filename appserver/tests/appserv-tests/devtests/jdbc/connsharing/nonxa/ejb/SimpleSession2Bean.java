/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.jdbc.connsharing.nonxa.ejb;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJB;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Set;
import java.util.HashSet;

public class SimpleSession2Bean implements SessionBean {

    @EJB
    DbUtilsBean dbUtilsBean;

    private InitialContext ic_;

    public void setSessionContext(SessionContext context) {
        try {
            ic_ = new InitialContext();
        } catch( NamingException ne ) {
            ne.printStackTrace();
        }
    }

    public void ejbCreate() throws CreateException {
    }

    public boolean test1UpdateWhereId100() throws Exception {
        logStartTest("test1UpdateWhereId100");
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource2");

        try (Connection conn1 = ds.getConnection();
             Statement stmt1 = conn1.createStatement()) {
            getPhysicalConnectionAndLog(ds, conn1);
            stmt1.executeUpdate("UPDATE CONNSHARING SET c_phone='CONN_SHARING_BEAN_2' WHERE c_id=100");

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean test2UpdateWhereId200() throws Exception {
        logStartTest("test2UpdateWhereId200");
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource2");

        try (Connection conn1 = ds.getConnection();
            Statement stmt1 = conn1.createStatement()) {
            getPhysicalConnectionAndLog(ds, conn1);
            stmt1.executeUpdate( "UPDATE CONNSHARING SET c_phone='CONN_SHARING_BEAN_2_2' WHERE c_id=200");

            return true;
        } catch( SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean test3GetConnection() throws Exception {
        logStartTest("test3GetConnection");
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource2");
        boolean passed = false;

        try (Connection conn1 = ds.getConnection()) {
            getPhysicalConnectionAndLog(ds, conn1);
            passed = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return passed;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public boolean test9Issue24085() throws Exception {
        logStartTest("test9Issue24085");
        // Do not use @Resource on purpose, but ask a connection from another central bean, in this case: "dbUtilsBean".
        // Why? Because it could provide additional logic like statistics or checks from a central place, plus
        // there is no need to define @Resource in a large number of beans in a large application.
        // 
        // So not using:
        // @Resource(mappedName="jdbc/connsharing")
        // private DataSource myworkDatabase;
        // from this bean itself in this example.
        // If we would call @Resource in this bean the problem would not occur. 
        try (Connection connection = dbUtilsBean.getConnection()) {
            Set<Connection> physicalConnections = new HashSet<>();
            
            // Perform another query while this connection is still open
            // Make a few getConnection calls.
            for (int i = 0; i < 5; i++) {
                try (Connection connectionB = dbUtilsBean.getConnection()) {};
            }
        }
        return true;
    }

    /**
     * Returns the physical connection and logs it. This is useful to see what
     * happens if the server implementation is changed and these tests start to
     * fail: it allows comparison of connection logic.
     */
    private static Connection getPhysicalConnectionAndLog(DataSource ds,
            Connection connection) throws SQLException {
        if (ds instanceof com.sun.appserv.jdbc.DataSource) {
            Connection physicalConnection = ((com.sun.appserv.jdbc.DataSource) ds).getConnection(connection);
            
            // Log both physical connection and the application server Connection
            System.out.println("Physical connection: " + physicalConnection
                    + " returned for Connection: " + connection);
            return physicalConnection;
        }
        throw new IllegalStateException("Expecting com.sun.appserv.jdbc.DataSource");
    }

    private void logStartTest(String testName) {
        System.out.println(this.getClass().getName() + " - Start " + testName);
    }
        
    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
