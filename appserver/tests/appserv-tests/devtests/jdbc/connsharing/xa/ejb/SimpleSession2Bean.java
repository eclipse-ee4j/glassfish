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

package com.sun.s1asdev.jdbc.connsharing.xa.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;
import jakarta.transaction.UserTransaction;

public class SimpleSession2Bean implements SessionBean
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

    /**
     * Get connection and do some database inserts. Then call another
     * EJB's method in the same transaction and change the inserted value.
     * Since all this is in the same tx, the other bean's method should
     * get the same connection (physical) and hence be able to see the
     * inserted value even though the tx has not committed yet.
     * The idea is to test connection sharing
     */
    public boolean test1(String newVal, int key) throws Exception {
        DataSource ds = (DataSource)ic_.lookup("java:comp/env/DataSource2");
        Connection conn1 = null;
        Statement stmt1 = null;
        boolean passed = false;

        try {
            conn1 = ds.getConnection();
            stmt1 = conn1.createStatement();
            stmt1.executeUpdate( "UPDATE CONNSHARING SET c_phone = '"+ newVal +"' WHERE" +
                " c_id = " + key);

            return true;
        } catch( SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (stmt1 != null) {
                try { stmt1.close(); } catch( Exception e1 ) {}
            }
            if (conn1 != null) {
                try { conn1.close(); } catch( Exception e1 ) {}
            }
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
