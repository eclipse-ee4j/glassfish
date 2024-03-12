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

package com.sun.ejb.monitoring.stats;

import java.util.logging.Logger;

import com.sun.ejb.containers.EjbContainerUtilImpl;

import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.external.probe.provider.annotations.*;
import org.glassfish.external.statistics.*;
import org.glassfish.external.statistics.impl.*;
import org.glassfish.gmbal.*;

/**
 * Probe listener for the Ejb Timed Object monitoring events.
 *
 * @author Marina Vatkina
 */
@AMXMetadata(type="ejb-timed-object-mon", group="monitoring", isSingleton=false)
@ManagedObject
@Description("Ejb Timed Object Statistics")
public class EjbTimedObjectStatsProvider {

    private CountStatisticImpl timerCreateStat = new CountStatisticImpl("NumTimersCreated",
            "count", "Number of timers created in the system");

    private CountStatisticImpl timerRemoveStat = new CountStatisticImpl("NumTimersRemoved",
            "count", "Number of timers removed from the system");

    private CountStatisticImpl timerDeliveredStat = new CountStatisticImpl("NumTimersDelivered",
            "count", "Number of timers delivered by the system");

    private static final Logger _logger = EjbContainerUtilImpl.getLogger();

    private String appName = null;
    private String moduleName = null;
    private String beanName = null;
    private boolean registered = false;

    public EjbTimedObjectStatsProvider(String appName, String moduleName,
            String beanName) {
        this.appName = appName;
        this.moduleName = moduleName;
        this.beanName = beanName;
    }

    public void register() {
        String invokerId = EjbMonitoringUtils.getInvokerId(appName, moduleName, beanName);
        String node = EjbMonitoringUtils.registerSubComponent(
                appName, moduleName, beanName, "timers", this, invokerId);
        if (node != null) {
            registered = true;
        }
    }

    public void unregister() {
        if (registered) {
            registered = false;
            StatsProviderManager.unregister(this);
        }
    }

    @ProbeListener("glassfish:ejb:timers:timerCreatedEvent")
    public void ejbTimerCreatedEvent() {
        _logger.fine("=== timerCreatedEvent");
        timerCreateStat.increment();
    }

    @ProbeListener("glassfish:ejb:timers:timerRemovedEvent")
    public void ejbTimerRemovedEvent() {
        _logger.fine("=== timerRemovedEvent");
        timerRemoveStat.increment();
    }

    @ProbeListener("glassfish:ejb:timers:timerDeliveredEvent")
    public void ejbTimerDeliveredEvent() {
        _logger.fine("=== timerDeliveredEvent");
        timerDeliveredStat.increment();
    }

    @ManagedAttribute(id="numtimerscreated")
    @Description( "Number of timers created in the system")
    public CountStatistic getNumTimersCreated() {
        return timerCreateStat;
    }

    @ManagedAttribute(id="numtimersremoved")
    @Description( "Number of timers removed from the system")
    public CountStatistic getNumTimersRemoved() {
        return timerRemoveStat;
    }

    @ManagedAttribute(id="numtimersdelivered")
    @Description( "Number of timers delivered by the system")
    public CountStatistic getNumTimersDelivered() {
        return timerDeliveredStat;
    }
}
