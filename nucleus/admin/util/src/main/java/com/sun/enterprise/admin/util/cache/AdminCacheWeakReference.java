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

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link AdminCache} based on week references and backgrounded by {@link AdminCacheFileStore} layer. <br/>
 * Max one object representation of cached data are stored by this implementation. If different type is requested, it
 * will be reloaded from file store using {@code AdminCacheFileStore}.<br/>
 *
 * @author mmares
 */
public class AdminCacheWeakReference implements AdminCache {

    private final static class CachedItem {

        private WeakReference item;
        private long updated = -1;
        private Date lastUpdateInStore;

        private CachedItem(final Object item) {
            setItem(item);
        }

        private CachedItem(final Object item, final Date lastUpdateInStore) {
            setItem(item);
            this.lastUpdateInStore = lastUpdateInStore;
        }

        private CachedItem(final Date lastUpdateInStore) {
            this.lastUpdateInStore = lastUpdateInStore;
        }

        public Object getItem() {
            if (item == null) {
                return null;
            } else {
                return item.get();
            }
        }

        public void setItem(Object item) {
            if (item == null) {
                this.item = null;
            } else {
                this.item = new WeakReference(item);
            }
            this.updated = System.currentTimeMillis();
        }

        public Date getLastUpdateInStore() {
            return lastUpdateInStore;
        }

        public void setLastUpdateInStore(Date lastUpdateInStore) {
            this.lastUpdateInStore = lastUpdateInStore;
        }

        public long getUpdated() {
            return updated;
        }

    }

    private static final AdminCacheWeakReference instance = new AdminCacheWeakReference();

    private AdminCacheFileStore fileCache = AdminCacheFileStore.getInstance();
    private final Map<String, CachedItem> cache = new HashMap<String, CachedItem>();

    private AdminCacheWeakReference() {
    }

    @Override
    public <A> A get(final String key, final Class<A> clazz) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Attribute key must be unempty.");
        }
        if (clazz == null) {
            throw new IllegalArgumentException("Attribute clazz can not be null.");
        }
        CachedItem cachedItem = cache.get(key);
        if (cachedItem != null) {
            Object obj = cachedItem.getItem();
            if (obj != null) {
                if (clazz.isAssignableFrom(obj.getClass())) {
                    return (A) obj;
                }
            }
        }
        //Not in local cache => load from underliing
        A item = fileCache.get(key, clazz);
        if (item != null) {
            if (cachedItem == null) {
                cache.put(key, new CachedItem(item));
            } else {
                cachedItem.setItem(item);
            }
        } else {
            cache.remove(key);
        }
        return item;
    }

    @Override
    public void put(final String key, final Object data) {
        fileCache.put(key, data);
        cache.put(key, new CachedItem(data, new Date()));
    }

    @Override
    public boolean contains(final String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Attribute key must be unempty.");
        }
        CachedItem item = cache.get(key);
        if (item != null) {
            return true;
        } else {
            boolean result = fileCache.contains(key);
            if (result) {
                cache.put(key, new CachedItem(null));
            }
            return result;
        }
    }

    @Override
    public Date lastUpdated(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Attribute key must be unempty.");
        }
        CachedItem item = cache.get(key);
        if (item != null && item.lastUpdateInStore != null) {
            return item.lastUpdateInStore;
        }
        Date result = fileCache.lastUpdated(key);
        if (result != null) {
            if (item == null) {
                cache.put(key, new CachedItem(result));
            } else {
                if (item.updated != -1 && item.updated < result.getTime()) {
                    item.setItem(null); //Cleare it because it was changed after load
                }
                item.lastUpdateInStore = result;
            }
        }
        return result;
    }

    public static AdminCacheWeakReference getInstance() {
        return instance;
    }

}
