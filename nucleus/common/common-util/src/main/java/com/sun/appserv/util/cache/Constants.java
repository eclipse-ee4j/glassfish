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

package com.sun.appserv.util.cache;

/**
 * define all cache related constants
 */
public class Constants {
    public final static String STAT_DEFAULT = "default";

    // default maximum number of entries in the cache
    public final static int DEFAULT_MAX_ENTRIES = 8192;

    // default maximum size in bytes of the cache
    public final static long DEFAULT_MAX_CACHE_SIZE = Long.MAX_VALUE;

    // maxSize specified in bytes, KB or MB
    public final static int KB = 1024;
    public final static int MB = (KB * KB);

    public final static String STAT_BASECACHE_MAX_ENTRIES="cache.BaseCache.stat_maxEntries";
    public final static String STAT_BASECACHE_THRESHOLD="cache.BaseCache.stat_threshold";
    public final static String STAT_BASECACHE_TABLE_SIZE="cache.BaseCache.stat_tableSize";
    public final static String STAT_BASECACHE_ENTRY_COUNT="cache.BaseCache.stat_entryCount";
    public final static String STAT_BASECACHE_HIT_COUNT="cache.BaseCache.stat_hitCount";
    public final static String STAT_BASECACHE_MISS_COUNT="cache.BaseCache.stat_missCount";
    public final static String STAT_BASECACHE_REMOVAL_COUNT="cache.BaseCache.stat_removalCount";
    public final static String STAT_BASECACHE_REFRESH_COUNT="cache.BaseCache.stat_refreshCount";
    public final static String STAT_BASECACHE_OVERFLOW_COUNT="cache.BaseCache.stat_overflowCount";
    public final static String STAT_BASECACHE_ADD_COUNT="cache.BaseCache.stat_addCount";

    public final static String STAT_LRUCACHE_LIST_LENGTH="cache.LruCache.stat_lruListLength";
    public final static String STAT_LRUCACHE_TRIM_COUNT="cache.LruCache.stat_trimCount";

    public final static String STAT_MULTILRUCACHE_SEGMENT_SIZE="cache.MultiLruCache.stat_segmentSize";
    public final static String STAT_MULTILRUCACHE_SEGMENT_LIST_LENGTH="cache.MultiLruCache.stat_segmentListLength";
    public final static String STAT_MULTILRUCACHE_TRIM_COUNT="cache.MultiLruCache.stat_trimCount";

    public final static String STAT_BOUNDEDMULTILRUCACHE_CURRENT_SIZE="cache.BoundedMultiLruCache.stat_currentSize";
    public final static String STAT_BOUNDEDMULTILRUCACHE_MAX_SIZE="cache.BoundedMultiLruCache.stat_maxSize";
}
