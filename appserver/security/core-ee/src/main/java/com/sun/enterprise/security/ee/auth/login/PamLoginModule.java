/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

import static com.sun.enterprise.util.Utility.isEmpty;
import static java.util.logging.Level.FINE;

import com.sun.enterprise.security.BasePasswordLoginModule;
import com.sun.enterprise.security.auth.realm.pam.PamRealm;
import javax.security.auth.login.LoginException;

/**
 * This is the main LoginModule for PAM realm that invokes the calls to libpam4j classes to authenticate the given
 * username and password
 *
 * @author Nithya Subramanian
 */
public class PamLoginModule extends BasePasswordLoginModule {

    @Override
    protected void authenticateUser() throws LoginException {
        PamRealm pamRealm = getRealm(PamRealm.class, "pamrealm.invalid_realm");

        // A Unix user must have a name not null so check here.
        if (isEmpty(_username)) {
            throw new LoginException("Invalid Username");
        }

        String[] groups = pamRealm.authenticate(_username, _password);

        if (groups == null) { // JAAS behavior
            throw new LoginException("Failed Pam Login for " + _username);
        }

        _logger.log(FINE, () -> "PAM login succeeded for: " + _username);

        commitUserAuthentication(groups);
    }

}
