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

package com.sun.s1asdev.jdbc.initsql.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

public class SimpleSessionBean implements SessionBean
{

    private SessionContext context;
    private InitialContext ic;
    private DataSource ds1;

    public void setSessionContext(SessionContext ctxt) {
        this.context = ctxt;
        try {
            ic = new InitialContext();
            ds1 = (com.sun.appserv.jdbc.DataSource)ic.lookup("java:comp/env/DataSource1");
        } catch( Exception ne ) {
            ne.printStackTrace();
        }
    }

    public void ejbCreate() throws CreateException {
    }

    /**
     * Test to select the names from a table.
     *
     * The result set would contain different number of rows based on
     * a session property set during the initialization sql phase.
     * Based on the property set, the number of rows are compared to
     * test the feature.
     */
    public boolean test1(boolean caseSensitive) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String query = "Select name from WORKERS where name='Joy Joy'";
        boolean result = false;
        int size = 0;
        try {
            con = ds1.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            if(rs != null) {
                while(rs.next()) {
                    size++;
                }
            }
            if(caseSensitive) {
                result = size == 1;
            } else {
                result = size == 3;
            }
        } catch (SQLException ex) {
            result = false;
            ex.printStackTrace();
        } finally {
            if(rs != null) {
                try {
                    rs.close();
                } catch(Exception ex) {}
            }
            if(stmt != null) {
                try {
                    stmt.close();
                } catch(Exception ex) {}
            }
            if(con != null) {
                try {
                    stmt.close();
                } catch(Exception ex) {}
            }
        }
        return result;
    }

    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
