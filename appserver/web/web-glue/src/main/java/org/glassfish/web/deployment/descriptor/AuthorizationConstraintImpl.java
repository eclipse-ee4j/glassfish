/*
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
package org.glassfish.web.deployment.descriptor;

import com.sun.enterprise.deployment.SecurityRoleDescriptor;
import com.sun.enterprise.deployment.web.AuthorizationConstraint;
import com.sun.enterprise.deployment.web.SecurityRole;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.deployment.common.Descriptor;

import static java.util.Collections.enumeration;

/**
 * This descriptor represents an authorization contraint on a security constraint in a web application.
 *
 * @author Danny Coward
 */
public class AuthorizationConstraintImpl extends Descriptor implements AuthorizationConstraint {

    private static final long serialVersionUID = 1L;

    private Set<SecurityRole> securityRoles;

    /**
     * Default constructor that creates an AuthorizationConstraint with no roles.
     */
    public AuthorizationConstraintImpl() {
    }

    /**
     * Copy constructor.
     */
    public AuthorizationConstraintImpl(AuthorizationConstraintImpl other) {
        this.securityRoles = new HashSet<>(other.getSecurityRoleSet());
    }

    /**
     * Return the set of roles.
     */
    private Set<SecurityRole> getSecurityRoleSet() {
        if (securityRoles == null) {
            securityRoles = new HashSet<>();
        }

        return securityRoles;
    }

    /**
     * Return the security roles involved in this constraint. The enumeration is empty if there are none.
     *
     * @return the enumeration of security roles in this constraint.
     */
    @Override
    public Enumeration<SecurityRole> getSecurityRoles() {
        if (securityRoles == null) {
            securityRoles = new HashSet<>();
        }

        return enumeration(getSecurityRoleSet());
    }

    /**
     * Adds a role to the authorization constraint.
     *
     * @param the role to be added.
     */
    @Override
    public void addSecurityRole(SecurityRole securityRole) {
        getSecurityRoleSet().add(securityRole);
    }

    /**
     * Adds a role to the authorization constraint
     *
     * @param the role name to be added
     */
    public void addSecurityRole(String roleName) {
        SecurityRoleDescriptor securityRoleDescriptor = new SecurityRoleDescriptor();
        securityRoleDescriptor.setName(roleName);
        addSecurityRole(securityRoleDescriptor);
    }

    /**
     * Removes the given role from the authorization constraint.
     *
     * @param the role to be removed.
     */
    public void removeSecurityRole(SecurityRole securityRole) {
        getSecurityRoleSet().remove(securityRole);
    }

    /**
     * Prints a formatted representation of this object.
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("AuthorizationConstraint ");
        super.print(toStringBuffer);
        toStringBuffer.append(" securityRoles ").append(this.securityRoles);
    }
}
