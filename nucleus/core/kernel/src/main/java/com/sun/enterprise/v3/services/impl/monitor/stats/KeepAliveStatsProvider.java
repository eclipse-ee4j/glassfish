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
import org.glassfish.grizzly.http.KeepAlive;

/**
 * Keep-alive statistics
 *
 * @author Alexey Stashok
 */
@AMXMetadata(type = "keep-alive-mon", group = "monitoring")
@ManagedObject
@Description("Keep-Alive Statistics")
public class KeepAliveStatsProvider implements StatsProvider {

    private final String name;
    protected final CountStatisticImpl maxRequestsCount = new CountStatisticImpl("MaxRequests", "count", "Maximum number of requests allowed on a single keep-alive connection");
    protected final CountStatisticImpl timeoutInSeconds = new CountStatisticImpl("SecondsTimeouts", "seconds", "Keep-alive timeout value in seconds");
    protected final CountStatisticImpl totalKeepAliveConnectionsCount = new CountStatisticImpl("TotalCountConnections", "count", "Total number of keep-alive connections that were accepted");
    protected final CountStatisticImpl keepAliveConnectionsCount = new CountStatisticImpl("CountConnections", "count", "Number of connections in keep-alive mode");
    protected final CountStatisticImpl hitsCount = new CountStatisticImpl("CountHits", "count", "Number of requests received by connections in keep-alive mode");
    protected final CountStatisticImpl refusalsCount = new CountStatisticImpl("CountRefusals", "count", "Number of keep-alive connections that were rejected");
    protected final CountStatisticImpl timeoutsCount = new CountStatisticImpl("CountTimeouts", "count", "Number of keep-alive connections that timed out");
    protected volatile KeepAlive keepAliveStats;

    public KeepAliveStatsProvider(String name) {
        this.name = name;
    }

    @Override
    public Object getStatsObject() {
        return keepAliveStats;
    }

    @Override
    public void setStatsObject(Object object) {
        if (object instanceof KeepAlive) {
            keepAliveStats = (KeepAlive) object;
        } else {
            keepAliveStats = null;
        }
    }

    @ManagedAttribute(id = "maxrequests")
    @Description("Maximum number of requests allowed on a single keep-alive connection")
    public CountStatistic getMaxKeepAliveRequestsCount() {
        return maxRequestsCount;
    }

    @ManagedAttribute(id = "secondstimeouts")
    @Description("Keep-alive timeout value in seconds")
    public CountStatistic getKeepAliveTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    @ManagedAttribute(id = "countconnections")
    @Description("Number of connections in keep-alive mode")
    public CountStatistic getKeepAliveConnectionsCount() {
        return keepAliveConnectionsCount;
    }

    @ManagedAttribute(id = "countflushes")
    @Description("Number of keep-alive connections that were closed")
    public CountStatistic getFlushesCount() {
        final CountStatisticImpl stats = new CountStatisticImpl(
                "CountFlushes",
                "count",
                "Number of keep-alive connections that were closed"
                );
        stats.setCount(Math.max(0, totalKeepAliveConnectionsCount.getCount() - timeoutsCount.getCount()));

        return stats;
    }

    @ManagedAttribute(id = "counthits")
    @Description("Number of requests received by connections in keep-alive mode")
    public CountStatistic getHitsCount() {
        return hitsCount;
    }

    @ManagedAttribute(id = "countrefusals")
    @Description("Number of keep-alive connections that were rejected")
    public CountStatistic getRefusalsCount() {
        return refusalsCount;
    }

    @ManagedAttribute(id = "counttimeouts")
    @Description("Number of keep-alive connections that timed out")
    public CountStatistic getTimeoutsCount() {
        return timeoutsCount;
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:setMaxCountRequestsEvent")
    public void setMaxCountRequestsEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("maxRequests") int max) {
        if (name.equals(listenerName)) {
            maxRequestsCount.setCount(max);
        }
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:setTimeoutInSecondsEvent")
    public void setTimeoutInSecondsEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("timeoutInSeconds") int timeoutInSeconds) {
        if (name.equals(listenerName)) {
            this.timeoutInSeconds.setCount(timeoutInSeconds);
        }
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:incrementCountConnectionsEvent")
    public void incrementCountConnectionsEvent(@ProbeParam("listenerName") String listenerName) {
        if (name.equals(listenerName)) {
            keepAliveConnectionsCount.increment();
            totalKeepAliveConnectionsCount.increment();
        }
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:decrementCountConnectionsEvent")
    public void decrementCountConnectionsEvent(@ProbeParam("listenerName") String listenerName) {
        if (name.equals(listenerName)) {
            keepAliveConnectionsCount.decrement();
        }
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:incrementCountFlushesEvent")
    public void incrementCountFlushesEvent(@ProbeParam("listenerName") String listenerName) {
//        if (name.equals(listenerName)) {
//            flushesCount.increment();
//        }
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:incrementCountHitsEvent")
    public void incrementCountHitsEvent(@ProbeParam("listenerName") String listenerName) {
        if (name.equals(listenerName)) {
            hitsCount.increment();
        }
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:incrementCountRefusalsEvent")
    public void incrementCountRefusalsEvent(@ProbeParam("listenerName") String listenerName) {
        if (name.equals(listenerName)) {
            refusalsCount.increment();
        }
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:incrementCountTimeoutsEvent")
    public void incrementCountTimeoutsEvent(@ProbeParam("listenerName") String listenerName) {
        if (name.equals(listenerName)) {
            timeoutsCount.increment();
        }
    }

    @Reset
    public void reset() {
        if (keepAliveStats != null) {
            maxRequestsCount.setCount(keepAliveStats.getMaxRequestsCount());
            timeoutInSeconds.setCount(keepAliveStats.getIdleTimeoutInSeconds());
        }

        keepAliveConnectionsCount.setCount(0);
        totalKeepAliveConnectionsCount.setCount(0);
        hitsCount.setCount(0);
        refusalsCount.setCount(0);
        timeoutsCount.setCount(0);
    }
}
