/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.security.auth.realm.exceptions.NoSuchRealmException;
import java.security.Principal;
import java.util.Enumeration;

/**
 * All users are principals ... perhaps in the native OS, perhaps not.
 *
 * <P>
 * Users always have authentication information, which is used to validate a user's proferred credentials. Different
 * kinds of realms use different kinds of authentication information. For example, realms could use X.509 public key
 * certificates, shared passphrases, encrypted passwords, smart cards, or biometric data to figure out if the user's
 * credentials are valid.
 *
 * <P>
 * Users typically have attributes that identify privileges granted/possesed by the user.
 *
 * @author Harish Prabandham
 */
public interface User extends Principal {

    /**
     * Returns the realm with which this user is associated.
     */
    Realm getRealm() throws NoSuchRealmException;

    /**
     * Returns the single requested attribute for the user.
     *
     * @param name string identifying the attribute.
     * @return value of that attribute, or null if no value has been defined
     */
    Object getAttribute(String name);

    /**
     * Returns an enumeration of the keys for the attributes supported for this user.
     */
    Enumeration<String> getAttributeNames();
}
