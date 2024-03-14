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

package org.glassfish.persistence.ejb.entitybean.container.stats;

import org.glassfish.persistence.ejb.entitybean.container.EntityContainer;

import com.sun.ejb.monitoring.stats.EjbMonitoringStatsProvider;
import org.glassfish.external.statistics.*;
import org.glassfish.external.statistics.impl.*;
import org.glassfish.gmbal.*;

/**
 * Probe listener for the Entity Beans part of the EJB monitoring events.
 *
 * @author Marina Vatkina
 */
@AMXMetadata(type="entity-bean-mon", group="monitoring", isSingleton=false)
@ManagedObject
public class EntityBeanStatsProvider extends EjbMonitoringStatsProvider {

    private BoundedRangeStatisticImpl pooledCount = null;
    private BoundedRangeStatisticImpl readyCount = null;

    private EntityContainer delegate;

    public EntityBeanStatsProvider(EntityContainer delegate, long beanId, String appName,
            String moduleName, String beanName) {

        super(beanId, appName, moduleName, beanName);
        this.delegate = delegate;

        long now = System.currentTimeMillis();

        pooledCount = new BoundedRangeStatisticImpl(
            0, 0, 0, delegate.getMaxPoolSize(), delegate.getSteadyPoolSize(),
            "PooledCount", "count", "Number of entity beans in pooled state",
            now, now);
        readyCount = new BoundedRangeStatisticImpl(
            0, 0, 0, delegate.getMaxCacheSize(), 0,
            "ReadyCount", "count", "Number of entity beans in ready state",
            now, now);
    }

    @ManagedAttribute(id="pooledcount")
    @Description( "Number of entity beans in pooled state")
    public RangeStatistic getPooledCount() {
        pooledCount.setCurrent(delegate.getPooledCount());
        return pooledCount;
    }

    @ManagedAttribute(id="readycount")
    @Description( "Number of entity beans in ready state")
    public RangeStatistic getReadyCount() {
        readyCount.setCurrent(delegate.getReadyCount());
        return readyCount;
    }
}
