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

package com.sun.enterprise.security.ee.auth.login;

import java.util.Arrays;
import java.util.logging.Level;

import com.sun.enterprise.security.auth.login.PasswordLoginModule;
import com.sun.enterprise.security.auth.login.common.LoginException;
import com.sun.enterprise.security.auth.realm.jdbc.JDBCRealm;

/**
 * This class implement a JDBC Login module for Glassfish. The work is derivated from Sun's sample JDBC login module.
 * Enhancement has been done to use latest features. sample setting in server.xml for JDBCLoginModule
 *
 * @author Jean-Baptiste Bugeaud
 */
public class JDBCLoginModule extends PasswordLoginModule {
    /**
     * Perform JDBC authentication. Delegates to JDBCRealm.
     *
     * @throws LoginException If login fails (JAAS login() behavior).
     */
    @Override
    protected void authenticate() throws LoginException {
        if (!(_currentRealm instanceof JDBCRealm)) {
            String msg = sm.getString("jdbclm.badrealm");
            throw new LoginException(msg);
        }

        final JDBCRealm jdbcRealm = (JDBCRealm) _currentRealm;

        // A JDBC user must have a name not null and non-empty.
        if (_username == null || _username.length() == 0) {
            String msg = sm.getString("jdbclm.nulluser");
            throw new LoginException(msg);
        }

        String[] grpList = jdbcRealm.authenticate(_username, getPasswordChar());

        if (grpList == null) { // JAAS behavior
            String msg = sm.getString("jdbclm.loginfail", _username);
            throw new LoginException(msg);
        }

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("JDBC login succeeded for: " + _username + " groups:" + Arrays.toString(grpList));
        }

        commitAuthentication(_username, getPasswordChar(), _currentRealm, grpList);
    }
}
