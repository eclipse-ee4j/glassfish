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
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedObject;

/**
 * Server wide Thread Pool statistics
 *
 * @author Amy Roh
 */
@AMXMetadata(type = "thread-pool-mon", group = "monitoring")
@ManagedObject
@Description("Thread Pool Statistics")
public class ThreadPoolStatsProviderGlobal extends ThreadPoolStatsProvider {

    public ThreadPoolStatsProviderGlobal(String name) {
        super(name);
    }

    @ProbeListener("glassfish:kernel:thread-pool:setMaxThreadsEvent")
    public void setMaxThreadsEvent(
            @ProbeParam("monitoringId") String monitoringId,
            @ProbeParam("threadPoolName") String threadPoolName,
            @ProbeParam("maxNumberOfThreads") int maxNumberOfThreads) {

        maxThreadsCount.setCount(maxNumberOfThreads);
    }

    @ProbeListener("glassfish:kernel:thread-pool:setCoreThreadsEvent")
    public void setCoreThreadsEvent(
            @ProbeParam("monitoringId") String monitoringId,
            @ProbeParam("threadPoolName") String threadPoolName,
            @ProbeParam("coreNumberOfThreads") int coreNumberOfThreads) {

        coreThreadsCount.setCount(coreNumberOfThreads);
    }

    @ProbeListener("glassfish:kernel:thread-pool:threadAllocatedEvent")
    public void threadAllocatedEvent(
            @ProbeParam("monitoringId") String monitoringId,
            @ProbeParam("threadPoolName") String threadPoolName,
            @ProbeParam("threadId") long threadId) {

        currentThreadCount.increment();
    }

    @ProbeListener("glassfish:kernel:thread-pool:threadReleasedEvent")
    public void threadReleasedEvent(
            @ProbeParam("monitoringId") String monitoringId,
            @ProbeParam("threadPoolName") String threadPoolName,
            @ProbeParam("threadId") long threadId) {

        currentThreadCount.decrement();
    }

    @ProbeListener("glassfish:kernel:thread-pool:threadDispatchedFromPoolEvent")
    public void threadDispatchedFromPoolEvent(
            @ProbeParam("monitoringId") String monitoringId,
            @ProbeParam("threadPoolName") String threadPoolName,
            @ProbeParam("threadId") long threadId,
            @ProbeParam("busyThreadCount") long busyThreadCount) {

        currentThreadsBusy.setCount(busyThreadCount);
    }

    @ProbeListener("glassfish:kernel:thread-pool:threadReturnedToPoolEvent")
    public void threadReturnedToPoolEvent(
            @ProbeParam("monitoringId") String monitoringId,
            @ProbeParam("threadPoolName") String threadPoolName,
            @ProbeParam("threadId") long threadId,
            @ProbeParam("busyThreadCount") long busyThreadCount) {

        totalExecutedTasksCount.increment();
        currentThreadsBusy.setCount(busyThreadCount);
    }

    @ProbeListener("glassfish:kernel:thread-pool:setCurrentThreadCountEvent")
    public void setCurrentThreadCountEvent(
            @ProbeParam("monitoringId") String monitoringId,
            @ProbeParam("threadPoolName") String threadPoolName,
            @ProbeParam("currentThreadCount") int currentThreadCount) {
        this.currentThreadCount.setCount(currentThreadCount);
    }

}
