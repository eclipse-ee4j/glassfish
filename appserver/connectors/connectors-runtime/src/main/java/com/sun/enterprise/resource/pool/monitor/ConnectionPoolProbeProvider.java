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

package com.sun.enterprise.resource.pool.monitor;

/**
 * An abstract class that houses the common implementations of various probe
 * providers. All probe providers extend this implementation.
 * 
 * @author Shalini M
 */
public abstract class ConnectionPoolProbeProvider {

    /**
     * Emits probe event/notification that the given  connection pool 
     * <code>poolName</code>has got a connection validation failed event.
     * 
     * @param poolName for which connection validation has failed
     * @param appName application-name in which the pool is defined
     * @param moduleName module-name in which the pool is defined
     * @param increment number of times the validation failed
     */
    public void connectionValidationFailedEvent(String poolName, String appName, String moduleName, int increment) {
    }

    /**
     * Emits probe event/notification that a  connection pool with the given
     * name <code>poolName</code> has got a connection timed out event.
     * 
     * @param poolName that has got a connection timed-out event
     * @param appName application-name in which the pool is defined
     * @param moduleName module-name in which the pool is defined
     */
    public void connectionTimedOutEvent(String poolName, String appName, String moduleName) {
    }

    /**
     * Emits probe event/notification that the pool with the given name 
     * <code>poolName</code> is having a potentialConnLeak event.
     * 
     * @param poolName
     * @param appName application-name in which the pool is defined
     * @param moduleName module-name in which the pool is defined
     */
    public void potentialConnLeakEvent(String poolName, String appName, String moduleName) {
    }

    /**
     * Emits probe event/notification that the given  connection pool 
     * <code>poolName</code>has got a decrement free connections size event.
     * 
     * @param poolName for which decrement numConnFree is got
     * @param appName application-name in which the pool is defined
     * @param moduleName module-name in which the pool is defined
     */
    public void decrementNumConnFreeEvent(String poolName, String appName, String moduleName) {
    }

    /**
     * Emits probe event/notification that the given  connection pool 
     * <code>poolName</code>has got a decrement free connections size event.
     * 
     * @param poolName for which decrement numConnFree is got
     * @param appName application-name in which the pool is defined
     * @param moduleName module-name in which the pool is defined
     * @param beingDestroyed if the connection is destroyed due to error
     * @param steadyPoolSize 
     */
    public void incrementNumConnFreeEvent(String poolName, String appName, String moduleName, boolean beingDestroyed,
            int steadyPoolSize) {
    }

    /**
     * Emits probe event/notification that the given  connection pool 
     * <code>poolName</code>has got a decrement connections used event.
     * 
     * @param poolName for which decrement numConnUsed is got
     * @param appName application-name in which the pool is defined
     * @param moduleName module-name in which the pool is defined
     */
    public void decrementConnectionUsedEvent(String poolName, String appName, String moduleName) {
    }

    /**
     * Emits probe event/notification that the given  connection pool 
     * <code>poolName</code>has got a increment connections used event.
     * 
     * @param poolName for which increment numConnUsed is got
     * @param appName application-name in which the pool is defined
     * @param moduleName module-name in which the pool is defined
     */
    public void connectionUsedEvent(String poolName, String appName, String moduleName) {
    }

    /**
     * Emits probe event/notification that the given  connection pool 
     * <code>poolName</code>has got a increment connections free event.
     * 
     * @param poolName for which increment numConnFree is got
     * @param appName application-name in which the pool is defined
     * @param moduleName module-name in which the pool is defined
     * @param count number of connections freed to pool
     */
    public void connectionsFreedEvent(String poolName, String appName, String moduleName, int count) {
    }

    /**
     * Emits probe event/notification that a connection request is served in the
     * time <code>timeTakenInMillis</code> for the given  connection pool
     * <code> poolName</code> 
     * 
     * @param poolName
     * @param appName application-name in which the pool is defined
     * @param moduleName module-name in which the pool is defined
     * @param timeTakenInMillis time taken to serve a connection
     */
    public void connectionRequestServedEvent(String poolName, String appName, String moduleName, long timeTakenInMillis) {
    }

    /**
     * Emits probe event/notification that a connection is destroyed for the 
     * given  connection pool <code>poolName</code>
     * 
     * @param poolName
     * @param appName application-name in which the pool is defined
     * @param moduleName module-name in which the pool is defined
     */
    public void connectionDestroyedEvent(String poolName, String appName, String moduleName) {
    }

    /**
     * Emits probe event/notification that a connection is acquired by application
     * for the given  connection pool <code>poolName</code>
     * 
     * @param poolName
     * @param appName application-name in which the pool is defined
     * @param moduleName module-name in which the pool is defined
     */
    public void connectionAcquiredEvent(String poolName, String appName, String moduleName) {
    }

    /**
     * Emits probe event/notification that a connection is released for the given
     *  connection pool <code>poolName</code>
     * 
     * @param poolName
     * @param appName application-name in which the pool is defined
     * @param moduleName module-name in which the pool is defined
     */
    public void connectionReleasedEvent(String poolName, String appName, String moduleName) {
    }

    /**
     * Emits probe event/notification that a new connection is created for the
     * given  connection pool <code>poolName</code>
     * 
     * @param poolName
     * @param appName application-name in which the pool is defined
     * @param moduleName module-name in which the pool is defined
     */
    public void connectionCreatedEvent(String poolName, String appName, String moduleName) {
    }

    public void toString(String poolName, String appName, String moduleName, StringBuffer stackTrace) {
    }

    /**
     * Emits probe event/notification that a connection under test matches the 
     * current request for the given  connection pool <code>poolName</code>
     * 
     * @param poolName
     * @param appName application-name in which the pool is defined
     * @param moduleName module-name in which the pool is defined
     */
    public void connectionMatchedEvent(String poolName, String appName, String moduleName) {
    }

    /**
     * Emits probe event/notification that a connection under test does not 
     * match the current request for the given  connection pool 
     * <code>poolName</code>
     * 
     * @param poolName
     * @param appName application-name in which the pool is defined
     * @param moduleName module-name in which the pool is defined
     */
    public void connectionNotMatchedEvent(String poolName, String appName, String moduleName) {
    }

    /**
     * Emits probe event/notification that the wait queue length has increased 
     * for the given  connection pool <code>poolName</code>
     * 
     * @param poolName
     * @param appName application-name in which the pool is defined
     * @param moduleName module-name in which the pool is defined
     */
    public void connectionRequestQueuedEvent(String poolName, String appName, String moduleName) {
    }

    /**
     * Emits probe event/notification that the wait queue length has decreased 
     * for the given  connection pool <code>poolName</code>
     * 
     * @param poolName
     * @param appName application-name in which the pool is defined
     * @param moduleName module-name in which the pool is defined
     */
    public void connectionRequestDequeuedEvent(String poolName, String appName, String moduleName) {
    }
}
