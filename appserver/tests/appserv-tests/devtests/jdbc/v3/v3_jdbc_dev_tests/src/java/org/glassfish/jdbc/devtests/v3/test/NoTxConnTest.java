/*
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

package org.glassfish.jdbc.devtests.v3.test;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import jakarta.transaction.UserTransaction;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;
import org.glassfish.jdbc.devtests.v3.util.TablesUtil;

/**
 *
 * @author shalini
 */
public class NoTxConnTest implements SimpleTest {

    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();
    
    public Map<String, Boolean> runTest(DataSource ds, PrintWriter out) {
        /*try {
            if (testNoTxConnTest1(ds, out)) {
                resultsMap.put("no-tx-test1", true);
            }else{
                resultsMap.put("no-tx-test1", false);
            }
        } catch (Exception e) {
            resultsMap.put("no-tx-test1", false);
        }*/

        try {
            if (testNoTxConnTest2(ds, out)) {
                resultsMap.put("no-tx-test2", true);
            }else{
                resultsMap.put("no-tx-test2", false);
            }
        } catch (Exception e) {
            resultsMap.put("no-tx-test2", false);
        }

        HtmlUtil.printHR(out);
        return resultsMap;        
    }

    private boolean testNoTxConnTest1(DataSource ds, PrintWriter out) {
        boolean result = false;
        Connection conn = null;
        Connection noTxConn = null;
        Statement stmt = null;
        Statement stmt2 = null;
        ResultSet rs = null;
        ResultSet rs2 = null;

        String tableName = "NOTXCONNTABLE";
        String content = "method1";
        String columnName = "name";
        TablesUtil.createTables(ds, out, tableName, columnName);
        
        out.println("<h4> NoTxConn Test1 </h4>");    
        UserTransaction tx = null;
        try {
            out.println("<br>");
            out.println("Starting test ...");
            InitialContext ctx = new InitialContext();
            tx =(UserTransaction) ctx.lookup("java:comp/UserTransaction");
            out.println("<br>Able to lookup UserTransaction");
            tx.begin();
            out.println("<br>");
            out.println("Started UserTransaction<br>");

            out.println("Trying to get connection ...<br>");

            out.println("ds value : " + ds);
            conn = ds.getConnection();
            out.println("<br>Got connection - conn : " + conn);
            noTxConn = ((com.sun.appserv.jdbc.DataSource)ds).getNonTxConnection();
            out.println("<br>Got noTx connection - noTxConn : " + noTxConn);
            stmt = conn.createStatement();
            stmt.executeUpdate("INSERT INTO " + tableName + " VALUES('" + 
                    content + "')");

            stmt2 = noTxConn.createStatement();
            rs2 = stmt2.executeQuery("SELECT * FROM " + tableName);

            if ( rs2.next() ) {
                result = false;
            }
            String query1 = "SELECT * FROM " + tableName;
            rs = stmt.executeQuery(query1);
            tx.commit();
            result = true;

        } catch (Exception e) {
            HtmlUtil.printException(e, out);
            tx.rollback();
            result = false;
        } finally {
            if (rs != null ) {
                try { rs.close(); } catch( Exception e1) {
                   HtmlUtil.printException(e1, out);
                }
            }
            if (stmt != null ) {
                try {stmt.close(); } catch( Exception e1) {
                   HtmlUtil.printException(e1, out);
                }
            }
            if (conn != null ) {
                try {conn.close();} catch( Exception e1) {
                   HtmlUtil.printException(e1, out);
                }
            }

            if (rs2 != null ) {
                try {rs2.close();} catch( Exception e1 ) {
                   HtmlUtil.printException(e1, out);
                }
            }
            if (stmt2 != null ) {
                try {stmt2.close(); } catch( Exception e1) {
                   HtmlUtil.printException(e1, out);
                }
            }

            if (noTxConn != null ) {
                try { noTxConn.close(); }catch( Exception e1) {
                   HtmlUtil.printException(e1, out);
                }
            }
            
            TablesUtil.deleteTables(ds, out, tableName);
            out.println("<br> Test result : " + result);
            return result;            
        }
    }

    private boolean testNoTxConnTest2(DataSource ds, PrintWriter out) {
        boolean result = false;
        Connection conn = null;
        Connection noTxConn = null;

        out.println("<h4> NoTxConn Test2 </h4>");
        jakarta.transaction.UserTransaction tx = null;
        try {
            out.println("<br>Starting test ...");
            
            InitialContext ctx = new InitialContext();

            tx =(UserTransaction)ctx.lookup("java:comp/UserTransaction");
            out.println("<br>Able to lookup UserTransaction");
            tx.begin();
            out.println("<br>Started UserTransaction");
            out.println("<br>Getting TRANSACTIONAL connection");
            conn = ds.getConnection();
            out.println("<br>Autocommit of conn => " + conn.getAutoCommit());

            if (conn.getAutoCommit() == true ) {
                result = false;
            }

            conn.close();
            for (int i = 0; i < 20; i++ ) {
                out.println("<br>Getting NonTx connection");
                noTxConn = ((com.sun.appserv.jdbc.DataSource)ds).getNonTxConnection();
                out.println("<br>Autocommit of noTxConn => " + noTxConn.getAutoCommit());
                if (noTxConn.getAutoCommit() == false ) {
                    result = false;       
                }
                noTxConn.close();
            }
            out.println("<br>Getting TRANSACTIONAL connection");
            conn = ds.getConnection();
            out.println("<br>Autocommit of conn => " + conn.getAutoCommit());
            if (conn.getAutoCommit() == true ) {
                result = false;
            }
            conn.close();
            tx.commit();

            result = true;

        } catch (Exception e) {
            HtmlUtil.printException(e, out);
            tx.rollback();
            result = false;
        } finally {
            try {
                if (noTxConn != null ) {
                    noTxConn.close();
                }
            } catch( Exception e1 ) {
                HtmlUtil.printException(e1, out);
            }
            out.println("<br> Test result : " + result);
            return result;
        }
    }
}
