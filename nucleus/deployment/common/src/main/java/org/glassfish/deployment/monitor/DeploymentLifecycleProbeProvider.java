/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.deployment.monitor;

import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;

/**
 * Provider interface for deployment lifecycle related probe events.
 */
@ProbeProvider(moduleProviderName="glassfish", moduleName="deployment", probeProviderName="lifecycle")
public class DeploymentLifecycleProbeProvider {

    /*
     * Emits probe event that the application with
     * <code>appName</code> and <code>appType</code> has been deployed.
     *
     * @param appName the name of the application has been deployed
     * @param appType the type of the application has been deployed
     * @param loadTime the time it took for this application to load
     *
     */
    @Probe(name="applicationDeployedEvent")
    public void applicationDeployedEvent(
        @ProbeParam("appName") String appName,
        @ProbeParam("appType") String appType,
        @ProbeParam("loadTime") String loadTime) {}


    /**
     * Emits probe event that the application with the given
     * <code>appName</code> and <code>appType</code> has been undeployed.
     *
     * @param appName the name of the application has been undeployed
     * @param appType the type of the application has been undeployed
     *
     */
    @Probe(name="applicationUndeployedEvent")
    public void applicationUndeployedEvent(
        @ProbeParam("appName") String appName,
        @ProbeParam("appType") String appType) {}
}
