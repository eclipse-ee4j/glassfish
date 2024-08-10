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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.annotations.Reset;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;

/**
 * Connection Queue statistics
 *
 * @author Alexey Stashok
 */
@AMXMetadata(type = "connection-queue-mon", group = "monitoring")
@ManagedObject
@Description("Connection Queue Statistics")
public class ConnectionQueueStatsProvider implements StatsProvider {
    protected static final long MINUTE = 60 * 1000;

    private final String name;

    protected final CountStatisticImpl countTotalConnections = new CountStatisticImpl("CountTotalConnections", "count", "Total number of connections that have been accepted");
    protected final Map<Integer, Long> openConnectionsCount = new ConcurrentHashMap<Integer, Long>();

    protected final CountStatisticImpl countOverflows = new CountStatisticImpl("CountOverflows", "count", "Number of times the queue has been too full to accommodate a connection");

    protected final AtomicInteger countQueuedAtomic = new AtomicInteger();
    protected final CountStatisticImpl countQueued = new CountStatisticImpl("CountQueued", "count", "Number of connections currently in the queue");

    protected final CountStatisticImpl countTotalQueued = new CountStatisticImpl("CountTotalQueued", "count", "Total number of connections that have been queued");

    protected final CountStatisticImpl maxQueued = new CountStatisticImpl("MaxQueued", "count", "Maximum size of the connection queue");

    protected final AtomicInteger peakQueuedAtomic = new AtomicInteger();
    protected final CountStatisticImpl peakQueued = new CountStatisticImpl("PeakQueued", "count", "Largest number of connections that were in the queue simultaneously");

    protected final CountStatisticImpl ticksTotalQueued = new CountStatisticImpl("TicksTotalQueued", "count", "(Unsupported) Total number of ticks that connections have spent in the queue");

    protected final int[] averageStatsPerMinute = new int[15];
    protected long averageLastShift;
    protected int averageMinuteCounter;

    protected volatile ThreadPoolConfig threadPoolConfig;

    public ConnectionQueueStatsProvider(String name) {
        this.name = name;
    }

    @Override
    public Object getStatsObject() {
        return threadPoolConfig;
    }

    @Override
    public void setStatsObject(Object object) {
        if (object instanceof ThreadPoolConfig) {
            threadPoolConfig = (ThreadPoolConfig) object;
        } else {
            threadPoolConfig = null;
        }
    }

    @ManagedAttribute(id = "counttotalconnections")
    @Description("Total number of connections that have been accepted")
    public CountStatistic getTotalConnectionsCount() {
        return countTotalConnections;
    }

    @ManagedAttribute(id = "countopenconnections")
    @Description("The number of open/active connections")
    public CountStatistic getOpenConnectionsCount() {
        final CountStatisticImpl stats =
                new CountStatisticImpl("CountOpenConnections",
                "count", "The number of open/active connections");
        stats.setCount(openConnectionsCount.size());
        return stats;
    }

    @ManagedAttribute(id = "countoverflows")
    @Description("Number of times the queue has been too full to accommodate a connection")
    public CountStatistic getCountOverflows() {
        return countOverflows;
    }

    @ManagedAttribute(id = "countqueued")
    @Description("Number of connections currently in the queue")
    public CountStatistic getCountQueued() {
        return countQueued;
    }

    @ManagedAttribute(id = "countqueued1minuteaverage")
    @Description("Average number of connections queued in the last 1 minute")
    public CountStatistic getCountQueued1MinuteAverage() {
        final CountStatisticImpl stats = new CountStatisticImpl(
                "CountQueued1MinuteAverage", "count",
                "Average number of connections queued in the last 1 minute");
        stats.setCount(getAverageBy(1));
        return stats;
    }

    @ManagedAttribute(id = "countqueued5minutesaverage")
    @Description("Average number of connections queued in the last 5 minutes")
    public CountStatistic getCountQueued5MinutesAverage() {
        final CountStatisticImpl stats = new CountStatisticImpl(
                "CountQueued5MinutesAverage", "count",
                "Average number of connections queued in the last 5 minutes");
        stats.setCount(getAverageBy(5));
        return stats;
    }

    @ManagedAttribute(id = "countqueued15minutesaverage")
    @Description("Average number of connections queued in the last 15 minutes")
    public CountStatistic getCountQueued15MinutesAverage() {
        final CountStatisticImpl stats = new CountStatisticImpl(
                "CountQueued15MinutesAverage", "count",
                "Average number of connections queued in the last 15 minutes");
        stats.setCount(getAverageBy(15));
        return stats;
    }

    @ManagedAttribute(id = "counttotalqueued")
    @Description("Total number of connections that have been queued")
    public CountStatistic getCountTotalQueued() {
        return countTotalQueued;
    }

    @ManagedAttribute(id = "maxqueued")
    @Description("Maximum size of the connection queue")
    public CountStatistic getMaxQueued() {
        return maxQueued;
    }

    @ManagedAttribute(id = "peakqueued")
    @Description("Largest number of connections that were in the queue simultaneously")
    public CountStatistic getPeakQueued() {
        return peakQueued;
    }

    @ManagedAttribute(id = "tickstotalqueued")
    @Description("(Unsupported) Total number of ticks that connections have spent in the queue")
    public CountStatistic getTicksTotalQueued() {
        return ticksTotalQueued;
    }

    // ---------------- Connection related listeners -----------
    @ProbeListener("glassfish:kernel:connection-queue:connectionAcceptedEvent")
    public void connectionAcceptedEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("connection") int connectionId,
            @ProbeParam("address") String address) {

        if (name.equals(listenerName)) {
            countTotalConnections.increment();
            openConnectionsCount.put(connectionId, System.currentTimeMillis());
        }
    }

// We're not interested in client connections, created via Grizzly
//    @ProbeListener("glassfish:kernel:connection-queue:connectionConnectedEvent")
//    public void connectionConnectedEvent(
//            @ProbeParam("listenerName") String listenerName,
//            @ProbeParam("connection") int connectionId,
//            @ProbeParam("address") String address) {
//    }
    @ProbeListener("glassfish:kernel:connection-queue:connectionClosedEvent")
    public void connectionClosedEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("connection") int connectionId) {
        if (name.equals(listenerName)) {
            openConnectionsCount.remove(connectionId);
        }
    }

    // -----------------------------------------------------------------------

    @ProbeListener("glassfish:kernel:connection-queue:setMaxTaskQueueSizeEvent")
    public void setMaxTaskQueueSizeEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("size") int size) {
        if (name.equals(listenerName)) {
            maxQueued.setCount(size);
        }
    }

    @ProbeListener("glassfish:kernel:connection-queue:onTaskQueuedEvent")
    public void onTaskQueuedEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("task") String taskId) {
        if (name.equals(listenerName)) {
            final int queued = countQueuedAtomic.incrementAndGet();
            countQueued.setCount(queued);

            do {
                final int peakQueue = peakQueuedAtomic.get();
                if (queued <= peakQueue) break;

                if (peakQueuedAtomic.compareAndSet(peakQueue, queued)) {
                    synchronized (peakQueuedAtomic) {
                        peakQueued.setCount(peakQueuedAtomic.get());
                        break;
                    }
                }
            } while (true);

            countTotalQueued.increment();

            incAverageMinute();
        }
    }

    @ProbeListener("glassfish:kernel:connection-queue:onTaskDequeuedEvent")
    public void onTaskDequeuedEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("task") String taskId) {
        if (name.equals(listenerName)) {
            countQueued.setCount(countQueuedAtomic.decrementAndGet());
        }
    }

    @ProbeListener("glassfish:kernel:connection-queue:onTaskQueueOverflowEvent")
    public void onTaskQueueOverflowEvent(
            @ProbeParam("listenerName") String listenerName) {
        if (name.equals(listenerName)) {
            countOverflows.increment();
        }
    }

    protected void incAverageMinute() {
        synchronized(averageStatsPerMinute) {
            final long currentTime = System.currentTimeMillis();
            if (currentTime - averageLastShift >= MINUTE) {
                shiftAverage(currentTime);
            }

            averageMinuteCounter++;
        }
    }

    protected int getAverageBy(int mins) {
        synchronized(averageStatsPerMinute) {
            final long currentTime = System.currentTimeMillis();
            if (currentTime - averageLastShift >= MINUTE) {
                shiftAverage(currentTime);
            }

            int result = 0;
            final int statsToCount = Math.min(mins, averageStatsPerMinute.length);
            for(int i = 0; i < statsToCount; i++) {
                result += averageStatsPerMinute[i];
            }

            return result;
        }
    }

    private void shiftAverage(long currentTime) {
        final int shift = (int) ((currentTime - averageLastShift) / MINUTE);
        if (shift == 0) {
            return;
        }

        final int statsSize = averageStatsPerMinute.length;
        for(int i = statsSize - 1; i >= 0; i--) {
            final int newIndex = shift + i;
            if (newIndex < statsSize) {
                averageStatsPerMinute[newIndex] = averageStatsPerMinute[i];
            }

            averageStatsPerMinute[i] = 0;
        }

        if (shift <= statsSize) {
            averageStatsPerMinute[shift - 1] = averageMinuteCounter;
        }

        averageMinuteCounter = 0;
        averageLastShift += (shift * MINUTE);
    }

    @Reset
    public void reset() {
        countTotalConnections.setCount(0);
        openConnectionsCount.clear();
        countOverflows.setCount(0);

        countQueuedAtomic.set(0);
        countQueued.setCount(0);

        countTotalQueued.setCount(0);
        if (threadPoolConfig != null) {
            maxQueued.setCount(threadPoolConfig.getQueueLimit());
        }

        peakQueuedAtomic.set(0);
        peakQueued.setCount(0);

        ticksTotalQueued.setCount(0);

        averageLastShift = 0;
        averageMinuteCounter = 0;

        for (int i = 0; i < averageStatsPerMinute.length; i++) {
            averageStatsPerMinute[i] = 0;
        }
    }
}
