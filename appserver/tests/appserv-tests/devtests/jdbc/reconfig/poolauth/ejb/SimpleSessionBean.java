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

package com.sun.s1asdev.jdbc.reconfig.poolauth.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleSessionBean implements SessionBean
{

    private SessionContext ctxt_;
    private InitialContext ic_;
    public void setSessionContext(SessionContext context) {
        ctxt_ = context;
        try {
            ic_ = new InitialContext();
        } catch( NamingException ne ) {
            ne.printStackTrace();
        }
    }

    public void ejbCreate() throws CreateException {
    }

    public boolean test1() throws Exception {
        DataSource ds = (DataSource)ic_.lookup("java:comp/env/DataSource");
        Connection conn1 = null;
        Statement stmt1 = null;
        boolean passed = false;
        //Get a connection with user,password = dbuser,dbpassword
        //and access a table that only this principal can
        //access
        try {
            conn1 = ds.getConnection();
            stmt1 = conn1.createStatement();
            stmt1.executeQuery("SELECT * FROM DBUSERTABLE");
        } catch( Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (stmt1 != null ) {
                try { stmt1.close(); } catch(Exception e) {}
            }
            if (conn1 != null) {
                try { conn1.close(); } catch( Exception e1 ) {}
            }
        }

        //Now try querying a table owned by another principal and
        //expect an exception
        try {
            conn1 = ds.getConnection();
            stmt1 = conn1.createStatement();
            stmt1.executeQuery("SELECT * FROM PBPUBLICTABLE");
        } catch( Exception e) {
            e.printStackTrace();
            return true;
        } finally {
            if (stmt1 != null ) {
                try { stmt1.close(); } catch(Exception e) {}
            }
            if (conn1 != null) {
                try { conn1.close(); } catch( Exception e1 ) {}
            }
        }

        return false;
    }

    public boolean test2() throws Exception {
        DataSource ds = (DataSource)ic_.lookup("java:comp/env/DataSource");
        Connection conn1 = null;
        Statement stmt1 = null;
        boolean passed = false;
        //Get a connection with user,password = pbpublic,pbpublic
        //and access a table that only this principal can
        //access
        try {
            conn1 = ds.getConnection();
            stmt1 = conn1.createStatement();
            stmt1.executeQuery("SELECT * FROM PBPUBLICTABLE");
        } catch( Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (stmt1 != null ) {
                try { stmt1.close(); } catch(Exception e) {}
            }
            if (conn1 != null) {
                try { conn1.close(); } catch( Exception e1 ) {}
            }
        }

        //Now try querying a table owned by another principal and
        //expect an exception
        try {
            conn1 = ds.getConnection();
            stmt1 = conn1.createStatement();
            stmt1.executeQuery("SELECT * FROM DBUSERTABLE");
        } catch( Exception e) {
            e.printStackTrace();
            return true;
        } finally {
            if (stmt1 != null ) {
                try { stmt1.close(); } catch(Exception e) {}
            }
            if (conn1 != null) {
                try { conn1.close(); } catch( Exception e1 ) {}
            }
        }

        return false;
    }

    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}

    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
