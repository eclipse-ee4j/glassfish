/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.resource.listener.PoolLifeCycle;

import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.resourcebase.resources.api.PoolInfo;

/**
 * Implementation of PoolLifeCycle to listen to events related to a connection pool creation or destroy. The registry
 * allows multiple listeners (ex: pool monitoring or self management) to listen to the pool's lifecyle. Maintains a list
 * of listeners for this pool identified by poolName.
 *
 * @author Shalini M
 */
@Singleton
public class PoolLifeCycleRegistry implements PoolLifeCycle {

    // List of listeners
    protected List<PoolLifeCycle> lifeCycleListeners = new ArrayList<>();
    private static PoolLifeCycleRegistry __poolLifeCycleRegistry = new PoolLifeCycleRegistry();

    public PoolLifeCycleRegistry() {
    }

    public static PoolLifeCycleRegistry getRegistry() {
        if (__poolLifeCycleRegistry == null) {
            throw new RuntimeException("PoolLifeCycleRegistry not initialized");
        }

        return __poolLifeCycleRegistry;
    }

    /**
     * Add a listener to the list of pool life cycle listeners maintained by this registry for the <code>poolName</code>.
     *
     * @param poolName
     * @param listener
     */
    public void registerPoolLifeCycle(PoolLifeCycle listener) {
        lifeCycleListeners.add(listener);

        // Check if lifecycleListeners has already been set to this. There
        // could be multiple listeners.
        if (!(lifeCycleListeners.size() > 1)) {
            // If the pool is already created, set this registry object to the pool.
            PoolManager poolMgr = ConnectorRuntime.getRuntime().getPoolManager();
            poolMgr.registerPoolLifeCycleListener(this);
        }
    }

    /**
     * Clear the list of pool lifecycle listeners maintained by the registry. This happens when a pool is destroyed so the
     * information about its listeners need not be stored.
     */
    public void unRegisterPoolLifeCycle(PoolLifeCycle listener) {
        if (lifeCycleListeners != null) {
            if (!lifeCycleListeners.isEmpty()) {
                lifeCycleListeners.remove(listener);
            } else {
                // TODO V3 : think about unregistering the registry?
            }
        }
    }

    /**
     * Invoke poolCreated for all listeners of this pool.
     *
     * @param poolInfo
     */
    @Override
    public void poolCreated(PoolInfo poolInfo) {
        for (PoolLifeCycle listener : lifeCycleListeners) {
            listener.poolCreated(poolInfo);
        }
    }

    /**
     * Invoke poolDestroyed for all listeners of this pool.
     *
     * @param poolInfo
     */
    @Override
    public void poolDestroyed(PoolInfo poolInfo) {
        for (PoolLifeCycle listener : lifeCycleListeners) {
            listener.poolDestroyed(poolInfo);
        }
    }
}
