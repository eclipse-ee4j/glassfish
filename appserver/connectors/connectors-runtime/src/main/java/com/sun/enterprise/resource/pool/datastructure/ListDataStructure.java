/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.resource.pool.datastructure;

import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.pool.ResourceHandler;
import com.sun.enterprise.resource.pool.datastructure.strategy.ResourceSelectionStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;


/**
 * List based datastructure that can be used by connection pool <br>
 *
 * @author Jagadish Ramu
 */
public class ListDataStructure implements DataStructure {
    private final ArrayList<ResourceHandle> free;
    private final ArrayList<ResourceHandle> resources;
    //Max Size of the datastructure.Depends mostly on the max-pool-size of
    // the connection pool.
    private int maxSize;
    private final DynamicSemaphore dynSemaphore;

    private ResourceHandler handler;
    private ResourceSelectionStrategy strategy;

    public ListDataStructure(String parameters, int maxSize, ResourceHandler handler) {
        resources = new ArrayList<>((maxSize > 1000) ? 1000 : maxSize);
        free = new ArrayList<>((maxSize > 1000) ? 1000 : maxSize);
        this.handler = handler;
        dynSemaphore = new DynamicSemaphore();
        setMaxSize(maxSize);
    }

    /**
     * Set maxSize based on the new max pool size set on the connection pool
     * during a reconfiguration.
     * 1. When permits contained within the dynamic semaphore are greater than 0,
     * maxSize is increased and hence so many permits are released.
     * 2. When permits contained within the dynamic semaphore are less than 0,
     * maxSize has reduced to a smaller value. Hence so many permits are reduced
     * from the semaphore's available limit for the subsequent resource requests
     * to act based on the new configuration.
     * @param newMaxSize
     */
    @Override
    public synchronized void setMaxSize(int newMaxSize) {

        //Find currently open with the current maxsize
        int permits = newMaxSize - this.maxSize;

        if (permits == 0) {
            //None are open
            return;
        } else if (permits > 0) {
            //Case when no of permits are increased
            this.dynSemaphore.release(permits);
        } else {
            //permits would be a -ve value
            //Case when no of permits are to be reduced.
            permits *= -1;
            this.dynSemaphore.reducePermits(permits);
        }
        this.maxSize = newMaxSize;
    }

    /**
     * creates a new resource and adds to the datastructure.
     *
     * @param allocator ResourceAllocator
     * @param count     Number (units) of resources to create
     * @return int number of resources added
     */
    @Override
    public int addResource(ResourceAllocator allocator, int count) throws PoolingException {
        int numResAdded = 0;
        for (int i = 0; i < count && resources.size() < maxSize; i++) {
            boolean lockAcquired = dynSemaphore.tryAcquire();
            if(lockAcquired) {
                try {
                    ResourceHandle handle = handler.createResource(allocator);
                    synchronized (resources) {
                        synchronized (free) {
                            free.add(handle);
                            resources.add(handle);
                            numResAdded++;
                        }
                    }
                } catch (Exception e) {
                    dynSemaphore.release();
                    PoolingException pe = new PoolingException(e.getMessage());
                    pe.initCause(e);
                    throw pe;
                }
            }
        }
        return numResAdded;
    }

    /**
     * get a resource from the datastructure
     *
     * @return ResourceHandle
     */
    @Override
    public ResourceHandle getResource() {
        ResourceHandle resource = null;
        if (strategy != null) {
            resource = strategy.retrieveResource();
        } else {
            synchronized (free) {
                if (free.size() > 0){
                    resource = free.remove(0);
                }
            }
        }
        return resource;
    }

    /**
     * remove the specified resource from the datastructure
     *
     * @param resource ResourceHandle
     */
    @Override
    public void removeResource(ResourceHandle resource) {
        boolean removed = false;
        synchronized (resources) {
            synchronized (free) {
                free.remove(resource);
                removed = resources.remove(resource);
            }
        }
        if(removed) {
            dynSemaphore.release();
            handler.deleteResource(resource);
        }
    }

    /**
     * returns the resource to the datastructure
     *
     * @param resource ResourceHandle
     */
    @Override
    public void returnResource(ResourceHandle resource) {
        synchronized (free) {
            free.add(resource);
        }
    }

    /**
     * get the count of free resources in the datastructure
     *
     * @return int count
     */
    @Override
    public int getFreeListSize() {
        return free.size();
    }

    /**
     * remove & destroy all resources from the datastructure.
     */
    @Override
    public void removeAll() {
        synchronized (resources) {
            synchronized (free) {
                while (resources.size() > 0) {
                    ResourceHandle handle = resources.remove(0);
                    free.remove(handle);
                    dynSemaphore.release();
                    handler.deleteResource(handle);
                }
            }
        }
        free.clear();
        resources.clear();
    }

    /**
     * get total number of resources in the datastructure
     *
     * @return int count
     */
    @Override
    public int getResourcesSize() {
        return resources.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceHandle> getAllResources() {
        return this.resources;
    }

    /**
     * Semaphore whose available permits change according to the
     * changes in max-pool-size via a reconfiguration.
     */
    private static final class DynamicSemaphore extends Semaphore {

        DynamicSemaphore() {
            //Default is 0
            super(0);
        }

        @Override
        protected void reducePermits(int size) {
            super.reducePermits(size);
        }
    }
}
