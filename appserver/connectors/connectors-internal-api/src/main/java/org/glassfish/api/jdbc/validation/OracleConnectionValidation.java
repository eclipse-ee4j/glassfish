/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.jdbc.validation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.glassfish.api.jdbc.ConnectionValidation;

/**
 * Provide custom implementation of connection validation for oracle dbvendor.
 *
 * Provides a custom connection validation
 * mechanism for Oracle dbVendor if custom-validation is chosen as the
 * connection-validation-method.
 *
 * @author Shalini M
 */
public class OracleConnectionValidation implements ConnectionValidation {

    private static final String SQL = "select sysdate from dual";

    /**
     * Check for validity of <code>java.sql.Connection</code>
     *
     * @param con       <code>java.sql.Connection</code>to be validated
     * @throws SQLException if the connection is not valid
     */
    public boolean isConnectionValid(Connection con) {
        boolean isValid = false;
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            isValid = stmt.execute(SQL);
        } catch (SQLException sqle) {
            isValid = false;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception ex) {
                }
            }
        }
        return isValid;
    }
}
