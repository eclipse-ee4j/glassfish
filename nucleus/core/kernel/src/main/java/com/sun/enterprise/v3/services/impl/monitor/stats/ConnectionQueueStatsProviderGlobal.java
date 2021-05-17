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
 * Server wide Connection Queue statistics
 *
 * @author Amy Roh
 */
@AMXMetadata(type = "connection-queue-mon", group = "monitoring")
@ManagedObject
@Description("Connection Queue Statistics")
public class ConnectionQueueStatsProviderGlobal extends ConnectionQueueStatsProvider {

    public ConnectionQueueStatsProviderGlobal(String name) {
        super(name);
    }

    // ---------------- Connection related listeners -----------
    @ProbeListener("glassfish:kernel:connection-queue:connectionAcceptedEvent")
    public void connectionAcceptedEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("connection") int connectionId,
            @ProbeParam("address") String address) {
        countTotalConnections.increment();
        openConnectionsCount.put(connectionId, System.currentTimeMillis());
    }

    @ProbeListener("glassfish:kernel:connection-queue:connectionClosedEvent")
    public void connectionClosedEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("connection") int connectionId) {
        openConnectionsCount.remove(connectionId);
    }

    // -----------------------------------------------------------------------

    @ProbeListener("glassfish:kernel:connection-queue:setMaxTaskQueueSizeEvent")
    public void setMaxTaskQueueSizeEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("size") int size) {
        maxQueued.setCount(size);
    }

    @ProbeListener("glassfish:kernel:connection-queue:onTaskQueuedEvent")
    public void onTaskQueuedEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("task") String taskId) {
        final int queued = countQueuedAtomic.incrementAndGet();
        countQueued.setCount(queued);

        do {
            final int peakQueue = peakQueuedAtomic.get();
            if (queued <= peakQueue) break;

            if (peakQueuedAtomic.compareAndSet(peakQueue, queued)) {
                peakQueued.setCount(queued);
                break;
            }
        } while (true);

        countTotalQueued.increment();

        incAverageMinute();
    }

    @ProbeListener("glassfish:kernel:connection-queue:onTaskDequeuedEvent")
    public void onTaskDequeuedEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("task") String taskId) {
        countQueued.setCount(countQueuedAtomic.decrementAndGet());
    }

    @ProbeListener("glassfish:kernel:connection-queue:onTaskQueueOverflowEvent")
    public void onTaskQueueOverflowEvent(
            @ProbeParam("listenerName") String listenerName) {
        countOverflows.increment();
    }

}
