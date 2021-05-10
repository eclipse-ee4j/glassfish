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

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.security.common.Role;

/**
 * Represents a method permission. A method permission can be associated to
 * a role, be unchecked or excluded.
 *
 * @author  Jerome Dochez
 * @version
 */
public class MethodPermission extends Descriptor {

    private static MethodPermission unchecked;
    private static MethodPermission excluded;
    private boolean isUnchecked = false;
    private boolean isExcluded = false;
    private Role role;

    /**
     * construct a new MethodPermission based on a security role
     *
     * @param role the security role associated to the method permission
     */
    public MethodPermission(Role role) {
        this.role = role;
    }

    // We don't want uninitialized method permissins
    private MethodPermission() {
    }

    /**
     * @return an unchecked method permission. Methods associated with such a
     * method permission can be invoked by anyone
     */
    public static synchronized MethodPermission getUncheckedMethodPermission() {
        if (unchecked==null) {
            unchecked = new MethodPermission();
            unchecked.isUnchecked=true;
        }
        return unchecked;
    }

    /**
     * @return an ecluded method permission. Methods associated with such a
     * method permission cannot be invoked by anyone.
     */
    public static synchronized MethodPermission getExcludedMethodPermission() {
        if (excluded==null) {
            excluded = new MethodPermission();
            excluded.isExcluded=true;
        }
        return excluded;
    }

    /**
     * @return true if the method permission is based on a security role
     */
    public boolean isRoleBased() {
        return role!=null;
    }

    /**
     * @return true if the method permission is unchecked
     */
    public boolean isUnchecked() {
        return isUnchecked;
    }

    /**
     * @return true if the method permission is excluded
     */
    public boolean isExcluded() {
        return isExcluded;
    }

    /**
     * @return the security role associated with this method permission when
     * applicable (role based method permission)
     */
    public Role getRole() {
        return role;
    }

    // For Map storage
    @Override
    public int hashCode() {
        if (role!=null) {
            return role.hashCode();
        } else {
            return super.hashCode();
        }
    }

    // for Map storage
    @Override
    public boolean equals(Object other) {
        boolean ret = false;
        if (other instanceof MethodPermission) {
            MethodPermission o = (MethodPermission) other;
            if (isRoleBased()) {
                ret = role.equals(o.getRole());
            } else {
                ret = (isExcluded == o.isExcluded()) && (isUnchecked == o.isUnchecked());
            }
        }
        return ret;
    }

    @Override
    public void print(StringBuffer toStringBuffer) {
        if (isRoleBased()) {
            toStringBuffer.append(role.toString());
        } else {
            if (isExcluded) {
                toStringBuffer.append("excluded");
            } else {
                toStringBuffer.append("unchecked");
            }
        }
    }
}

