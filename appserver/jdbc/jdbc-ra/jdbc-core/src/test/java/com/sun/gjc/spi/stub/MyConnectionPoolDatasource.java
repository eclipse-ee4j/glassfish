/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package com.sun.gjc.spi.stub;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

/**
 * Stub for javax.sql.ConnectionPoolDataSource with no implementation at all. Just to avoid using mocks.
 */
public class MyConnectionPoolDatasource implements ConnectionPoolDataSource {

    public static final String NOT_ALLOWED_PASSWORD = "notAllowedPassword";

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public PooledConnection getPooledConnection() throws SQLException {
        return new MyPooledConnection();
    }

    @Override
    public PooledConnection getPooledConnection(String user, String password) throws SQLException {
        if (password.equals(NOT_ALLOWED_PASSWORD)) {
            throw new SQLException("incorrect credentials");
        }
        return new MyPooledConnection();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }
}