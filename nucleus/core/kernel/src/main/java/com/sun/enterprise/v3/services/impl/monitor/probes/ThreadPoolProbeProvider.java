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

package com.sun.enterprise.v3.services.impl.monitor.probes;

import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;

/**
 * Probe provider interface for thread pool related events.
 */
@ProbeProvider (moduleProviderName="glassfish", moduleName="kernel", probeProviderName="thread-pool")
public class ThreadPoolProbeProvider {

    @Probe(name="setMaxThreadsEvent")
    public void setMaxThreadsEvent(
        @ProbeParam("monitoringId") String monitoringId,
        @ProbeParam("threadPoolName") String threadPoolName,
        @ProbeParam("maxNumberOfThreads") int maxNumberOfThreads) {}


    @Probe(name="setCoreThreadsEvent")
    public void setCoreThreadsEvent(
        @ProbeParam("monitoringId") String monitoringId,
        @ProbeParam("threadPoolName") String threadPoolName,
        @ProbeParam("coreNumberOfThreads") int coreNumberOfThreads) {}

    /**
     * Emits notification that new thread was created and added to the
     * thread pool.
     */
    @Probe(name="threadAllocatedEvent")
    public void threadAllocatedEvent(
        @ProbeParam("monitoringId") String monitoringId,
        @ProbeParam("threadPoolName") String threadPoolName,
        @ProbeParam("threadId") long threadId) {}


    @Probe(name="threadReleasedEvent")
    public void threadReleasedEvent(
        @ProbeParam("monitoringId") String monitoringId,
        @ProbeParam("threadPoolName") String threadPoolName,
        @ProbeParam("threadId") long threadId) {}


    @Probe(name="maxNumberOfThreadsReachedEvent")
    public void maxNumberOfThreadsReachedEvent(
        @ProbeParam("monitoringId") String monitoringId,
        @ProbeParam("threadPoolName") String threadPoolName,
        @ProbeParam("maxNumberOfThreads") int maxNumberOfThreads) {}


    @Probe(name="threadDispatchedFromPoolEvent")
    public void threadDispatchedFromPoolEvent(
        @ProbeParam("monitoringId") String monitoringId,
        @ProbeParam("threadPoolName") String threadPoolName,
        @ProbeParam("threadId") long threadId) {}


    @Probe(name="threadReturnedToPoolEvent")
    public void threadReturnedToPoolEvent(
        @ProbeParam("monitoringId") String monitoringId,
        @ProbeParam("threadPoolName") String threadPoolName,
        @ProbeParam("threadId") long threadId) {}

    @Probe(name="setCurrentThreadCountEvent")
    public void setCurrentThreadCountEvent(
        @ProbeParam("monitoringId") String monitoringId,
        @ProbeParam("threadPoolName") String threadPoolName,
        @ProbeParam("currentThreadCount") int currentThreadCount) {}
}
