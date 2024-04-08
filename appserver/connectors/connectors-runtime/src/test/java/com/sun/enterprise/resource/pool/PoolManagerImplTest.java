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

import com.sun.appserv.connectors.internal.api.ConnectorConstants.PoolType;
import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.resource.ClientSecurityInfo;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.allocator.ConnectorAllocator;
import com.sun.enterprise.resource.allocator.LocalTxConnectorAllocator;
import com.sun.enterprise.resource.allocator.NoTxConnectorAllocator;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.pool.mock.JavaEETransactionMock;
import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
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
    private JavaEETransactionManager javaEETransactionManager;
    private ManagedConnection managedConnection;
    private ManagedConnectionFactory managedConnectionFactory;

    // Regular fields
    private ClientSecurityInfo clientSecurityInfo = null;
    private PoolManagerImpl poolManagerImpl = new MyPoolManagerImpl();
    private PoolInfo poolInfo = getPoolInfo();
    private JavaEETransaction javaEETransaction = new MyJavaEETransaction();

    @BeforeEach
    public void createAndPopulateMocks() throws Exception {
        List<Object> mocks = new ArrayList<>();

        // Mock JavaEETransactionManager
        javaEETransactionManager = createNiceMock(JavaEETransactionManager.class);
        mocks.add(javaEETransactionManager);

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
        poolManagerImpl.createEmptyConnectionPool(poolInfo, PoolType.STANDARD_POOL, new Hashtable<>());

        ResourceSpec resourceSpec = new ResourceSpec(new SimpleJndiName("myResourceSpec"), ResourceSpec.JNDI_NAME);
        resourceSpec.setPoolInfo(poolInfo);

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
     * Test to show the getResourceFromPool behavior in relation to the ResourceHandle enlisted and busy states, while using
     * the LocalTxConnectorAllocator.
     */
    @Test
    public void getResourceFromPoolTest() throws Exception {
        poolManagerImpl.createEmptyConnectionPool(poolInfo, PoolType.STANDARD_POOL, new Hashtable<>());
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
        // The resource is not (yet) enlisted in a transaction
        assertFalse(resource.isEnlisted());
        // Expecting all getResource from pool calls to mark the resource as busy
        // ConnectionPool: getResourceFromPool / getUnenlistedResource / prefetch calls.
        assertTrue(resource.getResourceState().isBusy());

        // Enlist the resource in the transaction
        resource.enlistedInTransaction(javaEETransaction);
        assertTrue(resource.isEnlisted());
        assertTrue(resource.getResourceState().isBusy());

        // One resource from the pool should be added to the pool and should be occupied
        assertPoolStatusNumberOfConnectionsUsed(1);
        assertPoolStatusNumberOfConnectionsFree(0);

        // Return the resource to the pool
        poolManagerImpl.putbackResourceToPool(resource, false);

        // When resource is returned to the pool the state is no longer busy
        assertFalse(resource.getResourceState().isBusy());

        // Resource is still enlisted, because it is still part of a transaction
        assertTrue(resource.isEnlisted());

        // Resource is still in use
        assertPoolStatusNumberOfConnectionsUsed(1);
        assertPoolStatusNumberOfConnectionsFree(0);

        // Stop the transaction to get the resource delisted / unenlisted
        // Mimic com.sun.enterprise.transaction.JavaEETransactionImpl.commit() call
        poolManagerImpl.transactionCompleted(javaEETransaction, jakarta.transaction.Status.STATUS_COMMITTED);

        // Resource must be delisted / unenlisted by the transactionCompleted logic
        // (warning for next assert: in multi-threaded tests resource can be used instantly by another thread!)
        assertFalse(resource.isEnlisted());
        // Resource is returned to the pool, resource should be freed
        // (warning for next assert: in multi-threaded tests resource can be used instantly by another thread!)
        assertFalse(resource.getResourceState().isBusy());

        // Connection should no longer be in use
        assertPoolStatusNumberOfConnectionsUsed(0);
        assertPoolStatusNumberOfConnectionsFree(1);

        // Kill the pool
        poolManagerImpl.killPool(poolInfo);
        assertNull(poolManagerImpl.getPoolStatus(poolInfo));
    }

    private void assertPoolStatusNumberOfConnectionsUsed(int expectedNumber) {
        PoolStatus poolStatus = poolManagerImpl.getPoolStatus(poolInfo);
        assertEquals(expectedNumber, poolStatus.getNumConnUsed());
    }

    private void assertPoolStatusNumberOfConnectionsFree(int expectedNumber) {
        PoolStatus poolStatus = poolManagerImpl.getPoolStatus(poolInfo);
        assertEquals(expectedNumber, poolStatus.getNumConnFree());
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
    }

    // We cannot depend on the real JavaEETransactionImpl due to dependency limitations, create our own
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
