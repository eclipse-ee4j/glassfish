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

import com.sun.enterprise.deployment.web.SecurityRole;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.security.common.Role;

/**
 * I am an abstract role..
 *
 * @author Danny Coward
 */
public class SecurityRoleDescriptor extends Descriptor implements SecurityRole {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a SecurityRoleDescriptor from the given role name and description.
     */
    public SecurityRoleDescriptor(String name, String description) {
        super(name, description);
    }


    /**
     * Construct a SecurityRoleDescriptor from the given role object.
     */
    public SecurityRoleDescriptor(Role role) {
        super(role.getName(), role.getDescription());
    }


    /**
     * Default constructor.
     */
    public SecurityRoleDescriptor() {
    }


    /**
     * Equality on rolename.
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof SecurityRoleDescriptor
            && this.getName().equals(((SecurityRoleDescriptor) other).getName())) {
            return true;
        }

        return false;
    }


    /**
     * My hashcode.
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }


    /**
     * Formatted string representing my state.
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("SecurityRole ");
        super.print(toStringBuffer);
    }

}
