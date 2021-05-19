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

package com.sun.s1asdev.jdbc.simplemysql.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleBMPBean
    implements EntityBean
{

    protected DataSource ds;

    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context    = new InitialContext();
            ds = (DataSource) context.lookup("java:comp/env/DataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
        System.out.println("[**SimpleBMPBean**] Done with setEntityContext....");
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    /* Get 10 connections in a bunch and then close them*/
    public boolean test1( int numRuns ) {
        boolean passed = true;
        Connection[] conns = new Connection[10];
        for( int i = 0; i < numRuns; i++ ) {
            try {
                conns[i] = ds.getConnection();
            } catch (Exception e) {
                passed = false;
            } /*finally {
                if ( conn != null ) {
                    try {
                        conn.close();
                        } catch( Exception e1) {}
                }
            } */
        }

        for (int i = 0 ; i < numRuns;i++ ) {
            try {
                conns[i].close();
            } catch( Exception e) {
                passed = false;
            }
        }
        return passed;
    }

    /* Get a single connection and close it */
    public boolean test2() {
        Connection conn = null;
        boolean passed = true;
        try {
            conn = ds.getConnection();
        } catch (Exception e) {
            passed = false;
        } finally {
            if ( conn != null ) {
                try {
                    conn.close();
                } catch( Exception e1) {}
            }
        }

        return passed;
    }

    /* Get a single connection and do some DB operations */
    public boolean test3() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        boolean passed = true;
        try {
            conn = ds.getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate("INSERT INTO O_CUSTOMER VALUES (0,'abcd')");
            stmt.executeUpdate("INSERT INTO O_CUSTOMER VALUES (1,'pqrs')");
            System.out.println("Inserted values OK");
            rs = stmt.executeQuery("SELECT * FROM O_CUSTOMER");
            while( rs.next() ) {
                System.out.println(rs.getInt("c_id"));
                System.out.println(rs.getString("c_phone"));
            }

        } catch (Exception e) {
            passed = false;
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try { rs.close(); }catch( Exception e1) {}
            }
            if (stmt != null) {
                try { stmt.close(); }catch(Exception e1) {}
            }
            if ( conn != null ) {
                try { conn.close(); } catch( Exception e1) {}
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
