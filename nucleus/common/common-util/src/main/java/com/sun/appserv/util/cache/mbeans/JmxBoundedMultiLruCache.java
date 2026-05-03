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

import com.sun.appserv.util.cache.BoundedMultiLruCache;
import com.sun.appserv.util.cache.Constants;

/**
 * This class provides implementation for JmxLruCache MBean
 *
 * @author Krishnamohan Meduri (Krishna.Meduri@Sun.com) 2005
 */
public class JmxBoundedMultiLruCache extends JmxMultiLruCache implements JmxBoundedMultiLruCacheMBean {

    private BoundedMultiLruCache<?, ?> boundedMultiLruCache;

    public JmxBoundedMultiLruCache(BoundedMultiLruCache<?, ?> boundedMultiLruCache, String name) {
        super(boundedMultiLruCache, name);
        this.boundedMultiLruCache = boundedMultiLruCache;
    }

    /**
     * Returns the current size of the cache in bytes
     */
    @Override
    public Long getCurrentSize() {
        return (Long) boundedMultiLruCache.getStatByName(
                                        Constants.STAT_BOUNDEDMULTILRUCACHE_CURRENT_SIZE);
    }

    /**
     * Returns the upper bound on the cache size
     */
    @Override
    public Long getMaxSize() {
        Object object = boundedMultiLruCache.getStatByName(
                                        Constants.STAT_BOUNDEDMULTILRUCACHE_MAX_SIZE);
        /*
         * BoundedMultiLruCache class returns java.lang.String with a value
         * "default" if the maxSize == Constants.DEFAULT_MAX_CACHE_SIZE
         * To take care of this case, the if/else is added below
         */
        if (object instanceof String && ((String) object).equals(Constants.STAT_DEFAULT)) {
            return Long.valueOf(Constants.DEFAULT_MAX_CACHE_SIZE);
        } else {
            return (Long) object;
        }
    }
}
