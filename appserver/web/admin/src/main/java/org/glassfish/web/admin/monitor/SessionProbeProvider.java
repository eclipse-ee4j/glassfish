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

package org.glassfish.web.admin.monitor;

import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;

/**
 * Provider interface for HTTP session related probes.
 */
@ProbeProvider(moduleProviderName="glassfish", moduleName="web", probeProviderName="session")
public class SessionProbeProvider {

    @Probe(name="sessionCreatedEvent")
    public void sessionCreatedEvent(
        @ProbeParam("sessionId") String sessionId,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}

    @Probe(name="sessionDestroyedEvent")
    public void sessionDestroyedEvent(
        @ProbeParam("sessionId") String sessionId,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}

    @Probe(name="sessionRejectedEvent")
    public void sessionRejectedEvent(
        @ProbeParam("maxThresholdSize") int maxSessions,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}

    @Probe(name="sessionExpiredEvent")
    public void sessionExpiredEvent(
        @ProbeParam("sessionId") String sessionId,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}

    @Probe(name="sessionPersistedStartEvent")
    public void sessionPersistedStartEvent(
        @ProbeParam("sessionId") String sessionId,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}

    @Probe(name="sessionPersistedEndEvent")
    public void sessionPersistedEndEvent(
        @ProbeParam("sessionId") String sessionId,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}

    @Probe(name="sessionActivatedStartEvent")
    public void sessionActivatedStartEvent(
        @ProbeParam("sessionId") String sessionId,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}

    @Probe(name="sessionActivatedEndEvent")
    public void sessionActivatedEndEvent(
        @ProbeParam("sessionId") String sessionId,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}

    @Probe(name="sessionPassivatedStartEvent")
    public void sessionPassivatedStartEvent(
        @ProbeParam("sessionId") String sessionId,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}

    @Probe(name="sessionPassivatedEndEvent")
    public void sessionPassivatedEndEvent(
        @ProbeParam("sessionId") String sessionId,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}
}
