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
 * Probe listener for the Ejb Cache monitoring events.
 *
 * @author Marina Vatkina
 */
// TODO: find the right names
// v2: com.sun.appserv:application=__ejb_container_timer_app,name=bean-cache,type=bean-cache,category=monitor,ejb-module=ejb_jar,entity-bean=TimerBean,server=server
// v3: amx:pp=/mon/server-mon[server],type=bean-cache-mon,name=??????????
@AMXMetadata(type="bean-cache-mon", group="monitoring", isSingleton=false)
@ManagedObject
@Description("Bean Cache Statistics")
public class EjbCacheStatsProvider {

    private CountStatisticImpl expiredSessionsRemovedStat = new CountStatisticImpl(
            "NumExpiredSessionsRemoved", "count",
            "Provides a count value reflecting the number of expired sessions "
                + "that were removed from the bean cache.");

    private CountStatisticImpl passivations = new CountStatisticImpl(
            "NumPassivations", "count",
            "Provides a count value reflecting the number of passivations for a "
                + "StatefulSessionBean from the bean cache.");

    private CountStatisticImpl passivationErrors = new CountStatisticImpl(
            "NumPassivationErrors", "count",
            "Provides a count value reflecting the number of errors that occured "
                + "while passivating a StatefulSessionBean from the bean cache.");

    private CountStatisticImpl passivationSuccess = new CountStatisticImpl(
            "NumPassivationSuccess", "count",
            "Provides a count value reflecting the number of passivations for a "
                + "StatefulSessionBean from the bean cache that succeeded");

    private BoundedRangeStatisticImpl cacheHits;
    private BoundedRangeStatisticImpl cacheMisses;
    private BoundedRangeStatisticImpl numBeans;

    private static final Logger _logger = EjbContainerUtilImpl.getLogger();

    private long beanId;
    private String appName = null;
    private String moduleName = null;
    private String beanName = null;
    private boolean registered = false;
    private EjbCacheStatsProviderDelegate delegate;

    public EjbCacheStatsProvider(EjbCacheStatsProviderDelegate delegate, long beanId,
            String appName, String moduleName, String beanName) {

        this.delegate = delegate;
        this.beanId = beanId;
        this.appName = appName;
        this.moduleName = moduleName;
        this.beanName = beanName;

        long now = System.currentTimeMillis();

        numBeans = new BoundedRangeStatisticImpl(
            0, 0, 0, delegate.getMaxCacheSize(), 0, "NumBeansInCache", "count",
            "Provides total number of EJBs in the associated EJB Cache.",
            now, now);
        cacheHits = new BoundedRangeStatisticImpl(
            0, 0, 0, Long.MAX_VALUE, 0, "CacheHits", "count",
            "Provides the number of times a user request hits an EJB in associated EJB cache instance",
            now, now);
        cacheMisses = new BoundedRangeStatisticImpl(
            0, 0, 0, Long.MAX_VALUE, 0, "CacheMisses", "count",
            "Provides the number of times a user request fails to find an EJB in associated EJB cache instance",
            now, now);
    }

    public void register() {
        String invokerId = EjbMonitoringUtils.getInvokerId(appName, moduleName, beanName);
        String node = EjbMonitoringUtils.registerSubComponent(
                appName, moduleName, beanName, "bean-cache", this, invokerId);
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

    @ManagedAttribute(id="cachemisses")
    @Description( "Number of times a user request fails to find an EJB in associated EJB cache instance")
    public RangeStatistic getCacheMisses() {
        cacheMisses.setCurrent(delegate.getCacheMisses());
        return cacheMisses;
    }

    @ManagedAttribute(id="cachehits")
    @Description( "Number of times a user request hits an EJB in associated EJB cache instance")
    public RangeStatistic getCacheHits() {
        cacheHits.setCurrent(delegate.getCacheHits());
        return cacheHits;
    }

    @ManagedAttribute(id="numbeansincache")
    @Description( "Number of EJBs in the associated EJB Cache")
    public RangeStatistic getNumBeansInCache() {
        numBeans.setCurrent(delegate.getNumBeansInCache());
        return numBeans;
    }

    @ManagedAttribute(id="numexpiredsessionsremoved")
    @Description( "Number of expired sessions removed by the cleanup thread.")
    public CountStatistic getNumExpiredSessionsRemoved() {
        return expiredSessionsRemovedStat;
    }

    @ManagedAttribute(id="numpassivations")
    @Description( "Number of passivated beans")
    public CountStatistic getNumPassivations() {
        return passivations;
    }

    @ManagedAttribute(id="numpassivationerrors")
    @Description( "Number of errors during passivation.")
    public CountStatistic getNumPassivationErrors() {
        return passivationErrors;
    }

    @ManagedAttribute(id="numpassivationsuccess")
    @Description( "Number of times passivation completed successfully.")
    public CountStatistic getNumPassivationSuccess() {
        return passivationSuccess;
    }

    @ProbeListener("glassfish:ejb:cache:beanPassivatedEvent")
    public void ejbBeanPassivatedEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName,
            @ProbeParam("success") boolean success) {
        if (this.beanId == beanId) {
            passivations.increment();
            if (success) {
                passivationSuccess.increment();
            } else {
                passivationErrors.increment();
            }
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
            expiredSessionsRemovedStat.increment(num);
        }
    }
}
