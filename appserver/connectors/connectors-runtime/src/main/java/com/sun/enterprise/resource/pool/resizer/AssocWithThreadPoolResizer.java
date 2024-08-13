/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnection;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.glassfish.resourcebase.resources.api.PoolInfo;

/**
 * Resizer for Associate With Thread type pools to remove unusable connections
 * and maintain steady pool size.
 *
 * @author Shalini M
 */
public class AssocWithThreadPoolResizer extends Resizer {

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
        if (pool.getResizeQuantity() > 0 && forced) {

            scaleDownQuantity = (scaleDownQuantity <=
                    (dataStructure.getResourcesSize() - pool.getSteadyPoolSize())) ? scaleDownQuantity : 0;

            debug("Scaling down pool by quantity : " + scaleDownQuantity);
            Set<ResourceHandle> resourcesToRemove = new HashSet<>();
            try {
                for (ResourceHandle h : dataStructure.getAllResources()) {
                    if (scaleDownQuantity > 0) {
                        synchronized (h.lock) {
                            if (!h.getResourceState().isBusy()) {
                                resourcesToRemove.add(h);
                                ((AssocWithThreadResourceHandle) h).setDirty();
                                scaleDownQuantity--;
                            }
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
        int noOfResourcesRemoved = 0;
        // let's cache the current time since precision is not required here.
        long currentTime = System.currentTimeMillis();
        int validConnectionsCounter = 0;
        int idleConnKeptInSteadyCounter = 0;
        ResourceState state;

        Set<ResourceHandle> resourcesToValidate = new HashSet<>();
        Set<ResourceHandle> resourcesToRemove = new HashSet<>();
        try {
            //iterate through all the resources to find idle-time lapsed ones.
            for (ResourceHandle h : dataStructure.getAllResources()) {
                synchronized (h.lock) {
                    state = h.getResourceState();
                    if (!state.isBusy()) {
                        if (currentTime - state.getTimestamp() < pool.getIdleTimeout()) {
                            //Should be added for validation.
                            if (state.isUnenlisted() && state.isFree()) {
                                if (((AssocWithThreadResourceHandle) h).isAssociated()) {
                                    ((AssocWithThreadResourceHandle) h).setAssociated(false);
                                    validConnectionsCounter++;
                                    resourcesToValidate.add(h);
                                }
                            }
                        } else {
                            boolean isResourceEligibleForRemoval =
                                    isResourceEligibleForRemoval(h, validConnectionsCounter);
                            if (!isResourceEligibleForRemoval) {
                                //preferValidateOverrecreate true and connection is valid within SPS
                                validConnectionsCounter++;
                                idleConnKeptInSteadyCounter++;
                                debug("PreferValidateOverRecreate: Keeping idle resource "
                                        + h + " in the steady part of the free pool "
                                        + "as the RA reports it to be valid (" + validConnectionsCounter
                                        + " <= " + pool.getSteadyPoolSize() + ")");
                            } else {
                                //Add this to remove later
                                resourcesToRemove.add(h);
                                ((AssocWithThreadResourceHandle) h).setDirty();
                            }
                        }
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

        //remove invalid resources from the free (active) resources list.
        //Since the whole pool is not locked, it may happen that some of these
        //resources may be given to applications.
        int noOfInvalidResources = removeInvalidResources(resourcesToValidate);

        //These statistic computations will work fine as long as resizer
        //locks the pool throughout its operations.
        if (preferValidateOverRecreate) {
            debug("Idle resources validated and kept in the steady pool for pool [ "
                    + poolInfo + " ] - " + idleConnKeptInSteadyCounter);
            debug("Number of Idle resources freed for pool [ " + poolInfo + " ] - "
                    + (resourcesToRemove.size()));
            debug("Number of Invalid resources removed for pool [ " + poolInfo + " ] - "
                    + noOfInvalidResources);
        } else {
            debug("Number of Idle resources freed for pool [ " + poolInfo + " ] - "
                    + resourcesToRemove.size());
            debug("Number of Invalid resources removed for pool [ " + poolInfo + " ] - "
                    + noOfInvalidResources);
        }
        noOfResourcesRemoved = poolSizeBeforeRemoval - dataStructure.getResourcesSize();
        return noOfResourcesRemoved;
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
            debug("Sending a set of free connections to RA, "
                    + "of size : " + freeConnectionsToValidate.size());
            try {
                for (ResourceHandle handle : freeConnectionsToValidate) {
                    if (handle != null) {
                        Set<ManagedConnection> connectionsToTest = new HashSet<>();
                        connectionsToTest.add((ManagedConnection) handle.getResource());
                        Set<ManagedConnection> invalidConnections = handler.getInvalidConnections(connectionsToTest);
                        if (invalidConnections != null && !invalidConnections.isEmpty()) {
                            invalidConnectionsCount = validateAndRemoveResource(handle, invalidConnections);
                        }
                    }
                }
            } finally {
                debug("No. of invalid connections received from RA : " + invalidConnectionsCount);
            }
        } catch (ResourceException re) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "ResourceException while trying to get invalid connections from MCF", re);
            }
        } catch (Exception e) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Exception while trying to get invalid connections from MCF", e);
            }
        }
        return invalidConnectionsCount;
    }
}
