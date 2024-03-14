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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.easymock.IExpectationSetters;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.internal.api.DelegatingClassLoader;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.opentest4j.MultipleFailuresError;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.util.ConnectionPoolObjectsUtils;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.ResourceState;
import com.sun.enterprise.resource.allocator.LocalTxConnectorAllocator;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.pool.datastructure.RWLockDataStructure;
import com.sun.enterprise.transaction.api.JavaEETransaction;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.transaction.Transaction;

public class ConnectionPoolTest {

    private ManagedConnection managedConnection;
    private ManagedConnectionFactory managedConnectionFactory;
    private JavaEETransaction javaEETransaction;

    private ConnectionPool connectionPool;

    private ResourceSpec resourceSpec;

    @BeforeEach
    public void createAndPopulateMocks() throws PoolingException, ResourceException {
        List<Object> mocks = new ArrayList<>();

        // Mock ManagedConnection
        managedConnection = createNiceMock(ManagedConnection.class);
        mocks.add(managedConnection);

        // Mock ManagedConnectionFactory
        ManagedConnectionFactory localConnectionFactory = createMock(ManagedConnectionFactory.class);
        IExpectationSetters<ManagedConnection> createConnectionExpectation = expect(
                localConnectionFactory.createManagedConnection(isNull(), isNull()));
        createConnectionExpectation.andReturn(managedConnection)
                .atLeastOnce();

        // Must return a not null object in matchManagedConnections to ensure matching in the ConnectionPool is 'true'
        IExpectationSetters<ManagedConnection> matchExpectation = expect(localConnectionFactory.matchManagedConnections(notNull(), isNull(), isNull()));
        matchExpectation.andReturn(managedConnection)
                .atLeastOnce();

        managedConnectionFactory = localConnectionFactory;
        mocks.add(managedConnectionFactory);

        // Mock JavaEETransaction
        javaEETransaction = createNiceMock(JavaEETransaction.class);
        mocks.add(javaEETransaction);

        replay(mocks.toArray());

        // Make sure ConnectorRuntime singleton is initialized
        MyConnectorRuntime connectorRuntime = new MyConnectorRuntime();
        ProcessEnvironment processEnvironment = new ProcessEnvironment();
        connectorRuntime.setProcessEnvironment(processEnvironment);
        connectorRuntime.postConstruct();
    }

    private void createConnectionPool(int maxPoolSize) throws PoolingException {
        PoolInfo poolInfo = ConnectionPoolTest.getPoolInfo();
        MyConnectionPool.myMaxPoolSize = maxPoolSize;
        connectionPool = new MyConnectionPool(poolInfo);
        assertEquals(1, connectionPool.getSteadyPoolSize());
        assertEquals(maxPoolSize, connectionPool.getMaxPoolSize());

        resourceSpec = new ResourceSpec(new SimpleJndiName("myResourceSpec"), ResourceSpec.JNDI_NAME);
        resourceSpec.setPoolInfo(poolInfo);
    }

    /**
     * Basic test to show ConnectionPool can be instantiated in a unit test
     */
    @Test
    void basicConnectionPoolTest() throws Exception {
        createConnectionPool(2);

        ResourceAllocator alloc = new LocalTxConnectorAllocator(null, managedConnectionFactory, resourceSpec, null,
                null, null, null, false);
        Transaction transaction = javaEETransaction;

        // Test how many resources are available
        // Expect 0 because the pool is not initialized yet. It will be initialized when the first resource is requested
        assertResourcesSize(0);

        // Test getting 3 resources from the pool of 2
        ResourceHandle resource1 = connectionPool.getResource(resourceSpec, alloc, transaction);
        assertNotNull(resource1);
        assertResourceIsBusy(resource1);
        assertResourcesSize(1);

        ResourceHandle resource2 = connectionPool.getResource(resourceSpec, alloc, transaction);
        assertNotNull(resource2);
        assertResourceIsBusy(resource2);
        assertResourcesSize(2);

        assertThrows(PoolingException.class, () -> {
            connectionPool.getResource(resourceSpec, alloc, transaction);
        });

        // Test returning 2 resources to the pool
        connectionPool.resourceClosed(resource2);
        assertResourceIsNotBusy(resource2);

        // Test issue #24843: make the state of resource1 not busy anymore (it should not happen but it happens in rare cases),
        // resource should still be closed without throwing an exception.
        resource1.getResourceState().setBusy(false);
        connectionPool.resourceClosed(resource1);
        assertResourceIsNotBusy(resource1);

        // Test how many resources are available
        assertResourcesSize(2);

        connectionPool.emptyPool();
        assertResourcesSize(0);
    }

    @Test
    @Timeout(value = 10)
    void basicConnectionPoolMultiThreadedTest() throws Exception {
        int maxConnectionPoolSize = 30;
        createConnectionPool(maxConnectionPoolSize);

        ResourceAllocator alloc = new LocalTxConnectorAllocator(null, managedConnectionFactory, resourceSpec, null,
                null, null, null, false);

        int taskCount = 100;
        List<Callable<Void>> tasks = new ArrayList<>(taskCount);
        for (int i = 0; i < taskCount; i++) {
            tasks.add(() -> {
                while (true) {
                    try {
                        // Get resource
                        ResourceHandle resource = connectionPool.getResource(resourceSpec, alloc, javaEETransaction);
                        assertNotNull(resource);
                        assertResourceIsBusy(resource);

                        // Keep resource a bit occupied, to ensure more than 1 resource from the
                        // pool is used at the same time
                        Thread.sleep(0, 10);
                        // Return resource
                        connectionPool.resourceClosed(resource);
                        assertResourceIsNotBusy(resource);
                        return null;
                    } catch (PoolingException e) {
                        // Try again a bit later, should get a connection at one point
                        Thread.sleep(0, 5);
                    }
                }
            });
        }
        runTheTasks(tasks);
        assertResourcesSize(maxConnectionPoolSize);
        connectionPool.emptyPool();
        assertResourcesSize(0);
    }

    @Test
    @Timeout(value = 10)
    void resourceErrorOccurredMultiThreadedTest() throws Exception {
        int maxConnectionPoolSize = 30;
        createConnectionPool(maxConnectionPoolSize);

        ResourceAllocator alloc = new LocalTxConnectorAllocator(null, managedConnectionFactory, resourceSpec, null,
                null, null, null, false);

        int taskCount = 100;
        List<Callable<Void>> tasks = new ArrayList<>(taskCount);
        for (int i = 0; i < taskCount; i++) {
            tasks.add(() -> {
                while (true) {
                    try {
                        // Get resource
                        ResourceHandle resource = connectionPool.getResource(resourceSpec, alloc, javaEETransaction);
                        assertNotNull(resource);
                        assertResourceIsBusy(resource);

                        // Keep resource a bit occupied, to ensure more than 1 resource from the
                        // pool is used at the same time
                        Thread.sleep(0, 10);
                        // Return resource
                        connectionPool.resourceErrorOccurred(resource);
                        assertResourceIsNotBusy(resource);
                        return null;
                    } catch (PoolingException e) {
                        // Try again a bit later, should get a connection at one point
                        Thread.sleep(0, 5);
                    }
                }
            });
        }
        runTheTasks(tasks);

        connectionPool.emptyPool();
        assertResourcesSize(0);
    }

    private void runTheTasks(List<Callable<Void>> tasks) throws InterruptedException, MultipleFailuresError {
        ExecutorService threadPool = Executors.newFixedThreadPool(1000);
        List<Future<Void>> futures = threadPool.invokeAll(tasks, 10, TimeUnit.SECONDS);
        assertAll(
                () -> assertTrue(futures.stream().allMatch(f -> f.isDone()), "All done."),
                () -> assertFalse(futures.stream().anyMatch(f -> f.isCancelled()), "None cancelled."));
    }

    private void assertResourceIsBusy(ResourceHandle resource) {
        // Resource must be marked as isBusy right after call to getResource
        ResourceState resourceState = resource.getResourceState();
        assertTrue(resourceState.isBusy());
        assertFalse(resourceState.isFree());
    }

    private void assertResourceIsNotBusy(ResourceHandle resource) {
        // Resource must be marked as not isBusy / isFree right after call to resourceClosed
        ResourceState resourceState = resource.getResourceState();
        assertTrue(resourceState.isFree());
        assertFalse(resourceState.isBusy());
    }

    private void assertResourcesSize(int expectedSize) {
        RWLockDataStructure dataStructure = (RWLockDataStructure) connectionPool.dataStructure;
        assertEquals(expectedSize, dataStructure.getResourcesSize());
        assertEquals(expectedSize, dataStructure.getAllResources()
                .size());
    }

    public static class MyConnectionPool extends ConnectionPool {

        public static int myMaxPoolSize;

        public MyConnectionPool(PoolInfo poolInfo) throws PoolingException {
            super(ConnectionPoolTest.getPoolInfo(), new Hashtable<>());
        }

        @Override
        protected ConnectorConnectionPool getPoolConfigurationFromJndi(Hashtable env) throws PoolingException {
            ConnectorConnectionPool connectorConnectionPool = ConnectionPoolObjectsUtils
                    .createDefaultConnectorPoolObject(poolInfo, null);

            // lower the default pool size
            connectorConnectionPool.setSteadyPoolSize("1");
            connectorConnectionPool.setMaxPoolSize("" + myMaxPoolSize);

            return connectorConnectionPool;
        }
    }

    public class MyConnectorRuntime extends ConnectorRuntime {

        public void setProcessEnvironment(ProcessEnvironment processEnvironment) {
            this.processEnvironment = processEnvironment;
        }

        @Override
        public PoolType getPoolType(PoolInfo poolInfo) throws ConnectorRuntimeException {
            return PoolType.STANDARD_POOL;
        }

        @Override
        public ConnectorDescriptor getConnectorDescriptor(String rarName) throws ConnectorRuntimeException {
            throw new ConnectorRuntimeException("No rar in unit test");
        }

        @Override
        public DelegatingClassLoader getConnectorClassLoader() {
            // Return null, system classloader will be used
            return null;
        }
    }

    private static PoolInfo getPoolInfo() {
        SimpleJndiName jndiName = new SimpleJndiName("myPool");
        return new PoolInfo(jndiName);
    }

}
