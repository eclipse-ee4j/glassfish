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

package com.sun.enterprise.admin.monitor.stats;
import org.glassfish.j2ee.statistics.BoundedRangeStatistic;
import org.glassfish.j2ee.statistics.CountStatistic;
import org.glassfish.j2ee.statistics.Stats;

/**
 * A Stats interface to represent the statistics exposed by the Enterprise Bean Cache.
 * This is based on the statistics that were exposed in S1AS7.0. An implementation of EJB Cache
 * should provide statistical data by implementing this interface.
 *
 * @author Muralidhar Vempaty
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.0
 */
public interface EJBCacheStats extends Stats {

    /**
     * Returns the number of times a user request fails to find an EJB in associated EJB cache
     * instance, as a CountStatistic.
     *
     * @return an instance of {@link BoundedRangeStatistic}
     */
    BoundedRangeStatistic getCacheMisses();


    /**
     * Returns the number of times a user request hits an EJB in associated EJB cache instance, as a
     * CountStatistic.
     *
     * @return an instance of {@link BoundedRangeStatistic}
     */
    BoundedRangeStatistic getCacheHits();


    /**
     * Returns total number of EJBs in the associated EJB Cache, as a BoundedRangeStatistic.
     * Note that this returns the various statistical values like maximum and minimum value attained
     * as a part of the return value.
     *
     * @return an instance of {@link BoundedRangeStatistic}
     */
    BoundedRangeStatistic getNumBeansInCache();


    /**
     * Returns the number of passivations of a Stateful Session Bean, as a CountStatistic.
     *
     * @return an instance of {@link CountStatistic}
     */
    CountStatistic getNumPassivations();


    /**
     * Returns the number of errors in passivating a Stateful Session Bean, as a CountStatistic.
     * Must be less than or equal to {@link #getNumPassivations}
     *
     * @return an instance of {@link CountStatistic}
     */
    CountStatistic getNumPassivationErrors();


    /**
     * Returns the number of removed Expired Sessions as a CountStatistic.
     *
     * @return an instance of {@link CountStatistic}
     */
    CountStatistic getNumExpiredSessionsRemoved();


    /**
     * Returns the number of errors in passivating a Stateful Session Bean, as a CountStatistic.
     * Must be less than or equal to {@link #getNumPassivations}
     *
     * @return an instance of {@link CountStatistic}
     */
    CountStatistic getNumPassivationSuccess();
}
