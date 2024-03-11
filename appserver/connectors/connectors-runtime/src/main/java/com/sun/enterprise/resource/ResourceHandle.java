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
import jakarta.transaction.Transaction;

import java.util.logging.Logger;

import javax.security.auth.Subject;
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

    // unique ID for resource handles
    static private long idSequence;

    private final long id;
    private final ClientSecurityInfo info;
    private final Object resource; // represents ManagedConnection
    private ResourceSpec spec;
    private XAResource xaRes;
    private Object userConnection; // represents connection-handle to user
    private final ResourceAllocator resourceAllocator;
    private Object instance; // the component instance holding this resource
    private int shareCount; // sharing within a component (XA only)
    private final boolean supportsXAResource;

    private Subject subject;

    private ResourceState state;
    private ConnectionEventListener listener;

    private boolean enlistmentSuspended;

    private boolean supportsLazyEnlistment_;
    private boolean supportsLazyAssoc_;

    public final Object lock = new Object();
    private long lastValidated; // holds the latest time at which the connection was validated.
    private int usageCount; // holds the no. of times the handle(connection) is used so far.
    private int partition;
    private int index;
    private boolean isDestroyByLeakTimeOut;
    private boolean connectionErrorOccurred;

    static private long getNextId() {
        synchronized (ResourceHandle.class) {
            idSequence++;
            return idSequence;
        }
    }

    private boolean markedReclaim;

    public ResourceHandle(Object resource, ResourceSpec spec, ResourceAllocator alloc, ClientSecurityInfo info) {
        this.id = getNextId();
        this.spec = spec;
        this.info = info;
        this.resource = resource;
        this.resourceAllocator = alloc;

        if (alloc instanceof LocalTxConnectorAllocator) {
            supportsXAResource = false;
        } else {
            supportsXAResource = true;
        }

        if (resource instanceof LazyEnlistableManagedConnection) {
            supportsLazyEnlistment_ = true;
        }

        if (resource instanceof DissociatableManagedConnection) {
            supportsLazyAssoc_ = true;
        }
    }

    /**
     * Does this resource need enlistment to transaction manager?
     */
    @Override
    public boolean isTransactional() {
        return resourceAllocator.isTransactional();
    }

    /**
     * To check whether lazy enlistment is suspended or not.
     *
     * <p>If {@code true}, TM will not do enlist/lazy enlist.
     *
     * @return boolean
     */
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

    public Object getResource() {
        return resource;
    }

    public ClientSecurityInfo getClientSecurityInfo() {
        return info;
    }

    public void setResourceSpec(ResourceSpec spec) {
        this.spec = spec;
    }

    public ResourceSpec getResourceSpec() {
        return spec;
    }

    @Override
    public XAResource getXAResource() {
        return xaRes;
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

    public void fillInResourceObjects(Object userConnection, XAResource xaRes) {
        if (userConnection != null) {
            this.userConnection = userConnection;
        }

        if (xaRes != null) {
            if (logger.isLoggable(FINEST)) {
                // When Log level is Finest, XAResourceWrapper is used to log
                // all XA interactions - Don't wrap XAResourceWrapper if it is
                // already wrapped
                if ((xaRes instanceof XAResourceWrapper) || (xaRes instanceof ConnectorXAResource)) {
                    this.xaRes = xaRes;
                } else {
                    this.xaRes = new XAResourceWrapper(xaRes);
                }
            } else {
                this.xaRes = xaRes;
            }
        }
    }

    // For XA-capable connections, multiple connections within a
    // component are collapsed into one. shareCount keeps track of
    // the number of additional shared connections
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

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Subject getSubject() {
        return subject;
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
        return String.valueOf(id);
    }

    public void setConnectionErrorOccurred() {
        connectionErrorOccurred = true;
    }

    public boolean hasConnectionErrorOccurred() {
        return connectionErrorOccurred;
    }

    public void setResourceState(ResourceState state) {
        this.state = state;
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
    public void destroyResource() {
        throw new UnsupportedOperationException("Transaction is not supported yet");
    }

    @Override
    public boolean isEnlisted() {
        return state != null && state.isEnlisted();
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

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String getName() {
        return spec.getResourceId();
    }

    public boolean supportsLazyEnlistment() {
        return supportsLazyEnlistment_;
    }

    public boolean supportsLazyAssociation() {
        return supportsLazyAssoc_;
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
