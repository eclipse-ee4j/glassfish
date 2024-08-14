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

import com.sun.ejb.containers.EjbContainerUtilImpl;
import com.sun.ejb.containers.util.pool.AbstractPool;

import java.util.logging.Logger;

import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.RangeStatistic;
import org.glassfish.external.statistics.impl.BoundedRangeStatisticImpl;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/**
 * Probe listener for the Ejb Pool monitoring events.
 *
 * @author Marina Vatkina
 */
// TODO: find the right names
// v2: com.sun.appserv:application=MEjbApp,name=bean-pool,type=bean-pool,category=monitor,ejb-module=mejb_jar,server=server,stateless-session-bean=MEJBBean
// v3: amx:pp=/mon/server-mon[server],type=bean-pool-mon,name=??????????
@AMXMetadata(type="bean-pool-mon", group="monitoring", isSingleton=false)
@ManagedObject
@Description("Bean Pool Statistics")
public class EjbPoolStatsProvider {

    private CountStatisticImpl createdStat = new CountStatisticImpl(
            "TotalBeansCreated", "count", "Number of beans created in the associated pool");

    private CountStatisticImpl destroyedStat = new CountStatisticImpl(
            "TotalBeansDestroyed", "count", "Number of beans destroyed from the associated pool");

    private CountStatisticImpl jmsStat = new CountStatisticImpl(
            "JmsMaxMessagesLoad", "count",
            "Provides the maximum number of messages to load into a JMS session, at a time.");

    private BoundedRangeStatisticImpl beansInPool;
    private BoundedRangeStatisticImpl threadsWaiting;

    private static final Logger _logger = EjbContainerUtilImpl.getLogger();

    private String appName = null;
    private String moduleName = null;
    private String beanName = null;
    private boolean registered = false;
    private AbstractPool delegate;
    private long beanId;

    public EjbPoolStatsProvider(AbstractPool delegate,
            long beanId, String appName,
            String moduleName, String beanName) {

        this.delegate = delegate;
        this.beanId = beanId;
        this.appName = appName;
        this.moduleName = moduleName;
        this.beanName = beanName;
        delegate.setInfo(appName, moduleName, beanName);

        long now = System.currentTimeMillis();

        beansInPool = new BoundedRangeStatisticImpl(
            0, 0, 0, delegate.getMaxPoolSize(), delegate.getSteadyPoolSize(),
            "NumBeansInPool", "count", "Number of EJBs in associated pool",
            now, now);
        threadsWaiting = new BoundedRangeStatisticImpl(
            0, 0, 0, Long.MAX_VALUE, 0,
            "NumThreadsWaiting", "count", "Number of threads waiting for free beans",
            now, now);
    }

    public void register() {
        String invokerId = EjbMonitoringUtils.getInvokerId(appName, moduleName, beanName);
        String node = EjbMonitoringUtils.registerSubComponent(
                appName, moduleName, beanName, "bean-pool", this, invokerId);
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

    @ManagedAttribute(id="numbeansinpool")
    @Description( "Number of EJBs in associated pool")
    public RangeStatistic getNumBeansInPool() {
        beansInPool.setCurrent(delegate.getNumBeansInPool());
        return beansInPool;
    }

    @ManagedAttribute(id="numthreadswaiting")
    @Description( "Number of threads waiting for free beans")
    public RangeStatistic getNumThreadsWaiting() {
        threadsWaiting.setCurrent(delegate.getNumThreadsWaiting());
        return threadsWaiting;
    }

    @ManagedAttribute(id="totalbeanscreated")
    @Description( "Number of Beans created in associated pool")
    public CountStatistic getTotalBeansCreated() {
        return createdStat;
    }

    @ManagedAttribute(id="totalbeansdestroyed")
    @Description( "Number of Beans destroyed in associated pool")
    public CountStatistic getTotalBeansDestroyed() {
        return destroyedStat;
    }

    @ManagedAttribute(id="jmsmaxmessagesload")
    @Description( "Provides the maximum number of messages to load into a JMS session, at a time")
    public CountStatistic getJmsMaxMessagesLoad() {
        jmsStat.setCount(delegate.getJmsMaxMessagesLoad());
        return jmsStat;
    }

    @ProbeListener("glassfish:ejb:pool:objectAddedEvent")
    public void ejbObjectAddedEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {
        if (this.beanId == beanId) {
            createdStat.increment();
        } else {
            logWrongEvent(appName, modName, ejbName);
        }
    }

    @ProbeListener("glassfish:ejb:pool:objectAddFailedEvent")
    public void ejbObjectAddFailedEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {
        if (this.beanId == beanId) {
            createdStat.decrement();
        } else {
            logWrongEvent(appName, modName, ejbName);
        }
    }

    @ProbeListener("glassfish:ejb:pool:objectDestroyedEvent")
    public void ejbObjectDestroyedEvent(
            @ProbeParam("beanId") long beanId,
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {
        if (this.beanId == beanId) {
            destroyedStat.increment();
        } else {
            logWrongEvent(appName, modName, ejbName);
        }
    }

    private void logWrongEvent(String appName, String moduleName,
            String beanName) {
        _logger.fine("Recieved event for: [" + this.appName + ":" +
                this.moduleName + ":" + this.beanName + "] but this provider is for [" +
                appName + ":" + moduleName + ":" + beanName+ "]");
    }
}
