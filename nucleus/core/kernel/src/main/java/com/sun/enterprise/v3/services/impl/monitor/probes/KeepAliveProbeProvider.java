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
 * Probe provider interface for connections keep-alive related events.
 * 
 * @author Alexey Stashok
 */
@ProbeProvider (moduleProviderName="glassfish", moduleName="kernel", probeProviderName="connections-keep-alive")
public class KeepAliveProbeProvider {
    @Probe(name="setMaxCountRequestsEvent")
    public void setMaxCountRequestsEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("maxRequests") int maxRequests) {}

    @Probe(name="setTimeoutInSecondsEvent")
    public void setTimeoutInSecondsEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("timeoutInSeconds") int timeoutInSeconds) {}

    @Probe(name="incrementCountConnectionsEvent")
    public void incrementCountConnectionsEvent(
            @ProbeParam("listenerName") String listenerName) {}

    @Probe(name="decrementCountConnectionsEvent")
    public void decrementCountConnectionsEvent(
            @ProbeParam("listenerName") String listenerName) {}

    @Probe(name="incrementCountFlushesEvent")
    public void incrementCountFlushesEvent(
            @ProbeParam("listenerName") String listenerName) {}

    @Probe(name="incrementCountHitsEvent")
    public void incrementCountHitsEvent(
            @ProbeParam("listenerName") String listenerName) {}

    @Probe(name="incrementCountRefusalsEvent")
    public void incrementCountRefusalsEvent(
            @ProbeParam("listenerName") String listenerName) {}

    @Probe(name="incrementCountTimeoutsEvent")
    public void incrementCountTimeoutsEvent(
            @ProbeParam("listenerName") String listenerName) {}
}
