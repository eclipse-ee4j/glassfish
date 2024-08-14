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
 * Provider interface for HTTP request/response related probes.
 */
@ProbeProvider(moduleProviderName="glassfish", moduleName="web", probeProviderName="http-service")
public class RequestProbeProvider {

    @Probe(name="requestStartEvent")
    public void requestStartEvent(
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName,
        @ProbeParam("serverName") String serverName,
        @ProbeParam("serverPort") int serverPort,
        @ProbeParam("contextPath") String contextPath,
        @ProbeParam("servletPath") String servletPath) {}

    @Probe(name="requestEndEvent")
    public void requestEndEvent(
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName,
        @ProbeParam("serverName") String serverName,
        @ProbeParam("serverPort") int serverPort,
        @ProbeParam("contextPath") String contextPath,
        @ProbeParam("servletPath") String servletPath,
        @ProbeParam("statusCode") int statusCode,
        @ProbeParam("method") String method,
        @ProbeParam("uri") String uri) {}

    @Probe(name="dataReceivedEvent")
    public void dataReceivedEvent(
        @ProbeParam("size") int size,
        @ProbeParam("hostName") String hostName) {}

    @Probe(name="dataSentEvent")
    public void dataSentEvent(
        @ProbeParam("size") long size,
        @ProbeParam("hostName") String hostName) {}

}
