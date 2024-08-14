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

package com.sun.enterprise.v3.services.impl.monitor;

import com.sun.enterprise.v3.services.impl.monitor.stats.ConnectionQueueStatsProvider;
import com.sun.enterprise.v3.services.impl.monitor.stats.ThreadPoolStatsProvider;

import org.glassfish.grizzly.threadpool.AbstractThreadPool;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.grizzly.threadpool.ThreadPoolProbe;

/**
 *
 * @author oleksiys
 */
public class ThreadPoolMonitor implements ThreadPoolProbe {
    private final GrizzlyMonitoring grizzlyMonitoring;
    private final String monitoringId;

    public ThreadPoolMonitor(GrizzlyMonitoring grizzlyMonitoring,
            String monitoringId, ThreadPoolConfig config) {
        this.grizzlyMonitoring = grizzlyMonitoring;
        this.monitoringId = monitoringId;

        if (grizzlyMonitoring != null) {
            final ThreadPoolStatsProvider threadPoolStatsProvider =
                    grizzlyMonitoring.getThreadPoolStatsProvider(monitoringId);
            if (threadPoolStatsProvider != null) {
                threadPoolStatsProvider.setStatsObject(config);
                threadPoolStatsProvider.reset();
            }

            final ConnectionQueueStatsProvider connectionQueueStatsProvider =
                    grizzlyMonitoring.getConnectionQueueStatsProvider(monitoringId);
            if (connectionQueueStatsProvider != null) {
                connectionQueueStatsProvider.setStatsObject(config);
                connectionQueueStatsProvider.reset();
            }
        }
    }

    @Override
    public void onThreadPoolStartEvent(AbstractThreadPool threadPool) {
    }

    @Override
    public void onThreadPoolStopEvent(AbstractThreadPool threadPool) {
    }

    @Override
    public void onThreadAllocateEvent(AbstractThreadPool threadPool, Thread thread) {
        grizzlyMonitoring.getThreadPoolProbeProvider().threadAllocatedEvent(
                monitoringId, threadPool.getConfig().getPoolName(),
                thread.getId());
    }

    @Override
    public void onThreadReleaseEvent(AbstractThreadPool threadPool, Thread thread) {
        grizzlyMonitoring.getThreadPoolProbeProvider().threadReleasedEvent(
                monitoringId, threadPool.getConfig().getPoolName(),
                thread.getId());
    }

    @Override
    public void onMaxNumberOfThreadsEvent(AbstractThreadPool threadPool, int maxNumberOfThreads) {
        grizzlyMonitoring.getThreadPoolProbeProvider().maxNumberOfThreadsReachedEvent(
                monitoringId, threadPool.getConfig().getPoolName(),
                maxNumberOfThreads);
    }

    @Override
    public void onTaskDequeueEvent(AbstractThreadPool threadPool, Runnable task) {
        grizzlyMonitoring.getThreadPoolProbeProvider().threadDispatchedFromPoolEvent(
                monitoringId, threadPool.getConfig().getPoolName(),
                Thread.currentThread().getId());
        grizzlyMonitoring.getConnectionQueueProbeProvider().onTaskDequeuedEvent(
                monitoringId, task.getClass().getName());
    }

    @Override
    public void onTaskCancelEvent(AbstractThreadPool threadPool, Runnable task) {
        // when dequeued task is cancelled - we have to "return" the thread, that
        // we marked as dispatched from the pool
        grizzlyMonitoring.getThreadPoolProbeProvider().threadReturnedToPoolEvent(
                monitoringId, threadPool.getConfig().getPoolName(),
                Thread.currentThread().getId());
    }

    @Override
    public void onTaskCompleteEvent(AbstractThreadPool threadPool, Runnable task) {
        grizzlyMonitoring.getThreadPoolProbeProvider().threadReturnedToPoolEvent(
                monitoringId, threadPool.getConfig().getPoolName(),
                Thread.currentThread().getId());
    }

    @Override
    public void onTaskQueueEvent(AbstractThreadPool threadPool, Runnable task) {
        grizzlyMonitoring.getConnectionQueueProbeProvider().onTaskQueuedEvent(
                monitoringId, task.getClass().getName());
    }

    @Override
    public void onTaskQueueOverflowEvent(AbstractThreadPool threadPool) {
        grizzlyMonitoring.getConnectionQueueProbeProvider().onTaskQueueOverflowEvent(
                monitoringId);
    }
}
