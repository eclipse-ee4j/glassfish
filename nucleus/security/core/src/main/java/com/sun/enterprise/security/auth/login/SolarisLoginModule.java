/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.auth.login;

import com.sun.enterprise.security.BasePasswordLoginModule;
import com.sun.enterprise.security.auth.realm.solaris.SolarisRealm;

import java.util.logging.Level;

import javax.security.auth.login.LoginException;

import static com.sun.enterprise.util.Utility.isEmpty;

/**
 * Solaris realm login module.
 *
 * <P>
 * Processing is delegated to the SolarisRealm class which accesses the native methods.
 *
 * @see com.sun.enterprise.security.auth.realm.solaris.SolarisRealm
 *
 */
public class SolarisLoginModule extends BasePasswordLoginModule {

    /**
     * Perform solaris authentication. Delegates to SolarisRealm.
     *
     * @throws LoginException If login fails (JAAS login() behavior).
     */
    @Override
    protected void authenticateUser() throws LoginException {
        SolarisRealm solarisRealm = getRealm(SolarisRealm.class, "solarislm.badrealm");

        // A solaris user must have a name not null so check here.
        if (isEmpty(_username)) {
            throw new LoginException(sm.getString("solarislm.nulluser"));
        }

        String[] groups = solarisRealm.authenticate(_username, getPasswordChar());

        if (groups == null) { // JAAS behavior
            throw new LoginException(sm.getString("solarislm.loginfail", _username));
        }

        _logger.log(Level.FINEST, () -> "Solaris login succeeded for: " + _username);

        commitUserAuthentication(groups);
    }

}
