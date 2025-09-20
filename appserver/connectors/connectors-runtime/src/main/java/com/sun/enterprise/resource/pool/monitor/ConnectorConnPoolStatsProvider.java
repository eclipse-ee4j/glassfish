/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.resource.pool.monitor;

import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.resource.pool.PoolLifeCycleListenerRegistry;
import com.sun.enterprise.resource.pool.PoolStatus;

import java.lang.System.Logger;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.RangeStatistic;
import org.glassfish.external.statistics.annotations.Reset;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.external.statistics.impl.RangeStatisticImpl;
import org.glassfish.external.statistics.impl.StatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.resourcebase.resources.api.PoolInfo;

import static java.lang.System.Logger.Level.DEBUG;


/**
 * StatsProvider object for Jdbc pool monitoring.
 *
 * Implements various events related to jdbc pool monitoring and provides
 * objects to the calling modules that retrieve monitoring information.
 *
 * @author Shalini M
 */
@AMXMetadata(type = "connector-connection-pool-mon", group = "monitoring")
@ManagedObject
@Description("Connector Connection Pool Statistics")
public class ConnectorConnPoolStatsProvider {

    private static final Logger LOG = System.getLogger(ConnectorConnPoolStatsProvider.class.getName());
    private final PoolInfo poolInfo;


    //Registry that stores all listeners to this object
    private PoolLifeCycleListenerRegistry poolRegistry;


    //Objects that are exposed by this telemetry
    private final CountStatisticImpl numConnFailedValidation = new CountStatisticImpl(
            "NumConnFailedValidation", StatisticImpl.UNIT_COUNT,
            "The total number of connections in the connection pool that failed " +
            "validation from the start time until the last sample time.");
    private final CountStatisticImpl numConnTimedOut = new CountStatisticImpl(
            "NumConnTimedOut", StatisticImpl.UNIT_COUNT, "The total number of " +
            "connections in the pool that timed out between the start time and the last sample time.");
    private final RangeStatisticImpl numConnFree = new RangeStatisticImpl(
            0, 0, 0,
            "NumConnFree", StatisticImpl.UNIT_COUNT, "The total number of free " +
            "connections in the pool as of the last sampling.",
            System.currentTimeMillis(), System.currentTimeMillis());
    private final RangeStatisticImpl numConnUsed = new RangeStatisticImpl(
            0, 0, 0,
            "NumConnUsed", StatisticImpl.UNIT_COUNT, "Provides connection usage " +
            "statistics. The total number of connections that are currently being " +
            "used, as well as information about the maximum number of connections " +
            "that were used (the high water mark).",
            System.currentTimeMillis(), System.currentTimeMillis());
    private final RangeStatisticImpl connRequestWaitTime = new RangeStatisticImpl(
            0, 0, 0,
            "ConnRequestWaitTime", StatisticImpl.UNIT_MILLISECOND,
            "The longest and shortest wait times of connection requests. The " +
            "current value indicates the wait time of the last request that was " +
            "serviced by the pool.",
            System.currentTimeMillis(), System.currentTimeMillis());
    private final CountStatisticImpl numConnDestroyed = new CountStatisticImpl(
            "NumConnDestroyed", StatisticImpl.UNIT_COUNT,
            "Number of physical connections that were destroyed since the last reset.");
    private final CountStatisticImpl numConnAcquired = new CountStatisticImpl(
            "NumConnAcquired", StatisticImpl.UNIT_COUNT, "Number of logical " +
            "connections acquired from the pool.");
    private final CountStatisticImpl numConnReleased = new CountStatisticImpl(
            "NumConnReleased", StatisticImpl.UNIT_COUNT, "Number of logical " +
            "connections released to the pool.");
    private final CountStatisticImpl numConnCreated = new CountStatisticImpl(
            "NumConnCreated", StatisticImpl.UNIT_COUNT,
            "The number of physical connections that were created since the last reset.");
    private final CountStatisticImpl numPotentialConnLeak = new CountStatisticImpl(
            "NumPotentialConnLeak", StatisticImpl.UNIT_COUNT,
            "Number of potential connection leaks");
    private final CountStatisticImpl numConnSuccessfullyMatched = new CountStatisticImpl(
            "NumConnSuccessfullyMatched", StatisticImpl.UNIT_COUNT,
            "Number of connections succesfully matched");
    private final CountStatisticImpl numConnNotSuccessfullyMatched = new CountStatisticImpl(
            "NumConnNotSuccessfullyMatched", StatisticImpl.UNIT_COUNT,
            "Number of connections rejected during matching");
    private final CountStatisticImpl totalConnRequestWaitTime = new CountStatisticImpl(
            "TotalConnRequestWaitTime", StatisticImpl.UNIT_MILLISECOND,
            "Total wait time per successful connection request");
    private final CountStatisticImpl averageConnWaitTime = new CountStatisticImpl(
            "AverageConnWaitTime", StatisticImpl.UNIT_MILLISECOND,
            "Average wait-time-duration per successful connection request");
    private final CountStatisticImpl waitQueueLength = new CountStatisticImpl(
            "WaitQueueLength", StatisticImpl.UNIT_COUNT,
            "Number of connection requests in the queue waiting to be serviced.");
    private static final String JCA_PROBE_LISTENER = "glassfish:jca:connection-pool:";

    public ConnectorConnPoolStatsProvider(PoolInfo poolInfo) {
        this.poolInfo = poolInfo;
    }

    /**
     * Whenever connection leak happens, increment numPotentialConnLeak
     */
    @ProbeListener(JCA_PROBE_LISTENER + "potentialConnLeakEvent")
    public void potentialConnLeakEvent(@ProbeParam("poolName") String poolName,
                                       @ProbeParam("appName") String appName,
                                       @ProbeParam("moduleName") String moduleName
                                       ) {
        if (isMyPool(poolName, appName, moduleName)) {
            LOG.log(DEBUG, "Connection Leak event received for pool: {0}", poolInfo);
            numPotentialConnLeak.increment();
        }
    }

    /**
     * Whenever connection timed-out event occurs, increment numConnTimedOut
     */
    @ProbeListener(JCA_PROBE_LISTENER + "connectionTimedOutEvent")
    public void connectionTimedOutEvent(@ProbeParam("poolName") String poolName,
                                        @ProbeParam("appName") String appName,
                                        @ProbeParam("moduleName") String moduleName
                                        ) {
        if (isMyPool(poolName, appName, moduleName)) {
            LOG.log(DEBUG, "Connection Timed-out event received for pool: {0}", poolInfo);
            numConnTimedOut.increment();
        }
    }

    /**
     * Decrement numconnfree event
     */
    @ProbeListener(JCA_PROBE_LISTENER + "decrementNumConnFreeEvent")
    public void decrementNumConnFreeEvent(
            @ProbeParam("poolName") String poolName,
            @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName
            ) {
        if (isMyPool(poolName, appName, moduleName)) {
            LOG.log(DEBUG, "Decrement Num Connections Free event received for pool: {0}", poolInfo);
            synchronized (numConnFree) {
                numConnFree.setCurrent(numConnFree.getCurrent() - 1);
            }
        }
    }

    /**
     * Increment numconnfree event
     *
     * @param beingDestroyed if the connection is destroyed due to error
     */
    @ProbeListener(JCA_PROBE_LISTENER + "incrementNumConnFreeEvent")
    public void incrementNumConnFreeEvent(
            @ProbeParam("poolName") String poolName,
            @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName,
            @ProbeParam("beingDestroyed") boolean beingDestroyed,
            @ProbeParam("steadyPoolSize") int steadyPoolSize) {
        if (isMyPool(poolName, appName, moduleName)) {
            LOG.log(DEBUG, "Increment Num Connections Free event received for pool: {0}", poolInfo);
            if(beingDestroyed) {
                // if pruned by resizer thread
                synchronized(numConnFree) {
                    synchronized(numConnUsed) {
                        if (numConnFree.getCurrent() + numConnUsed.getCurrent() < steadyPoolSize) {
                            numConnFree.setCurrent(numConnFree.getCurrent() + 1);
                        }
                    }
                }
            } else {
                synchronized(numConnFree) {
                    numConnFree.setCurrent(numConnFree.getCurrent() + 1);
                }
            }
        }
    }

    /**
     * Decrement numConnUsed event
     */
    @ProbeListener(JCA_PROBE_LISTENER + "decrementConnectionUsedEvent")
    public void decrementConnectionUsedEvent(
            @ProbeParam("poolName") String poolName,
            @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName
            ) {
        if (isMyPool(poolName, appName, moduleName)) {
            LOG.log(DEBUG, "Decrement Num Connections Used event received for pool: {0}", poolInfo);
            synchronized(numConnUsed) {
                long newValue = numConnUsed.getCurrent() - 1;
                if (newValue < 0) {
                    return;
                }
                numConnUsed.setCurrent(newValue);
            }
        }
    }

    /**
     * Connections freed event
     *
     * @param count number of connections freed to the pool
     */
    @ProbeListener(JCA_PROBE_LISTENER + "connectionsFreedEvent")
    public void connectionsFreedEvent(
            @ProbeParam("poolName") String poolName,
            @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName,
            @ProbeParam("count") int count) {
        if (isMyPool(poolName, appName, moduleName)) {
            LOG.log(DEBUG, "Connections Freed event received for pool: {0}, count={1}.", poolInfo, count);
            synchronized(numConnFree) {
                numConnFree.setCurrent(count);
                LOG.log(DEBUG, "Pool: {0}; used/free={1}/{2}, freed={3}",
                    poolInfo, numConnUsed.getCurrent(), numConnFree.getCurrent(), count);
            }
        }
    }

    /**
     * Connection used event
     */
    @ProbeListener(JCA_PROBE_LISTENER + "connectionUsedEvent")
    public void connectionUsedEvent(
            @ProbeParam("poolName") String poolName,
            @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName
            ) {
        if (isMyPool(poolName, appName, moduleName)) {
            LOG.log(DEBUG, "Connection Used event received for pool: {0}", poolInfo);
            synchronized(numConnUsed) {
                numConnUsed.setCurrent(numConnUsed.getCurrent() + 1);
            }
        }
    }

    /**
     * Whenever connection validation failure happens, increment numConnFailedValidation
     */
    @ProbeListener(JCA_PROBE_LISTENER + "connectionValidationFailedEvent")
    public void connectionValidationFailedEvent(
            @ProbeParam("poolName") String poolName,
            @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName,
            @ProbeParam("increment") int increment) {
        if (isMyPool(poolName, appName, moduleName)) {
            LOG.log(DEBUG, "Connection Validation Failed event received for pool: {0}", poolInfo);
            numConnFailedValidation.increment(increment);
        }
    }

    /**
     * Event that a connection request is served in timeTakenInMillis.
     */
    @ProbeListener(JCA_PROBE_LISTENER + "connectionRequestServedEvent")
    public void connectionRequestServedEvent(
            @ProbeParam("poolName") String poolName,
            @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName,
            @ProbeParam("timeTakenInMillis") long timeTakenInMillis) {
        if (isMyPool(poolName, appName, moduleName)) {
            LOG.log(DEBUG, "Connection request served event received for pool: {0}", poolInfo);
            connRequestWaitTime.setCurrent(timeTakenInMillis);
            totalConnRequestWaitTime.increment(timeTakenInMillis);
        }
    }

    /**
     * When connection destroyed event is got increment numConnDestroyed.
     */
    @ProbeListener(JCA_PROBE_LISTENER + "connectionDestroyedEvent")
    public void connectionDestroyedEvent(
            @ProbeParam("poolName") String poolName,
            @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName
            ) {
        if (isMyPool(poolName, appName, moduleName)) {
            LOG.log(DEBUG, "Connection destroyed event received for pool: {0}", poolInfo);
            numConnDestroyed.increment();
        }
    }

    /**
     * When a connection is acquired increment counter
     */
    @ProbeListener(JCA_PROBE_LISTENER + "connectionAcquiredEvent")
    public void connectionAcquiredEvent(
            @ProbeParam("poolName") String poolName,
            @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName
            ) {
        if (isMyPool(poolName, appName, moduleName)) {
            LOG.log(DEBUG, "Connection acquired event received for pool: {0}", poolInfo);
            numConnAcquired.increment();
        }
    }

    /**
     * When a connection is released increment counter
     */
    @ProbeListener(JCA_PROBE_LISTENER + "connectionReleasedEvent")
    public void connectionReleasedEvent(
            @ProbeParam("poolName") String poolName,
            @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName
            ) {
        if (isMyPool(poolName, appName, moduleName)) {
            LOG.log(DEBUG, "Connection Released event received for pool: {0}", poolInfo);
            numConnReleased.increment();
        }
    }

    /**
     * When a connection is created increment counter
     */
    @ProbeListener(JCA_PROBE_LISTENER + "connectionCreatedEvent")
    public void connectionCreatedEvent(
            @ProbeParam("poolName") String poolName,
            @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName
            ) {
        if (isMyPool(poolName, appName, moduleName)) {
            LOG.log(DEBUG, "Connection Created event received for pool: {0}", poolInfo);
            numConnCreated.increment();
        }
    }

    /**
     * Reset pool statistics
     * When annotated with @Reset, this method is invoked whenever monitoring
     * is turned to HIGH from OFF, thereby setting the statistics to
     * appropriate values.
     */
    @Reset
    public void reset() {
        LOG.log(DEBUG, "Reset event received for pool: {0}", poolInfo);
        PoolStatus status = ConnectorRuntime.getRuntime().getPoolManager().getPoolStatus(poolInfo);
        numConnUsed.setCurrent(status.getNumConnUsed());
        numConnFree.setCurrent(status.getNumConnFree());
        numConnCreated.reset();
        numConnDestroyed.reset();
        numConnFailedValidation.reset();
        numConnTimedOut.reset();
        numConnAcquired.reset();
        numConnReleased.reset();
        connRequestWaitTime.reset();
        numConnSuccessfullyMatched.reset();
        numConnNotSuccessfullyMatched.reset();
        numPotentialConnLeak.reset();
        averageConnWaitTime.reset();
        totalConnRequestWaitTime.reset();
        waitQueueLength.reset();
    }

    /**
     * When connection under test matches the current request,
     * increment numConnSuccessfullyMatched.
     */
    @ProbeListener(JCA_PROBE_LISTENER + "connectionMatchedEvent")
    public void connectionMatchedEvent(
            @ProbeParam("poolName") String poolName,
            @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName
            ) {
        if (isMyPool(poolName, appName, moduleName)) {
            LOG.log(DEBUG, "Connection Matched event received for pool: {0}", poolInfo);
            numConnSuccessfullyMatched.increment();
        }
    }

    /**
     * When a connection under test does not match the current request,
     * increment numConnNotSuccessfullyMatched.
     */
    @ProbeListener(JCA_PROBE_LISTENER + "connectionNotMatchedEvent")
    public void connectionNotMatchedEvent(
            @ProbeParam("poolName") String poolName,
            @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName
            ) {
        if (isMyPool(poolName, appName, moduleName)) {
            LOG.log(DEBUG, "Connection Not Matched event received for pool: {0}", poolInfo);
            numConnNotSuccessfullyMatched.increment();
        }
    }

    /**
     * When an object is added to wait queue, increment the waitQueueLength.
     */
    @ProbeListener(JCA_PROBE_LISTENER + "connectionRequestQueuedEvent")
    public void connectionRequestQueuedEvent(
            @ProbeParam("poolName") String poolName,
            @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName
            ) {
        if (isMyPool(poolName, appName, moduleName)) {
            LOG.log(DEBUG, "Wait Queue Length Modified event received for pool: {0}", poolInfo);
            waitQueueLength.increment();
        }
    }

    /**
     * When an object is removed from the wait queue, decrement the waitQueueLength.
     */
    @ProbeListener(JCA_PROBE_LISTENER + "connectionRequestDequeuedEvent")
    public void connectionRequestDequeuedEvent(
            @ProbeParam("poolName") String poolName,
            @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName
            ) {
        if (isMyPool(poolName, appName, moduleName)) {
            LOG.log(DEBUG, "Wait Queue Length Modified event received for pool: {0}", poolInfo);
            waitQueueLength.decrement();
        }
    }

    protected PoolInfo getPoolInfo() {
        return poolInfo;
    }

    protected void setPoolRegistry(PoolLifeCycleListenerRegistry registry) {
        this.poolRegistry = registry;
    }

    protected PoolLifeCycleListenerRegistry getPoolRegistry() {
        return poolRegistry;
    }

    @ManagedAttribute(id = "numpotentialconnleak")
    public CountStatistic getNumPotentialConnLeakCount() {
        return numPotentialConnLeak;
    }

    @ManagedAttribute(id = "numconnfailedvalidation")
    public CountStatistic getNumConnFailedValidation() {
        return numConnFailedValidation;
    }

    @ManagedAttribute(id = "numconntimedout")
    public CountStatistic getNumConnTimedOut() {
        return numConnTimedOut;
    }

    @ManagedAttribute(id = "numconnused")
    public RangeStatistic getNumConnUsed() {
        return numConnUsed;
    }

    @ManagedAttribute(id = "numconnfree")
    public RangeStatistic getNumConnFree() {
        return numConnFree;
    }

    @ManagedAttribute(id = "connrequestwaittime")
    public RangeStatistic getConnRequestWaitTime() {
        return connRequestWaitTime;
    }

    @ManagedAttribute(id = "numconndestroyed")
    public CountStatistic getNumConnDestroyed() {
        return numConnDestroyed;
    }

    @ManagedAttribute(id = "numconnacquired")
    public CountStatistic getNumConnAcquired() {
        return numConnAcquired;
    }

    @ManagedAttribute(id = "numconncreated")
    public CountStatistic getNumConnCreated() {
        return numConnCreated;
    }

    @ManagedAttribute(id = "numconnreleased")
    public CountStatistic getNumConnReleased() {
        return numConnReleased;
    }

    @ManagedAttribute(id = "numconnsuccessfullymatched")
    public CountStatistic getNumConnSuccessfullyMatched() {
        return numConnSuccessfullyMatched;
    }

    @ManagedAttribute(id = "numconnnotsuccessfullymatched")
    public CountStatistic getNumConnNotSuccessfullyMatched() {
        return numConnNotSuccessfullyMatched;
    }

    @ManagedAttribute(id = "averageconnwaittime")
    public CountStatistic getAverageConnWaitTime() {
        // Time taken by all connection requests divided by total number of
        // connections acquired in the sampling period.
        long connAquired = numConnAcquired.getCount();
        long averageWaitTime = connAquired == 0 ? 0 : totalConnRequestWaitTime.getCount() / connAquired;
        averageConnWaitTime.setCount(averageWaitTime);
        return averageConnWaitTime;
    }

    @ManagedAttribute(id = "waitqueuelength")
    public CountStatistic getWaitQueueLength() {
        return waitQueueLength;
    }

    private boolean isMyPool(String poolName, String appName, String moduleName) {
        PoolInfo other = new PoolInfo(SimpleJndiName.of(poolName), appName, moduleName);
        return poolInfo.equals(other);
    }
}
