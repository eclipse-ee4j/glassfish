/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.ee.auth.login;

import static com.sun.enterprise.util.Utility.isEmpty;
import static java.util.logging.Level.FINEST;

import com.sun.enterprise.security.BasePasswordLoginModule;
import com.sun.enterprise.security.auth.realm.jdbc.JDBCRealm;
import java.util.Arrays;
import javax.security.auth.login.LoginException;

/**
 * This class implement a JDBC Login module for Glassfish. The work is derivated from Sun's sample JDBC login module.
 * Enhancement has been done to use latest features. sample setting in server.xml for JDBCLoginModule
 *
 * @author Jean-Baptiste Bugeaud
 */
public class JDBCLoginModule extends BasePasswordLoginModule {
    /**
     * Perform JDBC authentication. Delegates to JDBCRealm.
     * @throws javax.security.auth.login.LoginException
     *
     * @throws LoginException If login fails (JAAS login() behavior).
     * @throws javax.security.auth.login.LoginException
     */
    @Override
    protected void authenticateUser() throws LoginException {
        final JDBCRealm jdbcRealm = getRealm(JDBCRealm.class, "jdbclm.badrealm");

        // A JDBC user must have a name not null and non-empty.
        if (isEmpty(_username)) {
            throw new LoginException(sm.getString("jdbclm.nulluser"));
        }

        String[] groups = jdbcRealm.authenticate(_username, getPasswordChar());

        if (groups == null) { // JAAS behavior
            throw new LoginException(sm.getString("jdbclm.loginfail", _username));
        }

        _logger.log(FINEST, () -> "JDBC login succeeded for: " + _username + " groups:" + Arrays.toString(groups));

        commitUserAuthentication(groups);
    }
}
