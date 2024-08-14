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

package com.sun.ejb.containers.util.cache;

import com.sun.ejb.spi.container.SFSBContainerCallback;

import java.util.Properties;

public class NRUSessionCache
    extends LruSessionCache
{

    protected boolean doOrdering = false;
    protected int orderingThreshold = 0;

    public NRUSessionCache(String cacheName,
        SFSBContainerCallback container, int cacheIdleTime, int removalTime)
    {
        super("NRU-" + cacheName, container, cacheIdleTime, removalTime);
    }

    public void init(int maxEntries, float loadFactor, Properties props) {
        super.init(maxEntries, loadFactor, props);
        orderingThreshold = (int) (0.75 * threshold);
    }

    protected CacheItem itemAdded(CacheItem item) {
        CacheItem addedItem = super.itemAdded(item);
        doOrdering = (entryCount >= orderingThreshold);
        return addedItem;
    }

    protected void itemAccessed(CacheItem item) {
        LruCacheItem lc = (LruCacheItem) item;
        synchronized (this) {
            if (lc.isTrimmed()) {
                lc.setTrimmed(false);
                CacheItem overflow = super.itemAdded(item);
                if (overflow != null) {
                    trimItem(overflow);
                }
            } else if (doOrdering) {
                super.itemAccessed(item);
            }
        }
    }

    protected void itemRefreshed(CacheItem item, int oldSize) {
    }

    protected void itemRemoved(CacheItem item) {
        super.itemRemoved(item);
        doOrdering = (entryCount >= orderingThreshold);
    }

    public void trimTimedoutItems(int  maxCount) {
        // If we are maintaining an ordered list use
        // the superclass method for trimming
        if (doOrdering) {
            super.trimTimedoutItems(maxCount);
        } else {
            // we don't have an ordered list,
            // so go through the whole cache and pick victims
            trimUnSortedTimedoutItems(maxCount);
        }
    }

}
