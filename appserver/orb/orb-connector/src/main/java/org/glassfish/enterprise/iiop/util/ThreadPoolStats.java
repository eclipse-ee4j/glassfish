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

package org.glassfish.enterprise.iiop.util;

import org.glassfish.external.statistics.BoundedRangeStatistic;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.RangeStatistic;
import org.glassfish.external.statistics.Stats;

/**
 * Stats interface for the monitorable attributes of the
 * a generic ThreadPool. This combines the statistics that were exposed in 7.0
 * with the new ones. In 8.0, the generic Thread Pool that can be used by any component
 * in the server runtime is introduced.
 *
 * @author Kedar Mhaswade
 * @since S1AS8.0
 */
public interface ThreadPoolStats extends Stats {

    /**
     * Returns the statistical information about the number of Threads in the associated ThreaPool,
     * as an instance of BoundedRangeStatistic.
     * This returned value gives an idea about how the pool is changing.
     * @return an instance of {@link BoundedRangeStatistic}
     */
    BoundedRangeStatistic getCurrentNumberOfThreads();

    /** Returns the total number of available threads, as an instance of {@link CountStatistic}.
     * @return an instance of {@link CountStatistic}
     */
    CountStatistic getNumberOfAvailableThreads();

    /** Returns the number of busy threads, as an instance of {@link CountStatistic}.
     * @return an instance of {@link CountStatistic}
     */
    CountStatistic getNumberOfBusyThreads();

    /**
     * Returns the statistical information about the average completion time of a work item in milliseconds.
     * @return an instance of {@link RangeStatistic}
     */
    RangeStatistic getAverageWorkCompletionTime();

    /**
     * Returns the the total number of work items added so far to the work queue associated with threadpool.
     * @return an instance of {@link CountStatistic}
     */
    CountStatistic getTotalWorkItemsAdded();

    /**
     * Returns average time in milliseconds a work item waited in the work queue before getting processed.
     * @return an instance of {@link RangeStatistic}
     */
    RangeStatistic getAverageTimeInQueue();

    /**
     * Returns the work items in queue
     * @return an instance of {@link BoundedRangeStatistic}
     */
    BoundedRangeStatistic getNumberOfWorkItemsInQueue();

}
