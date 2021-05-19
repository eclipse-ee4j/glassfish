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

package org.glassfish.security.services.spi.authentication;

import java.util.Set;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.glassfish.security.services.api.common.Attributes;

/*
 * Provider interface for authentication service.
 */

public interface UserStoreProvider {

    /*
     * Represents a user entry in the store
     */
    public interface UserEntry {
        String getName();
        String getDN();
        String getUid();
        String getStoreId();
        Set<GroupEntry> getGroups();
        Attributes getAttributes();
    }

    /*
     * Represents a group entry in the store
     */
    public interface GroupEntry {
        String getName();
        String getDN();
        String getUid();
        String getStoreId();
        Set<String> getMembers();
    }

    /*
     * Page-able Result Set
     */
    public interface ResultSet<T> {
        boolean hasNext();
        T getNext();
        void close();
    }

    /**
     * Get the unique store ID for this user store.  This value must be unique across all
     * stores configured into the system or which might be propogated into the system via
     * SSO, etc.  If this USP aggregates multiple underlying stores, the user IDs returned
     * by the provider must be sufficient to uniquely identify users across all of the
     * underlying stores.
     *
     * @return The store ID for this USP.
     */
    String getStoreId();

    /**
     * Determine if authentication is supported and enabled by this USP.
     *
     * @return True or false.
     */
    boolean isAuthenticationEnabled();

    /**
     * Determine if user lookup is supported and enabled by this USP.
     *
     * @return True or false.
     */
    boolean isUserLookupEnabled();

    /**
     * Determine if user update (CRUD operations) is supported and enabled by this USP.
     *
     * @return True or false.
     */
    boolean isUserUpdateEnabled();

    /**
     * Authenticate using credentials supplied in the given CallbackHandler.  All USPs must support
     * at least NameCallback and PasswordCallback.  The only other callback type expected to be commonly
     * used is X509Certificate, but it's possible to imagine, e.g., KerberosToken or PasswordDigest.
     *
     * @param cbh
     * @param isGetGroups Whether or not to return the user's groups.
     * @param attributeNames Names of attributes to return, or null for no attributes.
     * @return If successful, a UserEntry representing the authenticated user, otherwise throws an exception.
     * @throws LoginException
     */
    UserEntry authenticate(CallbackHandler cbh, boolean isGetGroups, Set<String> attributeNames) throws LoginException;

    /*
     * User Lookup
     */

    /**
     * Lookup users by name.  Since name is not necessarily unique, more than one entry may be returned.
     * Group membership and selected attributes can also be requested, but requesting these may be inefficient
     * if more than one user is matched.
     *
     * @param name The user name to searech for.
     * @param isGetGroups Whether or not to return users' groups.
     * @param attributeNames Names of attributes to return, or null for no attributes.
     * @return The Set of UserEntrys found.
     *
     * @throws UserStoreException
     */
    ResultSet<UserEntry> lookupUsersByName(String name, boolean isGetGroups, Set<String> attributeNames) throws UserStoreException;

    /**
     * Lookup a user by unique ID.  Returns the corresponding UserEntry if found.
     * Group membership and selected attributes can also be requested.
     *
     * @param uid
     * @param isGetGroups Whether or not to return users' groups.
     * @param attributeNames Names of attributes to return, or null for no attributes.
     * @return The UserEntry (if found).
     *
     * @throws UserStoreException
     */
    UserEntry lookupUserByUid(String uid, boolean isGetGroups, Set<String> attributeNames) throws UserStoreException;

    /*
     * Group Lookup
     */

    /**
     * Get the GroupEntry(s) for the specified group name.
     *
     * @param name The name to search on, may include wildcards (e.g., a*, *b, etc.)
     * @return ResultSet of the GroupEntries matching the specified name.
     *
     * @throws UserStoreException
     */
    ResultSet<GroupEntry> lookupGroupsByName(String name) throws UserStoreException;

    /**
     * Get the GroupEntry for the specified group.
     *
     * @param uid The UID of the group to return.
     * @return GroupEntry corresponding to the group UID.
     *
     * @throws UserStoreException
     */
    GroupEntry lookupGroupByUid(String uid) throws UserStoreException;

    /*
     * User CRUD
     */

    /**
     * Create a new user and return the unique ID assigned.
     *
     * @param name Name of the new user entry.
     * @param pwd Password to set on the new entry.
     * @param attributes Attributes to set on the entry (or null if none).
     * @return Returns the UID assigned to the new entry (can be used for subsequent operations)
     *
     * @throws UserStoreException
     */
    String /*uid*/ createUser(String name, char[] pwd, Attributes attributes) throws UserStoreException;

    /**
     * Remove the specified user.
     *
     * @param uid UID of the user to remove.
     *
     * @throws UserStoreException
     */
    void deleteUser(String uid) throws UserStoreException;

    /**
     * Change the password for the specified user.  If old password is provided, verify before changing.
     *
     * @param uid UID of user whose password should be changed.
     * @param oldPwd Old password, if verification desired, or null.  If provided, must be valid.
     * @param newPwd New password to set.
     *
     * @throws UserStoreException
     */
    void changePassword(String uid, char[] oldPwd, char[] newPwd) throws UserStoreException;  // setPassword(String uid, char[] pwd)?  password reset?

    /**
     * Add the given attribute values to the user entry.
     *
     * @param uid
     * @param attributes
     * @param replace
     * @throws UserStoreException
     */
    void addAttributeValues(String uid, Attributes attributes, boolean replace) throws UserStoreException;

    /**
     * Remove the given attribute values from the user entry.
     *
     * @param uid
     * @param attributes
     * @throws UserStoreException
     */
    void removeAttributeValues(String uid, Attributes attributes) throws UserStoreException;

    /**
     * Remove the given attributes from the user entry.
     *
     * @param uid
     * @param attributeNames
     * @throws UserStoreException
     */
    void removeAttributes(String uid, Set<String> attributeNames) throws UserStoreException;

    /*
     * Group CRUD
     */

    /**
     * Create a new group.
     *
     * @param groupName
     * @return The UID for the newly created group
     * @throws UserStoreException
     */
    String /*uid*/ createGroup(String groupName) throws UserStoreException;

    /**
     * Delete a group.
     *
     * @param uid UID of group to delete.
     * @throws UserStoreException
     */
    void deleteGroup(String uid) throws UserStoreException;

    /**
     * Add the specified user to the set of groups.
     *
     * @param uid
     * @param groups
     * @throws UserStoreException
     */
    void addUserToGroups(String uid, Set<String> groups) throws UserStoreException;

    /**
     * Remove the specified user from the set of groups.
     *
     * @param uid
     * @param groups
     * @throws UserStoreException
     */
    void removeUserFromGroups(String uid, Set<String> groups) throws UserStoreException;

    /**
     * Add the set of users to the specified group.
     *
     * @param uids
     * @param group
     * @throws UserStoreException
     */
    void addUsersToGroup(Set<String> uids, String group) throws UserStoreException;

    /**
     * Remove the set of users from the specified group.
     *
     * @param uids
     * @param group
     * @throws UserStoreException
     */
    void removeUsersFromGroup(Set<String> uids, String group) throws UserStoreException;

}
