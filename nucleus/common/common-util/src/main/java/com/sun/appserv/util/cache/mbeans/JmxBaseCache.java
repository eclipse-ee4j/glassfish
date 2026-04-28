/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.appserv.util.cache.mbeans;

import com.sun.appserv.util.cache.BaseCache;
import com.sun.appserv.util.cache.Constants;

/**
 * This class provides implementation for JmxBaseCacheMBean
 *
 * @author Krishnamohan Meduri (Krishna.Meduri@Sun.com) 2005
 */
public class JmxBaseCache implements JmxBaseCacheMBean {

    private String name;
    private BaseCache<?, ?> baseCache;

    public JmxBaseCache(BaseCache<?, ?> baseCache, String name) {
        this.baseCache = baseCache;
        this.name = name;
    }


    /**
     * Returns a unique identifier for this MBean inside the domain
     */
    @Override
    public String getName() {
        return name;
    }


    /**
     * Returns maximum possible number of entries
     */
    @Override
    public Integer getMaxEntries() {
        return (Integer) baseCache.getStatByName(Constants.STAT_BASECACHE_MAX_ENTRIES);
    }


    /**
     * Returns threshold. This when reached, an overflow will occur
     */
    @Override
    public Integer getThreshold() {
        return (Integer) baseCache.getStatByName(Constants.STAT_BASECACHE_THRESHOLD);
    }


    /**
     * Returns current number of buckets
     */
    @Override
    public Integer getTableSize() {
        return (Integer) baseCache.getStatByName(Constants.STAT_BASECACHE_TABLE_SIZE);
    }


    /**
     * Returns current number of Entries
     */
    @Override
    public Integer getEntryCount() {
        return (Integer) baseCache.getStatByName(Constants.STAT_BASECACHE_ENTRY_COUNT);
    }


    /**
     * Return the number of cache hits
     */
    @Override
    public Integer getHitCount() {
        return (Integer) baseCache.getStatByName(Constants.STAT_BASECACHE_HIT_COUNT);
    }


    /**
     * Returns the number of cache misses
     */
    @Override
    public Integer getMissCount() {
        return (Integer) baseCache.getStatByName(Constants.STAT_BASECACHE_MISS_COUNT);
    }


    /**
     * Returns the number of entries that have been removed
     */
    @Override
    public Integer getRemovalCount() {
        return (Integer) baseCache.getStatByName(Constants.STAT_BASECACHE_REMOVAL_COUNT);
    }


    /**
     * Returns the number of values that have been refreshed
     * (replaced with a new value in an existing extry)
     */
    @Override
    public Integer getRefreshCount() {
        return (Integer) baseCache.getStatByName(Constants.STAT_BASECACHE_REFRESH_COUNT);
    }


    /**
     * Returns the number of times that an overflow has occurred
     */
    @Override
    public Integer getOverflowCount() {
        return (Integer) baseCache.getStatByName(Constants.STAT_BASECACHE_OVERFLOW_COUNT);
    }


    /**
     * Returns the number of times new entries have been added
     */
    @Override
    public Integer getAddCount() {
        return (Integer) baseCache.getStatByName(Constants.STAT_BASECACHE_ADD_COUNT);
    }
}
