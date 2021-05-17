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

package com.sun.enterprise.security.auth.login;

import java.util.*;

import java.util.logging.Logger;
import java.util.logging.Level;

import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import javax.security.auth.spi.*;

import com.sun.enterprise.security.auth.realm.solaris.SolarisRealm;

// limit RI imports
import com.sun.enterprise.security.auth.Privilege;
import com.sun.enterprise.security.auth.PrivilegeImpl;

import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.security.auth.login.common.PasswordCredential;
import javax.security.auth.login.LoginException;

/**
 * Solaris realm login module.
 *
 * <P>Processing is delegated to the SolarisRealm class which accesses
 * the native methods.
 *
 * @see com.sun.enterprise.security.auth.login.PasswordLoginModule
 * @see com.sun.enterprise.security.auth.realm.solaris.SolarisRealm
 *
 */
public class SolarisLoginModule extends PasswordLoginModule
{

    /**
     * Perform solaris authentication. Delegates to SolarisRealm.
     *
     * @throws LoginException If login fails (JAAS login() behavior).
     *
     */
    protected void authenticate()
        throws LoginException
    {
        if (!(_currentRealm instanceof SolarisRealm)) {
            String msg = sm.getString("solarislm.badrealm");
            throw new LoginException(msg);
        }

        SolarisRealm solarisRealm = (SolarisRealm)_currentRealm;

        // A solaris user must have a name not null so check here.
        if ( (_username == null) || (_username.length() == 0) ) {
            String msg = sm.getString("solarislm.nulluser");
            throw new LoginException(msg);
        }

        String[] grpList = solarisRealm.authenticate(_username, getPasswordChar());

        if (grpList == null) {  // JAAS behavior
            String msg = sm.getString("solarislm.loginfail", _username);
            throw new LoginException(msg);
        }

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("Solaris login succeeded for: " + _username);
        }

        commitAuthentication(_username, getPasswordChar(),
                             _currentRealm, grpList);
    }

}
