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

import com.sun.corba.ee.spi.threadpool.NoSuchWorkQueueException;
import com.sun.corba.ee.spi.threadpool.ThreadPool;
import com.sun.corba.ee.spi.threadpool.WorkQueue;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.external.statistics.BoundedRangeStatistic;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.RangeStatistic;
import org.glassfish.external.statistics.impl.BoundedRangeStatisticImpl;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/**
 * This is the implementation for the ThreadPoolStats
 * and provides the implementation required to get the statistics
 * for a threadpool
 *
 * @author Pramod Gopinath
 */
@ManagedObject
@Description("The implementation for the ThreadPoolStats")
public class ThreadPoolStatsImpl
        extends ORBCommonStatsImpl
        implements ThreadPoolStats {

    private ThreadPool threadPool;
    private WorkQueue workQueue;
    private String workQueueName;
    private CountStatisticImpl numberOfBusyThreads;
    private CountStatisticImpl numberOfAvailableThreads;
    private BoundedRangeStatisticImpl currentNumberOfThreads;
    private BoundedRangeStatisticImpl averageWorkCompletionTime;
    private CountStatisticImpl totalWorkItemsAdded;
    private BoundedRangeStatisticImpl numberOfWorkItemsInQueue;
    private BoundedRangeStatisticImpl averageTimeInQueue;
    private static final String stringNumberOfBusyThreads =
            MonitoringConstants.THREADPOOL_NUMBER_OF_BUSY_THREADS;
    private static final String stringNumberOfAvailableThreads =
            MonitoringConstants.THREADPOOL_NUMBER_OF_AVAILABLE_THREADS;
    private static final String stringCurrentNumberOfThreads =
            MonitoringConstants.THREADPOOL_CURRENT_NUMBER_OF_THREADS;
    private static final String stringAverageWorkCompletionTime =
            MonitoringConstants.THREADPOOL_AVERAGE_WORK_COMPLETION_TIME;
    private static final String stringTotalWorkItemsAdded =
            MonitoringConstants.WORKQUEUE_TOTAL_WORK_ITEMS_ADDED;
    private static final String stringNumberOfWorkItemsInQueue =
            MonitoringConstants.WORKQUEUE_WORK_ITEMS_IN_QUEUE;
    private static final String stringAverageTimeInQueue =
            MonitoringConstants.WORKQUEUE_AVERAGE_TIME_IN_QUEUE;

    public ThreadPoolStatsImpl(ThreadPool threadPool) throws NoSuchWorkQueueException {
        this.threadPool = threadPool;

        getWorkQueueForThreadPool();

        initializeStats();
    }

    private void getWorkQueueForThreadPool() {
        try {
            workQueue = threadPool.getWorkQueue(0);
            workQueueName = workQueue.getName();
        } catch (NoSuchWorkQueueException ex) {

            Logger.getLogger(workQueueName).log(Level.SEVERE, workQueueName);
            throw new RuntimeException(ex);

        }
    }

    private void initializeStats() throws NoSuchWorkQueueException {
        super.initialize("org.glassfish.enterprise.iiop.util.ThreadPoolStats");

        final long time = System.currentTimeMillis();

        numberOfBusyThreads =
                new CountStatisticImpl(threadPool.numberOfBusyThreads(), stringNumberOfBusyThreads, "COUNT",
                threadPool.getWorkQueue(0).toString(),
                time, time);

        numberOfAvailableThreads =
                new CountStatisticImpl(
                threadPool.numberOfAvailableThreads(), stringNumberOfAvailableThreads, "count",
                threadPool.getWorkQueue(0).toString(),
                time, time);

        currentNumberOfThreads =
                new BoundedRangeStatisticImpl(
                threadPool.currentNumberOfThreads(), threadPool.maximumNumberOfThreads(), threadPool.minimumNumberOfThreads(), java.lang.Long.MAX_VALUE, 0,
                stringCurrentNumberOfThreads, "count",
                threadPool.getWorkQueue(0).toString(),
                time, time);

        averageWorkCompletionTime =
                new BoundedRangeStatisticImpl(
                threadPool.averageWorkCompletionTime(), 0, 0, java.lang.Long.MAX_VALUE, 0,
                stringAverageWorkCompletionTime, "Milliseconds",
                threadPool.getWorkQueue(0).toString(),
                time, time);

        // WorkQueue workItems = threadPool.getWorkQueue(0);

        totalWorkItemsAdded =
                new CountStatisticImpl(
                workQueue.totalWorkItemsAdded(), stringTotalWorkItemsAdded, "count",
                workQueue.getName(),
                time, time);

        numberOfWorkItemsInQueue =
                new BoundedRangeStatisticImpl(
                workQueue.workItemsInQueue(), 0, 0, java.lang.Long.MAX_VALUE, 0,
                stringNumberOfWorkItemsInQueue, "count",
                workQueue.getName(),
                time, time);

        averageTimeInQueue =
                new BoundedRangeStatisticImpl(
                workQueue.averageTimeInQueue(), 0, 0, java.lang.Long.MAX_VALUE, 0,
                stringAverageTimeInQueue, "Milliseconds",
                workQueue.getName(),
                time, time);

    }

    @ManagedAttribute(id = "currentbusythreads")
    @Description("Total number of busy threads")
    public synchronized CountStatistic getNumberOfBusyThreads() {
        int numBusyThreads = threadPool.numberOfBusyThreads();
        numberOfBusyThreads.setCount(numBusyThreads);
        return (CountStatistic) numberOfBusyThreads;
    }

    @ManagedAttribute
    @Description("Total number of available threads")
    public synchronized CountStatistic getNumberOfAvailableThreads() {
        long numAvailableThreads = (long) threadPool.numberOfAvailableThreads();

        numberOfAvailableThreads.setCount(numAvailableThreads);

        return (CountStatistic) numberOfAvailableThreads;
    }

    @ManagedAttribute
    @Description("Total number of current threads")
    public synchronized BoundedRangeStatistic getCurrentNumberOfThreads() {
        int numCurrentThreads = threadPool.currentNumberOfThreads();
        currentNumberOfThreads.setCurrent(numCurrentThreads);
        return (BoundedRangeStatistic) currentNumberOfThreads;
    }

    @ManagedAttribute
    @Description("Average time to complete work")
    public synchronized RangeStatistic getAverageWorkCompletionTime() {
        long avgWorkCompletionTime = threadPool.averageWorkCompletionTime();
        averageWorkCompletionTime.setCurrent(avgWorkCompletionTime);
        return (RangeStatistic) averageWorkCompletionTime;
    }

    @ManagedAttribute
    @Description("Total number of work items added to the queue")
    public synchronized CountStatistic getTotalWorkItemsAdded() {
        long totWorkItemsAdded = workQueue.totalWorkItemsAdded();
        totalWorkItemsAdded.setCount(totWorkItemsAdded);
        return (CountStatistic) totalWorkItemsAdded;
    }

    @ManagedAttribute
    @Description("Total number of work items in the queue")
    public synchronized BoundedRangeStatistic getNumberOfWorkItemsInQueue() {
        int totWorkItemsInQueue = workQueue.workItemsInQueue();
        numberOfWorkItemsInQueue.setCurrent(totWorkItemsInQueue);
        return (BoundedRangeStatistic) numberOfWorkItemsInQueue;
    }

    @ManagedAttribute
    @Description("Average time in queue")
    public synchronized RangeStatistic getAverageTimeInQueue() {
        long avgTimeInQueue = workQueue.averageTimeInQueue();
        averageTimeInQueue.setCurrent(avgTimeInQueue);

        return (RangeStatistic) averageTimeInQueue;
    }
} //ThreadPoolStatsImpl{ }

