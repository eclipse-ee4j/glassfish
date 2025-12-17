/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Hashtable;
import java.util.Iterator;

/**
 * This class is the factory for creating and managing PermissionCache.
 *
 * @author Shing Wai Chan
 */

public class PermissionCacheFactory {

    private static final Hashtable cacheMap = new Hashtable();
    private static int factoryKey = 0;
    private static boolean supportsReuse = false;


    /**
     * Reserve the next Cache Key for subsequent registration.
     *
     * @return the key as an Integer object.
     */
    private static Integer getNextKey() {
        Integer key = factoryKey++;

        while (cacheMap.get(key) != null) {
            key = factoryKey++;
        }

        return key;
    }

    /**
     * Create a PermissionCache object. If the corresponding object exists, then it will overwrite the previous one.
     *
     * @param contextId - a string identifying the policy context and which must be set when getPermissions is called (internally). This
     * value may be null, in which case the permisions of the default policy context will be cached.
     * @param perms - an array of Permission objects identifying the permission types that will be managed by the cache. This value
     * may be null, in which case all permissions obtained by the getPermissions call will be cached.
     * @param name - a string corresponding to a value returned by Permission.getName() only permissions whose getName() value
     * matches the name parameter will be included in the cache. This value may be null, in which case permission name dos not factor
     * into the permission caching.
     */
    public static synchronized PermissionCache createPermissionCache(String contextId, Permission[] perms, String name) {
        if (!supportsReuse) {
            return null;
        }

        return registerPermissionCache(new PermissionCache(getNextKey(), contextId, perms, name));
    }

    /**
     * Create a PermissionCache object. If the corresponding object exists, then it will overwrite the previous one.
     *
     * @param contextId - a string identifying the policy context and which must be set when getPermissions is called (internally). This
     * value may be null, in which case the permisions of the default policy context will be cached.
     * @param codesource - the codesource argument to be used in the call to getPermissions. This value may be null.
     * @param clazz - a class object identifying the permission type that will be managed by the cache. This value may be null, in
     * which case all permissions obtained by the getPermissions call will be cached.
     * @param name - a string corresponding to a value returned by Permission.getName() only permissions whose getName() value
     * matches the name parameter will be included in the cache. This value may be null, in which case permission name dos not factor
     * into the permission caching.
     */
    public static synchronized PermissionCache createPermissionCache(String contextId, Class<?> clazz, String name) {
        if (!supportsReuse) {
            return null;
        }

        return registerPermissionCache(new PermissionCache(getNextKey(), contextId, clazz, name));
    }

    /**
     * Register a PermissionCache object with the factory. If an object is already registered at the key, it will be overidden.
     *
     * @param cache a cache with an internal key value.
     * @return the cache object
     */
    private static PermissionCache registerPermissionCache(PermissionCache cache) {
        cacheMap.put(cache.getFactoryKey(), cache);

        return cache;
    }

    public static synchronized PermissionCache removePermissionCache(PermissionCache cache) {
        PermissionCache permissionCache = null;

        if (cache != null) {
            Object value = cacheMap.remove(cache.getFactoryKey());

            if (value != null && value instanceof PermissionCache) {
                permissionCache = (PermissionCache) value;
                permissionCache.reset();
            }
        }

        return permissionCache;
    }

    /**
     * This resets all caches inside the factory.
     */
    public static synchronized void resetCaches() {
        supportsReuse = true;

        Iterator iter = cacheMap.values().iterator();
        while (iter.hasNext()) {
            Object cache = iter.next();
            if (cache instanceof PermissionCache) {
                ((PermissionCache) cache).reset();
            }
        }
    }
}
