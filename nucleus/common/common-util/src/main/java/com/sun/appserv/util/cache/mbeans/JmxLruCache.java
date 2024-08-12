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

/**
 * @Version $Id: JmxLruCache.java,v 1.4 2005/12/25 04:25:23 tcfujii Exp $
 * Created on May 4, 2005 1:20 PM
 */

package com.sun.appserv.util.cache.mbeans;

import com.sun.appserv.util.cache.Constants;
import com.sun.appserv.util.cache.LruCache;
/**
 * This class provides implementation for JmxLruCache MBean
 *
 * @author Krishnamohan Meduri (Krishna.Meduri@Sun.com)
 *
 */
public class JmxLruCache extends JmxBaseCache implements JmxLruCacheMBean {

    private LruCache lruCache;

    public JmxLruCache(LruCache lruCache, String name) {
        super(lruCache,name);
        this.lruCache = lruCache;
    }
    /**
     * Returns the current lenght of the LRU list
     */
    public Integer getLruListLength() {
        return (Integer) lruCache.getStatByName(
                                        Constants.STAT_LRUCACHE_LIST_LENGTH);
    }

    /**
     * Returns the number of entries that have been trimmed
     */
    public Integer getTrimCount() {
        return (Integer) lruCache.getStatByName(
                                        Constants.STAT_LRUCACHE_TRIM_COUNT);
    }
}
