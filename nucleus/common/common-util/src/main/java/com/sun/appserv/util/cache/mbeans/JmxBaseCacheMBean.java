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
 * @Version $Id: JmxBaseCacheMBean.java,v 1.4 2005/12/25 04:25:22 tcfujii Exp $
 * Created on May 4, 2005 11:43 AM
 */

package com.sun.appserv.util.cache.mbeans;

/**
 * This interface defines the attributes exposed by the BaseCache MBean
 *
 * @author Krishnamohan Meduri (Krishna.Meduri@Sun.com)
 *
 */
public interface JmxBaseCacheMBean {

    /**
     * Returns a unique identifier for this MBean inside the domain
     */
    String getName();

    /**
     * Returns maximum possible number of entries
     */
    Integer getMaxEntries();

    /**
     * Returns threshold. This when reached, an overflow will occur
     */
    Integer getThreshold();

    /**
     * Returns current number of buckets
     */
    Integer getTableSize();

    /**
     * Returns current number of Entries
     */
    Integer getEntryCount();

    /**
     * Return the number of cache hits
     */
    Integer getHitCount();

    /**
     * Returns the number of cache misses
     */
    Integer getMissCount();

    /**
     * Returns the number of entries that have been removed
     */
    Integer getRemovalCount();

    /**
     * Returns the number of values that have been refreshed
     * (replaced with a new value in an existing extry)
     */
    Integer getRefreshCount();

    /**
     * Returns the number of times that an overflow has occurred
     */
    Integer getOverflowCount();

    /**
     * Returns the number of times new entries have been added
     */
    Integer getAddCount();
}
