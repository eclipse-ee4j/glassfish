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

package com.sun.enterprise.v3.services.impl.monitor.stats;

import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.annotations.Reset;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/**
 * Thread Pool statistics
 *
 * @author Alexey Stashok
 */
@AMXMetadata(type = "thread-pool-mon", group = "monitoring")
@ManagedObject
@Description("Thread Pool Statistics")
public class ThreadPoolStatsProvider implements StatsProvider {

    private final String name;
    protected final CountStatisticImpl maxThreadsCount = new CountStatisticImpl("MaxThreads", "count", "Maximum number of threads allowed in the thread pool");
    protected final CountStatisticImpl coreThreadsCount = new CountStatisticImpl("CoreThreads", "count", "Core number of threads in the thread pool");

    protected final CountStatisticImpl totalExecutedTasksCount = new CountStatisticImpl("TotalExecutedTasksCount", "count", "Provides the total number of tasks, which were executed by the thread pool");
    protected final CountStatisticImpl currentThreadCount = new CountStatisticImpl("CurrentThreadCount", "count", "Provides the number of request processing threads currently in the listener thread pool");
    protected final CountStatisticImpl currentThreadsBusy = new CountStatisticImpl("CurrentThreadsBusy", "count", "Provides the number of request processing threads currently in use in the listener thread pool serving requests");

    protected volatile ThreadPoolStats threadPoolStats;

    public ThreadPoolStatsProvider(String name) {
        this.name = name;
    }

    @Override
    public Object getStatsObject() {
        return threadPoolStats;
    }

    @Override
    public void setStatsObject(Object object) {
        if (object instanceof ThreadPoolStats) {
            threadPoolStats = (ThreadPoolStats) object;
        } else {
            threadPoolStats = null;
        }
    }

    @ManagedAttribute(id = "maxthreads")
    @Description("Maximum number of threads allowed in the thread pool")
    public CountStatistic getMaxThreadsCount() {
        return maxThreadsCount;
    }

    @ManagedAttribute(id = "corethreads")
    @Description("Core number of threads in the thread pool")
    public CountStatistic getCoreThreadsCount() {
        return coreThreadsCount;
    }

    @ManagedAttribute(id = "totalexecutedtasks")
    @Description("Provides the total number of tasks, which were executed by the thread pool")
    public CountStatistic getTotalExecutedTasksCount() {
        return totalExecutedTasksCount;
    }

    @ManagedAttribute(id = "currentthreadcount")
    @Description("Provides the number of request processing threads currently in the listener thread pool")
    public CountStatistic getCurrentThreadCount() {
        if (threadPoolStats != null) {
            currentThreadCount.setCount(threadPoolStats.currentThreadCount);
        }
        return currentThreadCount;
    }

    @ManagedAttribute(id = "currentthreadsbusy")
    @Description("Provides the number of request processing threads currently in use in the listener thread pool serving requests.")
    public CountStatistic getCurrentThreadsBusy() {
        if (threadPoolStats != null) {
            currentThreadsBusy.setCount(threadPoolStats.currentBusyThreadCount.get());
        }
        return currentThreadsBusy;
    }

    @ProbeListener("glassfish:kernel:thread-pool:setMaxThreadsEvent")
    public void setMaxThreadsEvent(
            @ProbeParam("monitoringId") String monitoringId,
            @ProbeParam("threadPoolName") String threadPoolName,
            @ProbeParam("maxNumberOfThreads") int maxNumberOfThreads) {

        if (name.equals(monitoringId)) {
            maxThreadsCount.setCount(maxNumberOfThreads);
        }
    }

    @ProbeListener("glassfish:kernel:thread-pool:setCoreThreadsEvent")
    public void setCoreThreadsEvent(
            @ProbeParam("monitoringId") String monitoringId,
            @ProbeParam("threadPoolName") String threadPoolName,
            @ProbeParam("coreNumberOfThreads") int coreNumberOfThreads) {

        if (name.equals(monitoringId)) {
            coreThreadsCount.setCount(coreNumberOfThreads);
        }
    }

    @ProbeListener("glassfish:kernel:thread-pool:threadAllocatedEvent")
    public void threadAllocatedEvent(
            @ProbeParam("monitoringId") String monitoringId,
            @ProbeParam("threadPoolName") String threadPoolName,
            @ProbeParam("threadId") long threadId) {

        if (name.equals(monitoringId)) {
            currentThreadCount.increment();
        }
    }

    @ProbeListener("glassfish:kernel:thread-pool:threadReleasedEvent")
    public void threadReleasedEvent(
            @ProbeParam("monitoringId") String monitoringId,
            @ProbeParam("threadPoolName") String threadPoolName,
            @ProbeParam("threadId") long threadId) {

        if (name.equals(monitoringId)) {
            currentThreadCount.decrement();
        }
    }

    @ProbeListener("glassfish:kernel:thread-pool:threadDispatchedFromPoolEvent")
    public void threadDispatchedFromPoolEvent(
            @ProbeParam("monitoringId") String monitoringId,
            @ProbeParam("threadPoolName") String threadPoolName,
            @ProbeParam("threadId") long threadId,
            @ProbeParam("busyThreadCount") long busyThreadCount) {

        if (name.equals(monitoringId)) {
            currentThreadsBusy.setCount(busyThreadCount);
        }
    }

    @ProbeListener("glassfish:kernel:thread-pool:threadReturnedToPoolEvent")
    public void threadReturnedToPoolEvent(
            @ProbeParam("monitoringId") String monitoringId,
            @ProbeParam("threadPoolName") String threadPoolName,
            @ProbeParam("threadId") long threadId,
            @ProbeParam("busyThreadCount") long busyThreadCount) {

        if (name.equals(monitoringId)) {
            totalExecutedTasksCount.increment();
            currentThreadsBusy.setCount(busyThreadCount);
        }
    }

    @ProbeListener("glassfish:kernel:thread-pool:setCurrentThreadCountEvent")
    public void setCurrentThreadCountEvent(
            @ProbeParam("monitoringId") String monitoringId,
            @ProbeParam("threadPoolName") String threadPoolName,
            @ProbeParam("currentThreadCount") int currentThreadCount) {
        if (name.equals(monitoringId)) {
            this.currentThreadCount.setCount(currentThreadCount);
        }
    }

    @Reset
    public void reset() {
        if (threadPoolStats != null) {
            if (threadPoolStats.threadPoolConfig != null) {
                maxThreadsCount.setCount(threadPoolStats.threadPoolConfig.getMaxPoolSize());
                coreThreadsCount.setCount(threadPoolStats.threadPoolConfig.getCorePoolSize());
            }
            currentThreadCount.setCount(threadPoolStats.currentThreadCount);
            currentThreadsBusy.setCount(threadPoolStats.currentBusyThreadCount.get());
        }
        totalExecutedTasksCount.setCount(0);
    }
}
