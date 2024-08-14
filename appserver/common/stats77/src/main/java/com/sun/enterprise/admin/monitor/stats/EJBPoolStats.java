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
 * A Stats interface to represent the statistical data exposed by an EJB Bean Pool.
 * These are based on the statistics exposed in S1AS7.0.
 * All the EJB Pool implementations should expose statistical data by implementing this interface.
 *
 * @author Muralidhar Vempaty
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.0
 */
public interface EJBPoolStats extends Stats {

    /**
     * Returns the statistical information about the number of EJBs in the associated pool,
     * as an instance of BoundedRangeStatistic.
     * This returned value gives an idea about how the pool is changing.
     *
     * @return an instance of {@link BoundedRangeStatistic}
     */
    BoundedRangeStatistic getNumBeansInPool();


    /**
     * Returns the number of threads waiting for free Beans, as an instance of CountStatistic.
     * This indicates the congestion of requests.
     *
     * @return an instance of {@link BoundedRangeStatistic}
     */
    BoundedRangeStatistic getNumThreadsWaiting();


    /**
     * Returns the number of Beans created in associated pool so far over time, since the gathering
     * of data started, as a CountStatistic.
     *
     * @return an instance of {@link CountStatistic}
     */
    CountStatistic getTotalBeansCreated();


    /**
     * Returns the number of Beans destroyed from associated pool so far over time, since
     * the gathering of data started, as a CountStatistic.
     *
     * @return an instance of {@link CountStatistic}
     */
    CountStatistic getTotalBeansDestroyed();


    /**
     * Returns the maximum number of messages to load into a JMS session, at a time, as a CountStatistic.
     *
     * @return an instance of {@link CountStatistic}
     */
    CountStatistic getJmsMaxMessagesLoad();
}
