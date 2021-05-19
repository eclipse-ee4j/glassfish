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

package com.sun.s1asdev.jdbc.stmtcaching.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import java.sql.*;

public class SimpleBMPBean implements EntityBean {

    protected com.sun.appserv.jdbc.DataSource ds;

    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context = new InitialContext();
            ds = (com.sun.appserv.jdbc.DataSource) context.lookup("java:comp/env/DataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    public boolean testCloseOnCompletion() {
        System.out.println("closeOnCompletion JDBC 41 test Start");
        Connection conn = null;
        PreparedStatement stmt = null;
        String tableName = "customer_stmt_wrapper";
        ResultSet rs = null;
        ResultSet rs1 = null;
        boolean passed = true;
        try {
            conn = ds.getConnection();
            stmt = conn.prepareStatement("select * from "+ tableName +" where c_phone= ?");
            stmt.setString(1, "shal");
            stmt.closeOnCompletion();
            if(!stmt.isCloseOnCompletion()) {
                passed = false;
            }
            System.out.println(">>> stmt.isCloseOnCompletion()=" + stmt.isCloseOnCompletion());
            rs = stmt.executeQuery();
            rs1 = stmt.executeQuery(); //Got a second rs but not closing it.
            rs.close();
            //After this the isClosed should not be true as another resultSet is still open.
            if(stmt.isClosed()){
                passed = false;
            }
            System.out.println("Statement closed=" + stmt.isClosed());
            rs1.close();
            //Both the resultSets are closed. At this stage isClosed should be true.
            if(!stmt.isClosed()){
                passed = false;
            }
            System.out.println("Statement closed=" + stmt.isClosed());
            try {
                ResultSet rs2 = stmt.executeQuery();
                rs2.close();
            } catch(SQLException ex) {
                System.out.println("Statement object used after closeoncompletion : exception: expected");
                passed = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            passed = false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {}
            }
        }
        System.out.println("closeOnCompletion JDBC 41 test End");
        return passed;
    }

    public void ejbLoad() {
    }

    public void ejbStore() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void unsetEntityContext() {
    }

    public void ejbPostCreate() {
    }
}
