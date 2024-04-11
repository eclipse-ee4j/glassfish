/*
 * Copyright (c) 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

import static com.sun.enterprise.resource.pool.ConnectionPoolTest.getPoolInfo;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.sun.appserv.connectors.internal.api.ConnectorConstants.PoolType;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.resource.ClientSecurityInfo;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.allocator.ConnectorAllocator;
import com.sun.enterprise.resource.allocator.LocalTxConnectorAllocator;
import com.sun.enterprise.resource.allocator.NoTxConnectorAllocator;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.pool.mock.JavaEETransactionManagerMock;
import com.sun.enterprise.resource.pool.mock.JavaEETransactionMock;
import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.transaction.spi.TransactionalResource;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.invocation.InvocationManagerImpl;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.naming.ResourceNamingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PoolManagerImplTest {

    // Mocked instances
    private GlassfishNamingManager glassfishNamingManager;
    private ManagedConnection managedConnection;
    private ManagedConnectionFactory managedConnectionFactory;

    // Regular fields
    private ClientSecurityInfo clientSecurityInfo = null;
    private PoolManagerImpl poolManagerImpl = new MyPoolManagerImpl();
    private PoolInfo poolInfo = getPoolInfo();
    private JavaEETransaction javaEETransaction = new MyJavaEETransaction();
    private MyJavaEETransactionManager javaEETransactionManager = new MyJavaEETransactionManager();
    private PoolType poolType;

    @BeforeEach
    public void createAndPopulateMocks() throws Exception {
        List<Object> mocks = new ArrayList<>();

        // Mock GlassfishNamingManager
        glassfishNamingManager = createNiceMock(GlassfishNamingManager.class);
        mocks.add(glassfishNamingManager);

        // Mock ManagedConnection
        managedConnection = createNiceMock(ManagedConnection.class);
        expect(managedConnection.getConnection(isNull(), isNull()))
                .andReturn(new MyDatabaseConnection()).anyTimes();
        mocks.add(managedConnection);

        // Mock ManagedConnectionFactory
        ManagedConnectionFactory localConnectionFactory = createMock(ManagedConnectionFactory.class);
        expect(localConnectionFactory.createManagedConnection(isNull(), isNull()))
                .andReturn(managedConnection)
                .atLeastOnce();
        // Must return a not null object in matchManagedConnections to ensure matching in the ConnectionPool is 'true'
        expect(localConnectionFactory.matchManagedConnections(notNull(), isNull(), isNull()))
                .andReturn(managedConnection)
                .atLeastOnce();
        managedConnectionFactory = localConnectionFactory;
        mocks.add(managedConnectionFactory);

        replay(mocks.toArray());

        // Make sure ConnectorRuntime singleton is initialized
        MyConnectorRuntime connectorRuntime = new MyConnectorRuntime();
        connectorRuntime.postConstruct();
    }

    /**
     * Test to show the getResource behavior
     */
    @Test
    void getResourceTest() throws Exception {
        // Create Standard pool
        poolType = PoolType.STANDARD_POOL;
        poolManagerImpl.createEmptyConnectionPool(poolInfo, poolType, new Hashtable<>());

        ResourceSpec resourceSpec = createTestResourceSpec(poolInfo);

        // Test getting a single resource, this will return a 'userConnection' / 'connection handle' object representing the
        // physical connection, e.g. a database connection.

        // Test using the No transaction allocator
        ResourceAllocator noTxAllocator = new NoTxConnectorAllocator(null, managedConnectionFactory, resourceSpec, null,
                null, null, null);
        Object resource = poolManagerImpl.getResource(resourceSpec, noTxAllocator, clientSecurityInfo);
        assertTrue(resource instanceof MyDatabaseConnection);

        // Test using the Local transaction allocator
        ResourceAllocator localTxAllocator = new LocalTxConnectorAllocator(null, managedConnectionFactory, resourceSpec, null,
                null, null, null, false);
        resource = poolManagerImpl.getResource(resourceSpec, localTxAllocator, clientSecurityInfo);
        assertTrue(resource instanceof MyDatabaseConnection);

        // Test using the XA transaction allocator
        ResourceAllocator xAllocator = new ConnectorAllocator(null, managedConnectionFactory, resourceSpec, null,
                null, null, null, false);
        resource = poolManagerImpl.getResource(resourceSpec, xAllocator, clientSecurityInfo);
        assertTrue(resource instanceof MyDatabaseConnection);

        // Resources from the pool should be occupied
        assertPoolStatusNumberOfConnectionsUsed(3);

        // Get resource does not return a ResourceHandle, so we cannot return the resources to the pool.
        // For now just flush all resources from the pool.
        poolManagerImpl.flushConnectionPool(poolInfo);

        assertPoolStatusNumberOfConnectionsUsed(0);

        // Kill the pool
        poolManagerImpl.killPool(poolInfo);
        assertNull(poolManagerImpl.getPoolStatus(poolInfo));
    }

    /**
     * Test to show the getResourceFromPool and resourceClosed behavior in relation to the ResourceHandle enlisted and busy
     * states, while using the LocalTxConnectorAllocator.
     */
    @Test
    public void resourceClosedTest() throws Exception {
        // Create Standard pool
        poolType = PoolType.STANDARD_POOL;
        poolManagerImpl.createEmptyConnectionPool(poolInfo, poolType, new Hashtable<>());

        assertPoolStatusNumberOfConnectionsUsed(0);
        assertPoolStatusNumberOfConnectionsFree(0);

        ResourceSpec resourceSpec = new ResourceSpec(new SimpleJndiName("myResourceSpec"), ResourceSpec.JNDI_NAME);
        resourceSpec.setPoolInfo(poolInfo);

        // Test using the Local transaction allocator
        ResourceAllocator localTxAllocator = new LocalTxConnectorAllocator(null, managedConnectionFactory, resourceSpec, null,
                null, null, null, false);
        ResourceHandle resource = poolManagerImpl.getResourceFromPool(resourceSpec, localTxAllocator, clientSecurityInfo, javaEETransaction);
        assertNotNull(resource);
        assertTrue(resource.getUserConnection() instanceof MyDatabaseConnection);

        // State should be marked busy directly after a getResource call
        // The resource is not (yet) enlisted in a transaction
        assertResourceIsBusy(resource);
        assertResourceIsNotEnlisted(resource);

        // One resource from the pool should be added to the pool and should be occupied
        assertPoolStatusNumberOfConnectionsUsed(1);
        assertPoolStatusNumberOfConnectionsFree(0);

        // Enlist the resource in the transaction
        assertResourceIsNotPartOfTransaction(javaEETransaction, resource);
        resource.enlistedInTransaction(javaEETransaction);
        assertResourceIsBusy(resource);
        assertResourceIsEnlisted(resource);
        assertResourceIsPartOfTransaction(javaEETransaction, resource);

        // Return the resource to the pool
        poolManagerImpl.resourceClosed(resource);
        // NOTE: in a multi threaded test the resource cannot be tested after this point!

        // When resource is returned to the pool the state is no longer busy
        // But the resource is still enlisted in the transaction
        assertResourceIsNotBusy(resource);
        assertResourceIsEnlisted(resource);
        assertResourceHasNoConnectionErrorOccured(resource);
        assertResourceIsPartOfTransaction(javaEETransaction, resource);

        // Resource is still in use
        assertPoolStatusNumberOfConnectionsUsed(1);
        assertPoolStatusNumberOfConnectionsFree(0);

        // Stop the transaction to get the resource delisted / unenlisted from the transaction
        // Mimic com.sun.enterprise.transaction.JavaEETransactionImpl.commit() call
        poolManagerImpl.transactionCompleted(javaEETransaction, jakarta.transaction.Status.STATUS_COMMITTED);

        // State should remain not busy
        // And the resource no longer enlisted in the transaction
        assertResourceIsNotBusy(resource);
        assertResourceIsNotEnlisted(resource);
        assertResourceHasNoConnectionErrorOccured(resource);
        assertResourceIsNotPartOfTransaction(javaEETransaction, resource);

        // Connection should no longer be in use
        assertPoolStatusNumberOfConnectionsUsed(0);
        assertPoolStatusNumberOfConnectionsFree(1);

        // Kill the pool
        poolManagerImpl.killPool(poolInfo);
        assertNull(poolManagerImpl.getPoolStatus(poolInfo));
    }

    /**
     * Test to show the getResourceFromPool and resourceErrorOccurred behavior in relation to the ResourceHandle enlisted
     * and busy states, while using the LocalTxConnectorAllocator.
     */
    @Test
    public void resourceErrorOccurredTest() throws Exception {
        resourceErrorOrAbortedOccurredTest(false);
    }

    /**
     * Test to show the getResourceFromPool and resourceAbortOccurred behavior in relation to the ResourceHandle enlisted
     * and busy states, while using the LocalTxConnectorAllocator.
     */
    @Test
    public void resourceAbortOccurredTest() throws Exception {
        resourceErrorOrAbortedOccurredTest(true);
    }

    private void resourceErrorOrAbortedOccurredTest(boolean resourceAbortedOccured) throws Exception {
        // Create Standard pool
        poolType = PoolType.STANDARD_POOL;
        poolManagerImpl.createEmptyConnectionPool(poolInfo, poolType, new Hashtable<>());

        assertPoolStatusNumberOfConnectionsUsed(0);
        assertPoolStatusNumberOfConnectionsFree(0);

        ResourceSpec resourceSpec = new ResourceSpec(new SimpleJndiName("myResourceSpec"), ResourceSpec.JNDI_NAME);
        resourceSpec.setPoolInfo(poolInfo);

        // Test using the Local transaction allocator
        ResourceAllocator localTxAllocator = new LocalTxConnectorAllocator(null, managedConnectionFactory, resourceSpec, null,
                null, null, null, false);
        ResourceHandle resource = poolManagerImpl.getResourceFromPool(resourceSpec, localTxAllocator, clientSecurityInfo, javaEETransaction);
        assertNotNull(resource);
        assertTrue(resource.getUserConnection() instanceof MyDatabaseConnection);

        // State should be marked busy directly after a getResource call
        // The resource is not (yet) enlisted in a transaction
        assertResourceIsBusy(resource);
        assertResourceIsNotEnlisted(resource);

        // One resource from the pool should be added to the pool and should be occupied
        assertPoolStatusNumberOfConnectionsUsed(1);
        assertPoolStatusNumberOfConnectionsFree(0);

        // Enlist the resource in the transaction
        assertResourceIsNotPartOfTransaction(javaEETransaction, resource);
        resource.enlistedInTransaction(javaEETransaction);
        assertResourceIsBusy(resource);
        assertResourceIsEnlisted(resource);
        assertResourceIsPartOfTransaction(javaEETransaction, resource);

        if (resourceAbortedOccured) {
            // Return the resource to the pool using resourceAbortOccurred
            poolManagerImpl.resourceAbortOccurred(resource);
            // Related transaction delist should be called for the resource
            assertTrue(javaEETransactionManager.isDelistIsCalled(resource));

            // TODO: connection error occurred flag on the resource is only set via badConnectionClosed
            // why isn't it also called for resourceAbortedOccured? Bug?!
            assertResourceHasNoConnectionErrorOccured(resource);
        } else {
            // Return the resource to the pool using resourceErrorOccurred
            poolManagerImpl.resourceErrorOccurred(resource);
            // Related transaction delist should be called for the resource, shouldn't it?
            // resourceErrorOccurred does not seem to remove the resource from the transaction, possible bug?
            // TODO: should be assertTrue
            assertFalse(javaEETransactionManager.isDelistIsCalled(resource));

            // TODO: connection error occurred flag on the resource is only set via badConnectionClosed
            // why isn't it also called for resourceErrorOccurred? Bug?!
            assertResourceHasNoConnectionErrorOccured(resource);
        }
        // NOTE: in a multi threaded test the resource cannot be tested after this point!

        // When resource is returned to the pool the state is no longer busy
        // But the resource is still enlisted in the transaction
        assertResourceIsNotBusy(resource);
        assertResourceIsEnlisted(resource);
        assertResourceIsPartOfTransaction(javaEETransaction, resource);

        // In case of putbackResourceToPool we would expect: Resource is still in use
        // assertPoolStatusNumberOfConnectionsUsed(1);
        // In case of putbackBadResourceToPool the resource is no longer listed as in use
        assertPoolStatusNumberOfConnectionsUsed(0);
        // And the bad resource should not have been returned to the free pool
        assertPoolStatusNumberOfConnectionsFree(0);

        // Stop the transaction to get the resource delisted / unenlisted from the transaction
        // Mimic com.sun.enterprise.transaction.JavaEETransactionImpl.commit() call
        poolManagerImpl.transactionCompleted(javaEETransaction, jakarta.transaction.Status.STATUS_MARKED_ROLLBACK);

        // State should remain not busy
        // And the resource no longer enlisted in the transaction
        assertResourceIsNotBusy(resource);
        assertResourceIsNotEnlisted(resource);
        assertResourceIsNotPartOfTransaction(javaEETransaction, resource);

        // Connection should no longer be in use, and number of connections free should remain at 0
        assertPoolStatusNumberOfConnectionsUsed(0);
        assertPoolStatusNumberOfConnectionsFree(0);

        // Kill the pool
        poolManagerImpl.killPool(poolInfo);
        assertNull(poolManagerImpl.getPoolStatus(poolInfo));

    }

    private void assertResourceIsBusy(ResourceHandle resource) {
        assertTrue(resource.getResourceState().isBusy());
    }

    private void assertResourceIsNotBusy(ResourceHandle resource) {
        assertFalse(resource.getResourceState().isBusy());
    }

    private void assertResourceIsEnlisted(ResourceHandle resource) {
        assertTrue(resource.isEnlisted());
    }

    private void assertResourceIsNotEnlisted(ResourceHandle resource) {
        assertFalse(resource.isEnlisted());
    }
    private void assertResourceHasNoConnectionErrorOccured(ResourceHandle resource) {
        assertFalse(resource.hasConnectionErrorOccurred());
    }

    private void assertPoolStatusNumberOfConnectionsUsed(int expectedNumber) {
        PoolStatus poolStatus = poolManagerImpl.getPoolStatus(poolInfo);
        assertEquals(expectedNumber, poolStatus.getNumConnUsed());
    }

    private void assertPoolStatusNumberOfConnectionsFree(int expectedNumber) {
        PoolStatus poolStatus = poolManagerImpl.getPoolStatus(poolInfo);
        assertEquals(expectedNumber, poolStatus.getNumConnFree());
    }

    private static ResourceSpec createTestResourceSpec(PoolInfo thePoolInfo) {
        ResourceSpec resourceSpec = new ResourceSpec(new SimpleJndiName("myResourceSpec"), ResourceSpec.JNDI_NAME);
        resourceSpec.setPoolInfo(thePoolInfo);
        return resourceSpec;
    }

    private void assertResourceIsPartOfTransaction(JavaEETransaction transaction, ResourceHandle expectedResource) {
        for (Object resource : transaction.getResources(poolInfo)) {
            if (resource instanceof ResourceHandle) {
                ResourceHandle foundResource = (ResourceHandle) resource;
                if (foundResource.equals(expectedResource)) {
                    return;
                }
            }
        }
        fail();
    }

    private void assertResourceIsNotPartOfTransaction(JavaEETransaction transaction, ResourceHandle expectedResource) {
        Set resources = transaction.getResources(poolInfo);
        if (resources != null) {
            for (Object resource : resources) {
                if (resource instanceof ResourceHandle) {
                    ResourceHandle foundResource = (ResourceHandle) resource;
                    if (foundResource.equals(expectedResource)) {
                        fail();
                    }
                }
            }
        }
    }

    private class MyDatabaseConnection {
    }

    private class MyConnectorRuntime extends ConnectorRuntime {
        private InvocationManager manager = new InvocationManagerImpl();
        private ProcessEnvironment processEnvironment = new ProcessEnvironment();
        private ResourceNamingService resourceNamingService = new ResourceNamingService();

        public MyConnectorRuntime() throws Exception {
            // Force 'injection', unluckily ResourceNamingService is marked as final
            // otherwise we could mock it, or subclass it for this unit test.
            InjectionUtil.injectPrivateField(ResourceNamingService.class, resourceNamingService, "namingManager", glassfishNamingManager);

            // Force 'injection' of private field processEnvironment
            InjectionUtil.injectPrivateField(ConnectorRuntime.class, this, "processEnvironment", processEnvironment);
        }

        @Override
        public JavaEETransactionManager getTransactionManager() {
            return javaEETransactionManager;
        }

        @Override
        public InvocationManager getInvocationManager() {
            return manager;
        }

        @Override
        public ResourceNamingService getResourceNamingService() {
            return resourceNamingService ;
        }

        @Override
        public PoolManager getPoolManager() {
            return poolManagerImpl;
        }

        @Override
        public PoolType getPoolType(PoolInfo poolInfo) throws ConnectorRuntimeException {
            // Overriden to avoid ResourceNamingService jndi lookup calls in unit test
            return poolType;
        }
    }

    // We cannot depend on the real JavaEETransactionManagerSimplified implementation due to dependency limitations
    private class MyJavaEETransactionManager extends JavaEETransactionManagerMock {

        Map<TransactionalResource, Boolean> delistIsCalled = new HashMap<>();

        @Override
        public Transaction getTransaction() throws SystemException {
            // Assuming only 1 transaction used in each unit test, return it
            return javaEETransaction;
        }

        @Override
        public boolean delistResource(Transaction tran, TransactionalResource resource, int flag) throws IllegalStateException, SystemException {
            // Store state for unit test validation
            delistIsCalled.put(resource, Boolean.TRUE);

            // Return delist success
            return true;
        }

        public boolean isDelistIsCalled(TransactionalResource resource) {
            return delistIsCalled.getOrDefault(resource, Boolean.FALSE);
        }
    }

    // We cannot depend on the real JavaEETransactionImpl due to dependency limitations
    private class MyJavaEETransaction extends JavaEETransactionMock {

        private HashMap<Object, Set> resourceTable = new HashMap<>();

        @Override
        public Set getAllParticipatingPools() {
            return resourceTable.keySet();
        }

        @Override
        public Set getResources(Object poolInfo) {
            return resourceTable.get(poolInfo);
        }

        @Override
        public void setResources(Set resources, Object poolInfo) {
            resourceTable.put(poolInfo, resources);
        }
    }

    private class MyPoolManagerImpl extends PoolManagerImpl {

        // Override createAndInitPool to be able to use our MyConnectionPool implementation to be able to override
        // getPoolConfigurationFromJndi and scheduleResizerTask in the ConnectionPool class.
        @Override
        void createAndInitPool(PoolInfo poolInfo, PoolType poolType, Hashtable env) throws PoolingException {
            // Abuse env hashTable to get fields into the getPoolConfigurationFromJndi method
            env.put("maxPoolSize", Integer.valueOf(10));
            env.put("maxWaitTimeInMillis", Integer.valueOf(500));
            env.put("poolResizeQuantity", Integer.valueOf(1));

            ConnectionPoolTest.MyConnectionPool pool = new ConnectionPoolTest.MyConnectionPool(poolInfo, env);
            addPool(pool);
        }
    }
}
