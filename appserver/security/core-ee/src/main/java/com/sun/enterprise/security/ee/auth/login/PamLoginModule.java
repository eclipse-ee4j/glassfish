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

package com.sun.enterprise.security.ee.auth.login;

import java.util.Set;
import java.util.logging.Level;

import javax.security.auth.login.LoginException;

import org.jvnet.libpam.PAM;
import org.jvnet.libpam.PAMException;
import org.jvnet.libpam.UnixUser;

import com.sun.appserv.security.AppservPasswordLoginModule;
import com.sun.enterprise.security.auth.realm.pam.PamRealm;

/**
 * This is the main LoginModule for PAM realm that invokes the calls to libpam4j classes to authenticate the given
 * username and password
 *
 * @author Nithya Subramanian
 */
public class PamLoginModule extends AppservPasswordLoginModule {

    @Override
    protected void authenticateUser() throws LoginException {

        // A Unix user must have a name not null so check here.
        if (_username == null || _username.length() == 0) {
            throw new LoginException("Invalid Username");
        }
        UnixUser user = authenticate(_username, _password);

        if (user == null) { // JAAS behavior
            throw new LoginException("Failed Pam Login for " + _username);
        }
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "PAM login succeeded for: " + _username);
        }

        /*
         * Get the groups from the libpam4j UnixUser class that has been returned after a successful authentication.
         */

        String[] grpList = null;
        Set<String> groupSet = user.getGroups();

        if (groupSet != null) {
            grpList = new String[groupSet.size()];
            user.getGroups().toArray(grpList);
        } else {
            // Empty group list, create a zero-length group list
            grpList = new String[0];
        }
        commitUserAuthentication(grpList);
    }

    /**
     * Invokes the authentication call.This class uses the default PAM service - sshd
     *
     * @param username OS User to authenticate.
     * @param password Given password.
     * @returns null if authentication failed, returns the UnixUser object if authentication succeeded.
     *
     */
    private UnixUser authenticate(String username, String password) throws LoginException {
        UnixUser user = null;
        String pamService = null;

        if (!(_currentRealm instanceof PamRealm)) {
            throw new LoginException("pamrealm.invalid_realm");
        }
        pamService = ((PamRealm) _currentRealm).getPamService();

        try {
            user = new PAM(pamService).authenticate(username, password);

        } catch (PAMException e) {
            _logger.log(Level.SEVERE, "pam_exception_authenticate", e);
        }
        return user;
    }
}
