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

package com.iplanet.ias.security.auth.login;

import com.sun.enterprise.security.BasePasswordLoginModule;
import com.sun.enterprise.security.auth.AuthenticationStatus;
import com.sun.enterprise.security.auth.AuthenticationStatusImpl;
import com.sun.enterprise.security.auth.realm.Realm;

import javax.security.auth.login.LoginException;

/**
 * Provided for backward compatibility with SunOne 7.0
 * Newer implementations should extend
 * com.sun.appserv.security.BasePasswordLoginModule
 */
public abstract class PasswordLoginModule extends BasePasswordLoginModule
{
    /**
     * authenticateUser calls authenticate which is implemented by the implementation
     * of this subclass
     * @throws LoginException
     */
    protected final void authenticateUser() throws LoginException{
        AuthenticationStatus as = authenticate();
        if(as.getStatus() == as.AUTH_SUCCESS)
            return;
        else{
            throw new LoginException();
        }
    }

    @Deprecated
     public final AuthenticationStatus commitAuthentication(String username,
                                        String password,
                                        Realm theRealm,
                                        String[] groups)
    {
        return commitAuthentication(username, password.toCharArray(), theRealm, groups);
    }
    /** Called at the end of the authenticate method by the user
     * @return AuthenticationStatus indicating success/failure
     */
    public final AuthenticationStatus commitAuthentication(String username,
                                        char[] password,
                                        Realm theRealm,
                                        String[] groups)
    {
        commitUserAuthentication(groups);
        int status = AuthenticationStatus.AUTH_SUCCESS;
        String realm = theRealm.getName();
        String authMethod = theRealm.getAuthType();
        AuthenticationStatus as =
            new AuthenticationStatusImpl(username, authMethod, realm, status);
        return as;
    }
    abstract protected AuthenticationStatus authenticate() throws LoginException;
}
