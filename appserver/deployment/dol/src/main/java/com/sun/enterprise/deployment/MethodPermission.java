/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import java.util.Objects;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.security.common.Role;

/**
 * Represents a method permission. A method permission can be associated to
 * a role, be PERMIT_ALL or DENY_ALL.
 *
 * @author Jerome Dochez
 */
public class MethodPermission extends Descriptor {

    private static final long serialVersionUID = 1L;
    private static final MethodPermission PERMIT_ALL = new MethodPermission(true, false);
    private static final MethodPermission DENY_ALL = new MethodPermission(false, true);
    private boolean isUnchecked;
    private boolean isExcluded;
    private final Role role;

    /**
     * construct a new MethodPermission based on a security role
     *
     * @param role the security role associated to the method permission
     */
    public MethodPermission(Role role) {
        this.role = Objects.requireNonNull(role);
    }


    private MethodPermission(boolean unchecked, boolean excluded) {
        this.isUnchecked = unchecked;
        this.isExcluded = excluded;
        this.role = null;
    }


    /**
     * @return an PERMIT_ALL (unchecked in XML) method permission.
     *         Methods associated with such a method permission can be invoked by anyone
     */
    public static MethodPermission getPermitAllMethodPermission() {
        return PERMIT_ALL;
    }


    /**
     * @return an DENY_ALL method permission.
     *         Methods associated with such a method permission cannot be invoked by anyone.
     */
    public static MethodPermission getDenyAllMethodPermission() {
        return DENY_ALL;
    }


    /**
     * @return true if the method permission is based on a security role
     */
    public boolean isRoleBased() {
        return role != null;
    }


    /**
     * @return true if the method permission is PERMIT_ALL (unchecked in XML)
     */
    public boolean isUnchecked() {
        return isUnchecked;
    }


    /**
     * @return true if the method permission is DENY_ALL
     */
    public boolean isExcluded() {
        return isExcluded;
    }


    /**
     * @return the security role associated with this method permission when
     *         applicable (role based method permission)
     */
    public Role getRole() {
        return role;
    }


    @Override
    public int hashCode() {
        // hashCode must honor same rules as equals.
        if (isRoleBased()) {
            return Objects.hashCode(role);
        }
        return Objects.hash(isExcluded, isUnchecked);
    }


    @Override
    public boolean equals(Object other) {
        boolean ret = false;
        if (other instanceof MethodPermission) {
            MethodPermission o = (MethodPermission) other;
            if (isRoleBased()) {
                ret = role.equals(o.getRole());
            } else {
                ret = isExcluded == o.isExcluded() && isUnchecked == o.isUnchecked();
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
                toStringBuffer.append("DENY_ALL");
            } else if (isUnchecked) {
                toStringBuffer.append("PERMIT_ALL");
            }
        }
    }
}
