/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.services.api.authentication;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.glassfish.security.services.api.SecurityService;
import org.jvnet.hk2.annotations.Contract;

/**
 * The AuthenticationService provides basic authentication functions.
 * Consumers of the service must establish Subjects in any security context.
 */
@Contract
public interface AuthenticationService extends SecurityService {
    /**
     * Log in a user with username and password.
     *
     * @param username The username.
     * @param password The password.
     * @param subject An optional Subject to receive principals and credentials for the logged in user.
     * If provided, it will be returned as the return value; if not, a new Subject will be returned.
     *
     * @return A Subject representing the logged in user.
     *
     * @throws LoginException
     */
    public Subject login(String username, char[] password, Subject subject)
            throws LoginException;

    /**
     * Authenticate using a CallbackHandler to provider username/password, X.509 certificate, or
     * Secure Admin token.
     *
     * @param cbh  The CallbackHandler.
     * @param subject An optional Subject to receive principals and credentials for the logged in user.
     * If provided, it will be returned as the return value; if not, a new Subject will be returned.
     *
     * @return A Subject representing the logged in user.
     *
     * @throws LoginException
     */
    public Subject login(CallbackHandler cbh, Subject subject) throws LoginException;

    /**
     * Impersonate a user, specifying the user and group principal names that
     * should be established in the resulting Subject.
     *
     * Note that, that this method always behaves as if <bold>virtual</bold> were true in the case
     * that the underlying user store provider does not support user lookup.
     *
     * @param user The username.
     * @param groups An array of group names.  If <bold>virtual</bold> is true, group principals will be created
     * using this array.  If <bold>virtual</bold> is false and groups is non-null, it will be used to filter the
     * groups returned by the configured UserStoreProvider.
     * @param subject An optional Subject to receive principals and credentials for the logged in user.
     * If provided, it will be returned as the return value; if not, a new Subject will be returned.
     * @param virtual  If true, simply create a subject with the given user and group names.  If false, configured
     * UserStoreProvider will be queried for the given username and a Subject created only if the user exists.  Groups
     * will be populated with the intersection of the groups parameter and the groups returned by the UserStoreProvider.
     *
     * @return A Subject representing the impersonated user.
     *
     * @throws LoginException
     */
    public Subject impersonate(String user, String[] groups, Subject subject, boolean virtual)
            throws LoginException;
}
