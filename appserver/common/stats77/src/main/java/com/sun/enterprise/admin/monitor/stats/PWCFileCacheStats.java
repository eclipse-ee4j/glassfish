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

import org.glassfish.j2ee.statistics.CountStatistic;
import org.glassfish.j2ee.statistics.Stats;

/**
 * Provides statistical information on the httpservice file cache
 *
 * @author  nsegura
 */
public interface PWCFileCacheStats extends Stats {

    /**
     * Returns whether the file cache us enabled. 1 of enabled, 0 otherwise
     * @return enabled
     */
    CountStatistic getFlagEnabled();

    /**
     * The maximum age of a valid cache entry
     * @return cache entry max age
     */
    CountStatistic getSecondsMaxAge();

    /**
     * The number of current cache entries.  A single cache entry represents a single URI
     * @return current cache entries
     */
    CountStatistic getCountEntries();

    /** The maximum number of cache entries
     * @return max cache entries
     */
    CountStatistic getMaxEntries();

    /**
     * The number of current open cache entries
     * @return open cache entries
     */
    CountStatistic getCountOpenEntries();

    /**
     * The Maximum number of open cache entries
     * @return Max open cache entries
     */
    CountStatistic getMaxOpenEntries();

    /**
     * The  Heap space used for cache
     * @return heap size
     */
    CountStatistic getSizeHeapCache();

    /**
     * The Maximum heap space used for cache
     * @return Max heap size
     */
    CountStatistic getMaxHeapCacheSize();

    /**
     * The size of Mapped memory used for caching
     * @return Mapped memory size
     */
    CountStatistic getSizeMmapCache();

    /**
     * The Maximum Memory Map size to be used for caching
     * @return Max Memory Map size
     */
    CountStatistic getMaxMmapCacheSize();

    /**
     * The Number of cache lookup hits
     * @return cache hits
     */
    CountStatistic getCountHits();

    /**
     * The Number of cache lookup misses
     * @return cache misses
     */
    CountStatistic getCountMisses();

    /**
     * The Number of hits on cached file info
     * @return hits on cached file info
     */
    CountStatistic getCountInfoHits();

    /**
     * The Number of misses on cached file info
     * @return misses on cache file info
     */
    CountStatistic getCountInfoMisses();

    /**
     * The Number of hits on cached file content
     * @return hits on cache file content
     */
    CountStatistic getCountContentHits();

    /**
     * The Number of misses on cached file content
     * @return missed on cached file content
     */
    CountStatistic getCountContentMisses();

}
