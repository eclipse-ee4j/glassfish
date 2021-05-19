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
public interface Cache {

    /**
     * Check if an entry is found for this key object. If found, the entry is
     * put in the result object and back into the list.
     *
     * @param key whose mapping entry is to be checked.
     * @return result object that contains the key with the entry if not busy
     * or null when
     * (1) object not found in cache
     * (2) object found but is busy
     */
    public Object checkAndUpdateCache(CacheObjectKey key);

    /**
     * Add key and entry value into the cache.
     * @param key that contains the sql string and its type (PS/CS)
     * @param entry that is the wrapper of PreparedStatement or
     * CallableStatement
     * @param force If existing key is to be overwritten
     */
    public void addToCache(CacheObjectKey key, Object entry, boolean force);

    /**
     * Clear statement cache
     */
    public void clearCache();

    /**
     * Remove all statements stored in the statement cache after closing
     * the statement objects. Used when the statement cache size exceeds
     * user defined maximum cache size.
     */
    public void purge();

    /**
     * Closing all statements in statement cache and flush the statement cache
     * of all the statements. Used when a physical connection to the underlying
     * resource manager is destroyed.
     */
    public void flushCache();

    /**
     * Get the size of statement cache
     * @return int statement cache size
     */
    public int getSize();

    /**
     * Check if the statement cache is synchronized.
     * @return boolean synchronized flag.
     */
    public boolean isSynchronized();

    /**
     * Remove the specified entry stored in the statement cache after closing
     * the statement object associated with this entry. Used when statement is
     * being reclaimed and is being used for the subsequent requests from the
     * application.
     *
     * @param entry
     */
    public void purge(Object entry);
}
