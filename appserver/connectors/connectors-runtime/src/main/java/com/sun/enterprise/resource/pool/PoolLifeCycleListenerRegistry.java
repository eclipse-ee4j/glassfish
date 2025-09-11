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

package com.sun.enterprise.resource.pool;

import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.resource.listener.PoolLifeCycleListener;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.resourcebase.resources.api.PoolInfo;

/**
 * Implementation of PoolLifeCycleListener to listen to events related to a connection pool. The registry allows
 * multiple listeners (ex: pool monitoring) to listen to the pool's lifecyle. Maintains a list of listeners for this
 * pool identified by poolName.
 *
 * @author Shalini M
 */
public class PoolLifeCycleListenerRegistry implements PoolLifeCycleListener {

    // List of listeners
    private List<PoolLifeCycleListener> poolListenersList;

    // name of the pool for which the registry is maintained
    private PoolInfo poolInfo;

    public PoolLifeCycleListenerRegistry(PoolInfo poolInfo) {
        this.poolInfo = poolInfo;
        this.poolListenersList = new ArrayList<>();
    }

    /**
     * Add a listener to the list of pool life cycle listeners maintained by this registry.
     *
     * @param listener
     */
    public void registerPoolLifeCycleListener(PoolLifeCycleListener listener) {
        poolListenersList.add(listener);

        // Check if poolLifeCycleListener has already been set to this. There
        // could be multiple listeners.
        if (poolListenersList.size() <= 1) {
            // If the pool is already created, set this registry object to the pool.
            PoolManager poolMgr = ConnectorRuntime.getRuntime().getPoolManager();
            ResourcePool pool = poolMgr.getPool(poolInfo);
            pool.setPoolLifeCycleListener(this);
        }
    }

    /**
     * Clear the list of pool lifecycle listeners maintained by the registry. This happens when a pool is destroyed so the
     * information about its listeners need not be stored.
     *
     * @param poolName
     */
    public void unRegisterPoolLifeCycleListener(PoolInfo poolInfo) {
        // To make sure the registry is for the given pool name
        if (this.poolInfo.equals(poolInfo)) {
            if (poolListenersList != null && !poolListenersList.isEmpty()) {
                // Remove all listeners from this list
                poolListenersList.clear();
            }
        }
        // Its not needed to remove pool life cycle listener from the pool since
        // the pool will already be destroyed.
    }

    @Override
    public void toString(StringBuffer stackTrace) {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.toString(stackTrace);
        }
    }

    @Override
    public void connectionAcquired(long resourceHandleId) {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionAcquired(resourceHandleId);
        }
    }

    @Override
    public void connectionRequestServed(long timeTakenInMillis) {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionRequestServed(timeTakenInMillis);
        }
    }

    @Override
    public void connectionTimedOut() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionTimedOut();
        }
    }

    @Override
    public void connectionNotMatched() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionNotMatched();
        }
    }

    @Override
    public void connectionMatched() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionMatched();
        }
    }

    @Override
    public void connectionUsed(long resourceHandleId) {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionUsed(resourceHandleId);
        }
    }

    @Override
    public void connectionDestroyed(long resourceHandleId) {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionDestroyed(resourceHandleId);
        }
    }

    @Override
    public void connectionReleased(long resourceHandleId) {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionReleased(resourceHandleId);
        }
    }

    @Override
    public void connectionCreated() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionCreated();
        }
    }

    @Override
    public void foundPotentialConnectionLeak() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.foundPotentialConnectionLeak();
        }
    }

    @Override
    public void connectionValidationFailed(int count) {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionValidationFailed(count);
        }
    }

    @Override
    public void connectionsFreed(int count) {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionsFreed(count);
        }
    }

    @Override
    public void decrementConnectionUsed(long resourceHandleId) {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.decrementConnectionUsed(resourceHandleId);
        }
    }

    @Override
    public void decrementNumConnFree() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.decrementNumConnFree();
        }
    }

    @Override
    public void incrementNumConnFree(boolean beingDestroyed, int steadyPoolSize) {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.incrementNumConnFree(beingDestroyed, steadyPoolSize);
        }
    }

    @Override
    public void connectionRequestQueued() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionRequestQueued();
        }
    }

    @Override
    public void connectionRequestDequeued() {
        for (PoolLifeCycleListener listener : poolListenersList) {
            listener.connectionRequestDequeued();
        }
    }
}
