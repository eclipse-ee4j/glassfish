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


import com.sun.enterprise.security.auth.login.PasswordLoginModule;
import javax.security.auth.login.*;

import com.sun.enterprise.security.auth.realm.ldap.LDAPRealm;

/**
 * iAS JAAS LoginModule for an LDAP Realm.
 *
 * <P>Refer to the LDAPRealm documentation for necessary and optional
 * configuration parameters for the iAS LDAP login support.
 *
 * <P>There are various ways in which a user can be authenticated using
 * an LDAP directory. Currently this login module only supports one mode,
 * 'find and bind'. Other modes may be added as schedules permit.
 *
 * <P>Mode: <i>find-bind</i>
 * <ol>
 *  <li>An LDAP search is issued on the directory starting at base-dn
 *      with the given search-filter (having substituted the user name
 *      in place of %s). If no entries match this search, login fails
 *      and authentication is over.
 *  <li>The DN of the entry which matched the search as the DN
 *      of the user in the directory. If the search-filter
 *      is properly set there should always be a single match; if there are
 *      multiple matches, the first one found is used.
 *  <li>Next an LDAP bind is attempted using the above DN and the provided
 *      password. If this fails, login is considered to have failed and
 *      authentication is over.
 *  <li>Then an LDAP search is issued on the directory starting at
 *      group-base-dn with the given group-search-filter (having
 *      substituted %d for the user DN previously found). From the
 *      matched entry(ies) all the values of group-target are taken
 *      as group names in which the user has membership. If no entries
 *      are found, the group membership is empty.
 * </ol>
 *
 *
 */
public class LDAPLoginModule extends PasswordLoginModule
{
    private LDAPRealm _ldapRealm;

    /**
     * Performs authentication for the current user.
     *
     */
    protected void authenticate ()
        throws LoginException
    {
        if (!(_currentRealm instanceof LDAPRealm)) {
            String msg = sm.getString("ldaplm.badrealm");
            throw new LoginException(msg);
        }
        _ldapRealm = (LDAPRealm)_currentRealm;

                       // enforce that password cannot be empty.
                       // ldap may grant login on empty password!
        if (getPasswordChar() == null || getPasswordChar().length == 0) {
            String msg = sm.getString("ldaplm.emptypassword", _username);
            throw new LoginException(msg);
        }

        String mode = _currentRealm.getProperty(LDAPRealm.PARAM_MODE);

        if (LDAPRealm.MODE_FIND_BIND.equals(mode)) {
            String[] grpList = _ldapRealm.findAndBind(_username, getPasswordChar());
            commitAuthentication(_username, getPasswordChar(),
                    _currentRealm, grpList);
        } else {
            String msg = sm.getString("ldaplm.badmode", mode);
            throw new LoginException(msg);
        }
    }
}
