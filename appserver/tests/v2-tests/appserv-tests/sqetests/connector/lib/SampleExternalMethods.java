/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.sql.*;

public class SampleExternalMethods {

    /**
     * Counts rows in the coffee table.
     * This is the procedure body for COUNTCOFFEE procedure.
     */
    public static void countCoffee (int[] count) throws Exception {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = DriverManager.getConnection("jdbc:default:connection");

            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery ("select count(*) from coffee");

            if (rs.next()) {
                count[0] = rs.getInt(1);
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (Exception e) { }
            if (conn != null)
                try {
                    stmt.close();
                } catch (Exception e) { }
        }
    }

    /**
     * Inserts a row in the coffee table.
     * This is the procedure body for INSERTCOFFEE procedure.
     */
    public static void insertCoffee (String name, int qty) throws Exception {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DriverManager.getConnection("jdbc:default:connection");
            stmt = conn.prepareStatement("insert into coffee values (?, ?)");
            stmt.setString (1, name);
            stmt.setInt (2, qty);

            stmt.executeUpdate ();
            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (Exception e) { }
            if (conn != null)
                try {
                    stmt.close();
                } catch (Exception e) { }
        }
    }
}
