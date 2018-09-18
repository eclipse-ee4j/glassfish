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
 * Provider interface for web module lifecycle related probe events.
 */
@ProbeProvider(moduleProviderName="glassfish", moduleName="web", probeProviderName="web-module")
public class WebModuleProbeProvider {

    /**
     * Emits probe event that the web module with the given
     * <code>appName</code> has been loaded on the virtual server with
     * the given <code>hostName</code>.
     *
     * @param appName
     */
    @Probe(name="webModuleStartedEvent")
    public void webModuleStartedEvent(
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}


    /**
     * Emits probe event that the web module with the given
     * <code>appName</code> has been unloaded from the virtual server with
     * the given <code>hostName</code>.
     */
    @Probe(name="webModuleStoppedEvent")
    public void webModuleStoppedEvent(
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName) {}
}
