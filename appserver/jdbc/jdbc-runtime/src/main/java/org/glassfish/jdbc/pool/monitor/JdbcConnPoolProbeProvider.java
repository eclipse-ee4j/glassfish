/*
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

package org.glassfish.jdbc.pool.monitor;

import com.sun.enterprise.resource.pool.monitor.ConnectionPoolProbeProvider;

import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;

/**
 * Probe provider interface for JDBC connection pool related events to provide
 * information related to the various objects on jdbc pool monitoring.
 *
 * @author Shalini M
 */
@ProbeProvider(moduleProviderName = "glassfish", moduleName = "jdbc", probeProviderName = "connection-pool")
public class JdbcConnPoolProbeProvider extends ConnectionPoolProbeProvider {

    /**
     * Emits probe event/notification that the given jdbc connection pool
     * <code>poolName</code>has got a connection validation failed event.
     *
     * @param poolName for which connection validation has failed
     * @param increment number of times the validation failed
     */
    @Probe(name = "connectionValidationFailedEvent")
    @Override
    public void connectionValidationFailedEvent(
        @ProbeParam("poolName") String poolName,
        @ProbeParam("appName") String appName,
        @ProbeParam("moduleName") String moduleName,
        @ProbeParam("increment") int increment) {
    }

    /**
     * Emits probe event/notification that a jdbc connection pool with the given
     * name <code>poolName</code> has got a connection timed out event.
     *
     * @param poolName that has got a connection timed-out event
     */
    @Probe(name = "connectionTimedOutEvent")
    @Override
    public void connectionTimedOutEvent(
        @ProbeParam("poolName") String poolName,
        @ProbeParam("appName") String appName,
        @ProbeParam("moduleName") String moduleName) {
    }

    /**
     * Emits probe event/notification that the pool with the given name
     * <code>poolName</code> is having a potentialConnLeak event.
     *
     * @param poolName
     */
    @Probe(name = "potentialConnLeakEvent")
    @Override
    public void potentialConnLeakEvent(
        @ProbeParam("poolName") String poolName,
        @ProbeParam("appName") String appName,
        @ProbeParam("moduleName") String moduleName) {
    }

    /**
     * Emits probe event/notification that the given jdbc connection pool
     * <code>poolName</code>has got a decrement free connections size event.
     *
     * @param poolName for which decrement numConnFree is got
     * @param steadyPoolSize
     */
    @Probe(name = "decrementNumConnFreeEvent")
    @Override
    public void decrementNumConnFreeEvent(@ProbeParam("poolName") String poolName, @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName) {
    }

    /**
     * Emits probe event/notification that the given jdbc connection pool
     * <code>poolName</code>has got a increment free connections size event.
     *
     * @param poolName for which increment numConnFree is got
     * @param beingDestroyed if connection is destroyed due to error
     * @param steadyPoolSize
     */
    @Probe(name = "incrementNumConnFreeEvent")
    @Override
    public void incrementNumConnFreeEvent(@ProbeParam("poolName") String poolName, @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName, @ProbeParam("beingDestroyed") boolean beingDestroyed,
            @ProbeParam("steadyPoolSize") int steadyPoolSize) {
    }

    /**
     * Emits probe event/notification that the given jdbc connection pool
     * <code>poolName</code>has got a decrement connections used event.
     *
     * @param poolName for which decrement numConnUsed is got
     */
    @Probe(name = "decrementConnectionUsedEvent")
    @Override
    public void decrementConnectionUsedEvent(@ProbeParam("poolName") String poolName, @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName) {
    }

    /**
     * Emits probe event/notification that the given jdbc connection pool
     * <code>poolName</code>has got a increment connections used event.
     *
     * @param poolName for which increment numConnUsed is got
     */
    @Probe(name = "connectionUsedEvent")
    @Override
    public void connectionUsedEvent(@ProbeParam("poolName") String poolName, @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName) {
    }

    /**
     * Emits probe event/notification that the given jdbc connection pool
     * <code>poolName</code>has got a increment connections free event.
     *
     * @param poolName for which increment numConnFree is got
     * @param count number of connections freed to pool
     */
    @Probe(name = "connectionsFreedEvent")
    @Override
    public void connectionsFreedEvent(@ProbeParam("poolName") String poolName, @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName, @ProbeParam("count") int count) {
    }

    /**
     * Emits probe event/notification that a connection request is served in the
     * time <code>timeTakenInMillis</code> for the given jdbc connection pool
     * <code> poolName</code>
     *
     * @param poolName
     * @param timeTakenInMillis time taken to serve a connection
     */
    @Probe(name = "connectionRequestServedEvent")
    @Override
    public void connectionRequestServedEvent(@ProbeParam("poolName") String poolName, @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName, @ProbeParam("timeTakenInMillis") long timeTakenInMillis) {
    }

    /**
     * Emits probe event/notification that a connection is destroyed for the given
     * jdbc connection pool <code>poolName</code>
     *
     * @param poolName
     */
    @Probe(name = "connectionDestroyedEvent")
    @Override
    public void connectionDestroyedEvent(@ProbeParam("poolName") String poolName, @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName) {
    }

    /**
     * Emits probe event/notification that a connection is acquired by application
     * for the given jdbc connection pool <code>poolName</code>
     *
     * @param poolName
     */
    @Probe(name = "connectionAcquiredEvent")
    @Override
    public void connectionAcquiredEvent(@ProbeParam("poolName") String poolName, @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName) {
    }

    /**
     * Emits probe event/notification that a connection is released for the given
     * jdbc connection pool <code>poolName</code>
     *
     * @param poolName
     */
    @Probe(name = "connectionReleasedEvent")
    @Override
    public void connectionReleasedEvent(@ProbeParam("poolName") String poolName, @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName) {
    }

    /**
     * Emits probe event/notification that a new connection is created for the given
     * jdbc connection pool <code>poolName</code>
     *
     * @param poolName
     */
    @Probe(name = "connectionCreatedEvent")
    @Override
    public void connectionCreatedEvent(@ProbeParam("poolName") String poolName, @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName) {
    }

    @Probe(name = "toString", hidden = true)
    @Override
    public void toString(@ProbeParam("poolName") String poolName, @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName, @ProbeParam("stackTrace") StringBuffer stackTrace) {
    }

    /**
     * Emits probe event/notification that a connection under test matches the
     * current request for the given jdbc connection pool <code>poolName</code>
     *
     * @param poolName
     */
    @Probe(name = "connectionMatchedEvent")
    @Override
    public void connectionMatchedEvent(@ProbeParam("poolName") String poolName, @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName) {
    }

    /**
     * Emits probe event/notification that a connection under test does not match
     * the current request for the given jdbc connection pool <code>poolName</code>
     *
     * @param poolName
     */
    @Probe(name = "connectionNotMatchedEvent")
    @Override
    public void connectionNotMatchedEvent(@ProbeParam("poolName") String poolName, @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName) {
    }

    /**
     * Emits probe event/notification that the wait queue length has increased for
     * the given jdbc connection pool <code>poolName</code>
     *
     * @param poolName
     */
    @Probe(name = "connectionRequestQueuedEvent")
    @Override
    public void connectionRequestQueuedEvent(@ProbeParam("poolName") String poolName, @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName) {
    }

    /**
     * Emits probe event/notification that the wait queue length has decreased for
     * the given jdbc connection pool <code>poolName</code>
     *
     * @param poolName
     */
    @Probe(name = "connectionRequestDequeuedEvent")
    @Override
    public void connectionRequestDequeuedEvent(@ProbeParam("poolName") String poolName, @ProbeParam("appName") String appName,
            @ProbeParam("moduleName") String moduleName) {
    }
}
