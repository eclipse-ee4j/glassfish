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

package com.sun.jdo.spi.persistence.support.sqlstore;

/**
 * A cache of "version consistent" StateManager instances.  These instances
 * are used so that we can avoid loading state from the database.
 *
 * @author Dave Bristor
 */
public interface VersionConsistencyCache {
    /**
     * Puts the given StateManager into a map that is keyed by the given OID.
     * We anticipate that implementations will want to use a two-level map,
     * and so the pc's class can be used as a key into a map to access a
     * second map, which would be that keyed by OID.
     * @param pcType class of instance, used as key in outer map.
     * @param oid Object id, used as key in inner map.
     * @param sm StateManager bound to <code>oid</code> in inner map.
     */
    public StateManager put(Class pcType, Object oid, StateManager sm);

    /**
     * Returns an SM, if found, else null.
     * @param pcType class of instance, used as key in outer map.
     * @param oid Object id, used as key in inner map.
     */
    public StateManager get(Class pcType, Object oid);

    /**
     * Removes entry based on pc and oid.  If map is empty after remove,
     * removes it from its containint map.
     * @param pcType class of instance, used as key in outer map.
     * @param oid Object id, used as key in inner map.
     */
    public StateManager remove(Class pcType, Object oid);

    /**
     * Informs the cache to expect that the given pcType will be used as a key
     * for the outer map in subsequent <code>putEntry</code> operations.
     * @param pcType class of instance, used as key in outer map.
     */
    public void addPCType(Class pcType);

    /**
     * Removes the map for the given pcType and all its elements.
     * @param pcType class of instance, used as key in outer map.
     */
    public void removePCType(Class pcType);
}
