/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.monitoring.probes;

import org.glassfish.external.probe.provider.annotations.*;

/**
 * Probe emitter for the Ejb monitoring events. Used by the probe framework as an event notifier.
 *
 * @author Marina Vatkina
 */
@ProbeProvider(moduleProviderName="glassfish", moduleName="ejb", probeProviderName="bean")
public class EjbMonitoringProbeProvider {

    @Probe(name="containerEnteringEvent")
    public void ejbContainerEnteringEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {}

    @Probe(name="containerLeavingEvent")
    public void ejbContainerLeavingEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {}

    @Probe(name="methodStartEvent", hidden=true)
    public void ejbMethodStartEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName,
            @ProbeParam("method") String method_sig) {}

    @Probe(name="methodEndEvent", hidden=true)
    public void ejbMethodEndEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName,
            @ProbeParam("exception") Throwable exception,
            @ProbeParam("method") String method_sig) {}

    @Probe(name="beanCreatedEvent")
    public void ejbBeanCreatedEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {}

    @Probe(name="beanDestroyedEvent")
    public void ejbBeanDestroyedEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {}

    @Probe(name="messageDeliveredEvent")
    public void messageDeliveredEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {}

    @Probe(name="methodReadyAddEvent")
    public void methodReadyAddEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {}

    @Probe(name="methodReadyRemoveEvent")
    public void methodReadyRemoveEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {}

}
