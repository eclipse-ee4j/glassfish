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
import org.glassfish.j2ee.statistics.CountStatistic;
import org.glassfish.j2ee.statistics.RangeStatistic;
import org.glassfish.j2ee.statistics.Stats;

/**
 * A Stats interface to represent the statistical data exposed by a Connection
 * Pool. All the Connection Pool implementations should expose statistical data
 * by implementing this interface.
 */
public interface ConnectionPoolStats extends Stats {

    /**
     * Statistic to represent the Connection Usage
     * In addition to information about the number of connections being
     * used currently, this also contains information about the
     * Maximum number of connections that were used(High Watermark)
     * @return RangeStatistic
     */
    RangeStatistic getNumConnUsed();

    /*
     * represents the number of free connections in the pool.
     * @return CountStatistic
     */
    //public CountStatistic getNumConnFree() ;

    /**
     * represents the number of connections that failed validation
     * @return CountStatistic
     */
    CountStatistic getNumConnFailedValidation() ;

    /**
     * represents the number of connection requests that timed out
     * @return CountStatistic
     */
    CountStatistic getNumConnTimedOut();

    /**
     * Indicates the number of free connections in the pool in addition
     * to their high and low watermarks.
     * @return RangeStatistic
     */
     RangeStatistic getNumConnFree();


    /**
     * Indicates the average wait time of connections, for successful
     * connection request attempts to the connector connection pool
     * @return CountStatistic
     */
    CountStatistic getAverageConnWaitTime();

    /**
     * Indicates the number of connection requests in the queue waiting
     * to be serviced
     * @return CountStatistic
     */
    CountStatistic getWaitQueueLength();

    /**
     * Indicates the longest, shortest wait times of connection
     * requests. The current value indicates the wait time of
     * the last request that was serviced by the pool.
     * @return RangeStatistic
     */
    RangeStatistic getConnRequestWaitTime();

    /**
     * indicates the number of physical EIS/JDBC connections that were created,
     * since the last reset
     * @return CountStatistic
     */
    CountStatistic getNumConnCreated();

    /**
     * indicates the number of physical EIS/JDBC connections that were destroyed
     * , since the last reset
     * @return CountStatistic
     */
    CountStatistic getNumConnDestroyed();

    /**
     * indicates the number of logical EIS/JDBC connections that were acquired
     * from the pool, since the last reset
     * @return CountStatistic
     * @since 8.1
     */
    CountStatistic getNumConnAcquired();

    /**
     * indicates the number of logical EIS/JDBC connections that were released
     * to the pool, since the last reset
     * @return CountStatistic
     * @since 8.1
     */
    CountStatistic getNumConnReleased();

    /**
     * Indicates the number of connections that were successfully matched by
     * the Managed Connection Factory.
     *
     * @return CountStatistic
     * @since 9.0
     */
    CountStatistic getNumConnSuccessfullyMatched();

    /**
     * Indicates the number of connections that were rejected by the
     * Managed Connection Factory during matching.
     *
     * @return CountStatistic
     * @since 9.0
     */
    CountStatistic getNumConnNotSuccessfullyMatched();

    /**
     * Indicates the number of potential connection leaks
     *
     * @return CountStatistic
     * @since 9.1
     */
    CountStatistic getNumPotentialConnLeak();

}
