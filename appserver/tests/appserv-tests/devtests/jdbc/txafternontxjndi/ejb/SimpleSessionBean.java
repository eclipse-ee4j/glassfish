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

package com.sun.s1asdev.jdbc.txafternontx.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;
import jakarta.transaction.UserTransaction;

public class SimpleSessionBean implements SessionBean
{

    private SessionContext ctxt_;
    private InitialContext ic_;
    private InitialContext ic1_;
    public void setSessionContext(SessionContext context) {
        ctxt_ = context;
        try {
            ic_ = new InitialContext();
            Hashtable ht = new Hashtable();
            ht.put("com.sun.enterprise.connectors.jndisuffix", "__nontx");
            ic1_ = new InitialContext(ht);
        } catch( NamingException ne ) {
            ne.printStackTrace();
        }
    }

    public void ejbCreate() throws CreateException {
    }

    /**
     */
    public boolean test1() throws Exception {
        com.sun.appserv.jdbc.DataSource ds =
            (com.sun.appserv.jdbc.DataSource)ic_.lookup("java:comp/env/DataSource");
        com.sun.appserv.jdbc.DataSource ds1 =
            (com.sun.appserv.jdbc.DataSource)ic1_.lookup("jdbc/txafternontx");
        Connection conn1 = null;
        Statement stmt1 = null;
        ResultSet rs1 = null;
        boolean passed = false;
        System.out.println("getting first tx connection");
        try {
            conn1 = ds.getConnection();
            if (conn1.getAutoCommit() == true ) {
                throw new SQLException("Before nontx: Connection with wrong autocommit value");
            }
        } catch( SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (conn1 != null) {
                try { conn1.close(); } catch( Exception e1 ) {}
            }
        }

        System.out.println("getting nontx connection");
        try {
            conn1 = ds1.getConnection();
            if (conn1.getAutoCommit() == false ) {
                throw new SQLException("NonTX Connection with wrong autocommit value");
            }
        } catch( Exception e ) {
            e.printStackTrace();
            return false;
        } finally {
            try {conn1.close();} catch(Exception e) {}
        }

        System.out.println("getting second tx connection");
        try {
            conn1 = ds.getConnection();
            if (conn1.getAutoCommit() == true ) {
                throw new SQLException("After nontx: Connection with wrong autocommit value");
            }
        } catch( SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (conn1 != null) {
                try { conn1.close(); } catch( Exception e1 ) {}
            }
        }

        return true;
    }

    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
