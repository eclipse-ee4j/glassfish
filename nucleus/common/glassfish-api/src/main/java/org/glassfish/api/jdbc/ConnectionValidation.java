/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This interface can be implemented to provide a custom connection validation mechanism if custom-validation is chosen
 * as the connection-validation-method.
 *
 * @author Shalini M
 */
public interface ConnectionValidation {
    /**
     * Check for validity of <code>java.sql.Connection</code>
     *
     * @param con <code>java.sql.Connection</code>to be validated
     * @throws SQLException if the connection is not valid
     */
    boolean isConnectionValid(Connection con) throws SQLException;

}
