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

package com.sun.s1asdev.jdbc.lazyassoc.ejb;

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
    private DataSource ds;
    public void setSessionContext(SessionContext context) {
        ctxt_ = context;
        try {
            ic_ = new InitialContext();
            ds = (DataSource)ic_.lookup("java:comp/env/DataSource");
        } catch( NamingException ne ) {
            ne.printStackTrace();
        }
    }

    public void ejbCreate() throws CreateException {
    }

    public boolean test1() throws Exception {
        Connection conn1 = null;
        boolean passed = false;
        //clean the database
        try {
            conn1 = ds.getConnection();
        } catch( Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean test2() throws Exception {
        Connection conn1 = null;
        Statement stmt = null;
        boolean passed = false;
        //clean the database
        try {
            conn1 = ds.getConnection();
            stmt = conn1.createStatement();
            stmt.executeQuery( "SELECT * FROM TXLEVELSWITCH");
        } catch( Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (stmt != null ) {
                try { stmt.close(); }catch( Exception e ) {}
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
