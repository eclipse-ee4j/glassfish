/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package test;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.DataSource;

import java.sql.*;

import com.sun.jdo.api.persistence.support.PersistenceManagerFactory;

/**
 * This SessionBean is used to test Read-Only CMP beans by inserting
 * initial data into the table and updating column 'NAME' when requested.
 * This allows to use java2db for the actual beans.
 * This bean does not access CMP beans.
 */
public class TestBean implements SessionBean {

    private DataSource ds = null;

    // SessionBean methods

    public void ejbCreate() throws CreateException {
        System.out.println("TestBean ejbCreate");
        try {
            ds = lookupDataSource();

        } catch (NamingException ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public void ejbActivate() {
        System.out.println("TestBean ejbActivate");
    }

    public void ejbPassivate() {
            ds = null;
    }

    public void ejbRemove() {

    }

    public void setSessionContext(SessionContext sc) {

    }

    /** Look up a DataSource by JNDI name.
     * The JNDI name is expected to be 'jdo/pmf', but it can reference
     * either a PersistenceManagerFactory or a DataSource. In case of
     * a former, the DataSource name will be a ConnectionFactory's name.
     */
    private DataSource lookupDataSource() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("jdo/pmf");

        if (objref instanceof PersistenceManagerFactory) {
            PersistenceManagerFactory pmf = (PersistenceManagerFactory)objref;
            String cfname = pmf.getConnectionFactoryName();

            System.out.println("DATASOURCE NAME: " + cfname);
            objref = initial.lookup(cfname);
        }
        return (DataSource) objref;
    }

    /** Insert values via jdbc call */
    public void insertValues (String table_name) {
        String st = "INSERT INTO " + table_name + " VALUES ('" +
                table_name + "', '" + table_name + "', '" + table_name + "')";
        System.out.println("INSERT STATEMENT: " + st);
        executeStatement(st);

        // Insert another row
        st = "INSERT INTO " + table_name + " VALUES ('" +
                table_name + "1', '" + table_name + "1', '" + table_name + "1')";
        System.out.println("INSERT STATEMENT: " + st);
        executeStatement(st);
    }

    /** Update values via jdbc call */
    public void updateValues (String table_name) {
        String st = "UPDATE " + table_name + " SET SHORTNAME = 'FOO' WHERE ID = '" + table_name + "'";
        System.out.println("UPDATE STATEMENT: " + st);
        executeStatement(st);
    }

    /** Execute SQL statement.
     * @param st the SQL statement as a String.
     * @throws EJBException to wrap a SQLException.
     */
    private void executeStatement (String st) {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            PreparedStatement ps = conn.prepareStatement(st);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new EJBException(e.toString());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

}
