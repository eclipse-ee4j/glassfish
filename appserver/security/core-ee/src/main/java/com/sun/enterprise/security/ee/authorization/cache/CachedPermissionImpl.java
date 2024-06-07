/*
 * Copyright (c) 024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.ee.authorization.cache;

import java.security.Permission;

/**
 * This class is
 *
 * @author Ron Monzillo
 */

public class CachedPermissionImpl implements CachedPermission {

    PermissionCache permissionCache;
    Permission permission;
    Epoch epoch;

    public CachedPermissionImpl(PermissionCache c, Permission p) {
        this.permissionCache = c;
        this.permission = p;
        epoch = new Epoch();
    }

    @Override
    public Permission getPermission() {
        return this.permission;
    }

    @Override
    public PermissionCache getPermissionCache() {
        return this.permissionCache;
    }

    // synchronization done in PermissionCache
    @Override
    public boolean checkPermission() {
        boolean granted = false;
        if (permissionCache != null) {
            granted = permissionCache.checkPermission(this.permission, this.epoch);
        }

        return granted;
    }

    // used to hold last result obtained from cache and cache epoch.
    // epoch is used by PermissionCache to determine when result is out of date.
    static class Epoch {

        int epoch;
        boolean granted;

        Epoch() {
            this.epoch = 0;
            this.granted = false;
        }
    }

}
