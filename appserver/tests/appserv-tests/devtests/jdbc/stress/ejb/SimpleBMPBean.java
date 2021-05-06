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

package com.sun.s1asdev.jdbc.stress.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleBMPBean implements EntityBean {

    //protected ConnectionPoolDataSource ds;
    protected DataSource ds;
    int id;

    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context    = new InitialContext();
            ds = (DataSource) context.lookup("java:comp/env/DataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    public boolean test1(int testId) {
        Connection conn = null;
        boolean passed = true;
        try {
            conn = ds.getConnection();
            insertEntry( testId, "1234567890", conn);
            //queryTable( conn );
            emptyTable( conn, testId );
        } catch (Exception e) {
            e.printStackTrace();
            passed = false;
        } finally {
            if ( conn != null ) {
                try { conn.close(); } catch( Exception e1) {}
            }
        }

        return passed;

    }


    private void insertEntry( int id, String phone, Connection con )
        throws SQLException {

        PreparedStatement stmt = con.prepareStatement(
            "insert into O_Customer values (?, ?)" );

        stmt.setInt(1, id);
        stmt.setString(2, phone);

        stmt.executeUpdate();
        stmt.close();
    }

    private void emptyTable( Connection con, int testId ) {
        try {
            Statement stmt = con.createStatement();

            stmt.execute("delete * from O_Customer WHERE c_id="+testId);
            stmt.close();
        } catch( Exception e) {
        }

    }

    private void queryTable( Connection con ) {
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from O_Customer");
            while( rs.next() ) ;
            rs.close();
            stmt.close();
        } catch( Exception e) {
        }
    }

    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
