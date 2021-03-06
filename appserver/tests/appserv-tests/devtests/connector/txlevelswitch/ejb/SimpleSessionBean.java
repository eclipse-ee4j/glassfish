/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.connector.txlevelswitch.test1.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;
import jakarta.jms.QueueConnectionFactory;
import jakarta.jms.Queue;
import jakarta.jms.QueueConnection;
import jakarta.jms.QueueSession;
import jakarta.jms.Session;
import jakarta.jms.QueueSender;
import jakarta.jms.TextMessage;


public class SimpleSessionBean implements SessionBean
{

    private SessionContext ctxt_;

    public void setSessionContext(SessionContext context) {
        ctxt_ = context;
    }

    public void ejbCreate() throws CreateException {
    }

    /**
     * Get connection with one XA datasource and then getConnection
     * with another XA datasource. Do some work using both.
     * This should work since for this test both our pools are XA
     */
    public boolean test1() throws Exception {
        System.out.println("************IN TEST 1*************");
        InitialContext ic = new InitialContext();
        DataSource ds1 = (DataSource)ic.lookup("java:comp/env/DataSource1");
        DataSource ds2 = (DataSource)ic.lookup("java:comp/env/DataSource2");
        Connection conn1 = null;
        Connection conn2 = null;
        Statement stmt1 = null;
        Statement stmt2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        boolean passed = true;
        try {
            System.out.println("Before getConnection 1");
            conn1 = ds1.getConnection();
            System.out.println("After getConnection 1");
            System.out.println("Before getConnection 2");
            conn2 = ds2.getConnection();
            System.out.println("After getConnection 2");

            System.out.println("Before createStatement 1");
            stmt1 = conn1.createStatement();
            System.out.println("After createStatement 1");
            System.out.println("Before createStatement 2");
            stmt2 = conn2.createStatement();
            System.out.println("After createStatement 2");

            System.out.println("executing statement 1");
            rs1 = stmt1.executeQuery("SELECT * FROM TXLEVELSWITCH");

            System.out.println("executing statement 2");
            rs2 = stmt2.executeQuery("SELECT * FROM TXLEVELSWITCH2");

            System.out.println("finished executing statements");
            passed = rs1.next() & rs2.next();
        } catch (Exception e) {
            passed = false;
            e.printStackTrace();
        } finally {
            if (rs1 != null ) {
                try { rs1.close(); } catch( Exception e1 ) {}
            }

            if (rs2 != null ) {
                try { rs2.close(); } catch( Exception e1 ) {}
            }

            if ( stmt1 != null ) {
                try { stmt1.close(); } catch( Exception e1) {}
            }
            if ( stmt2 != null ) {
                try { stmt2.close(); } catch( Exception e1) {}
            }
            if ( conn1 != null ) {
                try { conn1.close(); } catch( Exception e1) {}
            }
            if ( conn2 != null ) {
                try { conn2.close(); } catch( Exception e1) {}
            }
        }

        return passed;
    }

    /**
     * Get connection with two non-xa datasources.
     * Do some work using both. Should throw an
     * exception (that we catch ) since 2 non-xa
     * resources cannot be mixed. This test is run
     * after converting the 2 connection-pools to LocaTransaction
     * so by catching the exception we are asserting taht this
     * changeover is indeed successful
     */
    public boolean test2() throws Exception {
        System.out.println("************IN TEST 2*************");
        InitialContext ic = new InitialContext();
        DataSource ds1 = (DataSource)ic.lookup("java:comp/env/DataSource1");
        DataSource ds2 = (DataSource)ic.lookup("java:comp/env/DataSource2");
        Connection conn1 = null;
        Connection conn2 = null;
        Statement stmt1 = null;
        Statement stmt2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        boolean passed = true;
        try {
            System.out.println("Before getConnection 1");
            conn1 = ds1.getConnection();
            System.out.println("After getConnection 1");
            System.out.println("Before getConnection 2");
            conn2 = ds2.getConnection();
            System.out.println("After getConnection 2");

            System.out.println("Before createStatement 1");
            stmt1 = conn1.createStatement();
            System.out.println("After createStatement 1");
            System.out.println("Before createStatement 2");
            stmt2 = conn2.createStatement();
            System.out.println("After createStatement 2");

            System.out.println("executing statement 1");

            try {
                rs1 = stmt1.executeQuery("SELECT * FROM TXLEVELSWITCH");
            } catch( Exception e2 ) {
                System.out.println("Exception for first query :" + e2.getMessage() );
            } finally {
                passed = false;
            }

            System.out.println("executing statement 2");
            try {
                rs2 = stmt2.executeQuery("SELECT * FROM TXLEVELSWITCH2");
            } catch( Exception e2) {
                System.out.println("Exception for second query :" + e2.getMessage() );
            } finally {
                passed = false;
            }

            System.out.println("finished executing statements");
            passed = !(rs1.next() & rs2.next());
        } catch (Exception e) {
            passed = true;
            System.out.println("final exception : " + e.getMessage() );
            throw new EJBException(e);
        } finally {
            if (rs1 != null ) {
                try { rs1.close(); } catch( Exception e1 ) {}
            }

            if (rs2 != null ) {
                try { rs2.close(); } catch( Exception e1 ) {}
            }

            if ( stmt1 != null ) {
                try { stmt1.close(); } catch( Exception e1) {}
            }
            if ( stmt2 != null ) {
                try { stmt2.close(); } catch( Exception e1) {}
            }
            if ( conn1 != null ) {
                try { conn1.close(); } catch( Exception e1) {}
            }
            if ( conn2 != null ) {
                try { conn2.close(); } catch( Exception e1) {}
            }
        }
        return passed;
    }

    /**
     * Get connection with one non-XA datasource and then getConnection
     * with a JMS resource
     */
    public boolean jmsJdbcTest1() throws Exception {
        System.out.println("************IN jmsJdbcTest 1*************");
        InitialContext ic = new InitialContext();
        DataSource ds1 = (DataSource)ic.lookup("java:comp/env/test-res-3");
        QueueConnectionFactory qcf = (QueueConnectionFactory)
            ic.lookup("jms/jms-jdbc-res-1");
        Queue q = (Queue) ic.lookup("java:comp/env/jms/SampleQueue");

        Connection conn1 = null;
        Statement stmt1 = null;
        ResultSet rs1 = null;

        QueueSession qSess = null;
        QueueConnection qConn = null;
        QueueSender qSender = null;
        TextMessage message = null;

        boolean passed = false;
        try {
            System.out.println("Before getConnection 1");
            conn1 = ds1.getConnection();
            System.out.println("After getConnection 1");
            System.out.println(" Before createStatent");
            stmt1 = conn1.createStatement();
            System.out.println(" After createStatent");
            System.out.println(" Before executeQuery");
            rs1 = stmt1.executeQuery("SELECT * FROM TXLEVELSWITCH");
            System.out.println(" After executeQuery");
            System.out.println("Before createQueueConnection");
            qConn = qcf.createQueueConnection();
            System.out.println("After createQueueConnection");
            System.out.println("Before createQueueSession");
            qSess = qConn.createQueueSession( false, Session.AUTO_ACKNOWLEDGE );
            System.out.println("After createQueueSession");
            qSender = qSess.createSender( q );
            message = qSess.createTextMessage();
            message.setText( "Hello World");
            qSender.send( message );
            System.out.println(" Sent Message");
            passed = true;

        } catch (Exception e) {
            passed = false;
            e.printStackTrace();
        } finally {
            if ( rs1 != null ) {
                try { rs1.close();} catch( Exception e1) {}
            }
            if ( stmt1 != null ) {
                try { stmt1.close();} catch( Exception e1) {}
            }
            if ( conn1 != null ) {
                try { conn1.close(); } catch( Exception e1) {}
            }
            if ( qSess != null ) {
                try { qSess.close();} catch(Exception e1) {}
            }

            if ( qConn != null ) {
                try { qConn.close(); } catch( Exception e1) {}
            }
        }

        return passed;
    }

    /**
     * Get connection with one XA datasource and then getConnection
     * with a JMS resource as non-xa
     */
    public boolean jmsJdbcTest2() throws Exception {
        System.out.println("************IN jmsJdbcTest 2*************");
        InitialContext ic = new InitialContext();
        DataSource ds1 = (DataSource)ic.lookup("java:comp/env/test-res-3");
        QueueConnectionFactory qcf = (QueueConnectionFactory)
            ic.lookup("java:comp/env/jms/jms-jdbc-res-1");
        Queue q = (Queue) ic.lookup("java:comp/env/jms/SampleQueue");

        Connection conn1 = null;
        Statement stmt1 = null;
        ResultSet rs1 = null;

        QueueSession qSess = null;
        QueueConnection qConn = null;
        QueueSender qSender = null;
        TextMessage message = null;

        boolean passed = true;
        try {
            System.out.println("Before getConnection 1");
            conn1 = ds1.getConnection();
            System.out.println("After getConnection 1");
            System.out.println(" Before createStatent");
            stmt1 = conn1.createStatement();
            System.out.println(" After createStatent");
            System.out.println(" Before executeQuery");
            rs1 = stmt1.executeQuery("SELECT * FROM TXLEVELSWITCH");
            System.out.println(" After executeQuery");

            System.out.println("Before createQueueConnection");
            qConn = qcf.createQueueConnection();
            System.out.println("After createQueueConnection");
            System.out.println("Before createQueueSession");
            qSess = qConn.createQueueSession( false, Session.AUTO_ACKNOWLEDGE );
            System.out.println("After createQueueSession");
            qSender = qSess.createSender( q );
            message = qSess.createTextMessage();
            message.setText( "Hello World");
            qSender.send( message );
            System.out.println(" Sent message");

        } catch (Exception e) {
            passed = false;
            e.printStackTrace();
        } finally {
            if ( rs1 != null ) {
                try { rs1.close();} catch( Exception e1) {}
            }
            if ( stmt1 != null ) {
                try { stmt1.close();} catch( Exception e1) {}
            }
            if ( conn1 != null ) {
                try { conn1.close(); } catch( Exception e1) {}
            }
            if ( qSess != null ) {
                try { qSess.close();} catch(Exception e1) {}
            }

            if ( qConn != null ) {
                try { qConn.close(); } catch( Exception e1) {}
            }
        }

        return passed;
    }

    /**
     * Get connection with one non-XA datasource and then getConnection
     * with a JMS resource as non-xa
     */
    public boolean jmsJdbcTest3() throws Exception {
        System.out.println("************IN jmsJdbcTest 3*************");
        InitialContext ic = new InitialContext();
        DataSource ds1 = (DataSource)ic.lookup("java:comp/env/test-res-3");
        QueueConnectionFactory qcf = (QueueConnectionFactory)
            ic.lookup("java:comp/env/jms/jms-jdbc-res-1");
        Queue q = (Queue) ic.lookup("java:comp/env/jms/SampleQueue");

        Connection conn1 = null;
        Statement stmt1 = null;
        ResultSet rs1 = null;

        QueueSession qSess = null;
        QueueConnection qConn = null;
        QueueSender qSender = null;
        TextMessage message = null;

        boolean passed = false;
        try {
            System.out.println("Before getConnection 1");
            conn1 = ds1.getConnection();
            System.out.println("After getConnection 1");
            System.out.println(" Before createStatent");
            stmt1 = conn1.createStatement();
            System.out.println(" After createStatent");
            System.out.println(" Before executeQuery");
            rs1 = stmt1.executeQuery("SELECT * FROM DBUSER.TXLEVELSWITCH");
            System.out.println(" After executeQuery");

            System.out.println("Before createQueueConnection");
            qConn = qcf.createQueueConnection();
            System.out.println("After createQueueConnection");
            System.out.println("Before createQueueSession");
            qSess = qConn.createQueueSession( false, Session.AUTO_ACKNOWLEDGE );
            System.out.println("After createQueueSession");
            qSender = qSess.createSender( q );
            message = qSess.createTextMessage();
            message.setText( "Hello World");
            qSender.send( message );
            System.out.println(" Sent message");

        } catch (Exception e) {
            passed = true;
            e.printStackTrace();
        } finally {
            if ( rs1 != null ) {
                try { rs1.close();} catch( Exception e1) {}
            }
            if ( stmt1 != null ) {
                try { stmt1.close();} catch( Exception e1) {}
            }
            if ( conn1 != null ) {
                try { conn1.close(); } catch( Exception e1) {}
            }
            if ( qSess != null ) {
                try { qSess.close();} catch(Exception e1) {}
            }

            if ( qConn != null ) {
                try { qConn.close(); } catch( Exception e1) {}
            }
        }

        return passed;
    }

    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
