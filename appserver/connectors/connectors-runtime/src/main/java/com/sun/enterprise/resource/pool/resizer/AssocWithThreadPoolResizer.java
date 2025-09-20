/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.pool.resizer;

import com.sun.enterprise.resource.AssocWithThreadResourceHandle;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceState;
import com.sun.enterprise.resource.pool.PoolProperties;
import com.sun.enterprise.resource.pool.ResourceHandler;
import com.sun.enterprise.resource.pool.datastructure.DataStructure;

import jakarta.resource.spi.ManagedConnection;

import java.lang.System.Logger;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.resourcebase.resources.api.PoolInfo;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Resizer for Associate With Thread type pools to remove unusable connections
 * and maintain steady pool size.
 *
 * @author Shalini M
 */
public class AssocWithThreadPoolResizer extends Resizer {

    private static final Logger LOG = System.getLogger(AssocWithThreadPoolResizer.class.getName());

    public AssocWithThreadPoolResizer(PoolInfo poolInfo, DataStructure ds,
            PoolProperties pp, ResourceHandler handler,
            boolean preferValidateOverRecreate) {
        super(poolInfo, ds, pp, handler, preferValidateOverRecreate);
    }

    /**
     * Scale down pool by a <code>size &lt;= pool-resize-quantity</code>
     *
     * @param forced            scale-down only when forced
     * @param scaleDownQuantity no. of resources to remove
     */
    @Override
    protected void scaleDownPool(int scaleDownQuantity, boolean forced) {
        if (pool.getResizeQuantity() <= 0 || !forced) {
            return;
        }
        if (scaleDownQuantity > dataStructure.getResourcesSize() - pool.getSteadyPoolSize()) {
            scaleDownQuantity = 0;
        }

        LOG.log(DEBUG, "Scaling down pool by quantity: {0}", scaleDownQuantity);
        Set<ResourceHandle> resourcesToRemove = new HashSet<>();
        try {
            for (ResourceHandle handle : dataStructure.getAllResources()) {
                if (scaleDownQuantity > 0) {
                    handle.lock();
                    try {
                        if (!handle.getResourceState().isBusy()) {
                            resourcesToRemove.add(handle);
                            ((AssocWithThreadResourceHandle) handle).setUnusable();
                            scaleDownQuantity--;
                        }
                    } finally {
                        handle.unlock();
                    }
                }
            }
        } finally {
            for (ResourceHandle resourceToRemove : resourcesToRemove) {
                if (dataStructure.getAllResources().contains(resourceToRemove)) {
                    dataStructure.removeResource(resourceToRemove);
                }
            }
        }
    }

    /**
     * Get the free connections list from the pool, remove idle-timed-out resources
     * and then invalid resources.
     *
     * @return int number of resources removed
     */
    @Override
    protected int removeIdleAndInvalidResources() {

        int poolSizeBeforeRemoval = dataStructure.getResourcesSize();
        // let's cache the current time since precision is not required here.
        long currentTime = System.currentTimeMillis();
        int validConnectionsCounter = 0;
        int idleConnKeptInSteadyCounter = 0;

        Set<ResourceHandle> resourcesToValidate = new HashSet<>();
        Set<ResourceHandle> resourcesToRemove = new HashSet<>();
        try {
            //iterate through all the resources to find idle-time lapsed ones.
            for (ResourceHandle handle : dataStructure.getAllResources()) {
                handle.lock();
                try {
                    final ResourceState state = handle.getResourceState();
                    if (state.isBusy()) {
                        continue;
                    }
                    final AssocWithThreadResourceHandle asociatedHandle = (AssocWithThreadResourceHandle) handle;
                    if (currentTime - state.getLastUsage() < pool.getIdleTimeout()) {
                        //Should be added for validation.
                        if (!state.isEnlisted() && !state.isBusy() && asociatedHandle.isAssociated()) {
                            asociatedHandle.setAssociated(false);
                            validConnectionsCounter++;
                            resourcesToValidate.add(handle);
                        }
                    } else {
                        boolean isResourceEligibleForRemoval = isResourceEligibleForRemoval(handle, validConnectionsCounter);
                        if (isResourceEligibleForRemoval) {
                            //Add this to remove later
                            resourcesToRemove.add(handle);
                            asociatedHandle.setUnusable();
                        } else {
                            //preferValidateOverrecreate true and connection is valid within SPS
                            validConnectionsCounter++;
                            idleConnKeptInSteadyCounter++;
                            LOG.log(DEBUG, "PreferValidateOverRecreate: Keeping idle resource {0}"
                                + " in the steady part of the free pool as the RA reports it to be valid ({1} <= {2})",
                                handle, validConnectionsCounter, pool.getSteadyPoolSize());
                        }
                    }
                } finally {
                    handle.unlock();
                }
            }
        } finally {
            for (ResourceHandle resourceToRemove : resourcesToRemove) {
                if (dataStructure.getAllResources().contains(resourceToRemove)) {
                    dataStructure.removeResource(resourceToRemove);
                }
            }
        }

        //remove invalid resources from the free (active) resources list.
        //Since the whole pool is not locked, it may happen that some of these
        //resources may be given to applications.
        int noOfInvalidResources = removeInvalidResources(resourcesToValidate);

        //These statistic computations will work fine as long as resizer
        //locks the pool throughout its operations.
        if (preferValidateOverRecreate) {
            LOG.log(DEBUG, "Idle resources validated and kept in the steady pool {0}: {1}", poolInfo, idleConnKeptInSteadyCounter);
            LOG.log(DEBUG, "Number of Idle resources freed from pool {0}: {1}", poolInfo, resourcesToRemove.size());
            LOG.log(DEBUG, "Number of Invalid resources removed from pool {0}: {1}", poolInfo, noOfInvalidResources);
        } else {
            LOG.log(DEBUG, "Number of Idle resources freed from pool {0}: {1}", poolInfo, resourcesToRemove.size());
            LOG.log(DEBUG, "Number of Invalid resources removed from pool {0}: {1}", poolInfo, noOfInvalidResources);
        }
        return poolSizeBeforeRemoval - dataStructure.getResourcesSize();
    }

    /**
     * Removes invalid resource handles in the pool while resizing the pool.
     * Uses the Connector 1.5 spec 6.5.3.4 optional RA feature to obtain
     * invalid ManagedConnections
     *
     * @param freeConnectionsToValidate Set of free connections
     */
    private int removeInvalidResources(Set<ResourceHandle> freeConnectionsToValidate) {
        int invalidConnectionsCount = 0;
        try {
            LOG.log(DEBUG, "Sending a set of free connections to RA, of size:  {0}", freeConnectionsToValidate.size());
            try {
                for (ResourceHandle handle : freeConnectionsToValidate) {
                    if (handle != null) {
                        Set<ManagedConnection> connectionsToTest = new HashSet<>();
                        connectionsToTest.add(handle.getResource());
                        Set<ManagedConnection> invalidConnections = handler.getInvalidConnections(connectionsToTest);
                        if (invalidConnections != null && !invalidConnections.isEmpty()) {
                            invalidConnectionsCount = validateAndRemoveResource(handle, invalidConnections);
                        }
                    }
                }
            } finally {
                LOG.log(DEBUG, "No. of invalid resources received from RA: {0}", invalidConnectionsCount);
            }
        } catch (Exception re) {
            LOG.log(WARNING, "Removing invalid resources from the pool " + poolInfo + " failed!", re);
        }
        return invalidConnectionsCount;
    }
}
