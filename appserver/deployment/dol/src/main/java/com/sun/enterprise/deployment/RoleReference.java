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
import com.sun.enterprise.deployment.web.SecurityRoleReference;

import org.glassfish.security.common.Role;

/**
 * Special kind of environment property that encapsulates the primitive roles
 * as defined by the bean developer. The name of a primitive role will appear
 * in the bean code, the value will be mapped to the name of a Role chosen by
 * the application assembler which is referenced by the EjbBundle being
 * assembled.
 *
 * @author Danny Coward
 */
public class RoleReference extends EnvironmentProperty implements SecurityRoleReference {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public RoleReference() {
    }


    /**
     * Construct a role reference from the given name and description.
     */
    public RoleReference(String name, String description) {
        super(name, "", description);
    }

    /**
     * Set the value for the reference.
     *
     * @param the role
     */
    void setRole(Role role) {
        super.setValue(role.getName());
    }

    /**
     * Return the role object from this descriptor.
     *
     * @return the role.
     */
    public Role getRole() {
        return new Role(super.getValue());
    }

    /**
     * Return the rolename.
     *
     * @return the role name.
     */
    @Override
    public SecurityRole getSecurityRoleLink() {
        return new SecurityRoleDescriptor(super.getValue(), "");
    }

    /**
     * Sets the rolename.
     *
     * @param the rolename.
     */
    @Override
    public void setSecurityRoleLink(SecurityRole securityRole) {
        super.setValue(securityRole.getName());
    }

    /**
     * Return the coded name.
     *
     * @return the role name used in the bean code.
     */
    @Override
    public String getRoleName() {
        return this.getName();
    }

    /**
     * Sets the coded name.
     *
     * @param the role name used in the bean code.
     */
    @Override
    public void setRoleName(String rolename) {
        this.setName(rolename);
    }

    /**
     * Returns a formatted version of this object as a String.
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("Role-Ref-Env-Prop: ").append(super.getName()).append("@").append(this.getRole())
            .append("@").append(super.getDescription());
    }
}

