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

package com.sun.enterprise.deployment;

import org.glassfish.security.common.PrincipalImpl;

/**
 * This class encapsulates the Resource Principal information needed
 * to access the Resource.
 *
 * @author Tony Ng
 */
public class ResourcePrincipal extends  PrincipalImpl {
    private String password;

    static private final int NULL_HASH_CODE = Integer.valueOf(1).hashCode();

    public ResourcePrincipal(String name, String password) {
        super(name);
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof ResourcePrincipal) {
            ResourcePrincipal other = (ResourcePrincipal) o;
            return ((isEqual(getName(), other.getName())) &&
                    (isEqual(this.password, other.password)));
        }
        return false;
    }

    public int hashCode() {
        int result = NULL_HASH_CODE;
        String name = getName();
        if (name != null) {
            result += name.hashCode();
        }
        if (password != null) {
            result += password.hashCode();
        }
        return result;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null) {
            return (b == null);
        } else {
            return (a.equals(b));
        }
    }

}
