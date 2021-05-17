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

package org.glassfish.security.common;

import java.security.Principal;

/**
 * This class implements the principal interface.
 *
 * @author Harish Prabandham
 */
public class PrincipalImpl implements Principal, java.io.Serializable {

    /**
     * @serial
     */
    private String name;

     /**
     * Construct a principal from a string user name.
     * @param user The string form of the principal name.
     */
    public PrincipalImpl(String user) {
    this.name = user;
    }

    /**
     * This function returns true if the object passed matches
     * the principal represented in this implementation
     * @param another the Principal to compare with.
     * @return true if the Principal passed is the same as that
     * encapsulated in this object, false otherwise
     */
    public boolean equals(Object another) {
        // XXX for bug 4889642: if groupA and userA have
        // the same name, then groupA.equals(userA) return false
        // BUT userA.equals(groupA) return "true"
        if (another instanceof Group) {
            return false;
        } else if (another instanceof PrincipalImpl) {
        Principal p = (Principal) another;
        return getName().equals(p.getName());
    } else
        return false;
    }

    /**
     * Prints a stringified version of the principal.
     * @return A java.lang.String object returned by the method getName()
     */
    @Override
    public String toString() {
    return getName();
    }

    /**
     * Returns the hashcode for this Principal object
     * @return a hashcode for the principal.
     */
    @Override
    public int hashCode() {
    return name.hashCode();
    }

    /**
     * Gets the name of the Principal as a java.lang.String
     * @return the name of the principal.
     */
    public String getName() {
    return name;
    }

}
