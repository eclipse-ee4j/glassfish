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

package com.sun.enterprise.security.jacc.provider;

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Set;

/**
 *
 * @author monzillo
 */
public class Role {

    String roleName;
    Permissions permissions;
    Set<Principal> principals;
    private boolean isAnyAuthenticatedUserRole = false;

    public Role(String name) {
        roleName = name;
    }

    /**
     * NB: Class Overrides equals and hashCode Methods such that 2 Roles are equal simply based on having a common name.
     * 
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        Role other = (o == null || !(o instanceof Role) ? null : (Role) o);
        return (o == null ? false : getName().equals(other.getName()));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.roleName != null ? this.roleName.hashCode() : 0);
        return hash;
    }

    public String getName() {
        return roleName;
    }

    void addPermission(Permission p) {
        if (permissions == null) {
            permissions = new Permissions();
        }
        permissions.add(p);
    }

    void addPermissions(PermissionCollection pc) {
        if (permissions == null) {
            permissions = new Permissions();
        }
        for (Enumeration<Permission> e = pc.elements(); e.hasMoreElements();) {
            permissions.add(e.nextElement());
        }
    }

    Permissions getPermissions() {
        return permissions;
    }

    void setPrincipals(Set<Principal> pSet) {
        if (pSet != null) {
            principals = pSet;
        }
    }

    boolean implies(Permission p) {
        boolean rvalue = false;
        if (permissions != null) {
            rvalue = permissions.implies(p);
        }
        return rvalue;
    }

    void determineAnyAuthenticatedUserRole() {
        isAnyAuthenticatedUserRole = false;
        // If no princiapls are present then any authenticated user is possible
        if ((principals == null) || principals.isEmpty()) {
            isAnyAuthenticatedUserRole = true;
        }
    }

    boolean isAnyAuthenticatedUserRole() {
        return isAnyAuthenticatedUserRole;
    }

    boolean isPrincipalInRole(Principal p) {
        if (isAnyAuthenticatedUserRole && (p != null)) {
            return true;
        }

        boolean rvalue = false;
        if (principals != null) {
            rvalue = principals.contains(p);
        }
        return rvalue;
    }

    boolean arePrincipalsInRole(Principal subject[]) {
        if (subject == null || subject.length == 0) {
            return false;
        }
        if (isAnyAuthenticatedUserRole) {
            return true;
        }
        if (principals == null || principals.isEmpty()) {
            return false;
        }

        boolean rvalue = false;
        for (Principal p : subject) {
            if (principals.contains(p)) {
                rvalue = true;
                break;
            }
        }
        return rvalue;
    }
}
