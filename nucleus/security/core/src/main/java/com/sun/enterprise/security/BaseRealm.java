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

package com.sun.enterprise.security;

import java.util.*;

import com.sun.enterprise.security.auth.realm.*;

import com.sun.enterprise.security.util.IASSecurityException;
import com.sun.enterprise.util.i18n.StringManager;


/**
 * Parent class for iAS Realm classes.
 *
 * <P>This class provides default implementation for most of the abstract
 * methods in com.sun.enterprise.security.auth.realm.Realm. Since most
 * of these abstract methods are not supported by Realms there is
 * no need for the subclasses to implement them. The default implementations
 * provided here generally throw an exception if invoked.
 *
 *  @author Harpreet Singh
 */
public abstract class BaseRealm extends Realm
{
    public static final String JAAS_CONTEXT_PARAM="jaas-context";

    protected static final StringManager sm =
        StringManager.getManager(IASRealm.class);


    /**
     * Returns an AuthenticationHandler object which can be used to
     * authenticate within this realm.
     *
     * <P>This method return null always, since AuthenticationHandlers
     * are generally not supported by iAS realms. Subclass can override
     * if necessary.
     *
     * @return An AuthenticationHandler object for this realm (always null)
     *
     */
    public AuthenticationHandler getAuthenticationHandler()
    {
        _logger.warning("iasrealm.noauth");
        return null;
    }


    /**
     * Returns names of all the users in this particular realm.
     *
     * <P>This method always throws a BadRealmException since by default
     * this operation is not supported. Subclasses which support this
     * method can override.
     *
     * @return enumeration of user names (strings)
     * @exception com.sun.enterprise.security.auth.realm.BadRealmException if realm data structures are bad
     *
     */
    public Enumeration getUserNames() throws BadRealmException
    {
        String msg = sm.getString("iasrealm.notsupported");
        throw new BadRealmException(msg);
    }


    /**
     * Returns the information recorded about a particular named user.
     *
     * <P>This method always throws a BadRealmException since by default
     * this operation is not supported. Subclasses which support this
     * method can override.
     *
     * @param name name of the user whose information is desired
     * @return the user object
     * @exception com.sun.enterprise.security.auth.realm.NoSuchUserException if the user doesn't exist
     * @exception com.sun.enterprise.security.auth.realm.BadRealmException if realm data structures are bad
     *
     */
    public User getUser(String name)
        throws NoSuchUserException, BadRealmException
    {
        String msg = sm.getString("iasrealm.notsupported");
        throw new BadRealmException(msg);
    }


    /**
     * Returns names of all the groups in this particular realm.
     *
     * <P>This method always throws a BadRealmException since by default
     * this operation is not supported. Subclasses which support this
     * method can override.
     *
     * @return enumeration of group names (strings)
     * @exception com.sun.enterprise.security.auth.realm.BadRealmException if realm data structures are bad
     *
     */
    public Enumeration getGroupNames()
        throws BadRealmException
    {
        String msg = sm.getString("iasrealm.notsupported");
        throw new BadRealmException(msg);
    }


    /**
     * Refreshes the realm data so that new users/groups are visible.
     *
     * <P>This method always throws a BadRealmException since by default
     * this operation is not supported. Subclasses which support this
     * method can override.
     *
     * @exception com.sun.enterprise.security.auth.realm.BadRealmException if realm data structures are bad
     *
     */
    public void refresh() throws BadRealmException
    {
        String msg = sm.getString("iasrealm.notsupported");
        throw new BadRealmException(msg);
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
    public  void addUser(String name, char[] password, String[] groupList)
        throws BadRealmException, IASSecurityException  {
        String msg = sm.getString("iasrealm.notsupported");
        throw new BadRealmException(msg);
    }

    /**
     * Adds new user to file realm. User cannot exist already.
     *
     * Deprecated - User of the overloaded method with char[] password is encouraged
     */
    @Deprecated
    public  void addUser(String name, String password, String[] groupList)
        throws BadRealmException, IASSecurityException  {
        addUser(name, password.toCharArray(), groupList);

    }



    /**
     * Remove user from file realm. User must exist.
     *
     * @param name User name.
     * @throws NoSuchUserException If user does not exist.
     *
     */
     public void removeUser(String name)
        throws NoSuchUserException, BadRealmException {
        String msg = sm.getString("iasrealm.notsupported");
        throw new BadRealmException(msg);
     }

     /**
     * Update data for an existing user. User must exist.
     * Deprecated - User of the overloaded method with char[] password is encouraged
     *
     */
     @Deprecated
    public void updateUser(String name, String newName, String password,
                           String[] groups)
        throws NoSuchUserException, BadRealmException,
                               IASSecurityException {
        updateUser(name, newName, (password ==null)? null : password.toCharArray(), groups);

    }

     /**
     * Update data for an existing user. User must exist.
     *
     * @param name Current name of the user to update.
     * @param newName New name to give this user. It can be the same as
     *     the original name. Otherwise it must be a new user name which
     *     does not already exist as a user.
     * @param password Cleartext password for the user. If non-null the user
     *     password is changed to this value. If null, the original password
     *     is retained.
     * @param groupList List of groups to which user belongs.
     * @throws BadRealmException If there are problems adding user.
     * @throws NoSuchUserException If user does not exist.
     *
     */
    public void updateUser(String name, String newName, char[] password,
                           String[] groups)
        throws NoSuchUserException, BadRealmException,
                               IASSecurityException {
        String msg = sm.getString("iasrealm.notsupported");
        throw new BadRealmException(msg);
    }

    /**
     * @return true if the realm implementation support User Management (add,remove,update user)
     */
    public boolean supportsUserManagement() {
        //false by default.
        return false;
    }

   /**
    * Persist the realm data to permanent storage
    * @throws com.sun.enterprise.security.auth.realm.BadRealmException
    */
    public  void persist() throws BadRealmException {
        //NOOP for realms that do not support UserManagement
    }
}
