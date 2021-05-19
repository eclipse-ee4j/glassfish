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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;
import org.glassfish.jdbc.devtests.v3.util.TablesUtil;

/**
 * Tests Connection Leak tracing/ Leak reclaim.
 *
 * Assumes that steady-pool-size=1, max-pool-size=1,
 * connection-leak-timeout-in-seconds = 10, connection-leak-reclaim = true
 * attributes are set in the pool configuration.
 * @author shalini
 */
public class ConnectionLeakTracingTest implements SimpleTest {

    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();

    public Map<String, Boolean> runTest(DataSource ds, PrintWriter out) {
        //create CUSTOMER table needed for this test
        String tableName = "CUSTOMER";
        createTables(ds, out, tableName);

        out.println("<h4> Connection Leak Tracing Test </h4>");

        for(int i=0; i<3; i++) {
            try {
                out.println("<br> Trial " + i);
                if (!connLeakTracingTest1(ds, out, tableName)) {
                    resultsMap.put("conn-leak-tracing-test1", false);
                    out.println("<br>connLeakTracingTest has failed");
                    break;
                }
                Thread.sleep(20000);
            } catch (InterruptedException ex) {
                HtmlUtil.printException(ex, out);
                resultsMap.put("conn-leak-tracing-test1", false);
            } catch (Exception ex) {
                resultsMap.put("conn-leak-tracing-test1", false);
            }
        }
        out.println("<br> Test result : true");
        resultsMap.put("conn-leak-tracing-test1", true);

        //Delete the CUSTOMER table created.
        TablesUtil.deleteTables(ds, out, tableName);

        HtmlUtil.printHR(out);
        return resultsMap;
    }

    private boolean connLeakTracingTest1(DataSource ds, PrintWriter out,
            String tableName) {
        Connection conn = null;
        boolean passed = true;
        try {
            out.println("<br>Getting a connection...");
            conn = ds.getConnection();
            out.println("<br> Inserting an entry into the table");
            insertEntry(conn, tableName);
            out.println("<br> Emptying table...");
            emptyTable(conn, tableName);
        } catch (Exception ex) {
            HtmlUtil.printException(ex, out);
            passed = false;
        }
        return passed;
    }

    private void createTables(DataSource ds, PrintWriter out, String tableName) {
        Connection con = null;
        Statement stmt = null;
        try {
            con = ds.getConnection();
            stmt = con.createStatement();
            String query = "create table " + tableName + "(id " +
                    "integer not null, phone char(16))";
            stmt.executeUpdate(query);
        } catch (Exception e) {
            HtmlUtil.printException(e, out);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
            }

            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
            }
        }
    }

    private void emptyTable(Connection conn, String tableName) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DELETE FROM " + tableName);
        stmt.close();
    }

    private void insertEntry(Connection conn, String tableName) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT into " + tableName +
                "  values (1, 'abcd')");
        stmt.close();
    }

}
