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

import com.sun.ejb.containers.StatefulSessionContainer;

import java.util.concurrent.atomic.AtomicLong;

import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.statistics.RangeStatistic;
import org.glassfish.external.statistics.impl.BoundedRangeStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/**
 * Probe listener for the Stateful Session Beans part of the EJB monitoring events.
 *
 * @author Marina Vatkina
 */
@AMXMetadata(type="stateful-session-bean-mon", group="monitoring", isSingleton=false)
@ManagedObject
public class StatefulSessionBeanStatsProvider extends EjbMonitoringStatsProvider {

    private BoundedRangeStatisticImpl methodReadyStat = null;
    private BoundedRangeStatisticImpl passiveCount = null;

    private AtomicLong methodReadyCount = new AtomicLong();
    private AtomicLong passivations = new AtomicLong();

    public StatefulSessionBeanStatsProvider(StatefulSessionContainer delegate,
            long beanId, String appName, String moduleName, String beanName) {

        super(beanId, appName, moduleName, beanName);

        long now = System.currentTimeMillis();

        methodReadyStat = new BoundedRangeStatisticImpl(
            0, 0, 0, delegate.getMaxCacheSize(), 0,
            "MethodReadyCount", "count", "Number of stateful session beans in MethodReady state",
            now, now);

        passiveCount = new BoundedRangeStatisticImpl(
            0, 0, 0, Long.MAX_VALUE, 0,
            "PassiveCount", "count", "Number of stateful session beans in Passive state",
            now, now);
    }

    @ManagedAttribute(id="methodreadycount")
    @Description( "Number of stateful session beans in MethodReady state")
    public RangeStatistic getMethodReadyCount() {
        methodReadyStat.setCurrent(methodReadyCount.get());
        return methodReadyStat;
    }

    @ManagedAttribute(id="passivecount")
    @Description( "Number of stateful session beans in Passive state")
    public RangeStatistic getPassiveCount() {
        passiveCount.setCurrent(passivations.get());
        return passiveCount;
    }

    @ProbeListener("glassfish:ejb:bean:methodReadyAddEvent")
    public void methodReadyAddEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {
        if (this.beanId == beanId) {
            log ("methodReadyAddEvent", "StatefulSessionBeanStatsProvider");
            methodReadyCount.incrementAndGet();
        }
    }

    @ProbeListener("glassfish:ejb:bean:methodReadyRemoveEvent")
    public void methodReadyRemoveEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {
        if (this.beanId == beanId) {
            log ("methodReadyRemoveEvent", "StatefulSessionBeanStatsProvider");
            methodReadyCount.decrementAndGet();
        }
    }

    @ProbeListener("glassfish:ejb:cache:beanPassivatedEvent")
    public void ejbBeanPassivatedEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName,
            @ProbeParam("success") boolean success) {
        if (this.beanId == beanId && success) {
            log ("beanPassivatedEvent", "StatefulSessionBeanStatsProvider");
            passivations.incrementAndGet();
        }
    }

    @ProbeListener("glassfish:ejb:cache:expiredSessionsRemovedEvent")
    public void ejbExpiredSessionsRemovedEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName,
            @ProbeParam("num") long num) {
        if (this.beanId == beanId) {
            log ("expiredSessionsRemovedEvent", "StatefulSessionBeanStatsProvider");
            passivations.addAndGet(-num);
        }
    }

    public void setPassiveCount(long passiveCount) {
        passivations.set(passiveCount);
    }
}
