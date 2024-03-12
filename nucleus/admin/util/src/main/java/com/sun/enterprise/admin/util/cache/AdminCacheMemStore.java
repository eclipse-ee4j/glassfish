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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * In memory {@link AdminCache} containing fixed amount of items. Rotation is based on last update first out.<br/>
 * This implementation is backgrounded by {@link AdminCacheWeakReference} and all non locally cached items are searched
 * from that implementation.
 *
 * @author mmares
 */
public class AdminCacheMemStore implements AdminCache {

    private final static class CachedItem implements Comparable<CachedItem> {

        private Object item;
        private long touched;

        private CachedItem(Object item) {
            this.item = item;
            this.touched = System.currentTimeMillis();
        }

        private Object getItem() {
            this.touched = System.currentTimeMillis();
            return this.item;
        }

        @Override
        public int compareTo(CachedItem o) {
            return (int) (this.touched - o.touched);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof CachedItem))
                return false;
            return compareTo((CachedItem) o) == 0;
        }

        @Override
        public int hashCode() {
            return (int) touched;
        }
    }

    private static final AdminCacheMemStore instance = new AdminCacheMemStore();

    /**
     * Maximal count of items in cache. Rotation is based on last used first out.
     */
    private static final int MAX_CACHED_ITEMS_COUNT = 16;

    private final Map<String, CachedItem> cache = new HashMap<String, CachedItem>(MAX_CACHED_ITEMS_COUNT + 1);
    private AdminCacheWeakReference underCache = AdminCacheWeakReference.getInstance();

    private AdminCacheMemStore() {
    }

    @Override
    public <A> A get(String key, final Class<A> clazz) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Attribute key must be unempty.");
        }
        if (clazz == null) {
            throw new IllegalArgumentException("Attribute clazz can not be null.");
        }
        CachedItem ci = cache.get(key);
        if (ci == null) {
            A item = underCache.get(key, clazz);
            if (item != null) {
                putToLocalCache(key, new CachedItem(item));
            }
            return item;
        } else {
            return (A) ci.getItem();
        }
    }

    @Override
    public void put(String key, final Object data) {
        underCache.put(key, data);
        putToLocalCache(key, new CachedItem(data));
    }

    private synchronized void putToLocalCache(String key, final CachedItem ci) {
        if (cache.size() >= MAX_CACHED_ITEMS_COUNT) {
            CachedItem oldest = null;
            Collection<CachedItem> values = cache.values();
            for (CachedItem item : values) {
                if (oldest == null) {
                    oldest = item;
                    continue;
                }
                if (item.touched < oldest.touched) {
                    oldest = item;
                }
            }
            values.remove(oldest);
        }
        cache.put(key, ci);
    }

    @Override
    public boolean contains(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Attribute key must be unempty.");
        }
        CachedItem item = cache.get(key);
        if (item != null) {
            item.getItem(); //Just for touch
            return true;
        }
        return underCache.contains(key);
    }

    @Override
    public Date lastUpdated(String key) {
        return underCache.lastUpdated(key);
    }

    public static AdminCacheMemStore getInstance() {
        return instance;
    }

}
