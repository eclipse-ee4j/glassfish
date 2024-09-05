/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
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
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.listener.PoolLifeCycleListener;
import com.sun.enterprise.resource.pool.waitqueue.PoolWaitQueue;

import jakarta.resource.spi.RetryableUnavailableException;
import jakarta.transaction.Transaction;

import org.glassfish.resourcebase.resources.api.PoolInfo;

/**
 * @author Tony Ng
 */
public interface ResourcePool {

    // Modify getResource() to throw PoolingException
    ResourceHandle getResource(ResourceSpec spec, ResourceAllocator alloc, Transaction transaction) throws PoolingException, RetryableUnavailableException;

    /**
     * Indicate that the resource is not used by a bean/application anymore.
     *
     * @param resource The resource that is not used anymore. After the call the resource is also marked as 'not busy' /
     * 'free'.
     */
    void resourceClosed(ResourceHandle resource);

    /**
     * Updates the resource to be marked as not busy / free, remove the resource from the connection pool and inform any
     * waiting threads that a resource has become available.
     *
     * @param resource the resource that will be removed from the connection pool
     */
    void resourceErrorOccurred(ResourceHandle resource);

    /**
     * This method is called when a resource is enlisted in a transaction.
     *
     * @param tran the Transaction to enlist the resource in
     * @param resource the ResourceHandle that will be enlisted in the given transaction
     */
    void resourceEnlisted(Transaction tran, ResourceHandle resource);

    /**
     * Get the Pool status by computing the free/used values of the connections in the pool. Computations are based on
     * whether the pool is initialized or not when this method is invoked.
     *
     * @return the PoolStatus object
     */
    PoolStatus getPoolStatus();

    /**
     * Called when a transaction is completed.<br>
     * All resource handles associated to the given transaction are delisted.<br>
     * All resource handles associated to the given transaction handed back to the connection pool.
     *
     * @param tran The transaction
     * @param status The jakarta.transaction.Status value of the transaction.
     */
    void transactionCompleted(Transaction tran, int status);

    /**
     * Resize the pool by removing idle and invalid resources.<br>
     * Only when forced is true the pool size is scaled down with the pool resize quantity.
     *
     * @param forced when force is true, scale down the pool with the pool resize quantity.
     */
    void resizePool(boolean forced);

    /**
     * Forcefully destroy all connections in the pool even if connections have transactions in progress
     */
    void emptyPool();

    /**
     * Reconfigure the Pool's properties. The reconfigConnectorConnectionPool method in the ConnectorRuntime will use this
     * method (through PoolManager) if it needs to just change pool properties and not recreate the pool
     *
     * @param poolResource - the ConnectorConnectionPool JavaBean that holds the new pool properties
     * @throws PoolingException if the pool resizing fails
     */
    void reconfigurePool(ConnectorConnectionPool ccp) throws PoolingException;

    /**
     * Cancel the resizer task in the pool if it exists.
     */
    void cancelResizerTask();

    /**
     * Switch on matching of connections in the pool.
     */
    void switchOnMatching();

    /**
     * Get the PoolInfo unique identifier of this pool.
     *
     * @return the PoolInfo unique identifier of this pool
     */
    PoolInfo getPoolInfo();

    void emptyFreeConnectionsInPool();

    // accessors for self mgmt

    /**
     * Gets the max-pool-size attribute of this pool. Envisaged to be used by the Self management framework to query the
     * pool size attribute for tweaking it using the setMaxPoolSize method
     *
     * @return max-pool-size value for this pool
     * @see setMaxPoolSize
     */
    int getMaxPoolSize();

    /**
     * Gets the steady-pool-size attribute of this pool. Envisaged to be used by the Self management framework to query the
     * pool size attribute for tweaking it using the setSteadyPoolSize method
     *
     * @return steady-pool-size value for this pool
     * @see setSteadyPoolSize
     */
    int getSteadyPoolSize();

    // mutators for self mgmt
    /**
     * Sets the max-pool-size value for this pool. This attribute is expected to be set by the self-management framework for
     * an optimum max-pool-size. The corresponding accessor gets this value.
     *
     * @param size - The new max-pool-size value
     * @see getMaxPoolSize
     */
    void setMaxPoolSize(int size);

    /**
     * Sets the steady-pool-size value for this pool. This attribute is expected to be set by the self-management framework
     * for an optimum steady-pool-size. The corresponding accessor gets this value.
     *
     * @param size - The new steady-pool-size value
     * @see getSteadyPoolSize
     */
    void setSteadyPoolSize(int size);

    /**
     * Sets/Resets the flag indicating if this pool is self managed. This method would be typically called by the self
     * management framework to indicate to the world (and this pool) that this pool is self managed. Its very important that
     * the self mgmt framework properly sets this flag to control the dynamic reconfig behavior of this pool. If this flag
     * is set to true, all dynamic reconfigs affecting the max/steady pool size of this pool will be ignored.
     *
     * @param selfManaged - true to switch on self management, false otherwise
     */
    void setSelfManaged(boolean selfManaged);

    /**
     * Set the pool life cycle listener
     *
     * @param listener the new PoolLifeCycleListener
     */
    void setPoolLifeCycleListener(PoolLifeCycleListener listener);

    /**
     * Remove the pool life cycle listener
     */
    void removePoolLifeCycleListener();

    /**
     * Flush Connection pool by removing all resources established in the connection pool and bring the pool to steady pool
     * size.
     *
     * @return boolean indicating whether flush operation was successful or not
     * @throws PoolingException in case the pool was not initialized
     */
    boolean flushConnectionPool() throws PoolingException;

    /**
     * block any new requests to the pool Used for transparent dynamic reconfiguration of the pool
     *
     * @param waitTimeout time for which the new requests will wait
     */
    void blockRequests(long waitTimeout);

    /**
     * returns pool-wait-queue
     *
     * @return wait-queue
     */
    PoolWaitQueue getPoolWaitQueue();

    /**
     * returns wait-queue used during transparent dynamic reconfiguration
     *
     * @return PoolWaitQueue
     */
    PoolWaitQueue getReconfigWaitQueue();

    /**
     * returns the reconfig-wait-time
     *
     * @return the reconfig-wait-time
     */
    long getReconfigWaitTime();
}
