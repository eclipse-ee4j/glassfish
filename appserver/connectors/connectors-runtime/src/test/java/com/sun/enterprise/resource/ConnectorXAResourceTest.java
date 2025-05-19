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

package com.sun.enterprise.resource;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.resource.allocator.LocalTxConnectorAllocator;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.pool.InjectionUtil;
import com.sun.enterprise.resource.pool.mock.JavaEETransactionManagerMock;
import com.sun.enterprise.resource.pool.mock.JavaEETransactionMock;
import com.sun.enterprise.resource.pool.mock.LocalTransactionMock;
import com.sun.enterprise.resource.pool.mock.ManagedConnectionFactoryMock;
import com.sun.enterprise.resource.pool.mock.ManagedConnectionMock;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.transaction.spi.TransactionalResource;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.LocalTransaction;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;

import javax.security.auth.Subject;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConnectorXAResourceTest {

    private ResourceSpec resourceSpec = new ResourceSpec(new SimpleJndiName("myResourceSpec"), ResourceSpec.JNDI_NAME);

    @Test
    void testGetResourceHandle_CurrentTransaction_Null() throws Exception {
        // Boilerplate
        MyLocalTransactionMock localTransaction = new MyLocalTransactionMock();
        ManagedConnection managedConnection = new MyManagedConnection(localTransaction);
        ManagedConnectionFactory managedConnectionFactory = new MyManagedConnectionFactory(managedConnection);

        // Essence of the test: no transaction
        MyJavaEETransaction javaEETransaction = null;

        // Make sure ConnectorRuntime singleton is initialized
        MyConnectorRuntime connectorRuntime = new MyConnectorRuntime(new MyJavaEETransactionManager(javaEETransaction));
        connectorRuntime.postConstruct();

        // Create ResourceAllocator
        ResourceAllocator allocator = new LocalTxConnectorAllocator(null, managedConnectionFactory, resourceSpec, null,
                null, null, null, false);

        // Create ResourceHandle
        ResourceHandle handle = allocator.createResource();
        ResourceSpec resourceSpec = handle.getResourceSpec();
        ResourceAllocator resourceAllocator = handle.getResourceAllocator();
        ClientSecurityInfo info = null;

        // Create ConnectorXAResource for the handle
        ConnectorXAResource connectorXAResource = new ConnectorXAResource(handle, resourceSpec, resourceAllocator, info);
        // Purpose of this test variation: current transaction is null
        assertNull(connectorXAResource.getCurrentTransaction());

        // Test getResource while current transaction is null
        ResourceHandle resourceHandle = connectorXAResource.getResourceHandle();
        assertEquals(handle, resourceHandle);

        // Make sure the resource is unenlisted
        assertFalse(resourceHandle.isEnlisted());
        assertTrue(resourceHandle.getResourceState().isUnenlisted());

        // In case the resource was unenlisted the transaction should also have been started
        LocalTransaction transaction = resourceHandle.getResource().getLocalTransaction();
        assertEquals(localTransaction, transaction);
        assertEquals(1, localTransaction.getBeginNrOfCalls());
    }

    @Test
    void testGetResourceHandle_NonXaResource_Null() throws Exception {
        // Boilerplate
        MyLocalTransactionMock localTransaction = new MyLocalTransactionMock();
        ManagedConnection managedConnection = new MyManagedConnection(localTransaction);
        ManagedConnectionFactory managedConnectionFactory = new MyManagedConnectionFactory(managedConnection);

        // Essence of the test MyJavaEETransaction returns null for getNonXaResource
        MyJavaEETransaction javaEETransaction = new MyJavaEETransaction();
        assertNull(javaEETransaction.getNonXAResource());

        // Make sure ConnectorRuntime singleton is initialized
        MyConnectorRuntime connectorRuntime = new MyConnectorRuntime(new MyJavaEETransactionManager(javaEETransaction));
        connectorRuntime.postConstruct();

        // Create ResourceAllocator
        ResourceAllocator allocator = new LocalTxConnectorAllocator(null, managedConnectionFactory, resourceSpec, null,
                null, null, null, false);

        // Create ResourceHandle
        ResourceHandle handle = allocator.createResource();
        ResourceSpec resourceSpec = handle.getResourceSpec();
        ResourceAllocator resourceAllocator = handle.getResourceAllocator();
        ClientSecurityInfo info = null;

        // Create ConnectorXAResource for the handle
        ConnectorXAResource connectorXAResource = new ConnectorXAResource(handle, resourceSpec, resourceAllocator, info);
        // Purpose of this test variation: current transaction is not null, but NonXaResource is null
        assertNotNull(connectorXAResource.getCurrentTransaction());
        assertNull(connectorXAResource.getCurrentTransaction().getNonXAResource());
        assertEquals(2, javaEETransaction.getGetNonXAResourceNrOfCalls());

        // Before the getResourceHandle, check the transaction is not yet started
        assertEquals(0, localTransaction.getBeginNrOfCalls());

        // Test getResource while NonXaResource is null
        ResourceHandle resourceHandle = connectorXAResource.getResourceHandle();
        assertEquals(handle, resourceHandle);
        assertEquals(3, javaEETransaction.getGetNonXAResourceNrOfCalls());

        // Make sure the resource is unenlisted
        assertFalse(resourceHandle.isEnlisted());
        assertTrue(resourceHandle.getResourceState().isUnenlisted());

        // In case the resource was unenlisted the transaction should also have been started
        LocalTransaction transaction = resourceHandle.getResource().getLocalTransaction();
        assertEquals(localTransaction, transaction);
        assertEquals(1, localTransaction.getBeginNrOfCalls());
    }

    @Test
    void testRollback_CurrentTransaction_Null_Unenlisted() throws Exception {
        // Boilerplate
        MyLocalTransactionMock localTransaction = new MyLocalTransactionMock();
        ManagedConnection managedConnection = new MyManagedConnection(localTransaction);
        ManagedConnectionFactory managedConnectionFactory = new MyManagedConnectionFactory(managedConnection);

        // Essence of the test: no transaction
        MyJavaEETransaction javaEETransaction = null;

        // Make sure ConnectorRuntime singleton is initialized
        MyConnectorRuntime connectorRuntime = new MyConnectorRuntime(new MyJavaEETransactionManager(javaEETransaction));
        connectorRuntime.postConstruct();

        // Create ResourceAllocator
        ResourceAllocator allocator = new LocalTxConnectorAllocator(null, managedConnectionFactory, resourceSpec, null,
                null, null, null, false);

        // Create ResourceHandle
        ResourceHandle resourceHandle = allocator.createResource();
        ResourceSpec resourceSpec = resourceHandle.getResourceSpec();
        ResourceAllocator resourceAllocator = resourceHandle.getResourceAllocator();
        ClientSecurityInfo info = null;

        // Create ConnectorXAResource for the handle
        ConnectorXAResource connectorXAResource = new ConnectorXAResource(resourceHandle, resourceSpec, resourceAllocator, info);
        // Purpose of this test variation: current transaction is null
        assertNull(connectorXAResource.getCurrentTransaction());

        // Test the rollback
        connectorXAResource.rollback(/* xidNotRequired */ null);

        // Make sure the resource is unenlisted
        assertFalse(resourceHandle.isEnlisted());
        assertTrue(resourceHandle.getResourceState().isUnenlisted());

        // In case the resource was unenlisted the transaction should also have been started
        LocalTransaction transaction = resourceHandle.getResource().getLocalTransaction();
        assertEquals(localTransaction, transaction);
        // Value should be 1 due to getResourceHandle calling begin for unenlisted resource
        // It should not be 2 due to resetAssociation calling begin again
        assertEquals(1, localTransaction.getBeginNrOfCalls());

        // Rollback should have erased the associated transaction
        assertNull(connectorXAResource.getAssociatedTransaction());
    }

    @Test
    void testRollback_CurrentTransaction_Null_Enlisted() throws Exception {
        // Boilerplate
        MyLocalTransactionMock localTransaction = new MyLocalTransactionMock();
        ManagedConnection managedConnection = new MyManagedConnection(localTransaction);
        ManagedConnectionFactory managedConnectionFactory = new MyManagedConnectionFactory(managedConnection);

        // Essence of the test: no transaction
        MyJavaEETransaction javaEETransaction = null;

        // Make sure ConnectorRuntime singleton is initialized
        MyConnectorRuntime connectorRuntime = new MyConnectorRuntime(new MyJavaEETransactionManager(javaEETransaction));
        connectorRuntime.postConstruct();

        // Create ResourceAllocator
        ResourceAllocator allocator = new LocalTxConnectorAllocator(null, managedConnectionFactory, resourceSpec, null,
                null, null, null, false);

        // Create ResourceHandle
        ResourceHandle resourceHandle = allocator.createResource();
        ResourceSpec resourceSpec = resourceHandle.getResourceSpec();
        ResourceAllocator resourceAllocator = resourceHandle.getResourceAllocator();
        ClientSecurityInfo info = null;

        // Create ConnectorXAResource for the handle
        ConnectorXAResource connectorXAResource = new ConnectorXAResource(resourceHandle, resourceSpec, resourceAllocator, info);
        // Purpose of this test variation: current transaction is null
        assertNull(connectorXAResource.getCurrentTransaction());

        // Make sure the resource is enlisted
        resourceHandle.getResourceState().setEnlisted(true);

        // Test the rollback
        connectorXAResource.rollback(/* xidNotRequired */ null);

        // Make sure the resource is still enlisted
        assertTrue(resourceHandle.isEnlisted());
        assertTrue(resourceHandle.getResourceState().isEnlisted());

        // In case the resource was unenlisted the transaction should also have been started
        LocalTransaction transaction = resourceHandle.getResource().getLocalTransaction();
        assertEquals(localTransaction, transaction);
        // Value should be 0, no begin should have been called
        assertEquals(0, localTransaction.getBeginNrOfCalls());

        // Rollback should have erased the associated transaction
        assertNull(connectorXAResource.getAssociatedTransaction());
    }

    @Test
    void testRollback_NonXaResource_Null_Unenlisted() throws Exception {
        // Boilerplate
        MyLocalTransactionMock localTransaction = new MyLocalTransactionMock();
        ManagedConnection managedConnection = new MyManagedConnection(localTransaction);
        ManagedConnectionFactory managedConnectionFactory = new MyManagedConnectionFactory(managedConnection);

        // Essence of the test MyJavaEETransaction returns null for getNonXaResource
        MyJavaEETransaction javaEETransaction = new MyJavaEETransaction();
        assertNull(javaEETransaction.getNonXAResource());

        // Make sure ConnectorRuntime singleton is initialized
        MyConnectorRuntime connectorRuntime = new MyConnectorRuntime(new MyJavaEETransactionManager(javaEETransaction));
        connectorRuntime.postConstruct();

        // Create ResourceAllocator
        ResourceAllocator allocator = new LocalTxConnectorAllocator(null, managedConnectionFactory, resourceSpec, null,
                null, null, null, false);

        // Create ResourceHandle
        ResourceHandle resourceHandle = allocator.createResource();
        ResourceSpec resourceSpec = resourceHandle.getResourceSpec();
        ResourceAllocator resourceAllocator = resourceHandle.getResourceAllocator();
        ClientSecurityInfo info = null;

        // Create ConnectorXAResource for the handle
        ConnectorXAResource connectorXAResource = new ConnectorXAResource(resourceHandle, resourceSpec, resourceAllocator, info);
        // Purpose of this test variation: current transaction is not null, but NonXaResource is null
        assertNotNull(connectorXAResource.getCurrentTransaction());
        assertNull(connectorXAResource.getCurrentTransaction().getNonXAResource());
        assertEquals(2, javaEETransaction.getGetNonXAResourceNrOfCalls());

        // Test the rollback
        connectorXAResource.rollback(/* xidNotRequired */ null);

        // Make sure the resource is unenlisted
        assertFalse(resourceHandle.isEnlisted());
        assertTrue(resourceHandle.getResourceState().isUnenlisted());

        // In case the resource was unenlisted the transaction should also have been started
        LocalTransaction transaction = resourceHandle.getResource().getLocalTransaction();
        assertEquals(localTransaction, transaction);
        // Value should be 1 due to getResourceHandle calling begin for unenlisted resource
        // It should not be 2 due to resetAssociation calling begin again
        assertEquals(1, localTransaction.getBeginNrOfCalls());

        // Rollback should have erased the associated transaction
        assertNull(connectorXAResource.getAssociatedTransaction());
    }

    @Test
    void testRollback_NonXaResource_Null_Enlisted() throws Exception {
        // Boilerplate
        MyLocalTransactionMock localTransaction = new MyLocalTransactionMock();
        ManagedConnection managedConnection = new MyManagedConnection(localTransaction);
        ManagedConnectionFactory managedConnectionFactory = new MyManagedConnectionFactory(managedConnection);

        // Essence of the test MyJavaEETransaction returns null for getNonXaResource
        MyJavaEETransaction javaEETransaction = new MyJavaEETransaction();
        assertNull(javaEETransaction.getNonXAResource());

        // Make sure ConnectorRuntime singleton is initialized
        MyConnectorRuntime connectorRuntime = new MyConnectorRuntime(new MyJavaEETransactionManager(javaEETransaction));
        connectorRuntime.postConstruct();

        // Create ResourceAllocator
        ResourceAllocator allocator = new LocalTxConnectorAllocator(null, managedConnectionFactory, resourceSpec, null,
                null, null, null, false);

        // Create ResourceHandle
        ResourceHandle resourceHandle = allocator.createResource();
        ResourceSpec resourceSpec = resourceHandle.getResourceSpec();
        ResourceAllocator resourceAllocator = resourceHandle.getResourceAllocator();
        ClientSecurityInfo info = null;

        // Create ConnectorXAResource for the handle
        ConnectorXAResource connectorXAResource = new ConnectorXAResource(resourceHandle, resourceSpec, resourceAllocator, info);
        // Purpose of this test variation: current transaction is not null, but NonXaResource is null
        assertNotNull(connectorXAResource.getCurrentTransaction());
        assertNull(connectorXAResource.getCurrentTransaction().getNonXAResource());
        assertEquals(2, javaEETransaction.getGetNonXAResourceNrOfCalls());

        // Make sure the resource is enlisted
        resourceHandle.getResourceState().setEnlisted(true);

        // Test the rollback
        connectorXAResource.rollback(/* xidNotRequired */ null);

        // Make sure the resource is still enlisted
        assertTrue(resourceHandle.isEnlisted());
        assertTrue(resourceHandle.getResourceState().isEnlisted());

        // In case the resource was unenlisted the transaction should also have been started
        LocalTransaction transaction = resourceHandle.getResource().getLocalTransaction();
        assertEquals(localTransaction, transaction);
        // Value should be 0, no begin should have been called
        assertEquals(0, localTransaction.getBeginNrOfCalls());

        // Rollback should have erased the associated transaction
        assertNull(connectorXAResource.getAssociatedTransaction());
    }

    public class MyManagedConnectionFactory extends ManagedConnectionFactoryMock {
        private static final long serialVersionUID = 1L;
        private ManagedConnection managedConnection;

        public MyManagedConnectionFactory(ManagedConnection managedConnection) {
            this.managedConnection = managedConnection;
        }

        @Override
        public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo)
                throws ResourceException {
            return managedConnection;
        }
    }

    public class MyManagedConnection extends ManagedConnectionMock {
        private LocalTransaction localTransaction;

        public MyManagedConnection(LocalTransaction localTransaction) {
            this.localTransaction = localTransaction;
        }

        @Override
        public LocalTransaction getLocalTransaction() throws ResourceException {
            return localTransaction;
        }
    }

    public class MyJavaEETransactionManager extends JavaEETransactionManagerMock {
        private Transaction transaction;

        public MyJavaEETransactionManager(Transaction transaction) {
            this.transaction = transaction;
        }

        @Override
        public Transaction getTransaction() throws SystemException {
            return transaction;
        }
    }

    private class MyJavaEETransaction extends JavaEETransactionMock {
        public int getNonXAResourceNrOfCalls;

        @Override
        public TransactionalResource getNonXAResource() {
            getNonXAResourceNrOfCalls = getGetNonXAResourceNrOfCalls() + 1;
            // Make sure the return value is null
            return null;
        }

        public int getGetNonXAResourceNrOfCalls() {
            return getNonXAResourceNrOfCalls;
        }
    }

    public class MyLocalTransactionMock extends LocalTransactionMock {
        private int getBeginNrOfCalls;

        @Override
        public void begin() throws ResourceException {
            getBeginNrOfCalls++;
        }

        public int getBeginNrOfCalls() {
            return getBeginNrOfCalls;
        }
    }

    public class MyConnectorRuntime extends ConnectorRuntime {
        private ProcessEnvironment processEnvironment = new ProcessEnvironment();

        public MyConnectorRuntime(JavaEETransactionManager transactionManager) throws Exception {
            // Force 'injection' of private field processEnvironment
            InjectionUtil.injectPrivateField(ConnectorRuntime.class, this, "processEnvironment", processEnvironment);
            InjectionUtil.injectPrivateField(ConnectorRuntime.class, this, "transactionManager", transactionManager);
        }

        @Override
        public PoolType getPoolType(PoolInfo poolInfo) throws ConnectorRuntimeException {
            return PoolType.STANDARD_POOL;
        }
    }

}
