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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Stub for java.sql.Driver with no implementation at all. Just to avoid using mocks.
 */
public class MyDriver implements Driver {

    public static final String ACCEPTED_URL = "jdbc/mydriver/url";
    public static final String NOT_ALLOWED_PASSWORD = "notAllowedPassword";

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        String password = info.getProperty("password");
        if (password != null && password.equals(NOT_ALLOWED_PASSWORD)) {
            throw new SQLException("incorrect credentials");
        }
        return new MyConnection();
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        if (url.equals(ACCEPTED_URL)) {
            return true;
        }
        return false;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return null;
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

}
