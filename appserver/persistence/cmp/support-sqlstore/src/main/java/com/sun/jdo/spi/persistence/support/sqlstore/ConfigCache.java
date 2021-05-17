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
 * Interface to access SQLStore config information.
 *
 * @author Marina Vatkina
 */
public interface ConfigCache {

    /**
     * Get the PersistenceConfig for given pcClass. The config is looked up
     * from a cache. If a config can not be found in cache, a new
     * instance is created and returned.
     *
     * @param pcClass The input pcClass.
     * @return PersistenceConfig for given pcClass.
     */
    PersistenceConfig getPersistenceConfig(Class pcClass);

    /**
     * Gets the Class instance corresponding to given oidType.
     *
     * @param oidType The input oidType.
     * @return The Class instance corresponding to given oidType.
     */
    Class getClassByOidClass(Class oidType);

    /**
     * Sets VersionConsistencyCache field.
     *
     * @param vcCache the VersionConsistencyCache instance.
     */
    void setVersionConsistencyCache(VersionConsistencyCache vcCache);
}
