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

public class CustomerEJB implements jakarta.ejb.EntityBean {
    //database fields
    double balance;
    EntityContext ejbContext = null;
    InitialContext ic = null;
    DataSource dataSource = null;

    public double getBalance() {
        return balance;
    }

    public void doCredit(double amount) {
        this.balance = this.balance + amount;
    }

    public void doDebit(double amount) {
        this.balance = this.balance - amount;
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

    public PKString ejbFindByPrimaryKey(PKString SSN) throws FinderException{
        try {
            Connection conn = null;
            conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
            String query = "SELECT * FROM customer1 where SSN = '" + SSN.getPK() + "'";
            ResultSet results = statement.executeQuery(query);
            conn.close();
            if (results.next()) {
                return SSN;
            } else {
                System.out.println("ERROR!! No entry matching the entered Social Security Number!");
                return new PKString("");
            }
        } catch (SQLException e) {
            System.out.println("SQLException occured in ejbFindbyPrimaryKey method.");
            return new PKString("");
        }
    }

    public PKString ejbCreate() {
        return null;
    }

    public void ejbPostCreate() {
    }

    public void ejbRemove() {
    }

    public void ejbStore() {
        try {
            Connection conn = null;
            PKString primaryKey = (PKString)ejbContext.getPrimaryKey();
            conn = dataSource.getConnection();
            PreparedStatement statement = null;
            statement = conn.prepareStatement(
                "UPDATE customer1 " +
                "set balance = ? " +
                "where SSN = ?"
            );
            statement.setDouble(1, this.balance);
            statement.setString(2, primaryKey.getPK());
            statement.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            System.out.println("SQL exception occured in ejbStore method");
        }
    }

    public void ejbLoad() {
        try {
            Connection conn = null;
            PKString primaryKey = (PKString)ejbContext.getPrimaryKey();
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
