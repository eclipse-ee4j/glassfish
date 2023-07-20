/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
import com.sun.logging.LogDomains;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ReadWriteLock based datastructure for pool.
 *
 * @author Jagadish Ramu
 */
public class RWLockDataStructure implements DataStructure {

    private static final Logger LOG = LogDomains.getLogger(RWLockDataStructure.class, LogDomains.RSR_LOGGER);

    private final ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = reentrantLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = reentrantLock.writeLock();

    private final ResourceHandler handler;
    private ResourceHandle[] resources;
    private int size;

    private int maxSize;

    public RWLockDataStructure(String parameters, int maxSize, ResourceHandler handler, String strategyClass) {
        this.resources = new ResourceHandle[maxSize];
        this.handler = handler;
        this.maxSize = maxSize;
        LOG.log(Level.FINEST, "pool.datastructure.rwlockds.init");
    }


    @Override
    public int addResource(ResourceAllocator allocator, int count) throws PoolingException {
        int numResAdded = 0;
        writeLock.lock();
        //for now, coarser lock. finer lock needs "resources.size() < maxSize()" once more.
        try {
            for (int i = 0; i < count && size < maxSize; i++) {
                if (resources.length < maxSize) {
                    resources = Arrays.copyOf(resources, maxSize);
                }
                ResourceHandle handle = handler.createResource(allocator);
                handle.setIndex(size);
                resources[size++] = handle;
                numResAdded++;
            }
        } catch (Exception e) {
            throw new PoolingException(e.getMessage(), e);
        } finally {
            writeLock.unlock();
        }
        return numResAdded;
    }

    @Override
    public ResourceHandle getResource() {
        readLock.lock();
        try {
            for (int i = 0; i < size; i++) {
                ResourceHandle resource = resources[i];
                if (!resource.isBusy()) {
                    if (resource.trySetBusy(true)) {
                        return resource;
                    }
                }
            }
        } finally {
            readLock.unlock();
        }
        return null;
    }

    @Override
    public void removeResource(ResourceHandle resource) {
        boolean removed = false;
        writeLock.lock();
        try {
            int removeIndex = resource.getIndex();
            if (removeIndex >= 0 && removeIndex < size) {
                if (resources[removeIndex] == resource) {
                    int lastIndex = size - 1;
                    if (removeIndex < lastIndex) {
                        // Move last resource in place of removed
                        ResourceHandle lastResource = resources[lastIndex];
                        lastResource.setIndex(removeIndex);
                        resources[removeIndex] = lastResource;
                    }
                    resources[lastIndex] = null;
                    size = lastIndex;
                    removed = true;
                }
            }
        } finally {
            writeLock.unlock();
        }
        if(removed) {
            handler.deleteResource(resource);
        }
    }

    @Override
    public void returnResource(ResourceHandle resource) {
        // We use write lock to prevent an unnecessary resize
        writeLock.lock();
        try{
            resource.setBusy(false);
        }finally{
            writeLock.unlock();
        }
    }

    @Override
    public int getFreeListSize() {
        // inefficient implementation.
        int free = 0;
        readLock.lock();
        try{
            for (int i = 0; i < size; i++) {
                ResourceHandle rh = resources[i];
                if(!rh.isBusy()){
                    free++;
                }
            }
        }finally{
            readLock.unlock();
        }
        return free;
    }

    @Override
    public void removeAll() {
        ResourceHandle[] resourcesToRemove;

        writeLock.lock();
        try {
            resourcesToRemove = Arrays.copyOf(resources, size);
            Arrays.fill(resources, null);
            size = 0;
        } finally {
            writeLock.unlock();
        }

        for (ResourceHandle resourceToRemove : resourcesToRemove) {
            handler.deleteResource(resourceToRemove);
        }
    }

    @Override
    public int getResourcesSize() {
        return size;
    }

    @Override
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public ArrayList<ResourceHandle> getAllResources() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
