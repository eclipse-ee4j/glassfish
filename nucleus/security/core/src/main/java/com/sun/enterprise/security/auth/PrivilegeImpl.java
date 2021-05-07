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

package com.sun.enterprise.security.auth;

import java.rmi.*;


/**
 * This method provides an implementation a Privilege
 * @author Harish Prabandham
 */

public class PrivilegeImpl implements Privilege {
    private String name;

    /**
     * Creates a new Privilege object..
     *
     */
    public PrivilegeImpl(String name) {
        this.name = name;
    }

    /**
     * Returns the hashCode ..
     */
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Returns the name of the name of the Privilege.
     * @return The name of the name of the Privilege.
     */
    public String getName() {
        return name;
    }

    /**
     * Compares equality...
     */
    public boolean equals(Object obj) {
        if(obj instanceof Privilege) {
            Privilege priv = (Privilege) obj;
            return getName().equals(priv.getName());
        } else {
            return false;
        }
    }
}
