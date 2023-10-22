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
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchUserException;
import com.sun.enterprise.security.auth.realm.exceptions.UnsupportedRealmException;
import com.sun.enterprise.security.util.IASSecurityException;

/**
 * Implementations of this interface provide operations to Add, Update or Remove user data (called user management) that is used for
 * authentication when using GlassFish native identity stores (the <code>LoginModule</code>.
 *
 * <p>
 * Identity stores often provide access to external systems (like LDAP, Databases, etc) which have their own UI for managing such data.
 * As such user management is rarely implemented, and only Get user data is used, which is provided by {@link GlassFishUserStore}
 * implementations.
 *
 */
public interface GlassFishUserManagement {

    /**
     * @return true if the realm implementation support User Management (add,remove,update user)
     */
    default boolean supportsUserManagement() {
        return false;
    }

    /**
     * Adds new user to file realm. User cannot exist already.
     *
     */
    default void addUser(String name, String password, String[] groupList) throws BadRealmException, IASSecurityException {
        addUser(name, password.toCharArray(), groupList);

    }

    /**
     * Adds new user to file realm. User cannot exist already.
     *
     * @param name User name.
     * @param password Cleartext password for the user.
     * @param groupList List of groups to which user belongs.
     * @throws BadRealmException If there are problems adding user.
     *
     */
    default void addUser(String name, char[] password, String[] groupList) throws BadRealmException, IASSecurityException {
        throw new UnsupportedRealmException();
    }

    /**
     * Update data for an existing user. User must exist.
     *
     */
    public default void updateUser(String name, String newName, String password, String[] groups) throws NoSuchUserException, BadRealmException, IASSecurityException {
        updateUser(name, newName, (password == null) ? null : password.toCharArray(), groups);

    }

    /**
     * Update data for an existing user. User must exist.
     *
     * @param name Current name of the user to update.
     * @param newName New name to give this user. It can be the same as the original name. Otherwise it must be a new user name which
     * does not already exist as a user.
     * @param password Cleartext password for the user. If non-null the user password is changed to this value. If null, the original
     * password is retained.
     * @param groupList List of groups to which user belongs.
     * @throws BadRealmException If there are problems adding user.
     * @throws NoSuchUserException If user does not exist.
     *
     */
    default void updateUser(String name, String newName, char[] password, String[] groups) throws NoSuchUserException, BadRealmException, IASSecurityException {
        throw new UnsupportedRealmException();
    }

    /**
     * Remove user from file realm. User must exist.
     *
     * @param name User name.
     * @throws NoSuchUserException If user does not exist.
     *
     */
    default void removeUser(String name) throws NoSuchUserException, BadRealmException {
        throw new UnsupportedRealmException();
    }

    /**
     * Persist the realm data to permanent storage
     *
     * @throws com.sun.enterprise.security.auth.realm.exceptions.BadRealmException
     */
    default void persist() throws BadRealmException {
        // NOOP for realms that do not support UserManagement
    }

}
