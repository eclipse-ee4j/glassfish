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

package com.sun.s1asdev.jdbc.cpdsperf.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleBMPBean implements EntityBean {

    //protected ConnectionPoolDataSource ds;
    protected DataSource dsCP;
    protected DataSource dsNormal;
    int id;
    protected int numRuns;

    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context    = new InitialContext();
            dsCP = (DataSource) context.lookup("java:comp/env/DataSource-cp");
            dsNormal = (DataSource) context.lookup("java:comp/env/DataSource-normal");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
    System.out.println("[**SimpleBMPBean**] Done with setEntityContext....");
    }

    public Integer ejbCreate(int _numRuns) throws CreateException {
        numRuns = _numRuns;
        return new Integer(1);
    }

    /**
     * Do an SQL insert into the database and time this
     * @return time taken. -1 If test fails
     */
    public long test1() {
        //ConnectionPoolDataSource
        System.out.println("-----------------Start test1--------------");
        Connection conn = null;
        boolean passed = true;
        long startTime = 0 ;
        long endTime = 0;
        try {
            startTime = System.currentTimeMillis();
            for ( int i = 0; i < numRuns; i++ ) {
                conn = dsCP.getConnection("system", "manager");
                insertEntry( i, "1234567890", conn);
                if (i / 10 == 0 ) {
                    queryTable( conn );
                }
                conn.close();
            }
            endTime = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
            passed = false;
        } finally {
            if ( conn != null ) {
                try { conn.close(); } catch( Exception e1) {}
            }
        }
        System.out.println("-----------------End test1--------------");

        try {
            emptyTable( conn );
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.close(); } catch( Exception e1 ) {}
            }
        }
        if (passed) {
            return (endTime - startTime)/1000;
        }
        return -1;
    }

    public long test2() {
        //Normal DataSource
        System.out.println("-----------------Start test2--------------");
        Connection conn = null;
        boolean passed = true;
        long startTime = 0 ;
        long endTime = 0;
        try {
            startTime = System.currentTimeMillis();
            for ( int i = 0; i < numRuns; i++ ) {
                conn = dsNormal.getConnection("system", "manager");
                insertEntry( i, "1234567890", conn);
                if (i / 10 == 0 ) {
                    queryTable( conn );
                }
                conn.close();
            }
            endTime = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
            passed = false;
        } finally {
            if ( conn != null ) {
                try {
                    conn.close();
                } catch( Exception e1) {}
            }
        }
        System.out.println("-----------------End test2--------------");

        try {
            conn = dsNormal.getConnection("system","manager");
            emptyTable(conn);
            conn.close();
        } catch( Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.close(); } catch( Exception e1) {}
            }
        }
        if (passed) {
            return (endTime - startTime)/1000;
        }
        return -1;
    }

    private void insertEntry( int id, String phone, Connection con )
        throws SQLException {

        PreparedStatement stmt = con.prepareStatement(
            "insert into O_Customer values (?, ?)" );

        stmt.setInt(1, id);
        stmt.setString(2, phone);

        stmt.executeUpdate();
        stmt.close();
        /*
        PreparedStatement stmt = con.prepareStatement(
            "select * from O_Customer" );
        stmt.executeUpdate();
        stmt.close();
        */
    }

    private void emptyTable( Connection con ) {
        try {
            Statement stmt = con.createStatement();

            stmt.execute("delete * from O_Customer");
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
        } catch( Exception e) {
        }
    }

    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate( int numTimes ) {}
}
