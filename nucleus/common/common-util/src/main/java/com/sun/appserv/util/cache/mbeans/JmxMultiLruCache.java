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

import com.sun.appserv.util.cache.Constants;
import com.sun.appserv.util.cache.MultiLruCache;

/**
 * This class provides implementation for JmxLruCache MBean
 *
 * @author Krishnamohan Meduri (Krishna.Meduri@Sun.com) 2005
 */
public class JmxMultiLruCache extends JmxBaseCache implements JmxMultiLruCacheMBean {

    private MultiLruCache multiLruCache;

    public JmxMultiLruCache(MultiLruCache multiLruCache, String name) {
        super(multiLruCache, name);
        this.multiLruCache = multiLruCache;
    }


    /**
     * Returns the number of entries that have been trimmed
     */
    @Override
    public Integer getTrimCount() {
        return (Integer) multiLruCache.getStatByName(Constants.STAT_MULTILRUCACHE_TRIM_COUNT);
    }


    /**
     * Returns the size of each segment
     */
    @Override
    public Integer getSegmentSize() {
        return (Integer) multiLruCache.getStatByName(Constants.STAT_MULTILRUCACHE_SEGMENT_SIZE);
    }


    /**
     * Returns the legnth of the segment list
     */
    @Override
    public Integer[] getSegmentListLength() {
        return (Integer[]) multiLruCache.getStatByName(Constants.STAT_MULTILRUCACHE_SEGMENT_LIST_LENGTH);
    }
}
