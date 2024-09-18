/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.auth.login.common;

import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class holds the user password for the shared password realm and the realm name.
 * This credential is added as a private credential to the JAAS subject.
 */
public class PasswordCredential {
    private String username;
    //   private String password;
    private char[] password;
    private String realm;
    private boolean readOnly;

    // target_name is filled in by the SecSecurityServer interceptor
    // only when a CSIv2 GSSUP authenticator is received.
    private byte[] target_name = {};

    /**
     * Construct a credential with the specified password and realm name.
     *
     * @param user username
     * @param password null or user password
     * @param realm Realm  name. The only value supported for now is "default".
     */
    public PasswordCredential(String user, char[] password, String realm) {
        this.username = user;
        //Copy the password to another reference before storing it to the
        //instance field.
        char[] passwordCopy = (password == null) ? null : Arrays.copyOf(password, password.length);
        this.password = passwordCopy;
        this.realm = realm;

        if (this.username == null) {
            this.username = "";
        }
        if (this.password == null) {
            this.password = new char[] {};
        }
        if (this.realm == null) {
            this.realm = "";
        }
    }

    /**
     * If created on the server side the instance is readonly
     */
    public PasswordCredential(String user, char[] password, String realm, byte[] target_name) {
        this(user, password, realm);
        this.target_name = target_name;
        readOnly = true;
    }

    /**
     * Return the realm name.
     *
     * @return the realm name. Only value supported for now is "default".
     */
    public String getRealm() {
        return realm;
    }

    /**
     * Return the username.
     *
     * @return the user name.
     */
    public String getUser() {
        return username;
    }

    public void setRealm(String realm) {
        if (!readOnly) {
            this.realm = realm;
        }
    }

    /**
     * Return the password.
     *
     * @return the password.
     */
    public char[] getPassword() {
        //Copy the password to another reference before returning it
        char[] passwordCopy = (password == null) ? null : Arrays.copyOf(password, password.length);
        return passwordCopy;
    }

    /**
     * Return the target_name
     *
     * @return the target_name
     */
    public byte[] getTargetName() {
        return this.target_name;
    }

    /**
     * Compare two instances of the credential and return true if they have equal realm username and password.
     *
     * @param o the object that this instance is being compared to.
     * @return true if the instances are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof PasswordCredential) {
            PasswordCredential pc = (PasswordCredential) o;
            if (pc.getUser().equals(username) && Arrays.equals(pc.getPassword(), password) && pc.getRealm().equals(realm)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the hashCode computed from the password and realm name.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return username.hashCode() + Arrays.hashCode(password) + realm.hashCode();
    }

    /**
     * The string representation of the credential.
     */
    @Override
    public String toString() {
        String s = "Realm=" + realm;
        s = s + " Username=" + username;
        s = s + " Password=" + "########";
        s = s + " TargetName = " + new String(target_name, UTF_8);
        return s;
    }

}
