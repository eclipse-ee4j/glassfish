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

package com.sun.gjc.spi.base.datastructure;

import com.sun.gjc.spi.base.CacheObjectKey;

/**
 *
 * @author Shalini M
 */
public class SynchronizedCache implements Cache {
    private Cache cacheImpl;

    public synchronized Object checkAndUpdateCache(CacheObjectKey key) {
        return cacheImpl.checkAndUpdateCache(key);
    }

    public synchronized void addToCache(CacheObjectKey key, Object entry, boolean force) {
        cacheImpl.addToCache(key, entry, force);
    }

    public synchronized void clearCache() {
        cacheImpl.clearCache();
    }

    public synchronized void purge() {
        cacheImpl.purge();
    }

    public synchronized int getSize() {
        return cacheImpl.getSize();
    }

    public boolean isSynchronized() {
        return true;
    }

    public SynchronizedCache(Cache cacheImpl) {
        this.cacheImpl = cacheImpl;
    }

    public void flushCache() {
        cacheImpl.flushCache();
    }

    public void purge(Object entry) {
        cacheImpl.purge(entry);
    }
}
