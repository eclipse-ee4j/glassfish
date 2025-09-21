/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceState;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.pool.PoolProperties;
import com.sun.enterprise.resource.pool.ResourceHandler;
import com.sun.enterprise.resource.pool.datastructure.DataStructure;

import jakarta.resource.spi.ManagedConnection;

import java.lang.System.Logger;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

import org.glassfish.resourcebase.resources.api.PoolInfo;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Resizer to remove unusable connections, maintain steady-pool <br>
 * <code>
 * Remove all invalid and idle resources, as a result one of the following may happen<br>
 * i)   equivalent to "pool-resize" quantity of resources are removed<br>
 * ii)  less than "pool-reize" quantity of resources are removed<br>
 * remove more resources to match pool-resize quantity, atmost scale-down till steady-pool-size<br>
 * iii) more than "pool-resize" quantity of resources are removed<br>
 * (1) if pool-size is less than steady-pool-size, bring it back to steady-pool-size.<br>
 * (2) if pool-size is greater than steady-pool-size, don't do anything.<br></code>
 *
 * @author Jagadish Ramu
 */
public class Resizer extends TimerTask {

    private static final Logger LOG = System.getLogger(Resizer.class.getName());

    protected PoolInfo poolInfo;
    protected DataStructure dataStructure;
    protected PoolProperties pool;
    protected ResourceHandler handler;
    protected boolean preferValidateOverRecreate;

    public Resizer(PoolInfo poolInfo, DataStructure ds, PoolProperties pp, ResourceHandler handler, boolean preferValidateOverRecreate) {
        this.poolInfo = poolInfo;
        this.dataStructure = ds;
        this.pool = pp;
        this.handler = handler;
        this.preferValidateOverRecreate = preferValidateOverRecreate;
    }

    @Override
    public void run() {
        LOG.log(DEBUG, () -> "Resizing the pool " + poolInfo);
        try {
            resizePool(true);
        } catch (Exception e) {
            LOG.log(WARNING, "Resizing the pool failed for pool: " + poolInfo, e);
        }
    }

    /**
     * Resize the pool by removing idle and invalid resources.<br>
     * Only when forced is true the pool size is scaled down with the pool resize quantity.
     *
     * @param forced when force is true, scale down the pool with the pool resize quantity.
     */
    public void resizePool(boolean forced) {

        // If the wait queue is NOT empty, don't do anything.
        if (pool.getWaitQueueLength() > 0) {
            return;
        }

        // remove invalid and idle resource(s)
        int noOfResourcesRemoved = removeIdleAndInvalidResources();
        int poolScaleDownQuantity = pool.getResizeQuantity() - noOfResourcesRemoved;

        // scale down pool by at most "resize-quantity"
        scaleDownPool(poolScaleDownQuantity, forced);

        // ensure that steady-pool-size is maintained
        ensureSteadyPool();

        LOG.log(DEBUG, "No. of resources held for pool {0}: ", poolInfo, dataStructure.getResourcesSize());
    }

    /**
     * Make sure that steady pool size is maintained after all idle-timed-out, invalid and scale-down resource removals.
     */
    private void ensureSteadyPool() {
        if (dataStructure.getResourcesSize() < pool.getSteadyPoolSize()) {
            // Create resources to match the steady pool size
            for (int i = dataStructure.getResourcesSize(); i < pool.getSteadyPoolSize(); i++) {
                try {
                    handler.createResourceAndAddToPool();
                } catch (PoolingException e) {
                    LOG.log(WARNING, "Steady pool size could not be ensured for pool: " + poolInfo, e);
                }
            }
        }
    }

    /**
     * Scale down pool by a <code>size &lt;= pool-resize-quantity</code> but only if forced is true
     *
     * @param scaleDownQuantity the number of resources to remove
     * @param forced scale-down only when forced value is true
     *
     * TODO: move forced parameter out of this method and move it to the calling code
     */
    protected void scaleDownPool(int scaleDownQuantity, boolean forced) {
        if (pool.getResizeQuantity() > 0 && forced) {

            scaleDownQuantity = (scaleDownQuantity <= (dataStructure.getResourcesSize() - pool.getSteadyPoolSize())) ? scaleDownQuantity : 0;

            ResourceHandle h;
            while (scaleDownQuantity > 0 && ((h = dataStructure.getResource()) != null)) {
                dataStructure.removeResource(h);
                scaleDownQuantity--;
            }
        }
    }

    /**
     * Get the free connections list from the pool, remove idle-timed-out resources and then invalid resources.
     *
     * @return int number of resources removed
     */
    protected int removeIdleAndInvalidResources() {

        int poolSizeBeforeRemoval = dataStructure.getResourcesSize();
        // Find all Connections that are free/not-in-use
        int size = dataStructure.getFreeListSize();
        // let's cache the current time since precision is not required here.
        long currentTime = System.currentTimeMillis();
        int validConnectionsCounter = 0;
        int idleConnKeptInSteadyCounter = 0;

        // iterate through all the active resources to find idle-time lapsed ones.
        ResourceHandle h;
        Set<ResourceHandle> activeResources = new HashSet<>();
        Set<String> resourcesToValidate = new HashSet<>();
        try {
            while ((h = dataStructure.getResource()) != null) {
                ResourceState state = h.getResourceState();
                if (currentTime - state.getLastUsage() < pool.getIdleTimeout()) {
                    // Should be added for validation.
                    validConnectionsCounter++;
                    resourcesToValidate.add(h.toString());
                    activeResources.add(h);
                } else {
                    boolean isResourceEligibleForRemoval = isResourceEligibleForRemoval(h, validConnectionsCounter);
                    if (!isResourceEligibleForRemoval) {
                        // preferValidateOverrecreate true and connection is valid within SPS
                        validConnectionsCounter++;
                        idleConnKeptInSteadyCounter++;
                        activeResources.add(h);
                        LOG.log(DEBUG,"PreferValidateOverRecreate: Keeping idle resource {0}"
                            + " in the steady part of the free pool as the RA reports it to be valid ({1} <= {2})",
                            h, validConnectionsCounter, pool.getSteadyPoolSize());

                    } else {
                        // Add to remove
                        dataStructure.removeResource(h);
                    }
                }
            }
        } finally {
            for (ResourceHandle activeResource : activeResources) {
                dataStructure.returnResource(activeResource);
            }
        }

        // remove invalid resources from the free (active) resources list.
        // Since the whole pool is not locked, it may happen that some of these resources may be
        // given to applications.
        removeInvalidResources(resourcesToValidate);

        // These statistic computations will work fine as long as resizer locks the pool throughout its operations.
        if (preferValidateOverRecreate) {
            LOG.log(DEBUG, "Idle resources validated and kept in the steady pool {0}: {1}", poolInfo,
                idleConnKeptInSteadyCounter);
            LOG.log(DEBUG, "Number of Idle resources freed from pool {0}: {1}", poolInfo,
                size - activeResources.size() - idleConnKeptInSteadyCounter);
            LOG.log(DEBUG, "Number of Invalid resources removed from pool {0}: {1}", poolInfo,
                activeResources.size() - dataStructure.getFreeListSize() + idleConnKeptInSteadyCounter);
        } else {
            LOG.log(DEBUG, "Number of Idle resources freed from pool {0}: {1}", poolInfo, size - activeResources.size());
            LOG.log(DEBUG, "Number of Invalid resources removed from pool {0}: {1}", poolInfo,
                activeResources.size() - dataStructure.getFreeListSize());
        }
        return poolSizeBeforeRemoval - dataStructure.getResourcesSize();
    }


    /**
     * Removes invalid resource handles in the pool while resizing the pool. Uses the Connector 1.5
     * spec 6.5.3.4 optional RA feature to obtain invalid ManagedConnections
     *
     * @param freeConnectionsToValidate Set of free connections
     */
    private void removeInvalidResources(Set<String> freeConnectionsToValidate) {
        try {
            LOG.log(DEBUG, "Sending a set of free connections to RA, of size: {0}", freeConnectionsToValidate.size());
            int invalidConnectionsCount = 0;
            ResourceHandle handle;
            Set<ResourceHandle> validResources = new HashSet<>();
            try {
                while ((handle = dataStructure.getResource()) != null) {
                    // validate if the connection is one in the freeConnectionsToValidate
                    if (freeConnectionsToValidate.contains(handle.toString())) {
                        Set<ManagedConnection> connectionsToTest = new HashSet<>();
                        connectionsToTest.add(handle.getResource());
                        Set<ManagedConnection> invalidConnections = handler.getInvalidConnections(connectionsToTest);
                        if (invalidConnections != null && !invalidConnections.isEmpty()) {
                            invalidConnectionsCount = validateAndRemoveResource(handle, invalidConnections);
                        } else {
                            // valid resource, return to pool
                            validResources.add(handle);
                        }
                    } else {
                        // valid resource, return to pool
                        validResources.add(handle);
                    }
                }
            } finally {
                for (ResourceHandle resourceHandle : validResources) {
                    dataStructure.returnResource(resourceHandle);
                }
                validResources.clear();
                LOG.log(DEBUG, "No. of invalid resources received from RA: {0}", invalidConnectionsCount);
            }
        } catch (Exception re) {
            LOG.log(WARNING, "Removing invalid resources from the pool " + poolInfo + " failed!", re);
        }
    }

    protected int validateAndRemoveResource(ResourceHandle handle, Set<ManagedConnection> invalidConnections) {
        int invalidConnectionsCount = 0;
        for (ManagedConnection invalid : invalidConnections) {
            if (invalid.equals(handle.getResource())) {
                dataStructure.removeResource(handle);
                handler.invalidConnectionDetected(handle);
                invalidConnectionsCount++;
            }
        }
        return invalidConnectionsCount;
    }

    protected boolean isResourceEligibleForRemoval(ResourceHandle handle, int validConnectionsCounter) {
        ResourceAllocator allocator = handle.getResourceAllocator();
        if (!preferValidateOverRecreate || !allocator.hasValidatingMCF()) {
            return true;
        }
        // remove all idle-time lapsed resources.
        // validConnectionsCounter is incremented if the connection
        // is valid but only till the steady pool size.
        if (validConnectionsCounter < pool.getSteadyPoolSize() && allocator.isConnectionValid(handle)) {
            handle.getResourceState().setLastValidated(System.currentTimeMillis());
            return false;
        }
        // Connection invalid and hence remove resource.
        if (LOG.isLoggable(DEBUG)) {
            if (validConnectionsCounter <= pool.getSteadyPoolSize()) {
                LOG.log(DEBUG, "PreferValidateOverRecreate: Removing idle resource " + handle
                    + " from the free pool as the RA reports it to be invalid");
            } else {
                LOG.log(DEBUG, "PreferValidateOverRecreate: Removing idle resource " + handle
                    + " from the free pool as the steady part size has already been exceeded ("
                    + validConnectionsCounter + " > " + pool.getSteadyPoolSize() + ")");
            }
        }
        return true;
    }
}
