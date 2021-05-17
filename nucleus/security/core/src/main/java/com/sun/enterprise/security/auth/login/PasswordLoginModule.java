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

import com.sun.enterprise.security.BasePasswordLoginModule;
import com.sun.enterprise.security.auth.realm.Realm;
import javax.security.auth.login.LoginException;

/**
 * Abstract base class for password-based login modules.
 * This class is provided for
 * backward compatibility and is a candidate for deprecation.
 *
 */
@Deprecated
public abstract class PasswordLoginModule extends BasePasswordLoginModule
{

    /**
     * Maintain RI compatibility.
     *
     * <P>This is a convenience method which can be used by subclasses
     * to complete the steps required by RI legacy authentication code.
     * Most of this should go away if a clean JAAS/Subject based
     * infrastructure is provided. But for now this must be done.
     *
     * <P>Note that this method is called after the authentication
     * has succeeded. If authentication failed do not call this method.
     *
     * <P>A lot of the RI behavior is still present here. Some of the
     * most notable points to remember:
     * <ul>
     *  <li>Global instance field succeeded is set to true by this method.
     *
     * @param username Name of authenticated user.
     * @param password Password of this user.
     * @param theRealm Current Realm object for this authentication.
     * @param groups String array of group memberships for user (could be
     *     empty).
     * @returns void
     *
     */
    @Deprecated
    public final void commitAuthentication(String username,
                                        String password,
                                        Realm theRealm,
                                        String[] groups)
    {
        commitUserAuthentication(groups);
    }

   /**
     * Since the passwords are to be stored as to have char[]
     * BT: 6946553.
     * Retaining the other method for backward compatability
     *
     * @param username Name of authenticated user.
     * @param password Password of this user in char[].
     * @param theRealm Current Realm object for this authentication.
     * @param groups String array of group memberships for user (could be
     *     empty).
     * @returns void
     *
     */
    public final void commitAuthentication(String username,
                                        char[] password,
                                        Realm theRealm,
                                        String[] groups)
    {
        commitUserAuthentication(groups);
    }
    /**
     * Older implementations can implement authenticate. While new implementation
     * calls authenticateUser
     * @throws LoginException
     */
    protected final void authenticateUser () throws LoginException{
        authenticate();
    }

    /**
     * Perform authentication decision.
     * Method returns silently on success and returns a LoginException
     * on failure.
     * To be implmented by sub-classes
     * @return void authenticate returns silently on successful authentication.
     * @throws com.sun.enterprise.security.LoginException on authentication failure.
     *
     */
    abstract protected void authenticate()
        throws LoginException;
}
