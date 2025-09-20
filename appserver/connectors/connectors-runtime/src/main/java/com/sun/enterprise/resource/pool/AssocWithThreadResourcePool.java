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

package com.sun.enterprise.resource.pool;

import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.resource.AssocWithThreadResourceHandle;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.pool.datastructure.DataStructureFactory;
import com.sun.enterprise.resource.pool.datastructure.ListDataStructure;
import com.sun.enterprise.resource.pool.resizer.AssocWithThreadPoolResizer;
import com.sun.enterprise.resource.pool.resizer.Resizer;

import java.util.Hashtable;

import org.glassfish.resourcebase.resources.api.PoolInfo;

/**
 * Associates a resource with the thread. When the same thread is used again, it checks whether the resource associated
 * with the thread can serve the request.
 *
 * @author Aditya Gore, Jagadish Ramu
 */
public class AssocWithThreadResourcePool extends ConnectionPool {

    private final ThreadLocal<AssocWithThreadResourceHandle> localResource = new ThreadLocal<>();

    public AssocWithThreadResourcePool(PoolInfo poolInfo, Hashtable env) throws PoolingException {
        super(poolInfo, env);
    }

    @Override
    protected void initializePoolDataStructure() throws PoolingException {
        dataStructure = DataStructureFactory.getDataStructure(ListDataStructure.class.getName(),
                dataStructureParameters, maxPoolSize, this);
    }

    /**
     * Prefetch is called to check whether there there is a free resource is already associated with the thread.
     * Only when prefetch is unable to find a resource, normal routine (getUnenlistedResource) will happen.
     *
     * @param spec the ResourceSpec used to locate the correct resource pool
     * @param alloc ResourceAllocator to create a resource
     * @return ResourceHandle resource associated with the thread, if any
     */
    @Override
    protected ResourceHandle prefetch(ResourceSpec spec, ResourceAllocator alloc) {
        AssocWithThreadResourceHandle handle = localResource.get();
        if (handle == null) {
            return null;
        }
        // synch on ar and do a quick-n-dirty check to see if the local
        // resource is usable at all
        handle.lock();
        try {
            if (handle.getThreadId() != Thread.currentThread().getId() || handle.hasConnectionErrorOccurred()
                || handle.isUnusable() || !handle.isAssociated()) {
                // we were associated with someone else or resource error
                // occurred or resource was disassociated and used by some one else. So evict
                // NOTE: We do not setAssociated to false here since someone
                // else has associated this resource to themself. Also, if
                // the eviction is because of a resourceError, the resource is
                // not going to be used anyway.

                localResource.remove();
                return null;
            }
            if (handle.getResourceState().isBusy() || handle.getResourceState().isEnlisted()) {
                return null;
            }
            if (matchConnections) {
                if (!alloc.matchConnection(handle)) {
                    // again, since the credentials of the caller don't match
                    // evict from ThreadLocal
                    // also, mark the resource as unassociated and make this resource
                    // potentially usable
                    localResource.remove();
                    handle.setAssociated(false);
                    if (poolLifeCycleListener != null) {
                        poolLifeCycleListener.connectionNotMatched();
                    }
                    return null;
                }
                if (poolLifeCycleListener != null) {
                    poolLifeCycleListener.connectionMatched();
                }
            }

            if (!isConnectionValid(handle, alloc)) {
                localResource.remove();
                handle.setAssociated(false);
                // disassociating the connection from the thread.
                // validation failure will mark the connectionErrorOccurred flag
                // and the connection will be removed whenever it is retrieved again
                // from the pool.
                return null;
            }

            setResourceStateToBusy(handle);
            handle.getResourceState().incrementUsageCount();
            if (poolLifeCycleListener != null) {
                poolLifeCycleListener.connectionUsed(handle.getId());
                // Decrement numConnFree
                poolLifeCycleListener.decrementNumConnFree();

            }
            return handle;
        } finally {
            handle.unlock();
        }
    }

    @Override
    protected Resizer initializeResizer() {
        return new AssocWithThreadPoolResizer(poolInfo, dataStructure, this, this, preferValidateOverRecreate);
    }

    /**
     * check whether the resource is unused
     *
     * @param h ResourceHandle
     * @return boolean representing resource usefullness
     */
    @Override
    protected boolean isResourceUnused(ResourceHandle h) {
        if (h instanceof AssocWithThreadResourceHandle) {
            return !h.getResourceState().isBusy() && !((AssocWithThreadResourceHandle) h).isAssociated();
        }
        return !h.getResourceState().isBusy();
    }

    /**
     * This is the RI getUnenlistedResource() with some modifications
     * return resource in free list. If none is found, returns null
     */
    @Override
    protected ResourceHandle getUnenlistedResource(ResourceSpec spec, ResourceAllocator alloc) throws PoolingException {
        ResourceHandle handle = resolvePossibleRemoval(super.getUnenlistedResource(spec, alloc));
        // If we came here, that's because free doesn't have anything
        // to offer us. This could be because:
        // 1. All free resources are associated
        // 2. There are no free resources
        // 3. We cannot create anymore free resources
        // Handle case 1 here
        if (handle == null) {
            handle = searchFreeUnenlisted(alloc);
        }
        if (localResource.get() == null && handle instanceof AssocWithThreadResourceHandle) {
            setInThreadLocal((AssocWithThreadResourceHandle) handle);
        }
        return handle;
    }

    /**
     * Return the resource back to pool only if it is not associated with the thread.
     *
     * @param resourceHandle the ResourceHandle to be returned
     */
    @Override
    protected synchronized void freeUnenlistedResource(ResourceHandle resourceHandle) {
        if (cleanupResource(resourceHandle)) {
            if (resourceHandle instanceof AssocWithThreadResourceHandle) {
                // Only when resource handle usage count is more than maxConnUsage
                if (maxConnectionUsage > 0 && resourceHandle.getResourceState().getUsageCount() >= maxConnectionUsage) {
                    performMaxConnectionUsageOperation(resourceHandle);
                } else {
                    if (!((AssocWithThreadResourceHandle) resourceHandle).isAssociated()) {
                        returnResourceToPool(resourceHandle);
                    }
                    // update monitoring data
                    if (poolLifeCycleListener != null) {
                        poolLifeCycleListener.decrementConnectionUsed(resourceHandle.getId());
                        poolLifeCycleListener.incrementNumConnFree(false, steadyPoolSize);
                    }
                }
                // for both the cases of free.add and maxConUsageOperation, a free resource is added.
                // Hence notify waiting threads
                notifyWaitingThreads();
            }
        }
    }

    /**
     * destroys the resource
     *
     * @param resourceHandle resource to be destroyed
     */
    @Override
    public void deleteResource(ResourceHandle resourceHandle) {
        try {
            super.deleteResource(resourceHandle);
        } finally {
            if (resourceHandle instanceof AssocWithThreadResourceHandle) {
                ((AssocWithThreadResourceHandle) resourceHandle).setUnusable();
            }
        }
    }

    /**
     * to associate a resource with the thread
     *
     * @param h ResourceHandle
     */
    private void setInThreadLocal(AssocWithThreadResourceHandle h) {
        if (h == null) {
            return;
        }
        h.lock();
        try {
            h.setAssociated(true);
            localResource.set(h);
        } finally {
            h.unlock();
        }
    }

    /**
     * It is possible that Resizer might have marked the resource for recycle
     * and hence we should not use this resource.
     */
    private ResourceHandle resolvePossibleRemoval(ResourceHandle handle) {
        if (handle == null) {
            return null;
        }
        handle.lock();
        try {
            if (dataStructure.getAllResources().contains(handle) && ((AssocWithThreadResourceHandle) handle).isUnusable()) {
                dataStructure.removeResource(handle);
                return null;
            }
            return handle;
        } finally {
            handle.unlock();
        }
    }

    private synchronized ResourceHandle searchFreeUnenlisted(ResourceAllocator alloc) {
        for (ResourceHandle handle : dataStructure.getAllResources()) {
            handle.lock();
            try {
                // though we are checking resources from within the free list,
                // we could have a situation where the resource was free upto
                // this point, put just before we entered the synchronized block,
                // the resource "h" got used by the thread that was associating it
                // so we need to check for isFree also

                if (handle.getResourceState().isEnlisted() || handle.getResourceState().isBusy()
                    || handle.hasConnectionErrorOccurred() || ((AssocWithThreadResourceHandle) handle).isUnusable()
                    || !matchConnection(handle, alloc)) {
                    continue;
                }
                setResourceStateToBusy(handle);
                ((AssocWithThreadResourceHandle) handle).setAssociated(false);
                return handle;
            } finally {
                handle.unlock();
            }
        }
        return null;
    }
}
