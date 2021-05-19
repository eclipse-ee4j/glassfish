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

package com.sun.s1asdev.ejb.sfsb.cacheNPE.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleEntityBean
    implements EntityBean
{

    private transient DataSource ds;

    private EntityContext entityCtx;
    private String key;
    private String name;

    public void setEntityContext(EntityContext entityContext) {
        this.entityCtx = entityContext;
        initDataSource();
        System.out.println("[**SimpleEntityBean**] setEntityContext called");
    }

    private void initDataSource() {
        try {
            Context context = null;
            context    = new InitialContext();
            ds = (DataSource) context.lookup("java:comp/env/jdbc/DataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
    }

    public String ejbCreate(String key, String name)
        throws CreateException
    {

        Connection c = null;
        PreparedStatement ps = null;
        try {
                c = ds.getConnection();
                 ps = c.prepareStatement(
                "INSERT INTO SimpleEntity (keyid, name) VALUES (?,?)");
            ps.setString(1, key);
            ps.setString(2, name);
            if (ps.executeUpdate() != 1) {
                throw new CreateException("Didnt create ejb");
            }
            this.key = key;
            this.name = name;
        } catch (SQLException e)  {
            throw new CreateException("SQL exception " + e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (c != null)
                    c.close();
            } catch (Exception e) {}
        }
        return key;
    }

    public void ejbPostCreate(String key, String name) {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String ejbFindByPrimaryKey(String key)
        throws FinderException
    {
        Connection c = null;
             PreparedStatement ps = null;
            try {
                c = ds.getConnection();
                 ps = c.prepareStatement(
                "SELECT keyid from SimpleEntity where keyid = ?");
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new FinderException("No cust for " + key);
            }
            return key;
        } catch (SQLException e)  {
            throw new FinderException("SQL exception " + e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (c != null)
                    c.close();
            } catch (Exception e) {}
        }
    }

    public void ejbLoad() {
        Connection c = null;
        PreparedStatement ps = null;
        try {
            c = ds.getConnection();
            ps = c.prepareStatement(
                "SELECT name from SimpleEntity where keyid = ?");
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
               throw new NoSuchEntityException("No cust for " + key);
            this.name = rs.getString(1);
        } catch (SQLException e)  {
            throw new NoSuchEntityException("SQL exception " + e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (c != null)
                    c.close();
            } catch (Exception e) {}
        }
    }

    public void ejbStore() {
        Connection c = null;
             PreparedStatement ps = null;
        try {
            c = ds.getConnection();
                 ps = c.prepareStatement(
                "UPDATE SimpleEntity SET name = ? WHERE keyid = ?");
            ps.setString(1, name);
            ps.setString(2, key);
            if (ps.executeUpdate() != 1)
                throw new EJBException("Didnt store ejb");
        } catch (SQLException e)  {
            throw new EJBException("SQL exception " + e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (c != null)
                    c.close();
            } catch (Exception e) {}
        }
    }

    public void ejbRemove() throws RemoveException {
        Connection c = null;
             PreparedStatement ps = null;
        try {
            System.out.println("[**SimpleEntityBean**] ejbRemove called for key = " + this.key);
            c = ds.getConnection();
                 ps = c.prepareStatement(
                "DELETE FROM SimpleEntity WHERE keyid = ?");
            ps.setString(1, name);
            if (ps.executeUpdate() != 1)
                throw new RemoveException("Didnt remove ejb");
            System.out.println("[**SimpleEntityBean**] ejbRemove for key = "
                + this.key + " succeeded");
        } catch (SQLException e)  {
            throw new RemoveException("SQL exception " + e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (c != null)
                    c.close();
            } catch (Exception e) {}
        }
    }

    public void ejbActivate() {
        this.key = (String) entityCtx.getPrimaryKey();
        initDataSource();
    }

    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate(String key) {
    }
}
