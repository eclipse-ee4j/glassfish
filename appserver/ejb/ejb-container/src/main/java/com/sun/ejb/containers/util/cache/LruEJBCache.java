/*
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

package com.sun.ejb.containers.util.cache;

import com.sun.appserv.util.cache.LruCache;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.ejb.LogFacade;
import org.glassfish.logging.annotation.LogMessageInfo;

/**
 * LRUCache
 * in-memory bounded cache with an LRU list
 */
public class LruEJBCache extends LruCache {

    protected static final Logger _logger  = LogFacade.getLogger();

    @LogMessageInfo(
        message = "[{0}]: trimLru(), resetting head and tail",
        level = "WARNING")
    private static final String TRIM_LRU_RESETTING_HEAD_AND_TAIL = "AS-EJB-00001";

    protected String cacheName;

    /**
     * default constructor
     */
    public LruEJBCache() { }

    @Override
    protected CacheItem trimLru(long currentTime) {

        LruCacheItem trimItem = tail;

        if (tail != head) {
            tail = trimItem.getLPrev();
            if (tail == null) {
                _logger.log(Level.WARNING, TRIM_LRU_RESETTING_HEAD_AND_TAIL, cacheName);
                // do not let the tail go past the head
                tail = head = null;
            } else {
                tail.setLNext(null);
            }
        } else {
            tail = head = null;
        }

        if (trimItem != null) {
            trimItem.setTrimmed(true);
            trimItem.setLPrev(null);
            trimCount++;
            listSize--;
        }

        return trimItem;
    }

    @Override
    protected CacheItem itemAdded(CacheItem item) {
        boolean wasUnbounded = isUnbounded;
        CacheItem overflow = null;

        // force not to check
        isUnbounded = false;
        try {
            overflow = super.itemAdded(item);
        } finally {
            //restore
            isUnbounded = wasUnbounded;
        }

        return overflow;
    }

    public void setCacheName(String name) {
        this.cacheName = name;
    }
}
