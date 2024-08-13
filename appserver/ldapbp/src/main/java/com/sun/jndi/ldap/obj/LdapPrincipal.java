/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jndi.ldap.obj;

import com.sun.jndi.ldap.LdapName;

import java.security.Principal;

import javax.naming.NamingException;

/**
 * A principal from the LDAP directory.
 *
 * @author Vincent Ryan
 */
class LdapPrincipal implements Principal {

    private LdapName ldapName = null;
    private int hash = -1;
    private String name;

    /**
     * Create a principal.
     *
     * @param name The principal's string name.
     */
    public LdapPrincipal(String name) {
        this.name = name;
    }


    /**
     * Compares this principal to the specified object.
     *
     * @param object The object to compare this principal against.
     * @return true if they are equal; false otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (ldapName == null) {
            ldapName = getLdapName(name);
        }
        // this is a broken equals implementation. It doesnot have any dependency on Glassfish code
        /*
         * if (object instanceof String) {
         * return ldapName.equals(getLdapName((String)object));
         * }
         */
        if (object instanceof Principal) {
            return ldapName.equals(getLdapName(((Principal) object).getName()));
        }
        return false;
    }


    /**
     * Returns a hash code for this principal.
     *
     * @return The principal's hash code.
     */
    @Override
    public int hashCode() {
        if (hash == -1) {
            if (ldapName == null) {
                ldapName = getLdapName(name);
            }
            hash = ldapName.hashCode();
        }
        return hash;
    }


    /**
     * Returns the name of this principal.
     *
     * @return String The principal's string name.
     */
    @Override
    public String getName() {
        return name;
    }


    /**
     * Returns a string representation of this principal.
     *
     * @return String The principal's string name.
     */
    @Override
    public String toString() {
        return name;
    }


    private LdapName getLdapName(String name) {
        LdapName ldapName = null;
        try {
            ldapName = new LdapName(name);
        } catch (NamingException e) {
            // ignore
        }
        return ldapName;
    }
}
