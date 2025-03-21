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

import jakarta.annotation.Resource;
import jakarta.ejb.CreateException;
import jakarta.ejb.EJB;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Set;
import java.util.HashSet;

@Stateless
public class SimpleSessionBean implements SessionBean {

    private InitialContext ic_;
    
    // With multiple @Resource injections on same resource name, injection
    // and lookup should still succeed. See also test7() for the manual lookup.
    private final static String SAME_RESOURCE_NAME = "jdbc/connsharing";

    @Resource(mappedName=SAME_RESOURCE_NAME)
    DataSource ds1;

    @Resource(mappedName=SAME_RESOURCE_NAME)
    DataSource ds2;

    @Resource(lookup="jdbc/assoc-with-thread-resource-1")
    DataSource ds3;

    @Resource(lookup="jdbc/assoc-with-thread-resource-2")
    DataSource ds4;
    
    @Override
    public void setSessionContext(SessionContext context) {
        try {
            ic_ = new InitialContext();
        } catch (NamingException ne) {
            ne.printStackTrace();
        }
    }

    public void ejbCreate() throws CreateException {
    }

    /**
     * Scenario 1:
     * <pre>
     * tx {
     *   Bean1 -> getConnection -> insert data into table -> call bean2 -> close connection
     *   Bean2 -> getConnection -> update above data -> close connection
     * }
     *
     * tx {
     *   Bean1 -> new transaction query that data indeed got updated
     * }
     * </pre>
     * Get connection and do some database inserts. Then call another
     * EJB's method in the same transaction and change the inserted value.
     * Since all this is in the same tx, the other bean's method should
     * get the same connection (physical) and hence be able to see the
     * inserted value even though the tx has not committed yet.
     * The idea is to test connection sharing
     */
    public boolean test1() throws Exception {
        logStartTest("test1");
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource");

        try (Connection conn1 = ds.getConnection();
            Statement stmt1 = conn1.createStatement()) {
            getPhysicalConnectionAndLog(ds, conn1);
            stmt1.executeUpdate("INSERT INTO CONNSHARING values (100, 'CONN_SHARING')");

            // conn1 is not yet closed, expect call to test1() to reuse it because 
            // the transaction is still in use.
            SimpleSession2 bean = lookupSimpleSession2();
            return bean.test1UpdateWhereId100();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Scenario 2:
     * <pre>
     * tx {
     *   Bean1 -> getConnection -> insert data into table -> close connection -> call bean2
     *   Bean2 -> getConnection -> update above data --> close connection
     * }
     *
     * tx {
     *   Bean1 -> new transaction query that data indeed got updated
     * }
     * </pre>
     * Get connection and do some database inserts. Then call another
     * EJB's method in the same transaction and change the inserted value.
     * Since all this is in the same tx, the other bean's method should
     * get the same connection (physical) and hence be able to see the
     * inserted value even though the tx has not committed yet.
     * This test does the same thing as test1 except that it closes the
     * connection it obtains and then opens a new connection in bean2's method
     * The idea is to test connection sharing
     */
    public boolean test2() throws Exception {
        logStartTest("test2");
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource");

        try (Connection conn1 = ds.getConnection();
            Statement stmt1 = conn1.createStatement()) {
            getPhysicalConnectionAndLog(ds, conn1);
            stmt1.executeUpdate("INSERT INTO CONNSHARING values (100, 'CONN_SHARING')");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // conn1 is already closed, expect call to test1() to reuse it because 
        // the transaction is still in use.
        SimpleSession2 bean = lookupSimpleSession2();
        return bean.test1UpdateWhereId100();
    }
    
    /**
     * Scenario 3:
     * <pre>
     * tx {
     *   Bean1 -> getConnection:c1  -> insert data into table -> getConnection:c2
     *         -> insert some more data -> call bean2 -> close c1, c2
     *   Bean2 -> getConnection -> update above data --> close connection
     * }
     *
     * tx {
     *   Bean1 -> new transaction query that data indeed got updated
     * }
     * </pre>
     */
    public boolean test3() throws Exception {
        logStartTest("test3");
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource");

        try (Connection conn1 = ds.getConnection();
             Statement stmt1 = conn1.createStatement()) {
            getPhysicalConnectionAndLog(ds, conn1);
            stmt1.executeUpdate("INSERT INTO CONNSHARING values (100, 'CONN_SHARING')");

            try (Connection conn2 = ds.getConnection(); 
                 Statement stmt2 = conn2.createStatement()) {
                getPhysicalConnectionAndLog(ds, conn2);
                stmt2.executeUpdate("INSERT INTO CONNSHARING values (200, 'CONN_SHARING_200')");

                SimpleSession2 bean = lookupSimpleSession2();
                return bean.test2UpdateWhereId200();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Scenario 4:
     * <pre>
     * tx {
     *   Bean1 -> getConnection:c1  -> insert data into table -> getConnection:c2
     *         -> insert some more data -> close c1,c2 -> call bean2
     *   Bean2 -> getConnection -> update above data --> close connection
     * }
     *
     * tx {
     *   Bean1 -> new transaction query that data indeed got updated
     * }
     * </pre>
     */
    public boolean test4() throws Exception {
        logStartTest("test4");
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource");

        try (Connection conn1 = ds.getConnection();
             Statement stmt1 = conn1.createStatement()) {
            getPhysicalConnectionAndLog(ds, conn1);
            stmt1.executeUpdate("INSERT INTO CONNSHARING values (100, 'CONN_SHARING')");

            try (Connection conn2 = ds.getConnection();
                 Statement stmt2 = conn2.createStatement()) {
                getPhysicalConnectionAndLog(ds, conn2);
                stmt2.executeUpdate("INSERT INTO CONNSHARING values (200, 'CONN_SHARING_200')");
            }

            SimpleSession2 bean = lookupSimpleSession2();
            return bean.test2UpdateWhereId200();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Scenario 5:
     * 
     * This test opens 4 connections, and closes the connections in another
     * order than the connections are opened (which you would not do anymore 
     * when using try with resources).
     * 
     * It is expected that all 4 connections are using the same 1 physical 
     * connection to the database. This test depends on the equals method of 
     * the Connection interface implementation.
     */
    public boolean test5() throws Exception {
        logStartTest("test5");
        boolean result = false;
        
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource");
        Connection con1 = ds.getConnection();
        
        // Retrieve the actual Connection from the application server specific 
        // DataSource connection wrapper implementation. Use this actual 
        // connection to check later on if the same physical connection is used 
        // for all 4 connections.
        Connection physicalConn1 = getPhysicalConnectionAndLog(ds, con1);

        // Open a 2nd connection, and close it using try with resources
        Connection physicalConn2 = null;
        try (Connection con2 = ds.getConnection();
                Statement stmt = con2.createStatement()) {
            physicalConn2 = getPhysicalConnectionAndLog(ds, con2);
            stmt.executeQuery("select * from connsharing");
        }

        // Open a 3rd and 4th connection
        Connection con3 = ds.getConnection();
        Connection physicalConn3 = getPhysicalConnectionAndLog(ds, con3);

        Connection con4 = ds.getConnection();
        Connection physicalConn4 = getPhysicalConnectionAndLog(ds, con4);

        // Use unexpected closing of connections on purpose
        con4.close();
        con1.close();
        con3.close();

        // See server.log file in 
        // ./glassfish/target/glassfish7/glassfish/domains/domain1/logs/server.log 
        // for the test output of:
        System.out.println("Conn 1 : " + physicalConn1);
        System.out.println("Conn 2 : " + physicalConn2);
        System.out.println("Conn 3 : " + physicalConn3);
        System.out.println("Conn 4 : " + physicalConn4);
        // Expecting output like:
        //  Conn 1 : org.apache.derby.client.net.NetConnection@11847474]]
        //  Conn 2 : org.apache.derby.client.net.NetConnection@11847474]]
        //  Conn 3 : org.apache.derby.client.net.NetConnection@11847474]]
        //  Conn 4 : org.apache.derby.client.net.NetConnection@11847474]]

        // Trust on the Connection equals implementation and create a Set 
        // which should have 1 connection when adding the 4 from this test:
        Set<Connection> physicalConnections = new HashSet<>();
        physicalConnections.add(physicalConn1);
        physicalConnections.add(physicalConn2);
        physicalConnections.add(physicalConn3);
        physicalConnections.add(physicalConn4);

        if (physicalConnections.size() == 1) {
            result = true;
        }
        
        physicalConnections.clear();
        return result;
    }

    /**
     * Scenario 6:
     *
     * This test is about using 2 datasources and about the connection pool size. 
     * A 100 bean calls are made in the same transaction, default connection pool 
     * size is 32.
     *
     * Expecting that the implementation reuses connections,
     * while code runs in the same transaction.
     * 
     * Expecting that getting a resource from a second datasource is possible,
     * while code runs in the same transaction.
     * 
     * The pool size in datasource: "jdbc-connsharing-pool" is not
     * initialized with a certain max pool size. Thus default "max-pool-size" in
     * public class ConnectionPool is set to 32 connections.
     * 
     * Calling the second bean, while first conn1 connection is still open
     * should be possible, and should not lead to pool problems. 
     * The connection pool maximum should not be reached in this test.
     */
    public boolean test6() throws Exception {
        logStartTest("test6");
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource");
        boolean passed = false;

        // Get a connection in this bean, to make the first connection in this 
        // transaction 'busy'.
        // This connection is requested from "java:comp/env/DataSource"
        try (Connection conn1 = ds.getConnection()) {
            getPhysicalConnectionAndLog(ds, conn1);
            
            SimpleSession2 bean = lookupSimpleSession2();
            
            // Make a 100 calls in the same transaction
            for (int i = 0; i < 100; i++) {
                // Note: test3 calls a connection from another DataSource:
                // named: "java:comp/env/DataSource2"
                bean.test3GetConnection();
                
                // TODO: Would be nice to call connection pool statistics
                // here for DataSource and DataSource2 to proof the maximum 
                // of 32 is set and not reached at all.
            }

            passed = true;
        } catch (SQLException e) {
            e.printStackTrace();
            passed = false;
        }
        return passed;
    }

    /**
     * Adding a test-case (test7 in EJB) for GlassFish-Issue : 15443 Two
     * resource-refs (in this case @Resource injections) for same resource and
     * doing a physical lookup of same resource (initialContext.lookup), which
     * was failing. Fix for 15443 should make this test pass.
     * <p>
     * With multiple @Resource injections on same resource name, test physical
     * lookup. It should succeed.
     */
    public boolean test7() throws Exception {
        logStartTest("test7");
        DataSource ds = (DataSource) ic_.lookup(SAME_RESOURCE_NAME);
        boolean result = false;

        try (Connection con = ds.getConnection()) {
            getPhysicalConnectionAndLog(ds, con);
            con.close();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Adding a test-case (test8 in EJB) for GlassFish-Issue : 15577 or 15586
     * Two resources from two different pools (each one with different database,
     * assoc-with-thread=true) to make sure that connections from appropriate
     * pool is retrieved from the thread-local.
     * <p>
     * With multiple assoc-with-thread pool based resources, they should return
     * connections from appropriate resource/pool. Test-case for Issue : 15577
     */
    public boolean test8() {
        logStartTest("test8");
        boolean result = false;
        try {
            Connection ds3Conn = ds3.getConnection();
            getPhysicalConnectionAndLog(ds3, ds3Conn);
            String ds3URL = ds3Conn.getMetaData().getURL();
            System.out.println("ds3 url : " + ds3URL);
            ds3Conn.close();

            Connection ds4Conn = ds4.getConnection();
            getPhysicalConnectionAndLog(ds4, ds4Conn);
            String ds4URL = ds4Conn.getMetaData().getURL();
            System.out.println("ds4 url : " + ds4URL);
            ds4Conn.close();

            if (ds3URL.contains("awt-1") && ds4URL.contains("awt-2")) {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Scenario 9:
     * 
     * Call code that resembles issue 24805 situations: the connection pool might contain
     * resources that are marked as enlisted in a transaction, and should not be handed out
     * from the pool. If it would be handed out, it could end up in another transaction where
     * it would not be enlisted and in the end closing the connection would fail because 
     * the closeResouce logic has no transaction associated.
     * This test only tests a single thread, to ensure the enlisted state is correct when a 
     * resource is returned from the connection pool. 
     *
     * This tests asks a Singleton utility bean for a database Connection from a @Datasource,
     * Then the connection is used and while it is in use another few connections are requested
     * from the pool using the same Singleton. The connection pool should not return a 
     * ResourceHandle that has the state enlisted.
     *
     * It could be argued that the Singleton utility class should not return a Connection, but 
     * the utility method is marked as Transaction SUPPORTS, and it allows a codebase to only define
     * 1 location where @Resource is located, plus it allows adding connection statistics and validations
     * inside this 1 location. Instead of replicating this all over the codebase in a large system. 
     */
    public boolean test9() throws Exception {
        logStartTest("test9");

        try {
            SimpleSession2 bean = lookupSimpleSession2();
            bean.test9Issue24085();
            
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Query the value modified in the second bean and ensure that it
     * is correct.
     */
    public boolean query() throws Exception {
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource");
        boolean success = false;

        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM CONNSHARING WHERE c_id=100")) {

            if (rs.next()) {
                String str = rs.getString(2);
                System.out.println(" str => " + str);
                if ("CONN_SHARING_BEAN_2".equals(str.trim())) {
                    success = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //cleanup table
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM CONNSHARING WHERE c_id=100");
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return success;
    }

    /**
     * Query the value modified in the second bean and ensure that it
     * is correct.
     */
    public boolean query2() throws Exception {
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource");
        String str1 = null;
        String str2 = null;
        
        boolean success = false;
        
        try (Connection conn = ds.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM CONNSHARING WHERE c_id=100")) {
            
            if (rs.next()) {
                str1 = rs.getString(2);
                System.out.println(" str1 => " + str1);
            }

            try (ResultSet rs1 = stmt.executeQuery("SELECT * FROM CONNSHARING WHERE c_id=200")) {
                if (rs1.next()) {
                    str2 = rs1.getString(2);
                    System.out.println(" str2 => " + str2);
                }
            }
            if ("CONN_SHARING".equals(str1.trim()) &&
                    "CONN_SHARING_BEAN_2_2".equals(str2.trim())) {
                success = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        //cleanup table
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM CONNSHARING WHERE c_id=100");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM CONNSHARING WHERE c_id=200");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return success;
    }

    private SimpleSession2 lookupSimpleSession2() throws Exception {
        Object o = ic_.lookup("java:comp/env/ejb/SimpleSession2EJB");
        SimpleSession2Home home = (SimpleSession2Home)
                javax.rmi.PortableRemoteObject.narrow(o, SimpleSession2Home.class);
        SimpleSession2 bean = home.create();
        return bean;
    }

    /**
     * Returns the physical connection and logs it. This is useful to see what
     * happens if the server implementation is changed and these tests start to
     * fail: it allows comparison of connection logic.
     */
    public static Connection getPhysicalConnectionAndLog(DataSource ds,
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
