/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import com.sun.logging.LogDomains;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.jdbc.ConnectionValidation;

/**
 * Default connection validation mechanism used by common database vendors to
 * perform connection validation.
 *
 * @author Shalini M
 */
public class DefaultConnectionValidation implements ConnectionValidation {

    private static final Logger LOG = Logger.getLogger(LogDomains.RSR_LOGGER);

    @Override
    public boolean isConnectionValid(Connection con) {
        try (Statement stmt = con.createStatement()) {
            return stmt.execute("SELECT '1'");
        } catch (SQLException sqle) {
            LOG.log(Level.WARNING, "Exception while validating connection!", sqle);
            return false;
        }
    }
}
