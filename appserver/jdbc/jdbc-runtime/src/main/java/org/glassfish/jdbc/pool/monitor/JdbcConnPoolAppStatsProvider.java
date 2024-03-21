/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.jdbc.pool.monitor;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.RangeStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.external.statistics.impl.RangeStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.resourcebase.resources.api.PoolInfo;

import static org.glassfish.external.statistics.impl.StatisticImpl.UNIT_COUNT;

/**
 * StatsProvider object for Jdbc pool monitoring grouped by application names.
 *
 * Implements various events related to jdbc pool monitoring and provides
 * objects to the calling modules that retrieve monitoring information.
 *
 * @author Shalini M
 */
@AMXMetadata(type = "jdbc-connection-pool-app-mon", group = "monitoring")
@ManagedObject
@Description("JDBC Connection pool Application based Statistics")
public class JdbcConnPoolAppStatsProvider {

    private static final String JDBC_APP_PROBE_LISTENER = "glassfish:jdbc-pool:applications:";

    private final RangeStatisticImpl numConnUsed =
        new RangeStatisticImpl(
            0, 0, 0, "NumConnUsed", UNIT_COUNT,
            "Provides connection usage " +
            "statistics. The total number of connections that are currently being " +
            "used, as well as information about the maximum number of connections " +
            "that were used (the high water mark).",
            System.currentTimeMillis(),
            System.currentTimeMillis());

    private final CountStatisticImpl numConnAcquired =
        new CountStatisticImpl("NumConnAcquired", UNIT_COUNT,
            "Number of logical " +
            "connections acquired from the pool.");

    private final CountStatisticImpl numConnReleased =
        new CountStatisticImpl("NumConnReleased", UNIT_COUNT,
            "Number of logical " +
            "connections released to the pool.");


    private final String appName;
    private final SimpleJndiName poolName;

    public JdbcConnPoolAppStatsProvider(PoolInfo poolInfo, String appName) {
        this.poolName = poolInfo.getName();
        this.appName = appName;
    }

    public SimpleJndiName getPoolName() {
        return this.poolName;
    }

    public String getAppName() {
        return this.appName;
    }

    @ProbeListener(JDBC_APP_PROBE_LISTENER + "decrementConnectionUsedEvent")
    public void decrementConnectionUsedEvent(@ProbeParam("poolName") String poolName, @ProbeParam("appName") String appName) {
        // handle the num conn used decrement event
        if ((poolName != null) && (poolName.equals(this.poolName.toString()))) {
            if (appName != null && appName.equals(this.appName)) {
                // Decrement numConnUsed counter
                synchronized (numConnUsed) {
                    numConnUsed.setCurrent(numConnUsed.getCurrent() - 1);
                }
            }
        }
    }

    /**
     * Connection used event
     *
     * @param poolName
     * @param appName
     */
    @ProbeListener(JDBC_APP_PROBE_LISTENER + "connectionUsedEvent")
    public void connectionUsedEvent(@ProbeParam("poolName") String poolName, @ProbeParam("appName") String appName) {
        // handle the connection used event
        if ((poolName != null) && (poolName.equals(this.poolName.toString()))) {
            if (appName != null && appName.equals(this.appName)) {
                // increment numConnUsed
                synchronized (numConnUsed) {
                    numConnUsed.setCurrent(numConnUsed.getCurrent() + 1);
                }
            }
        }
    }

    /**
     * When a connection is acquired increment counter
     */
    @ProbeListener(JDBC_APP_PROBE_LISTENER + "connectionAcquiredEvent")
    public void connectionAcquiredEvent(@ProbeParam("poolName") String poolName, @ProbeParam("appName") String appName) {
        if ((poolName != null) && (poolName.equals(this.poolName.toString()))) {
            if (appName != null && appName.equals(this.appName)) {
                numConnAcquired.increment();
            }
        }
    }

    /**
     * When a connection is released increment counter
     */
    @ProbeListener(JDBC_APP_PROBE_LISTENER + "connectionReleasedEvent")
    public void connectionReleasedEvent(@ProbeParam("poolName") String poolName, @ProbeParam("appName") String appName) {
        if ((poolName != null) && (poolName.equals(this.poolName.toString()))) {
            if (appName != null && appName.equals(this.appName)) {
                numConnReleased.increment();
            }
        }
    }

    @ManagedAttribute(id = "numconnused")
    public RangeStatistic getNumConnUsed() {
        return numConnUsed;
    }

    @ManagedAttribute(id = "numconnacquired")
    public CountStatistic getNumConnAcquired() {
        return numConnAcquired;
    }

    @ManagedAttribute(id = "numconnreleased")
    public CountStatistic getNumConnReleased() {
        return numConnReleased;
    }

}
