/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.gjc.spi.base.datastructure;

import com.sun.gjc.spi.base.CacheObjectKey;
import com.sun.gjc.spi.base.PreparedStatementWrapper;

import org.glassfish.resourcebase.resources.api.PoolInfo;

/**
 * This ia a FIXED size cache implementation.
 * <p/>
 * If the cache is full the statement is not added to the cache.
 *
 * @author Shalini M
 */

public class FIXEDCacheImpl extends LRUCacheImpl {

    public FIXEDCacheImpl(PoolInfo poolInfo, int maxSize) {
        super(poolInfo, maxSize);
    }

    @Override
    public void addToCache(CacheObjectKey key, Object o, boolean force) {
        if (getSize() >= getMaxSize()) {
            if (o instanceof PreparedStatementWrapper) {
                ((PreparedStatementWrapper) o).setCached(false);
            }
        } else {
            super.addToCache(key, o, force);
        }
    }
}
