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

package com.sun.enterprise.security.auth.realm.file;

import java.util.*;

import com.sun.enterprise.security.auth.realm.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.security.auth.realm.User;
import org.glassfish.security.common.FileRealmHelper;


/**
 * Represents a FileRealm user.
 *
 *
 */
public class FileRealmUser implements User
{
    FileRealmHelper.User user;
    Hashtable attributes = new Hashtable();
    String realm;
    /**
     * Constructor.
     *
     */
    public FileRealmUser(FileRealmHelper.User user, String realm)
    {
        this.user = user;
        this.realm = realm;
    }

    public String[] getGroups() {
        return user.getGroups();
    }

    /**
     * Returns the realm with which this user is associated
     *
     * @return Realm name.
     * @exception NoSuchRealmException if the realm associated this user
     *            no longer exist
     *
     */
    @Override
    public Realm getRealm() throws NoSuchRealmException
    {
        return Realm.getInstance(realm);
    }

    /**
     * Return the requested attribute for the user.
     * <P>Not really needed.
     *
     * @param key string identifies the attribute.
     */
    @Override
    public Object getAttribute (String key)
    {
        return attributes.get(key);
    }

    /**
     * Return the names of the supported attributes for this user.
     * <P>Not really needed.
     */
    @Override
    public Enumeration getAttributeNames () {
        return attributes.keys();
    }

    @Override
    public String getName() {
        return user.getName();
    }
}
