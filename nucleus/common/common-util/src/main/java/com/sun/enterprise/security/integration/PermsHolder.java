/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.integration;

import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

public class PermsHolder {

    /**
     * The PermissionCollection for each CodeSource
     */
    private ConcurrentHashMap<String, PermissionCollection> loaderPC = new ConcurrentHashMap<>();

    /**
     * EE permissions for a module
     */
    private PermissionCollection eePermissionCollection;

    /**
     * declared permissions in a module
     */
    private PermissionCollection declaredPermissionCollection;

    public PermsHolder() {

    }

    public PermsHolder(PermissionCollection eePC, PermissionCollection declPC, PermissionCollection restrictPC) {
        setEEPermissions(eePC);
        setDeclaredPermissions(declPC);
        setRestrictPermissions(restrictPC);
    }

    public void setEEPermissions(PermissionCollection eePc) {
        eePermissionCollection = eePc;
    }

    public void setDeclaredPermissions(PermissionCollection declaredPc) {
        declaredPermissionCollection = declaredPc;
    }

    public void setRestrictPermissions(PermissionCollection restrictPC) {
    }

    public PermissionCollection getCachedPerms(CodeSource codesource) {
        if (codesource == null) {
            return null;
        }

        return loaderPC.get(codesource.getLocation().toString());
    }

    public PermissionCollection getPermissions(CodeSource codesource, PermissionCollection parentPC) {
        String codeUrl = codesource.getLocation().toString();
        PermissionCollection cachedPermissons = loaderPC.get(codeUrl);

        if (cachedPermissons != null) {
            return cachedPermissons;
        }

        cachedPermissons = new Permissions();


        PermissionCollection permissionCollection = parentPC;

        if (permissionCollection != null) {
            Enumeration<Permission> permissions = permissionCollection.elements();
            while (permissions.hasMoreElements()) {
                cachedPermissons.add(permissions.nextElement());
            }
        }

        if (declaredPermissionCollection != null) {
            Enumeration<Permission> declaredPermissions = declaredPermissionCollection.elements();
            while (declaredPermissions.hasMoreElements()) {
                cachedPermissons.add(declaredPermissions.nextElement());
            }
        }

        if (eePermissionCollection != null) {
            Enumeration<Permission> eePermissions = eePermissionCollection.elements();
            while (eePermissions.hasMoreElements()) {
                cachedPermissons.add(eePermissions.nextElement());
            }

        }

        PermissionCollection tmpPermissionCollection = loaderPC.putIfAbsent(codeUrl, cachedPermissons);
        if (tmpPermissionCollection != null) {
            cachedPermissons = tmpPermissionCollection;
        }

        return cachedPermissons;

    }
}
