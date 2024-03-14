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

import com.sun.ejb.containers.StatelessSessionContainer;

import org.glassfish.external.statistics.*;
import org.glassfish.external.statistics.impl.*;
import org.glassfish.gmbal.*;

/**
 * Probe listener for the Stateless Session Beans part of the EJB monitoring events.
 *
 * @author Marina Vatkina
 */
@AMXMetadata(type="stateless-session-bean-mon", group="monitoring", isSingleton=false)
@ManagedObject
public class StatelessSessionBeanStatsProvider extends EjbMonitoringStatsProvider {

    private BoundedRangeStatisticImpl methodReadyCount = null;

    private StatelessSessionContainer delegate;

    public StatelessSessionBeanStatsProvider(StatelessSessionContainer delegate, long beanId,
            String appName, String moduleName, String beanName) {

        super(beanId, appName, moduleName, beanName);
        this.delegate = delegate;

        long now = System.currentTimeMillis();

        methodReadyCount = new BoundedRangeStatisticImpl(
            0, 0, 0, delegate.getMaxPoolSize(), delegate.getSteadyPoolSize(),
            "MethodReadyCount", "count", "Number of stateless session beans in MethodReady state",
            now, now);
    }

    @ManagedAttribute(id="methodreadycount")
    @Description( "Number of stateless session beans in MethodReady state")
    public RangeStatistic getMethodReadyCount() {
        methodReadyCount.setCurrent(delegate.getMethodReadyCount());
        return methodReadyCount;
    }

}
