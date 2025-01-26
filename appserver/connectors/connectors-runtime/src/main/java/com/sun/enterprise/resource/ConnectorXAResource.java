/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.listener.LocalTxConnectionEventListener;
import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.api.TransactionConstants;
import com.sun.logging.LogDomains;

import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ResourceAllocationException;
import jakarta.transaction.SystemException;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;

/**
 * @author Tony Ng, Jagadish Ramu
 *
 */
public class ConnectorXAResource implements XAResource {

    private static Logger _logger = LogDomains.getLogger(ConnectorXAResource.class, LogDomains.RSR_LOGGER);

    /**
     * userHandle meaning: an object representing the "connection handle for the underlying physical connection". In some
     * code also named connectionHandle.
     */
    private Object userHandle;
    private ResourceSpec spec;
    private ClientSecurityInfo info;
    private ResourceHandle localHandle_;
    private JavaEETransaction associatedTransaction;

    public ConnectorXAResource(ResourceHandle handle, ResourceSpec resourceSpec, ResourceAllocator resourceAllocator,
            ClientSecurityInfo info) {
        // initially userHandle is associated with mc
        this.userHandle = null;
        this.spec = resourceSpec;
        this.info = info;
        localHandle_ = handle;
    }

    public void setUserHandle(Object userHandle) {
        this.userHandle = userHandle;
    }

    private void handleResourceException(Exception ex) throws XAException {
        _logger.log(SEVERE, "poolmgr.system_exception", ex);
        XAException xae = new XAException(ex.toString());
        xae.errorCode = XAException.XAER_RMERR;
        throw xae;
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        try {
            ManagedConnection managedConnection = getResourceHandle().getResource();
            managedConnection.getLocalTransaction().commit();
        } catch (Exception ex) {
            handleResourceException(ex);
        } finally {
            resetAssociation();
            resetAssociatedTransaction();
        }
    }

    @Override
    public void start(Xid xid, int flags) throws XAException {
        try {
            ResourceHandle handle = getResourceHandle();
            if (!localHandle_.equals(handle)) {
                ManagedConnection managedConnection = handle.getResource();
                managedConnection.associateConnection(userHandle);
                LocalTxConnectionEventListener listener = (LocalTxConnectionEventListener) handle.getListener();

                _logger.log(FINE, "connection_sharing_start", userHandle);

                listener.associateHandle(userHandle, localHandle_);
            } else {
                associatedTransaction = getCurrentTransaction();
            }
        } catch (Exception ex) {
            handleResourceException(ex);
        }
    }

    @Override
    public void end(Xid xid, int flags) throws XAException {
        _logger.log(FINE, "connection_sharing_end");

        try {
            ResourceHandle handleInTransaction = getResourceHandle();
            if (!localHandle_.equals(handleInTransaction)) {
                LocalTxConnectionEventListener listener = (LocalTxConnectionEventListener) handleInTransaction.getListener();

                ResourceHandle handle = listener.removeAssociation(userHandle);
                if (handle != null) { // not needed, just to be sure.
                    ManagedConnection associatedConnection = handle.getResource();
                    associatedConnection.associateConnection(userHandle);
                    _logger.log(FINE, "connection_sharing_reset_association", userHandle);
                }
            }
        } catch (Exception e) {
            handleResourceException(e);
        }
    }

    @Override
    public void forget(Xid xid) throws XAException {
        if (_logger.isLoggable(FINE)) {
            _logger.fine("Well, forget is called for xid :" + xid);
        }
        // no-op
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    @Override
    public boolean isSameRM(XAResource other) throws XAException {
        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (other instanceof ConnectorXAResource) {
            ConnectorXAResource obj = (ConnectorXAResource) other;
            return this.spec.equals(obj.spec) && this.info.equals(obj.info);
        }

        return false;
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        return TransactionConstants.LAO_PREPARE_OK;
    }

    @Override
    public Xid[] recover(int flag) throws XAException {
        return new Xid[0];
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        try {
            ResourceHandle handle = getResourceHandle();
            ManagedConnection managedConnection = handle.getResource();
            managedConnection.getLocalTransaction().rollback();
        } catch (Exception ex) {
            handleResourceException(ex);
        } finally {
            resetAssociation();
            resetAssociatedTransaction();
        }
    }

    @Override
    public boolean setTransactionTimeout(int seconds) throws XAException {
        return false;
    }

    private ResourceHandle getResourceHandle() throws PoolingException {
        try {
            ResourceHandle resourceHandle = null;
            JavaEETransaction javaEETransaction = getCurrentTransaction();
            if (javaEETransaction == null) { // Only if some thing is wrong with tx manager.
                resourceHandle = localHandle_; // Just return the local handle.
            } else {
                resourceHandle = (ResourceHandle) javaEETransaction.getNonXAResource();

                // Make sure that if local-tx resource is set as 'unshareable', only one resource
                // can be acquired. If the resource in question is not the one in transaction, fail
                if (!localHandle_.isShareable()) {
                    if (resourceHandle != localHandle_) {
                        throw new ResourceAllocationException("Cannot use more than one local-tx resource in unshareable scope");
                    }
                }
            }

            if (resourceHandle.getResourceState().isUnenlisted()) {
                ManagedConnection managedConnection = resourceHandle.getResource();

                // Begin the local transaction if first time
                // this ManagedConnection is used in this JTA transaction
                managedConnection.getLocalTransaction().begin();
            }
            return resourceHandle;
        } catch (Exception ex) {
            _logger.log(SEVERE, "poolmgr.system_exception", ex);
            throw new PoolingException(ex.toString(), ex);
        }
    }

    private JavaEETransaction getCurrentTransaction() throws SystemException {
        return (JavaEETransaction) ConnectorRuntime.getRuntime().getTransactionManager().getTransaction();
    }

    private void resetAssociation() throws XAException {
        try {
            ResourceHandle handle = getResourceHandle();
            LocalTxConnectionEventListener listener = (LocalTxConnectionEventListener) handle.getListener();

            // Clear the associations and Map all associated handles back to their actual Managed Connection.
            Map<Object, ResourceHandle> associatedHandles = listener.getAssociatedHandlesAndClearMap();
            for (Entry<Object, ResourceHandle> userHandleEntry : associatedHandles.entrySet()) {
                ResourceHandle associatedHandle = userHandleEntry.getValue();
                ManagedConnection associatedConnection = associatedHandle.getResource();
                associatedConnection.associateConnection(userHandleEntry.getKey());
                _logger.log(FINE, "connection_sharing_reset_association", userHandleEntry.getKey());
            }
        } catch (Exception ex) {
            handleResourceException(ex);
        }
    }

    public JavaEETransaction getAssociatedTransaction() {
        return associatedTransaction;
    }

    private void resetAssociatedTransaction() {
        associatedTransaction = null;
    }
}
