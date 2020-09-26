/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.connector.blackbox;

import javax.naming.Reference;
import jakarta.resource.Referenceable;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnectionFactory;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * @author Tony Ng
 */
public class JdbcDataSource_XA implements
        DataSource, Serializable, Referenceable {

    private String desc;
    private ManagedConnectionFactory mcf;
    private ConnectionManager cm;
    private Reference reference;

    public JdbcDataSource_XA(ManagedConnectionFactory mcf,
                          ConnectionManager cm) {
        this.mcf = mcf;
        if (cm == null) {
            this.cm = new JdbcConnectionManager();
        } else {
            this.cm = cm;
        }
    }

    public Connection getConnection() throws SQLException {
        try {
            return (Connection) cm.allocateConnection(mcf, null);
        } catch (ResourceException ex) {
            throw new SQLException(ex.getMessage());
        }
    }

    public Connection getConnection(String username, String password)
            throws SQLException {
        try {
            ConnectionRequestInfo info =
                    new JdbcConnectionRequestInfo(username, password);
            return (Connection) cm.allocateConnection(mcf, info);
        } catch (ResourceException ex) {
            throw new SQLException(ex.getMessage());
        }
    }

    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        DriverManager.setLoginTimeout(seconds);
    }

    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        DriverManager.setLogWriter(out);
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException{
      throw new SQLFeatureNotSupportedException("Do not support Java 7 feature.");
    }

    public String getDescription() {
        return desc;
    }

    public void setDescription(String desc) {
        this.desc = desc;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public Reference getReference() {
        return reference;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
