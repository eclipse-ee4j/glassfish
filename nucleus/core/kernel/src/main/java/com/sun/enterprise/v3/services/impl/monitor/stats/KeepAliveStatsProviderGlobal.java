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
 * Server wide Keep-alive statistics
 *
 * @author Amy Roh
 */
@AMXMetadata(type = "keep-alive-mon", group = "monitoring")
@ManagedObject
@Description("Keep-Alive Statistics")
public class KeepAliveStatsProviderGlobal extends KeepAliveStatsProvider {

    public KeepAliveStatsProviderGlobal(String name) {
        super(name);
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:setMaxCountRequestsEvent")
    @Override
    public void setMaxCountRequestsEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("maxRequests") int max) {
        maxRequestsCount.setCount(max);
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:setTimeoutInSecondsEvent")
    @Override
    public void setTimeoutInSecondsEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("timeoutInSeconds") int timeoutInSeconds) {
        this.timeoutInSeconds.setCount(timeoutInSeconds);
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:incrementCountConnectionsEvent")
    @Override
    public void incrementCountConnectionsEvent(@ProbeParam("listenerName") String listenerName) {
        keepAliveConnectionsCount.increment();
        totalKeepAliveConnectionsCount.increment();
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:decrementCountConnectionsEvent")
    @Override
    public void decrementCountConnectionsEvent(@ProbeParam("listenerName") String listenerName) {
        keepAliveConnectionsCount.decrement();
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:incrementCountFlushesEvent")
    @Override
    public void incrementCountFlushesEvent(@ProbeParam("listenerName") String listenerName) {
//        flushesCount.increment();
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:incrementCountHitsEvent")
    @Override
    public void incrementCountHitsEvent(@ProbeParam("listenerName") String listenerName) {
        hitsCount.increment();
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:incrementCountRefusalsEvent")
    @Override
    public void incrementCountRefusalsEvent(@ProbeParam("listenerName") String listenerName) {
        refusalsCount.increment();
    }

    @ProbeListener("glassfish:kernel:connections-keep-alive:incrementCountTimeoutsEvent")
    @Override
    public void incrementCountTimeoutsEvent(@ProbeParam("listenerName") String listenerName) {
        timeoutsCount.increment();
    }

}
