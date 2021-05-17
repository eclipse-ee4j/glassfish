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

package com.sun.enterprise.resource.pool.datastructure;

import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.pool.ResourceHandler;
import com.sun.enterprise.resource.pool.datastructure.strategy.ResourceSelectionStrategy;
import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.logging.LogDomains;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ReadWriteLock based datastructure for pool
 * @author Jagadish Ramu
 */
public class RWLockDataStructure implements DataStructure {

    private ResourceHandler handler;
    private ResourceSelectionStrategy strategy;
    private int maxSize;

    private final ArrayList<ResourceHandle> resources;
    private ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock readLock = reentrantLock.readLock();
    private ReentrantReadWriteLock.WriteLock writeLock = reentrantLock.writeLock();

    protected final static Logger _logger =
            LogDomains.getLogger(RWLockDataStructure.class,LogDomains.RSR_LOGGER);

    public RWLockDataStructure(String parameters, int maxSize,
                                              ResourceHandler handler, String strategyClass) {
        resources = new ArrayList<ResourceHandle>((maxSize > 1000) ? 1000 : maxSize);
        this.maxSize = maxSize;
        this.handler = handler;
        initializeStrategy(strategyClass);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, "pool.datastructure.rwlockds.init");
        }
    }

    private void initializeStrategy(String strategyClass) {
        //TODO
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public ResourceHandle getResource() {
        readLock.lock();
        try {
            for (int i = 0; i < resources.size(); i++) {
                ResourceHandle h = resources.get(i);
                if (!h.isBusy()) {
                    readLock.unlock();
                    writeLock.lock();
                    try {
                        if (!h.isBusy()) {
                            h.setBusy(true);
                            return h;
                        } else {
                            readLock.lock();
                            continue;
                        }
                    } finally {
                        writeLock.unlock();
                    }
                } else {
                    continue;
                }
            }
        } finally {
            try {
                readLock.unlock();
            } catch ( Exception e) {
                //ignore
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public void returnResource(ResourceHandle resource) {
        writeLock.lock();
        try{
            resource.setBusy(false);
        }finally{
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getFreeListSize() {
        //inefficient implementation.
        int free = 0;
        readLock.lock();
        try{
            Iterator it = resources.iterator();
            while (it.hasNext()) {
                ResourceHandle rh = (ResourceHandle)it.next();
                if(!rh.isBusy()){
                    free++;
                }
            }
        }finally{
            readLock.unlock();
        }
        return free;
    }

    /**
     * {@inheritDoc}
     */
    public void removeAll() {
        writeLock.lock();
        try {
            Iterator it = resources.iterator();
            while (it.hasNext()) {
                handler.deleteResource((ResourceHandle) it.next());
                it.remove();
            }
        } finally {
            writeLock.unlock();
        }
        resources.clear();
    }

    /**
     * {@inheritDoc}
     */
    public int getResourcesSize() {
        return resources.size();
    }

    /**
     * Set maxSize based on the new max pool size set on the connection pool
     * during a reconfiguration.
     *
     * @param maxSize
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public ArrayList<ResourceHandle> getAllResources() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
