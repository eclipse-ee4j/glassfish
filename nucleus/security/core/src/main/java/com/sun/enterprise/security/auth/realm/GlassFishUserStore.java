/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.auth.realm;

import com.sun.enterprise.security.auth.realm.exceptions.BadRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.InvalidOperationException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchUserException;
import com.sun.enterprise.security.auth.realm.exceptions.UnsupportedRealmException;
import java.util.Enumeration;

/**
 * Implementations of this interface provide access to Get user data useful for authentication purposes.
 *
 * <p>
 * A user store has a strong connection with the concept of an identity store. In GlassFish the identity store concept
 * (validating caller credentials and returning user data) is split into multiple artifacts. The identity store itself
 * is provided by the <code>LoginModule</code>, of which an implementation can use the <code>GlassFishUserStore</code>
 * if needed (but will typically need to use functionality beyond this interface as well).
 *
 * <p>
 * GlassFish provides an extra interface to optionally Add, Update or Remove user data (via the admin console of GlassFish) via
 * the {@link GlassFishUserManagement} interface. This one is however rarely implemented, as most systems behind the user store (like
 * LDAP and Database) have their own UI for this.
 *
 */
public interface GlassFishUserStore {

    /**
     * Returns names of all the users in this particular realm.
     *
     * @return enumeration of user names (strings)
     * @throws BadRealmException if realm data structures are bad
     */
    default Enumeration<String> getUserNames() throws BadRealmException {
        throw new UnsupportedRealmException();
    }

    /**
     * Returns the information recorded about a particular named user.
     *
     * @param name name of the user whose information is desired
     *
     * @return the user object
     * @throws NoSuchUserException if the user doesn't exist
     * @throws BadRealmException if realm data structures are bad
     */
    default User getUser(String name) throws NoSuchUserException, BadRealmException {
        throw new UnsupportedRealmException();
    }

    /**
     * Returns names of all the groups in this particular realm.
     *
     * @return enumeration of group names (strings)
     * @throws BadRealmException if realm data structures are bad
     */
    default Enumeration<String> getGroupNames() throws BadRealmException {
        throw new UnsupportedRealmException();
    }

    /**
     * Returns the name of all the groups that this user belongs to
     *
     * @param username name of the user in this realm whose group listing is needed.
     * @return enumeration of group names (strings)
     * @throws InvalidOperationException thrown if the realm does not support this operation
     * @throws NoSuchUserException
     */
    Enumeration<String> getGroupNames(String username) throws NoSuchUserException;

    /**
     * Refreshes the realm data so that new users/groups are visible.
     *
     * @throws BadRealmException if realm data structures are bad
     */
    default void refresh() throws BadRealmException {
        throw new UnsupportedRealmException();
    }

}
