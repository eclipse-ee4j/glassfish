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
 * Probe provider interface for connection queue related events.
 *
 * @author Alexey Stashok
 */
@ProbeProvider(moduleProviderName = "glassfish", moduleName = "kernel", probeProviderName = "connection-queue")
public class ConnectionQueueProbeProvider {

    @Probe(name = "connectionAcceptedEvent")
    public void connectionAcceptedEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("connection") int connectionId,
            @ProbeParam("address") String address) {}

    @Probe(name = "connectionConnectedEvent")
    public void connectionConnectedEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("connection") int connectionId,
            @ProbeParam("address") String address) {}

    @Probe(name = "connectionClosedEvent")
    public void connectionClosedEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("connection") int connectionId) {}

    @Probe(name = "setMaxTaskQueueSizeEvent")
    public void setMaxTaskQueueSizeEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("size") int size) {}

    @Probe(name = "onTaskQueuedEvent")
    public void onTaskQueuedEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("task") String taskId) {}

    @Probe(name = "onTaskDequeuedEvent")
    public void onTaskDequeuedEvent(
            @ProbeParam("listenerName") String listenerName,
            @ProbeParam("task") String taskId) {}

    @Probe(name = "onTaskQueueOverflowEvent")
    public void onTaskQueueOverflowEvent(
            @ProbeParam("listenerName") String listenerName) {}
}
