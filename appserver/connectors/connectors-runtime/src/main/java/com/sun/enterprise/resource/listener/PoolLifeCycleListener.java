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

package com.sun.enterprise.resource.listener;

/**
 * Pool Life cycle listener that can be implemented by listeners for pool monitoring
 *
 * @author Jagadish Ramu
 */
public interface PoolLifeCycleListener {

    /**
     * Print stack trace in server.log
     * @param stackTrace
     */
    void toString(StringBuffer stackTrace);

    /**
     * indicates that a connection is acquired by application
     */
    void connectionAcquired(long resourceHandleId);

    /**
     * indicates that a connection request is server in the time
     * @param timeTakenInMillis time taken to serve a connection
     */
    void connectionRequestServed(long timeTakenInMillis);

    /**
     * indicates that a connection is timed-out
     */
    void connectionTimedOut();

    /**
     * indicates that a connection under test does not match the current request
     */
    void connectionNotMatched();

    /**
     * indicates that a connection under test matches the current request
     */
    void connectionMatched();

    /**
     * indicates that a connection is being used
     */
    void connectionUsed(long resourceHandleId);

    /**
     * indicates that a connection is destroyed
     */
    void connectionDestroyed(long resourceHandleId);

    /**
     * indicates that a connection is released
     */
    void connectionReleased(long resourceHandleId);

    /**
     * indicates that a new connection is created
     */
    void connectionCreated();

    /**
     * indicates that a potential connection leak happened
     */
    void foundPotentialConnectionLeak();

    /**
     * indicates that a number of connections have failed validation
     * @param count number of connections
     */
    void connectionValidationFailed(int count);

    /**
     * indicates the number of connections freed to pool
     * @param count number of connections
     */
    void connectionsFreed(int count);

    /**
     * indicates that connection count that is used has to be decremented.
     */
    void decrementConnectionUsed(long resourceHandleId);

    /**
     * indicates that free connections count in the pool has to be decremented.
     */
    void decrementNumConnFree();

    /**
     * indicates that a connection is freed and the count is to be incremented.
     * @param beingDestroyed in case of an error.
     * @param steadyPoolSize
     */
    void incrementNumConnFree(boolean beingDestroyed, int steadyPoolSize);

    /**
     * indicates that the wait queue length has increased.
     */
    void connectionRequestQueued();

    /**
     * indicates that the wait queue length has decreased.
     */
    void connectionRequestDequeued();

}
