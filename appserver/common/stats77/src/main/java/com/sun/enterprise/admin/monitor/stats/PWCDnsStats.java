/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
 * The DNS Cache caches IP addresses and DNS names. The server’s DNS cache is
 * disabled by default. A single cache entry represents a single IP address or DNS
 * name lookup
 *
 * @author nsegura
 */
public interface PWCDnsStats extends Stats {

    /**
     * Indicates whether the DNS cache is enabled or disable. Default is disabled.
     * @return DNS cache enabled?
     */
    CountStatistic getFlagCacheEnabled();

    /**
     * The number of current cache entries
     * @return current cache entries
     */
    CountStatistic getCountCacheEntries();

    /**
     * The maximum number of cache entries
     * @return max cache entries
     */
    CountStatistic getMaxCacheEntries();

    /**
     * The number of cache hits
     * @return cache hits
     */
    CountStatistic getCountCacheHits();

    /**
     * The number of cache misses
     * @return cache misses
     */
    CountStatistic getCountCacheMisses();

    /**
     * Returns whether asynchronic lookup is enabled. 1 if true, 0 otherwise
     * @return enabled
     */
    CountStatistic getFlagAsyncEnabled();

    /**
     * The total number of asynchronic name lookups
     * @return asyn name lookups
     */
    CountStatistic getCountAsyncNameLookups();

    /**
     * The total number of asynchronic address lookups
     * @return asyn address lookups
     */
    CountStatistic getCountAsyncAddrLookups();

    /**
     * The number of asynchronic lookups in progress
     * @return async lookups in progress
     */
    CountStatistic getCountAsyncLookupsInProgress();

}
