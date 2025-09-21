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
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.appserv.connectors.internal.spi.MCFLifecycleListener;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.resource.ClientSecurityInfo;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.listener.PoolLifeCycle;
import com.sun.enterprise.resource.rm.LazyEnlistableResourceManagerImpl;
import com.sun.enterprise.resource.rm.NoTxResourceManagerImpl;
import com.sun.enterprise.resource.rm.ResourceManager;
import com.sun.enterprise.resource.rm.ResourceManagerImpl;
import com.sun.enterprise.resource.rm.SystemResourceManagerImpl;
import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.DissociatableManagedConnection;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.RetryableUnavailableException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.Transaction;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.ComponentInvocationHandler;
import org.glassfish.api.invocation.InvocationException;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static javax.transaction.xa.XAResource.TMSUCCESS;

/**
 * @author Tony Ng, Aditya Gore
 */
@Service
public class PoolManagerImpl extends AbstractPoolManager implements ComponentInvocationHandler {

    private static final Logger LOG = LogDomains.getLogger(PoolManagerImpl.class, LogDomains.RSR_LOGGER);

    private final ConcurrentHashMap<PoolInfo, ResourcePool> poolTable;
    private final ResourceManager resourceManager;
    private final ResourceManager sysResourceManager;
    private final ResourceManager noTxResourceManager;
    private final LazyEnlistableResourceManagerImpl lazyEnlistableResourceManager;

    @Inject
    private Provider<ConnectorRuntime> connectorRuntimeProvider;
    private ConnectorRuntime runtime;
    private PoolLifeCycle listener;

    public PoolManagerImpl() {
        this.poolTable = new ConcurrentHashMap<>();
        resourceManager = new ResourceManagerImpl();
        sysResourceManager = new SystemResourceManagerImpl();
        noTxResourceManager = new NoTxResourceManagerImpl();
        lazyEnlistableResourceManager = new LazyEnlistableResourceManagerImpl();
    }

    @Override
    public void createEmptyConnectionPool(PoolInfo poolInfo, PoolType pooltype, Hashtable<?, ?> env) throws PoolingException {
        // Create and initialise the connection pool
        createAndInitPool(poolInfo, pooltype, env);

        // notify mcf-create
        ManagedConnectionFactory managedConnectionFactory = ConnectorRegistry.getInstance().getManagedConnectionFactory(poolInfo);
        if (managedConnectionFactory != null) {
            if (managedConnectionFactory instanceof MCFLifecycleListener) {
                ((MCFLifecycleListener) managedConnectionFactory).mcfCreated();
            }
        }

        if (listener != null) {
            try {
                listener.poolCreated(poolInfo);
            } catch (RuntimeException ex) {
                LOG.log(SEVERE, "Listener " + listener + " failed for " + poolInfo, ex);
            }
        }
    }

    /**
     * Create and initialize pool if not created already.
     * <p>
     * package default for unit test
     *
     * @param poolInfo pool identifier of the pool to be created
     * @param poolType the type of pool to be created
     * @param env the jndi information to find the ConnectorConnectionPool configuration used to configure the pool
     * @throws PoolingException when unable to create/initialize pool
     */
    void createAndInitPool(final PoolInfo poolInfo, PoolType poolType, Hashtable<?, ?> env) throws PoolingException {
        ResourcePool pool = getPool(poolInfo);
        if (pool == null) {
            pool = ResourcePoolFactoryImpl.newInstance(poolInfo, poolType, env);
            addPool(pool);
            LOG.log(INFO, "Created connection pool and added it to PoolManager: {0}", pool);
        }
    }

    // invoked by DataSource objects to obtain a connection
    @Override
    public Object getResource(ResourceSpec resourceSpec, ResourceAllocator resourceAllocator, ClientSecurityInfo clientSecurityInfo) throws PoolingException, RetryableUnavailableException {
        if (LOG.isLoggable(FINE)) {
            LOG.log(FINE, "PoolManagerImpl.getResource START, resourceSpec=" + resourceSpec + "\nresourceAllocator="
                    + resourceAllocator + "\nclientSecurityInfo=" + clientSecurityInfo);
        }
        Transaction transaction = null;
        boolean transactional = resourceAllocator.isTransactional();

        if (transactional) {
            transaction = getResourceManager(resourceSpec).getTransaction();
        }

        ResourceHandle resourceHandle = getResourceFromPool(resourceSpec, resourceAllocator, clientSecurityInfo, transaction);
        if (LOG.isLoggable(FINE)) {
            LOG.log(FINE, "PoolManagerImpl.getResource handle=" + resourceHandle + ", poolStatus" + getPoolStatus(resourceSpec.getPoolInfo()));
        }
        if (!resourceHandle.supportsLazyAssociation()) {
            resourceSpec.setLazyAssociatable(false);
        }

        if (resourceSpec.isLazyAssociatable() && resourceSpec.getConnectionToAssociate() != null) {
            // If getConnectionToAssociate returns a connection that means
            // we need to associate a new connection with it
            try {
                Object connection = resourceSpec.getConnectionToAssociate();
                ManagedConnection managedConnection = resourceHandle.getResource();
                managedConnection.associateConnection(connection);
            } catch (ResourceException e) {
                putbackDirectToPool(resourceHandle, resourceSpec.getPoolInfo());

                throw new PoolingException(e.getMessage(), e);
            }
        }

        // If the ResourceAdapter does not support lazy enlistment we cannot either
        if (!resourceHandle.supportsLazyEnlistment()) {
            resourceSpec.setLazyEnlistable(false);
        }

        // Overwrite the resourceSpec value in the resourceHandle.
        // TODO: explain why this is done and why resourceAllocator in the resourceHandle is not overwritten with this
        // resourceAllocator.
        resourceHandle.setResourceSpec(resourceSpec);

        LOG.log(FINE, "PoolManagerImpl.getResource, transaction=" + transaction + " resourceHandle=" + resourceHandle
            + " isEnlisted=" + resourceHandle.getResourceState().isEnlisted());

        try {
            if (resourceHandle.getResourceState().isEnlisted()) {
                LOG.log(FINE, "getResource - DO NOT ENLIST because it is already Enlisted, resource=" + resourceHandle);
            } else {
                // The spec being used here is the spec with the updated lazy enlistment info.

                // Here's the real place where we care about the correct resource manager
                // (which in turn depends upon the ResourceSpec)
                // and that's because if lazy enlistment needs to be done we need to get the
                // LazyEnlistableResourceManager

                getResourceManager(resourceSpec).enlistResource(resourceHandle);
            }
        } catch (Exception e) {
            // In the rare cases where enlistResource throws exception, we should return the resource to the pool
            putbackDirectToPool(resourceHandle, resourceSpec.getPoolInfo());

            LOG.log(WARNING, "poolmgr.err_enlisting_res_in_getconn", resourceSpec.getPoolInfo());
            LOG.fine("rm.enlistResource threw Exception. Returning resource to pool");

            // and rethrow the exception
            throw new PoolingException(e);
        }

        Object result = resourceHandle.getUserConnection();
        if (LOG.isLoggable(FINE)) {
            LOG.log(FINE, "PoolManagerImpl.getResource END, resourceHandle=" + resourceHandle);
        }
        return result;
    }

    @Override
    public void putbackDirectToPool(ResourceHandle resourceHandle, PoolInfo poolInfo) {
        // Notify pool
        if (poolInfo != null) {
            ResourcePool pool = poolTable.get(poolInfo);
            if (pool != null) {
                pool.resourceClosed(resourceHandle);
            }
        }
    }

    @Override
    public ResourceHandle getResourceFromPool(ResourceSpec resourceSpec, ResourceAllocator resourceAllocator, ClientSecurityInfo info, Transaction transaction) throws PoolingException, RetryableUnavailableException {

        // pool.getResource() has been modified to:
        // - be able to create new resource if needed
        // - block the caller until a resource is acquired or
        // the max-wait-time expires

        return getPool(resourceSpec.getPoolInfo()).getResource(resourceSpec, resourceAllocator, transaction);
    }

    @Override
    public boolean switchOnMatching(PoolInfo poolInfo) {
        ResourcePool pool = getPool(poolInfo);
        if (pool == null) {
            return false;
        }

        pool.switchOnMatching();

        return true;
    }

    /* package protected for unit test */
    protected void addPool(ResourcePool pool) {
        LOG.log(FINE, "Adding pool {0} to pooltable", pool.getPoolInfo());
        poolTable.put(pool.getPoolInfo(), pool);
    }

    private ResourceManager getResourceManager(ResourceSpec resourceSpec) {
        if (resourceSpec.isNonTx()) {
            LOG.fine("Returning noTxResourceManager");
            return noTxResourceManager;
        }

        if (resourceSpec.isPM()) {
            LOG.fine("Returning sysResourceManager");
            return sysResourceManager;
        }

        if (resourceSpec.isLazyEnlistable()) {
            LOG.fine("Returning LazyEnlistableResourceManager");
            return lazyEnlistableResourceManager;
        }

        LOG.fine("Returning resourceManager");
        return resourceManager;
    }

    private void addSyncListener(Transaction transaction) {
        try {
            transaction.registerSynchronization(new SynchronizationListener(transaction));
        } catch (Exception ex) {
            LOG.log(FINE, () -> "Error adding syncListener : " + (ex.getMessage() != null ? ex.getMessage() : " "));
        }
    }

    @Override
    public void transactionCompleted(Transaction transaction, int status) throws IllegalStateException {
        Set<PoolInfo> pools = ((JavaEETransaction) transaction).getAllParticipatingPools();

        for (PoolInfo poolInfo : pools) {
            ResourcePool pool = getPool(poolInfo);
            if (LOG.isLoggable(FINE)) {
                LOG.fine("calling transactionCompleted on " + pool.getPoolInfo());
            }

            pool.transactionCompleted(transaction, status);
        }
    }

    @Override
    public void resourceEnlisted(Transaction transaction, com.sun.appserv.connectors.internal.api.ResourceHandle internalHandle) throws IllegalStateException {
        ResourceHandle resourceHandle = (ResourceHandle) internalHandle;

        PoolInfo poolInfo = resourceHandle.getResourceSpec().getPoolInfo();
        try {
            JavaEETransaction javaEETransaction = (JavaEETransaction) transaction;
            if (poolInfo != null && javaEETransaction.getResources(poolInfo) == null) {
                addSyncListener(transaction);
            }
        } catch (ClassCastException e) {
            addSyncListener(transaction);
        }

        if (poolInfo != null) {
            ResourcePool pool = getPool(poolInfo);
            if (pool != null) {
                pool.resourceEnlisted(transaction, resourceHandle);
            }
        }
    }

    @Override
    public void lazyEnlist(ManagedConnection mc) throws ResourceException {
        lazyEnlistableResourceManager.lazyEnlist(mc);
    }

    private ConnectorRuntime getConnectorRuntime() {
        if (runtime == null) {
            runtime = connectorRuntimeProvider.get();
        }
        return runtime;
    }

    @Override
    public void registerResource(com.sun.appserv.connectors.internal.api.ResourceHandle internalHandle) throws PoolingException {
        ResourceHandle resourceHandle = (ResourceHandle) internalHandle;
        getResourceManager(resourceHandle.getResourceSpec()).registerResource(resourceHandle);
    }

    @Override
    public void registerPoolLifeCycleListener(PoolLifeCycle poolListener) {
        listener = poolListener;
    }

    @Override
    public void unregisterPoolLifeCycleListener() {
        listener = null;
    }

    @Override
    public void unregisterResource(com.sun.appserv.connectors.internal.api.ResourceHandle internalResource, int xaresFlag) {
        ResourceHandle resourceHandle = (ResourceHandle) internalResource;
        getResourceManager(resourceHandle.getResourceSpec()).unregisterResource(resourceHandle, xaresFlag);
    }

    @Override
    public void resourceClosed(ResourceHandle resource) {
        if (LOG.isLoggable(FINE)) {
            LOG.log(FINE, "PoolManagerImpl.resourceClosed START, resource=" + resource);
        }

        getResourceManager(resource.getResourceSpec()).delistResource(resource, TMSUCCESS);
        putbackResourceToPool(resource, false);

        if (LOG.isLoggable(FINE)) {
            // Warning resource state logged here might not be valid, could be part of another thread already
            // but logging the resource id is useful.
            LOG.log(FINE, "PoolManagerImpl.resourceClosed END, resource=" + resource);
        }
    }

    @Override
    public void badResourceClosed(ResourceHandle resource) {
        getResourceManager(resource.getResourceSpec()).delistResource(resource, TMSUCCESS);
        putbackBadResourceToPool(resource);
    }

    @Override
    public void resourceErrorOccurred(ResourceHandle resource) {
        // TODO: Why is delistResource not called like done in resourceAbortOccurred and in resourceClosed?
        // Shouldn't delistResource be called with TMFAIL ?
        // Added example in unit test PoolManagerImplTest that shows the delist is not called.

        // TODO: Why is resourceHandle.setConnectionErrorOccurred() not performed?
        // Added example in unit test PoolManagerImplTest that shows connection error is not updated

        putbackResourceToPool(resource, true);
    }

    @Override
    public void resourceAbortOccurred(ResourceHandle resource) {
        // TODO: Why is TMSUCCESS used and not TMFAIL? Document the meaning of Abort.
        // <p>
        // com.sun.gjc.spi.ManagedConnectionImpl.transactionCompleted()
        // performs: badConnectionEventListener.connectionAbortOccurred -> jdbc.markedForRemoval_conAborted
        // <p>
        // com.sun.gjc.spi.jdbc40.ConnectionHolder40.abort(Executor) documents: "Abort operation to mark the connection
        // internally as a bad connection for removal and to close the connection. This ensures that at the end of the
        // transaction, the connection is destroyed. A running thread holding a connection will run to completion before the
        // connection is destroyed"
        getResourceManager(resource.getResourceSpec()).delistResource(resource, TMSUCCESS);

        // TODO: Why is resourceHandle.setConnectionErrorOccurred() not performed?
        // Added example in unit test PoolManagerImplTest that shows connection error is not updated
        putbackResourceToPool(resource, true);
    }

    private void putbackBadResourceToPool(ResourceHandle resourceHandle) {
        // Notify pool
        PoolInfo poolInfo = resourceHandle.getResourceSpec().getPoolInfo();
        if (poolInfo != null) {
            ResourcePool pool = poolTable.get(poolInfo);
            if (pool != null) {
                synchronized (pool) {
                    // TODO: why is resourceClosed called AND resourceErrorOccurred?
                    pool.resourceClosed(resourceHandle);
                    resourceHandle.setConnectionErrorOccurred();
                    pool.resourceErrorOccurred(resourceHandle);
                }
            }
        }
    }

    @Override
    public void putbackResourceToPool(ResourceHandle resourceHandle, boolean errorOccurred) {
        if (LOG.isLoggable(FINE)) {
            LOG.log(FINE, "PoolManagerImpl.putbackResourceToPool START, resource=" + resourceHandle + ", errorOccurred=" + errorOccurred);
        }
        // Notify pool
        PoolInfo poolInfo = resourceHandle.getResourceSpec().getPoolInfo();
        if (poolInfo != null) {
            ResourcePool pool = poolTable.get(poolInfo);
            if (pool != null) {
                if (errorOccurred) {
                    // TODO: this code is different from putbackBadResourceToPool logic, explain why
                    // TODO: why is resourceHandle.setConnectionErrorOccurred(); not called?
                    pool.resourceErrorOccurred(resourceHandle);
                } else {
                    pool.resourceClosed(resourceHandle);
                }
            }
        }
        if (LOG.isLoggable(FINE)) {
            LOG.log(FINE, "PoolManagerImpl.putbackResourceToPool END, resource=" + resourceHandle + ", errorOccurred=" + errorOccurred);
        }
    }

    @Override
    public ResourcePool getPool(PoolInfo poolInfo) {
        if (poolInfo == null) {
            return null;
        }

        return poolTable.get(poolInfo);
    }

    /**
     * Kill the pool with the specified pool name
     *
     * @param poolInfo - The name of the pool to kill
     */
    @Override
    public void killPool(PoolInfo poolInfo) {
        LOG.log(Level.FINEST, "killPool(poolInfo={0})", poolInfo);
        // Empty the pool and remove from poolTable
        ResourcePool pool = poolTable.get(poolInfo);
        if (pool != null) {
            pool.cancelResizerTask();
            pool.emptyPool();
            LOG.log(Level.CONFIG, "Removing pool {0} from pooltable", pool);

            poolTable.remove(poolInfo);
            if (listener != null) {
                listener.poolDestroyed(poolInfo);
            }

            // Notify mcf-destroy
            ManagedConnectionFactory managedConnectionFactory = ConnectorRegistry.getInstance().getManagedConnectionFactory(poolInfo);
            if (managedConnectionFactory != null) {
                if (managedConnectionFactory instanceof MCFLifecycleListener) {
                    ((MCFLifecycleListener) managedConnectionFactory).mcfDestroyed();
                }
            }
        }
    }

    @Override
    public void killFreeConnectionsInPools() {
        LOG.fine("Killing all free connections in pools");
        for (ResourcePool pool : poolTable.values()) {
            PoolInfo poolInfo = pool.getPoolStatus().getPoolInfo();
            try {
                if (poolInfo != null) {
                    ResourcePool poolToKill = poolTable.get(poolInfo);
                    if (poolToKill != null) {
                        pool.emptyFreeConnectionsInPool();
                    }

                    if (LOG.isLoggable(FINE)) {
                        LOG.fine("Now killing free connections in pool : " + poolInfo);
                    }
                }
            } catch (Exception e) {
                if (LOG.isLoggable(FINE)) {
                    LOG.fine("Error killing pool : " + poolInfo + " :: " + (e.getMessage() == null ? " " : e.getMessage()));
                }
            }
        }
    }

    @Override
    public ResourceReferenceDescriptor getResourceReference(SimpleJndiName jndiName, SimpleJndiName logicalName) {
        Set<ResourceReferenceDescriptor> descriptors = getConnectorRuntime().getResourceReferenceDescriptor();
        List<ResourceReferenceDescriptor> matchingRefs = new ArrayList<>();

        if (descriptors != null) {
            for (ResourceReferenceDescriptor descriptor : descriptors) {
                SimpleJndiName name = descriptor.getJndiName();
                if (jndiName.equals(name)) {
                    matchingRefs.add(descriptor);
                }
            }
        }

        if (matchingRefs.size() == 1) {
            return matchingRefs.get(0);
        }

        if (matchingRefs.size() > 1) {
            for (ResourceReferenceDescriptor resourceReferenceDescriptor : matchingRefs) {
                String refName = resourceReferenceDescriptor.getName();
                if (refName != null && logicalName != null) {
                    if (getJavaName(new SimpleJndiName(refName)).equals(getJavaName(logicalName))) {
                        return resourceReferenceDescriptor;
                    }
                }
            }
        }

        return null;
    }

    private static SimpleJndiName getJavaName(SimpleJndiName name) {
        if (name == null || name.hasJavaPrefix()) {
            return name;
        }

        // by default, scope is "comp"
        return new SimpleJndiName(SimpleJndiName.JNDI_CTX_JAVA_COMPONENT_ENV + name);
    }

    @Override
    public void beforePreInvoke(ComponentInvocation.ComponentInvocationType invType, ComponentInvocation prevInv, ComponentInvocation newInv) throws InvocationException {
        // no-op
    }

    @Override
    public void afterPreInvoke(ComponentInvocation.ComponentInvocationType invType, ComponentInvocation prevInv, ComponentInvocation curInv) throws InvocationException {
        // no-op
    }

    @Override
    public void beforePostInvoke(ComponentInvocation.ComponentInvocationType invType, ComponentInvocation prevInv, ComponentInvocation curInv) throws InvocationException {
        // no-op
    }

    /*
     * Called by the InvocationManager at methodEnd. This method will disassociate ManagedConnection instances from
     * Connection handles if the ResourceAdapter supports that.
     */
    @Override
    public void afterPostInvoke(ComponentInvocation.ComponentInvocationType invType, ComponentInvocation prevInv, ComponentInvocation curInv) throws InvocationException {
        postInvoke(curInv);
    }

    private void postInvoke(ComponentInvocation curInv) {
        ComponentInvocation invToUse = curInv;

        if (invToUse == null) {
            return;
        }

        Object comp = invToUse.getInstance();

        if (comp == null) {
            return;
        }

        handleLazilyAssociatedConnectionPools(comp, invToUse);
    }

    /**
     * If the connections associated with the component are lazily-associatable, dissociate them.
     *
     * @param comp Component that acquired connections
     * @param invToUse component invocation
     */
    private void handleLazilyAssociatedConnectionPools(Object comp, ComponentInvocation invToUse) {
        JavaEETransactionManager jJavaEETransactionManager = getConnectorRuntime().getTransactionManager();
        List<ResourceHandle> list = jJavaEETransactionManager.getExistingResourceList(comp, invToUse);

        if ((list == null) || list.isEmpty()) {
            return;
        }

        ResourceHandle[] handles = list.toArray(ResourceHandle[]::new);
        for (ResourceHandle resourceHandle : handles) {

            if (resourceHandle == null) {
                LOG.log(WARNING, "lazy_association.lazy_association_resource_handle");
            } else {
                ResourceSpec spec = resourceHandle.getResourceSpec();
                if (spec == null) {
                    LOG.log(WARNING, "lazy_association.lazy_association_resource_spec");
                } else if (spec.isLazyAssociatable()) {
                    // In this case we are assured that the managedConnection is
                    // of type DissociatableManagedConnection
                    if (resourceHandle.getResource() != null) {
                        DissociatableManagedConnection managedConnection = (DissociatableManagedConnection)
                            resourceHandle.getResource();

                        if (resourceHandle.isEnlisted()) {
                            getResourceManager(spec).delistResource(resourceHandle, TMSUCCESS);
                        }

                        try {
                            managedConnection.dissociateConnections();
                        } catch (ResourceException re) {
                            throw new InvocationException(re.getMessage(), re);
                        } finally {
                            if (resourceHandle.getResourceState().isBusy()) {
                                putbackDirectToPool(resourceHandle, spec.getPoolInfo());
                            }
                        }
                    } else {
                        LOG.log(WARNING, "lazy_association.lazy_association_resource");
                    }
                }
            }
        }
    }

    class SynchronizationListener implements Synchronization {

        private final Transaction transaction;

        SynchronizationListener(Transaction transaction) {
            this.transaction = transaction;
        }

        @Override
        public void afterCompletion(int status) {
            try {
                transactionCompleted(transaction, status);
            } catch (Exception ex) {
                LOG.log(FINE, () -> "Exception in afterCompletion : " + (ex.getMessage() == null ? " " : ex.getMessage()));
            }
        }

        @Override
        public void beforeCompletion() {
            // do nothing
        }
    }

    @Override
    public void reconfigPoolProperties(ConnectorConnectionPool connectorConnectionPool) throws PoolingException {
        ResourcePool pool = getPool(connectorConnectionPool.getPoolInfo());
        if (pool != null) {
            pool.reconfigurePool(connectorConnectionPool);
        }
    }

    /**
     * Flush Connection pool by reinitializing the connections established in the pool.
     *
     * @param poolInfo
     * @throws com.sun.appserv.connectors.internal.api.PoolingException
     */
    @Override
    public boolean flushConnectionPool(PoolInfo poolInfo) throws PoolingException {
        ResourcePool pool = getPool(poolInfo);
        if (pool == null) {
            throw new PoolingException(
                "Flush Connection Pool did not happen as pool " + poolInfo + " is not initialized");
        }
        return pool.flushConnectionPool();
    }

    @Override
    public PoolStatus getPoolStatus(PoolInfo poolInfo) {
        ResourcePool pool = poolTable.get(poolInfo);
        if (pool != null) {
            return pool.getPoolStatus();
        }
        return null;
    }
}
