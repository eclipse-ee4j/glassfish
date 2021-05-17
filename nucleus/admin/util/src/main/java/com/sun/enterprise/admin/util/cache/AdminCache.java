/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.util.cache;

import java.util.Date;

/**
 * Simple cache for administration framework.
 *
 * @author mmares
 */
public interface AdminCache {

    /**
     * Retrieve data from cache.
     *
     * @param key in the cache
     * @param clazz Cache data will be converted to requested type using appropriate {@code AdminCacheObjectProvider}
     * @return Cached data converted to requested type or {@code null} if not cached
     */
    public <A> A get(String key, Class<A> clazz);

    /**
     * Puts data to cache.
     *
     * @param key in the cache
     * @param data Cached data will be converted to raw bytes using appropriate {@code AdminCacheObjectProvider}
     * @return Cached data converted to requested type or {@code null} if not cached
     */
    public void put(String key, Object data);

    /**
     * Checked if content is in the cache.
     *
     * @param key in the cache
     */
    public boolean contains(String key);

    /**
     * Date when was cached data last updated.
     *
     * @param key in the cache
     * @return {@code Date} of last update or null if does not exist in cache
     */
    public Date lastUpdated(String key);

}
