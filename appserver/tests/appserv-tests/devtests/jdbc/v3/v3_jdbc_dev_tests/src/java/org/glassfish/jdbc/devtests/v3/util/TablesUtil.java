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

package org.glassfish.jdbc.devtests.v3.util;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

/**
 *
 * @author jagadish
 */
public class TablesUtil {

    /**
     * Creates Tables needed to execute JDBC devtests
     * @param ds1
     * @param out
     * @param tableName
     * @param columnName
     */
    public static void createTables(DataSource ds1, PrintWriter out, String tableName, String columnName) {
        Connection con = null;
        Statement stmt = null;
        try {

            con = ds1.getConnection();
            stmt = con.createStatement();
            String query = "create table " + tableName + "(" + columnName + " char(50))";
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

    /**
     * Deletes tables used by JDBC devtests.
     * @param ds1
     * @param out
     * @param tableName
     */
    public static void deleteTables(DataSource ds1, PrintWriter out, String tableName) {
        Connection con = null;
        Statement stmt = null;
        try {

            con = ds1.getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate("drop table " + tableName);
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

    /**
     * Insert values into tables needed by JDBC devtests
     * @param ds
     * @param out
     * @param tableName
     * @param content
     */
    public static void insertEntry(DataSource ds, PrintWriter out, String tableName, String content) {
        Connection con = null;
        Statement stmt = null;
        try {
            con = ds.getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate("INSERT INTO " + tableName + " VALUES('" +
                content + "')");
        } catch (SQLException ex) {
            HtmlUtil.printException(ex, out);
        } finally {
            if(stmt != null) {
                try {
                    stmt.close();
                } catch(SQLException ex) {
                    HtmlUtil.printException(ex, out);
                }
            }
            if(con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    HtmlUtil.printException(ex, out);
                }
            }
        }
    }

    /**
     * Verifies table content by getting the number of rows in it, used by the
     * JDBC devtests. Returns a true if there are any rows in the table.
     * @param ds1
     * @param out
     * @param tableName
     * @param columnName
     * @param content
     * @return boolean result
     */
    public static boolean verifyTableContent(DataSource ds1, PrintWriter out, String tableName, String columnName, String content) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        boolean result = false;
        try {

            con = ds1.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery("select count(*) ROW_COUNT from " + tableName + " where " + columnName + " = '" + content + "'");
            if (rs.next()) {
                if (rs.getInt("ROW_COUNT") > 0) {
                    result = true;
                }
            }
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
            return result;
        }
    }
}
