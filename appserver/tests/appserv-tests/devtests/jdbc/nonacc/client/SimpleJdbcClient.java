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

package com.sun.s1asdev.jdbc.nonacc;

import javax.naming.*;
import java.sql.*;
import javax.sql.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleJdbcClient {

    public static void main( String argv[] ) throws Exception {
        String testSuite = "NonACC ";
        SimpleReporterAdapter stat = new SimpleReporterAdapter();
        stat.addDescription("Test a stand-alone java program that does getConnection");
        Connection con = null;
        Statement stmt = null;

        try {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup(argv[0]);
            con = ds.getConnection();
            System.out.println(" Got connection " + con );

            stmt = con.createStatement();
            stmt.executeQuery("SELECT * FROM NONACC");
            stat.addStatus(testSuite + "test1 ", stat.PASS );
        } catch( Exception e) {
            e.printStackTrace();
            stat.addStatus(testSuite + "test1 ",  stat.FAIL );
        } finally {
            if (stmt != null) { try { stmt.close(); }catch( Exception e) {} }
            if (con != null) { try { con.close(); }catch( Exception e) {} }
        }

        stat.printSummary();
    }
}

