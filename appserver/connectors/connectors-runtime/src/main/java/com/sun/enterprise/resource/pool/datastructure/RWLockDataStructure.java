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

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.StampedLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optimistic read-write lock based datastructure for pool.
 *
 * <p><strong>Warning:</strong> Do not replace {@code for} loops with the {@code foreach} loops
 * because we do not reduce internal array size after resources removal for performance reason.
 *
 * @author Jagadish Ramu
 * @author Alexander Pincuk
 */
public class RWLockDataStructure implements DataStructure {

    private static final Logger LOG = LogDomains.getLogger(RWLockDataStructure.class, LogDomains.RSR_LOGGER);

    private final StampedLock lock = new StampedLock();
    private final DataStructureSemaphore availableResources;

    private final ResourceHandler handler;
    private final BitSet useMask;
    private ResourceHandle[] resources;
    private int size;

    private volatile int maxSize;

    public RWLockDataStructure(String parameters, int maxSize, ResourceHandler handler, String strategyClass) {
        this.availableResources = new DataStructureSemaphore(maxSize);
        this.useMask = new BitSet(maxSize);
        this.resources = new ResourceHandle[maxSize];
        this.handler = handler;
        this.maxSize = maxSize;

        LOG.log(Level.FINEST, "pool.datastructure.rwlockds.init");
    }


    @Override
    public int addResource(ResourceAllocator allocator, int count) throws PoolingException {
        int numResAdded = 0;
        for (int i = 0; i < count; i++) {
            if (!availableResources.tryAcquire()) {
                break;
            }

            ResourceHandle resource;
            try {
                resource = handler.createResource(allocator);
            } catch (Exception e) {
                availableResources.release();
                throw new PoolingException(e.getMessage(), e);
            }

            long stamp = lock.tryOptimisticRead();
            try {
                for (;; stamp = lock.writeLock()) {
                    if (stamp == 0L) {
                        continue;
                    }

                    int currentLength = resources.length;
                    int currentMaxSize = maxSize;

                    ResourceHandle[] newResources = null;
                    if (currentLength < currentMaxSize) {
                        newResources = Arrays.copyOf(resources, currentMaxSize);
                    }

                    resource.setIndex(size);

                    stamp = lock.tryConvertToWriteLock(stamp);
                    if (stamp == 0L) {
                        continue;
                    }

                    if (newResources != null) {
                        resources = newResources;
                    }
                    resources[size++] = resource;

                    break;
                }
            } finally {
                if (StampedLock.isWriteLockStamp(stamp)) {
                    lock.unlockWrite(stamp);
                }
            }
            numResAdded++;
        }
        return numResAdded;
    }

    @Override
    public ResourceHandle getResource() {
        long stamp = lock.tryOptimisticRead();
        try {
            for (;; stamp = lock.writeLock()) {
                if (stamp == 0L) {
                    continue;
                }

                int index = useMask.nextClearBit(0);
                int currentSize = size;
                if (!lock.validate(stamp)) {
                    continue;
                }

                if (index >= currentSize) {
                    return null;
                }

                stamp = lock.tryConvertToWriteLock(stamp);
                if (stamp == 0L) {
                    continue;
                }

                useMask.set(index);

                return resources[index];
            }
        } finally {
            if (StampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    @Override
    public void removeResource(ResourceHandle resource) {
        boolean removed = false;

        long stamp = lock.tryOptimisticRead();
        try {
            for (;; stamp = lock.writeLock()) {
                if (stamp == 0L) {
                    continue;
                }

                int removeIndex = resource.getIndex();

                ResourceHandle currentResource = resources[removeIndex];
                if (!lock.validate(stamp)) {
                    continue;
                }

                if (currentResource != resource) {
                    break;
                }

                stamp = lock.tryConvertToWriteLock(stamp);
                if (stamp == 0L) {
                    continue;
                }

                availableResources.release();

                int lastIndex = size - 1;
                if (removeIndex < lastIndex) {
                    // Move last resource in place of removed
                    ResourceHandle lastResource = resources[lastIndex];
                    lastResource.setIndex(removeIndex);
                    resources[removeIndex] = lastResource;
                    useMask.set(removeIndex, useMask.get(lastIndex));
                }
                resources[lastIndex] = null;
                useMask.clear(lastIndex);
                size = lastIndex;
                removed = true;

                break;
            }
        } finally {
            if (StampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }

        if (removed) {
            handler.deleteResource(resource);
        }
    }

    @Override
    public void returnResource(ResourceHandle resource) {
        long stamp = lock.tryOptimisticRead();
        try {
            for (;; stamp = lock.writeLock()) {
                if (stamp == 0L) {
                    continue;
                }

                int returnIndex = resource.getIndex();

                ResourceHandle currentResource = resources[returnIndex];
                if (!lock.validate(stamp)) {
                    continue;
                }

                if (currentResource != resource) {
                    break;
                }

                stamp = lock.tryConvertToWriteLock(stamp);
                if (stamp == 0L) {
                    continue;
                }

                useMask.clear(returnIndex);

                break;
            }
        } finally {
            if (StampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    @Override
    public int getFreeListSize() {
        long stamp = lock.tryOptimisticRead();
        try {
            for (;; stamp = lock.readLock()) {
                if (stamp == 0L) {
                    continue;
                }

                int freeListSize = size - useMask.cardinality();
                if (!lock.validate(stamp)) {
                    continue;
                }

                return freeListSize;
            }
        } finally {
            if (StampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    @Override
    public void removeAll() {
        ResourceHandle[] resourcesToRemove;

        long stamp = lock.writeLock();
        try {
            availableResources.release(size);
            resourcesToRemove = Arrays.copyOf(resources, size);
            Arrays.fill(resources, 0, size, null);
            useMask.clear(0, size);
            size = 0;
        } finally {
            lock.unlockWrite(stamp);
        }

        for (ResourceHandle resourceToRemove : resourcesToRemove) {
            handler.deleteResource(resourceToRemove);
        }
    }

    @Override
    public int getResourcesSize() {
        long stamp = lock.tryOptimisticRead();
        try {
            for (;; stamp = lock.readLock()) {
                if (stamp == 0L) {
                    continue;
                }

                int currentSize = size;
                if (!lock.validate(stamp)) {
                    continue;
                }

                return currentSize;
            }
        } finally {
            if (StampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    @Override
    public synchronized void setMaxSize(int newMaxSize) {
        int permits = newMaxSize - maxSize;

        switch (Integer.signum(permits)) {
            case 1:
                availableResources.release(permits);
                break;
            case -1:
                availableResources.reducePermits(Math.abs(permits));
                break;
            default:
                return;
        }

        this.maxSize = newMaxSize;
    }

    @Override
    public List<ResourceHandle> getAllResources() {
        long stamp = lock.tryOptimisticRead();
        try {
            for (;; stamp = lock.readLock()) {
                if (stamp == 0L) {
                    continue;
                }

                ResourceHandle[] allResources = Arrays.copyOf(resources, size);
                if (!lock.validate(stamp)) {
                    continue;
                }

                return Arrays.asList(allResources);
            }
        } finally {
            if (StampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    /**
     * Semaphore whose available permits change according to the
     * changes in max-pool-size via a reconfiguration.
     */
    private static final class DataStructureSemaphore extends Semaphore {

        public DataStructureSemaphore(int permits) {
            super(permits);
        }

        @Override
        protected void reducePermits(int reduction) {
            super.reducePermits(reduction);
        }
    }
}
