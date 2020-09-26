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

package com.sun.s1asdev.connector.serializabletest.ejb;

import jakarta.ejb.EJBContext;
import jakarta.ejb.SessionBean;
import javax.naming.InitialContext;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SimpleSessionBean implements SessionBean {

    private EJBContext ejbcontext;
    private transient jakarta.ejb.SessionContext m_ctx = null;
    transient javax.sql.DataSource ds;


    public void setSessionContext(jakarta.ejb.SessionContext ctx) {
        m_ctx = ctx;
    }

    public void ejbCreate() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public boolean test1() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        Connection conn2 = null;
        Statement stmt2 = null;
        ResultSet rs2 = null;
        try {

            InitialContext ctx = new InitialContext();
            ds = (javax.sql.DataSource) ctx.lookup("java:comp/env/DataSource1");
            conn = ds.getConnection();
            stmt = conn.createStatement();
            String query1 = "SELECT * FROM TXLEVELSWITCH";
            rs = stmt.executeQuery(query1);

            conn2 = ds.getConnection();
            stmt2 = conn2.createStatement();
            rs2 = stmt2.executeQuery("SELECT * FROM TXLEVELSWITCH");

            return true;
        } catch (Exception e) {
            System.out.println("Caught Exception---");
            e.printStackTrace();
            return false;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e1) {
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e1) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                }
            }

            if (rs2 != null) {
                try {
                    rs2.close();
                } catch (Exception e1) {
                }
            }
            if (stmt2 != null) {
                try {
                    stmt2.close();
                } catch (Exception e1) {
                }
            }
            if (conn2 != null) {
                try {
                    conn2.close();
                } catch (Exception e1) {
                }
            }

        }
    }


}



