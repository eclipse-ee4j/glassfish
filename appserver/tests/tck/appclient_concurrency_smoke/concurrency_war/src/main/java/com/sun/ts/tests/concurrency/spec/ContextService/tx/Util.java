/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ts.tests.concurrency.spec.ContextService.tx;

import static java.util.logging.Level.SEVERE;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class Util {

    private static final Logger LOGGER = Logger.getLogger(Util.class.getName());

    private Util() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T lookup(String jndiName) {
        Context context = null;
        T targetObject = null;
        try {
            context = new InitialContext();
            targetObject = (T) context.lookup(jndiName);
        } catch (Exception e) {
        } finally {
            try {
                context.close();
            } catch (NamingException e) {
                LOGGER.log(SEVERE, "failed to lookup resource.", e);
            }
        }
        return targetObject;
    }

    public static Connection getConnection(DataSource dataSource, String user, String pwd, boolean autoCommit) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection(); // Try without user password for EE case
            if (connection == null) {
                connection = dataSource.getConnection(user, pwd); // For standalone cases
            }
            if (connection != null) {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            LOGGER.log(SEVERE, "failed to get connection.", e);
        }
        return connection;
    }

    public static int getCount(String tableName, String username, String password) {
        Connection conn = getConnection(true, username, password);
        Statement stmt = null;
        try {
            final String queryStr = "select count(*) from " + tableName;
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(queryStr);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public static Connection getConnection(boolean autoCommit, String username, String password) {
        return getConnection(lookup("jdbc/DB1"), username, password, autoCommit);
    }
}
