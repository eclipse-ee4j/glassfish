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
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ReadWriteLock based datastructure for pool
 *
 * @deprecated Incorrect locking. Note: ListDataStructure uses synchronization, better, but slow.
 * @author Jagadish Ramu
 */
@Deprecated
public class RWLockDataStructure implements DataStructure {
    private static final Logger LOG = LogDomains.getLogger(RWLockDataStructure.class, LogDomains.RSR_LOGGER);

    private int maxSize;

    private final ResourceHandler handler;
    private final ArrayList<ResourceHandle> resources;

    private final ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = reentrantLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = reentrantLock.writeLock();


    public RWLockDataStructure(String parameters, int maxSize, ResourceHandler handler, String strategyClass) {
        this.resources = new ArrayList<>(maxSize);
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
            for (int i = 0; i < count && resources.size() < maxSize; i++) {
                ResourceHandle handle = handler.createResource(allocator);
                resources.add(handle);
                numResAdded++;
            }
        } catch (Exception e) {
            PoolingException pe = new PoolingException(e.getMessage());
            pe.initCause(e);
            throw pe;
        } finally {
            writeLock.unlock();
        }
        return numResAdded;
    }

    @Override
    public ResourceHandle getResource() {
        readLock.lock();
        try {
            for (ResourceHandle resource : resources) {
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
            removed = resources.remove(resource);
        } finally {
            writeLock.unlock();
        }
        if(removed) {
            handler.deleteResource(resource);
        }
    }

    @Override
    public void returnResource(ResourceHandle resource) {
        writeLock.lock();
        try{
            resource.setBusy(false);
        }finally{
            writeLock.unlock();
        }
    }

    @Override
    public int getFreeListSize() {
        //inefficient implementation.
        int free = 0;
        readLock.lock();
        try{
            for (ResourceHandle rh : resources) {
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
        writeLock.lock();
        try {
            Iterator<ResourceHandle> it = resources.iterator();
            while (it.hasNext()) {
                handler.deleteResource(it.next());
                it.remove();
            }
            resources.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public int getResourcesSize() {
        return resources.size();
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
