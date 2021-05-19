/*
 * Copyright (c) 2004, 2018 Oracle and/or its affiliates. All rights reserved.
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

package samples.ejb.bmp.robean.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import java.sql.*;
import javax.sql.DataSource;

public class CustomerProgRefreshEJB implements jakarta.ejb.EntityBean {
    //database fields
    double balance;
    EntityContext ejbContext = null;
    InitialContext ic = null;
    DataSource dataSource = null;

    public double getBalance() {
        return balance;
    }

    public void setEntityContext(EntityContext cntx) {
        ejbContext = cntx;
        try {
            ic = new InitialContext();
            dataSource = (DataSource)ic.lookup("java:comp/env/jdbc/bmp-robean");
        } catch (NamingException e) {
            System.out.println("Naming exception occured while trying to lookup the datasource");
        }
    }

    public void unsetEntityContext() {
        ejbContext = null;
    }

    public PKString1 ejbFindByPrimaryKey(PKString1 SSN) throws FinderException {
        Connection conn = null;
    Statement statement = null;
    ResultSet results = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();
            String query = "SELECT * FROM customer1 where SSN = '" + SSN.getPK() + "'";
            results = statement.executeQuery(query);
            if (results.next()) {
                return SSN;
            } else {
                System.out.println("ERROR!! No entry matching the entered Social Security Number!");
                return new PKString1("");
            }
        } catch (SQLException e) {
            System.out.println("SQLException occured in ejbFindbyPrimaryKey method.");
            return new PKString1("");
        } finally {
        if (results != null)
        try {
            results.close();
        } catch (Exception ex) { }
        if (statement != null)
        try {
            statement.close();
        } catch (Exception ex) { }
        if (conn != null)
        try {
            conn.close();
        } catch (Exception ex) { }
    }
    }

    public PKString1 ejbCreate() {
        return null;
    }

    public void ejbPostCreate() {
    }

    public void ejbRemove() {
    }

    public void ejbStore() {
        // No need to implement EJB store since its ROB
    }

    public void ejbLoad() {
        try {
            Connection conn = null;
            PKString1 primaryKey = (PKString1)ejbContext.getPrimaryKey();
            conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
            String query = "SELECT balance FROM customer1 where SSN = '" + primaryKey.getPK() + "'";
            ResultSet results = statement.executeQuery(query);
            if (results.next()) {
                this.balance = results.getDouble("balance");
            } else {
                System.out.println("ERROR!! No entry matching the entered Social Security Number!");
            }
            conn.close();
        } catch (SQLException e) {
            System.out.println("SQLException occurred in ejbLoad() method");
        }
    }

    public void ejbActivate() {
    }
    public void ejbPassivate() {
    }
}
