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
 * Provider interface for servlet related probes.
 */
@ProbeProvider(moduleProviderName="glassfish", moduleName="web", probeProviderName="servlet")
public class ServletProbeProvider {

    /**
     * Emits notification that a servlet has been initialized.
     *
     * @param servletName the name of the servlet that was initialized
     * @param appName the name of the app to which the servlet belongs
     * @param hostName the name of the virtual server on which the app has
     * been deployed
     */
    @Probe(name="servletInitializedEvent")
    public void servletInitializedEvent(
        @ProbeParam("servletName") String servletName,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}

    /**
     * Emits notification that a servlet has been destroyed.
     *
     * @param servletName the name of the servlet that was destroyed
     * @param appName the name of the app to which the servlet belongs
     * @param hostName the name of the virtual server on which the app has
     * been deployed
     */
    @Probe(name="servletDestroyedEvent")
    public void servletDestroyedEvent(
        @ProbeParam("servletName") String servletName,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}

    /**
     * Emits notification that a servlet is about to enter its service
     * method.
     *
     * @param servletName the name of the servlet
     * @param appName the name of the app to which the servlet belongs
     * @param hostName the name of the virtual server on which the app has
     * been deployed
     */
    @Probe(name="beforeServiceEvent")
    public void beforeServiceEvent(
        @ProbeParam("servletName") String servletName,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}

    /**
     * Emits notification that a servlet has returned from its service
     * method.
     *
     * @param servletName the name of the servlet
     * @param responseStatus the response status
     * @param appName the name of the app to which the servlet belongs
     * @param hostName the name of the virtual server on which the app has
     * been deployed
     */
    @Probe(name="afterServiceEvent")
    public void afterServiceEvent(
        @ProbeParam("servletName") String servletName,
        @ProbeParam("responseStatus") int responseStatus,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}
}
