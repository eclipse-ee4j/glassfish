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

package com.sun.s1asdev.jdbc.connectioncreationretry.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;

public class SimpleBMPBean
        implements EntityBean {

    protected DataSource ds;

    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context = new InitialContext();
            ds = (DataSource) context.lookup("java:comp/env/DataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }



/* Get a single connection and close it */
    public boolean test1(){
        Connection conn = null;
        boolean passed = true;
        try {
            conn = ds.getConnection();
            insertEntry(conn);
            emptyTable(conn);
        } catch (Exception e) {
            //System.out.println("Exception caught : " + e.getMessage() );
            e.printStackTrace();
            passed = false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                }
            }
        }

        return passed;
    }

    private void insertEntry(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT into O_CUSTOMER" +
                "  values (1, 'abcd')");
        stmt.close();
    }

    private void emptyTable(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DELETE FROM O_CUSTOMER");
        stmt.close();
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
