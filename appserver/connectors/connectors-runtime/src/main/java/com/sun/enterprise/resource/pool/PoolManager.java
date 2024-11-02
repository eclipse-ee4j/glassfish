/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import com.sun.appserv.connectors.internal.api.ConnectorConstants.PoolType;
import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.appserv.connectors.internal.api.TransactedPoolManager;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.resource.ClientSecurityInfo;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.listener.PoolLifeCycle;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.RetryableUnavailableException;
import jakarta.transaction.Transaction;

import java.util.Hashtable;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.jvnet.hk2.annotations.Contract;

/**
 * PoolManager manages jdbc and connector connection pool
 */
@Contract
public interface PoolManager extends TransactedPoolManager {

    // transaction support levels
    // int NO_TRANSACTION = 0;
    // int LOCAL_TRANSACTION = 1;
    // int XA_TRANSACTION = 2;

    // Authentication mechanism levels
    // int BASIC_PASSWORD = 0;
    // int KERBV5 = 1;

    // Credential Interest levels
    // String PASSWORD_CREDENTIAL = "jakarta.resource.spi.security.PasswordCredential";
    // String GENERIC_CREDENTIAL = "jakarta.resource.spi.security.GenericCredential";

    /**
     * Flush Connection pool by reinitializing the connections established in the pool.
     *
     * @param poolInfo the pool identifier for which the status needs to be flushed.
     * @throws com.sun.appserv.connectors.internal.api.PoolingException in case the given pool is not initialized.
     */
    boolean flushConnectionPool(PoolInfo poolInfo) throws PoolingException;

    /**
     * Returns the pool status for the pool identified by the given poolInfo.
     *
     * @param poolInfo the pool identifier for which the status needs to be returned.
     * @return pool status information if the pool is found based on the provided poolInfo, otherwise null is returned.
     */
    PoolStatus getPoolStatus(PoolInfo poolInfo);

    ResourceHandle getResourceFromPool(ResourceSpec spec, ResourceAllocator alloc, ClientSecurityInfo info,
            Transaction transaction) throws PoolingException, RetryableUnavailableException;

    /**
     * Creates an empty connection pool with the given pool info and pool type.
     *
     * @param poolInfo the pool identifier of the new pool
     * @param poolType the type of the connection pool
     * @param env hashtable used to find the connection pool ConnectorConnectionPool information to set parameters like
     * maximum number of connections
     * @throws PoolingException when unable to create/initialize pool
     */
    void createEmptyConnectionPool(PoolInfo poolInfo, PoolType poolType, Hashtable env) throws PoolingException;

    /**
     * Returns the resource back to the pool IF errorOccurred is false. If errorOccurred is true the resource is removed
     * from the pool.
     * <p>
     * Note: The resource object may not be used anymore by the calling code after this call.
     *
     * @param resourceHandle the resource handle.
     * @param errorOccurred true if an error occurred and the resource should not be returned to the pool, otherwise use
     * false
     * <p>
     * TODO The method name is misleading<br>
     * TODO Why is NoTxConnectorAllocator the only one calling this interface method and the rest of the calls are using
     * resourceClosed / resourceErrorOccurred and resourceAbortOccurred ? Is the difference between resourceClosed and
     * putbackResourceToPool that resourceClosed informs the transaction and putbackResourceToPool does not?
     */
    void putbackResourceToPool(ResourceHandle resourceHandle, boolean errorOccurred);

    /**
     * Notifies the pool the resource is not used by a bean/application anymore.<br>
     * The resource is returned to the pool and the state of the resource is no longer busy.<br>
     * The resource is NOT delisted / unenlisted from the current transaction.
     * <p>
     * Note: The resource object may not be used anymore by the calling code after this call.
     *
     * @param resourceHandle the resource handle
     * @param poolInfo the pool information of the pool where the resource is expected to be removed from. If the given pool
     * info is not found no exception is thrown.
     * <p>
     * TODO Why is this method public available and only used internal in PoolManagerImpl and externally in
     * LazyEnlistableResourceManagerImpl. Why can't LazyEnlistableResourceManagerImpl just call resourceClosed? If poolInfo
     * = resourceHandle.getResourceSpec().getPoolInfo() then LazyEnlistableResourceManagerImpl could just as well call
     * resourceClosed and then this method can be removed from the interface.
     */
    void putbackDirectToPool(ResourceHandle resourceHandle, PoolInfo poolInfo);

    /**
     * Closes the resource handle and returns it back to the connection pool.<br>
     * The resource is returned to the pool and the state of the resource is no longer busy.<br>
     * The resource is also delisted / unenlisted from the current transaction.
     * <p>
     * Note: The resource object may not be used anymore by the calling code after this call.
     *
     * @param resourceHandle the resource handle to be closed and returned to the pool.
     */
    void resourceClosed(ResourceHandle resourceHandle);

    // TODO Why is this method public available? Why is it not called via resourceErrorOccurred or resourceAbortOccurred?
    void badResourceClosed(ResourceHandle resourceHandle);

    void resourceErrorOccurred(ResourceHandle resourceHandle);

    void resourceAbortOccurred(ResourceHandle resourceHandle);

    /**
     * Inform all the connection pools using this transaction that the transaction has completed. This method is called by
     * the EJB Transaction Manager.<br>
     * All resource handles associated to the given transaction are delisted.<br>
     * All resource handles associated to the given transaction handed back to the connection pool.
     *
     * @param transaction the transaction that is completed
     * @param status the status of the transaction
     */
    void transactionCompleted(Transaction transaction, int status);

    // Not used in the code
    // void emptyResourcePool(ResourceSpec spec);

    /**
     * Kills the pool for the given PoolInfo.<br>
     * Note: if the pool is not found the method ends with success.
     *
     * @param poolInfo the pool identifier for which the pool needs to be killed.
     */
    void killPool(PoolInfo poolInfo);

    /**
     * Reconfigures the connection pool to apply the given properties.<br>
     * Note: if the pool is not found the method ends with success.
     *
     * @param ccp the object containing the new connection pool properties that will be applied to the existing pool
     * @throws PoolingException if reconfiguration of the pool failed
     */
    void reconfigPoolProperties(ConnectorConnectionPool ccp) throws PoolingException;

    /**
     * Switch on matching in the pool.
     *
     * @param poolInfo the pool identifier for which the pool matching needs to be switched on.
     */
    boolean switchOnMatching(PoolInfo poolInfo);

    /**
     * Obtain a transactional resource such as JDBC connection from the connection pool. The caller is blocked until a
     * resource is acquired or the max-wait-time of the connection pool expires.
     *
     * @param spec Specification for the resource
     * @param alloc Allocator for the resource
     * @param info Client security for this request
     * @return An object that represents a connection to the resource
     * @throws PoolingException Thrown if some error occurs while obtaining the resource
     */
    Object getResource(ResourceSpec spec, ResourceAllocator alloc, ClientSecurityInfo info) throws PoolingException, RetryableUnavailableException;

    ResourceReferenceDescriptor getResourceReference(SimpleJndiName jndiName, SimpleJndiName logicalName);

    /**
     * Kills all free connections in the registered connection pools. The pools are not killed.
     */
    void killFreeConnectionsInPools();

    /**
     * Returns the ResourcePool for the given PoolInfo
     *
     * @param poolInfo the pool identifier
     * @return the ResourcePool if found, otherwise null
     */
    ResourcePool getPool(PoolInfo poolInfo);

    /**
     * This method gets called by the LazyEnlistableConnectionManagerImpl when a connection needs enlistment, i.e on use of
     * a Statement etc. Based on the given managagedConneciton the transaction manager is found and used to find the
     * transaction. If no transaction was found this method returns without an error.
     *
     * @param managedConnection the managedConnection that needs to be enlisted in the transaction
     * @throws ResourceException in case enlistment to the transaction failed.
     */
    void lazyEnlist(ManagedConnection managedConnection) throws ResourceException;

    /**
     * Registers a PoolLifeCycle listener in the pool manager. The listener is used to keep track of statistics of the pool
     * life cycle: pool created and pool destroyed events.
     *
     * @param poolListener the listener instance
     */
    void registerPoolLifeCycleListener(PoolLifeCycle poolListener);

    /**
     * Unregisters the PoolLifeCycle listener in the pool manager.
     */
    void unregisterPoolLifeCycleListener();
}
