/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.resource;

import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.resource.allocator.LocalTxConnectorAllocator;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.transaction.spi.TransactionalResource;
import com.sun.logging.LogDomains;

import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.DissociatableManagedConnection;
import jakarta.resource.spi.LazyEnlistableManagedConnection;
import jakarta.resource.spi.ManagedConnection;
import jakarta.transaction.Transaction;

import java.util.logging.Logger;

import javax.transaction.xa.XAResource;

import static java.util.logging.Level.FINEST;

/**
 * ResourceHandle encapsulates a resource connection.
 *
 * <p>Equality on the handle is based on the id field.
 *
 * @author Tony Ng
 */
public class ResourceHandle implements com.sun.appserv.connectors.internal.api.ResourceHandle, TransactionalResource {

    private static final Logger logger = LogDomains.getLogger(ResourceHandle.class, LogDomains.RSR_LOGGER);

    /**
     * Unique Id sequence generator value for generating unique ResourceHandle ids.
     */
    static private long idSequence;

    /**
     * The unique Id of this ResourceHandle instance.
     */
    private final long id;

    /**
     * The (optional) object represented by this handle. Normally the ManagedConnection object.<br>
     * THe object is known to be null in case of the BasicResourrceAllocator.
     */
    private final ManagedConnection resource;

    /**
     * The ResourceSpec reference, which can be used to find the correct connection pool for the resource handle.<br>
     * Note: this value is set in the constructor, but can be overwritten later during the lifetime of the resource handle,
     * while {@link #resourceAllocator} is not overwritten.
     */
    private ResourceSpec resourceSpec;

    /**
     * The (optional) XAResource reference for this resource handle. The value is set in this resource handle together with
     * the {@link #userConnection}.
     */
    private XAResource xaResource;

    /**
     * The (optional) 'userConnection' / 'connection handle', used by the application code to refer to the underlying
     * physical connection.<br>
     * The value is set in this resource handle together with the {@link #xaResource}.
     * <p>
     * TODO: consider renaming userConnection to connectionHandle
     */
    private Object userConnection;

    /**
     * The ResourceAllocator that created this resource handle instance.
     */
    private final ResourceAllocator resourceAllocator;

    /**
     * The component instance holding this resource handle.
     */
    private Object instance;

    /**
     * Sharing within a component (XA only). For XA-capable connections, multiple connections within a component are
     * collapsed into one. shareCount keeps track of the number of additional shared connections
     */
    private int shareCount;

    /**
     * Resource state class providing 'timestamp', 'enlisted' and 'busy' fields.<br>
     * Other state fields like 'enlistmentSuspended', 'shareCount', 'usageCount', 'lastValidated' and others are directly
     * part of this class as a field.
     */
    private ResourceState state = new ResourceState();

    /**
     * Used by LocalTxConnectorAllocator to save a listener for the resource handle.
     */
    private ConnectionEventListener listener;

    /**
     * Used by LazyEnlistableResourceManagerImpl to keep track of the enlistment suspended state of a resource handle.
     * LazyEnlistableResourceManagerImpl is used when setting "lazy-connection-enlistment" to enabled in the connection
     * pool.<br>
     * Default: false, false means: every resource is enlisted to a transaction.
     */
    private boolean enlistmentSuspended;

    /**
     * False if the ResourceAllocator is of the type LocalTxConnectorAllocator, otherwise true.
     */
    private final boolean supportsXAResource;

    /**
     * True if the Resource object ManagedConnection is of the type LazyEnlistableManagedConnection, otherwise false.
     */
    private boolean supportsLazyEnlistment;

    /**
     * True if the Resource object ManagedConnection is of the type DissociatableManagedConnection, otherwise false.
     */
    private boolean supportsLazyAssociation;

    /**
     * Resource handle specific lock, which can be used to lock the resource handle when state of the resource needs to be
     * changed. This is currently only used in the AssocWithThreadResourcePool implementation.
     */
    public final Object lock = new Object();

    /**
     * Holds the latest time at which the connection was validated.<br>
     * Could also be seen as part of ResourceState.
     */
    private long lastValidated;

    /**
     * Holds the number of times the handle(connection) is used so far. It is used by the ConnectionPool logic in case the
     * "max-connection-usage-count" option is set in the connection pool.<br>
     * Could also be seen as part of ResourceState.
     */
    private int usageCount;

    /**
     * Index of this ResourceHandle in the RWLockDataStructure internal ResourceHandle[]. RWLockDataStructure uses this
     * index value for optimistic locking of the resource.
     */
    private int rwLockDataStructureResourceIndex;

    /**
     * Value isDestroyByLeakTimeOut is set to true if ConnectionPool reclaimConnection logic was called when a potential
     * leak was found. The value is used when a resource is freed or closed by the poolLifeCycleListener to update
     * statistics.<br>
     * Could also be seen as part of ResourceState.
     */
    private boolean isDestroyByLeakTimeOut;

    /**
     * The connectionErrorOccurred field is set to true when a connection was aborted, or a connection error occurred, or
     * when a connection being closes is bad.<br>
     * Could also be seen as part of ResourceState.
     */
    private boolean connectionErrorOccurred;

    /**
     * Value markedReclaim is set by the
     * {@link com.sun.enterprise.resource.pool.ConnectionLeakDetector#potentialConnectionLeakFound(ResourceHandle)} method
     * when the resource is set to be reclaimed. Note: this value is only used to update statistics, the resource itself is
     * already removed from the pool the moment it is marked for reclaim. Code seems to suggest that this is also set when
     * {@link #isDestroyByLeakTimeOut} is set. Perhaps this boolean is not needed, statistics could perhaps be updated
     * immediately when isDestroyByLeakTimeOut is updated.<br>
     * Could also be seen as part of ResourceState.
     */
    private boolean markedReclaim;

    static private long getNextId() {
        synchronized (ResourceHandle.class) {
            idSequence++;
            return idSequence;
        }
    }

    /**
     * ResourceHandle constructor
     *
     * @param resource the (optional) object represented by this handle. Normally the ManagedConnection object.
     * @param resourceSpec the ResourceSpec reference, allowing this resource handle to find the correct connection pool.
     * @param resourceAllocator the ResourceAllocator that is creating this resource handle instance.
     */
    public ResourceHandle(ManagedConnection resource, ResourceSpec resourceSpec, ResourceAllocator resourceAllocator) {
        this.id = getNextId();
        this.resource = resource;
        this.resourceSpec = resourceSpec;
        this.resourceAllocator = resourceAllocator;

        if (resourceAllocator instanceof LocalTxConnectorAllocator) {
            supportsXAResource = false;
        } else {
            supportsXAResource = true;
        }

        if (resource instanceof LazyEnlistableManagedConnection) {
            supportsLazyEnlistment = true;
        }

        if (resource instanceof DissociatableManagedConnection) {
            supportsLazyAssociation = true;
        }
    }

    /**
     * Does this resource need enlistment to transaction manager?
     */
    @Override
    public boolean isTransactional() {
        return resourceAllocator.isTransactional();
    }

    @Override
    public boolean isEnlistmentSuspended() {
        return enlistmentSuspended;
    }

    @Override
    public void setEnlistmentSuspended(boolean enlistmentSuspended) {
        this.enlistmentSuspended = enlistmentSuspended;
    }

    public void markForReclaim(boolean reclaim) {
        this.markedReclaim = reclaim;
    }

    /**
     * To check if the resourceHandle is marked for leak reclaim or not.
     *
     * @return boolean
     */
    public boolean isMarkedForReclaim() {
        return markedReclaim;
    }

    @Override
    public boolean supportsXA() {
        return supportsXAResource;
    }

    public ResourceAllocator getResourceAllocator() {
        return resourceAllocator;
    }

    public ManagedConnection getResource() {
        return resource;
    }

    public void setResourceSpec(ResourceSpec resourceSpec) {
        this.resourceSpec = resourceSpec;
    }

    public ResourceSpec getResourceSpec() {
        return resourceSpec;
    }

    @Override
    public XAResource getXAResource() {
        return xaResource;
    }

    public Object getUserConnection() {
        return userConnection;
    }

    @Override
    public void setComponentInstance(Object instance) {
        this.instance = instance;
    }

    @Override
    public void closeUserConnection() throws PoolingException {
        getResourceAllocator().closeUserConnection(this);
    }

    @Override
    public Object getComponentInstance() {
        return instance;
    }

    public long getId() {
        return id;
    }

    public void fillInResourceObjects(Object userConnection, XAResource xaResource) {
        if (userConnection != null) {
            this.userConnection = userConnection;
        }

        if (xaResource != null) {
            if (logger.isLoggable(FINEST)) {
                // When Log level is Finest, XAResourceWrapper is used to log
                // all XA interactions - Don't wrap XAResourceWrapper if it is
                // already wrapped
                if ((xaResource instanceof XAResourceWrapper) || (xaResource instanceof ConnectorXAResource)) {
                    this.xaResource = xaResource;
                } else {
                    this.xaResource = new XAResourceWrapper(xaResource);
                }
            } else {
                this.xaResource = xaResource;
            }
        }
    }

    public void incrementCount() {
        shareCount++;
    }

    public void decrementCount() {
        if (shareCount == 0) {
            throw new IllegalStateException("shareCount cannot be negative");
        }

        shareCount--;
    }

    public int getShareCount() {
        return shareCount;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        if (other instanceof ResourceHandle) {
            return this.id == (((ResourceHandle) other).id);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(id).hashCode();
    }

    @Override
    public String toString() {
        return "<ResourceHandle id=" + id + ", state=" + state + "/>";
    }

    public void setConnectionErrorOccurred() {
        connectionErrorOccurred = true;
    }

    public boolean hasConnectionErrorOccurred() {
        return connectionErrorOccurred;
    }

    public ResourceState getResourceState() {
        return state;
    }

    public void setListener(ConnectionEventListener l) {
        this.listener = l;
    }

    public ConnectionEventListener getListener() {
        return listener;
    }

    @Override
    public boolean isShareable() {
        return resourceAllocator.shareableWithinComponent();
    }

    @Override
    public boolean isEnlisted() {
        return state.isEnlisted();
    }

    public long getLastValidated() {
        return lastValidated;
    }

    public void setLastValidated(long lastValidated) {
        this.lastValidated = lastValidated;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void incrementUsageCount() {
        usageCount++;
    }

    public int getRwLockDataStructureResourceIndex() {
        return rwLockDataStructureResourceIndex;
    }

    public void setRwLockDataStructureResourceIndex(int rwLockDataStructureResourceIndex) {
        this.rwLockDataStructureResourceIndex = rwLockDataStructureResourceIndex;
    }

    @Override
    public String getName() {
        return resourceSpec.getResourceId();
    }

    public boolean supportsLazyEnlistment() {
        return supportsLazyEnlistment;
    }

    public boolean supportsLazyAssociation() {
        return supportsLazyAssociation;
    }

    @Override
    public void enlistedInTransaction(Transaction transaction) throws IllegalStateException {
        ConnectorRuntime.getRuntime().getPoolManager().resourceEnlisted(transaction, this);
    }

    public boolean getDestroyByLeakTimeOut() {
        return isDestroyByLeakTimeOut;
    }

    public void setDestroyByLeakTimeOut(boolean isDestroyByLeakTimeOut) {
        this.isDestroyByLeakTimeOut = isDestroyByLeakTimeOut;
    }
}
