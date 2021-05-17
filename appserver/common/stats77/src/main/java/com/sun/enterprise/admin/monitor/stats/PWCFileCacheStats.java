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

package com.sun.enterprise.admin.monitor.stats;

/**
 *
 * @author  nsegura
 */

import org.glassfish.j2ee.statistics.Stats;
import org.glassfish.j2ee.statistics.CountStatistic;

/** Provides statistical information on the httpservice file cache */
public interface PWCFileCacheStats extends Stats {

    /**
     * Returns whether the file cache us enabled. 1 of enabled, 0 otherwise
     * @return enabled
     */
    public CountStatistic getFlagEnabled();

    /**
     * The maximum age of a valid cache entry
     * @return cache entry max age
     */
    public CountStatistic getSecondsMaxAge();

    /**
     * The number of current cache entries.  A single cache entry represents a single URI
     * @return current cache entries
     */
    public CountStatistic getCountEntries();

    /** The maximum number of cache entries
     * @return max cache entries
     */
    public CountStatistic getMaxEntries();

    /**
     * The number of current open cache entries
     * @return open cache entries
     */
    public CountStatistic getCountOpenEntries();

    /**
     * The Maximum number of open cache entries
     * @return Max open cache entries
     */
    public CountStatistic getMaxOpenEntries();

    /**
     * The  Heap space used for cache
     * @return heap size
     */
    public CountStatistic getSizeHeapCache();

    /**
     * The Maximum heap space used for cache
     * @return Max heap size
     */
    public CountStatistic getMaxHeapCacheSize();

    /**
     * The size of Mapped memory used for caching
     * @return Mapped memory size
     */
    public CountStatistic getSizeMmapCache();

    /**
     * The Maximum Memory Map size to be used for caching
     * @return Max Memory Map size
     */
    public CountStatistic getMaxMmapCacheSize();

    /**
     * The Number of cache lookup hits
     * @return cache hits
     */
    public CountStatistic getCountHits();

    /**
     * The Number of cache lookup misses
     * @return cache misses
     */
    public CountStatistic getCountMisses();

    /**
     * The Number of hits on cached file info
     * @return hits on cached file info
     */
    public CountStatistic getCountInfoHits();

    /**
     * The Number of misses on cached file info
     * @return misses on cache file info
     */
    public CountStatistic getCountInfoMisses();

    /**
     * The Number of hits on cached file content
     * @return hits on cache file content
     */
    public CountStatistic getCountContentHits();

    /**
     * The Number of misses on cached file content
     * @return missed on cached file content
     */
    public CountStatistic getCountContentMisses();

}
