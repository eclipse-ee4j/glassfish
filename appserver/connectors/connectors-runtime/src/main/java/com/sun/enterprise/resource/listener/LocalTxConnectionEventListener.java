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

package com.sun.enterprise.resource.listener;

import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.pool.PoolManager;
import com.sun.logging.LogDomains;

import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.ManagedConnection;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Binod P.G
 */
public class LocalTxConnectionEventListener extends ConnectionEventListener {

    private static final Logger logger = LogDomains.getLogger(ResourceHandle.class, LogDomains.RSR_LOGGER);

    /**
     * A shortcut to the singleton PoolManager instance. Field could also be removed.
     */
    private final PoolManager poolManager = ConnectorRuntime.getRuntime().getPoolManager();

    /**
     * Map to store the relation: "userHandle/connectionHandle -> ResourceHandle" using reference-equality. Whenever a
     * connection is associated with a ManagedConnection, that connection and the resourceHandle associated with its
     * original ManagedConnection will be put in this table.
     * <p>
     * userHandle meaning: an object representing the "connection handle for the underlying physical connection". In some
     * code also named connectionHandle.
     * <p>
     * All code altering associatedHandles must be synchronized.
     */
    private final IdentityHashMap<Object, ResourceHandle> associatedHandles = new IdentityHashMap<>(10);

    /**
     * The original resource for which this listener is created.
     */
    private final ResourceHandle resource;

    public LocalTxConnectionEventListener(ResourceHandle resource) {
        this.resource = resource;

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "LocalTxConnectionEventListener constructor, resource=" + resource + ", this=" + this);
        }
    }

    @Override
    public synchronized void connectionClosed(ConnectionEvent evt) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "LocalTxConnectionEventListener.connectionClosed START, resource=" + resource + ", this=" + this);
            for (Object key : associatedHandles.keySet()) {
                ResourceHandle associatedHandle = associatedHandles.get(key);
                logger.log(Level.FINE,
                        "LocalTxConnectionEventListener.connectionClosed associatedHandles: key=" + key + ", handle=" + associatedHandle);
                logger.log(Level.FINE,
                        "LocalTxConnectionEventListener.connectionClosed associatedHandles: resource=" + associatedHandle.getResource());
            }
        }

        Object connectionHandle = evt.getConnectionHandle();
        ResourceHandle handle = associatedHandles.getOrDefault(connectionHandle, resource);
        // ManagedConnection instance is still valid and put back in the pool: do not remove the event listener.
        poolManager.resourceClosed(handle);

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "LocalTxConnectionEventListener.connectionClosed END, resource=" + resource + ", handle=" + handle + ", this=" + this);
        }
    }

    @Override
    public synchronized void connectionErrorOccurred(ConnectionEvent evt) {
        resource.setConnectionErrorOccurred();

        // ManagedConnection instance is now invalid and unusable. Remove this event listener.
        ManagedConnection mc = (ManagedConnection) evt.getSource();
        mc.removeConnectionEventListener(this);

        poolManager.resourceErrorOccurred(resource);
    }

    /**
     * Resource adapters will signal that the connection being closed is bad.
     *
     * @param evt ConnectionEvent
     */
    @Override
    public synchronized void badConnectionClosed(ConnectionEvent evt) {
        Object connectionHandle = evt.getConnectionHandle();
        ResourceHandle handle = associatedHandles.getOrDefault(connectionHandle, resource);

        // TODO: Explain why event listener needs to be removed.
        // There is no documentation mentioning: ManagedConnection instance is now invalid and unusable.
        ManagedConnection mc = (ManagedConnection) evt.getSource();
        mc.removeConnectionEventListener(this);

        poolManager.badResourceClosed(handle);
    }

    @Override
    public void localTransactionStarted(ConnectionEvent evt) {
        // no-op
    }

    @Override
    public void localTransactionCommitted(ConnectionEvent evt) {
        // no-op
    }

    @Override
    public void localTransactionRolledback(ConnectionEvent evt) {
        // no-op
    }

    /**
     * Associate the given userHandle to the resourceHandle.
     *
     * @param userHandle the userHandle object to be associated with the new handle
     * @param resourceHandle the original Handle
     */
    public synchronized void associateHandle(Object userHandle, ResourceHandle resourceHandle) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "LocalTxConnectionEventListener associateHandle, userHandle=" + userHandle + ", resourceHandle=" + resourceHandle + ", this=" + this);
        }
        associatedHandles.put(userHandle, resourceHandle);
    }

    /**
     * Removes the Map entry for the given userHandle key.
     *
     * @param userHandle The userHandle key to be removed from the map.
     * @return the associated ResourceHandle that is removed from the map or null if no association was found. A null return
     * can also indicate that the map previously associated null with userHandle.
     */
    public synchronized ResourceHandle removeAssociation(Object userHandle) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "LocalTxConnectionEventListener removeAssociation, userHandle=" + userHandle + ", this=" + this);
        }
        return associatedHandles.remove(userHandle);
    }

    /**
     * Returns a clone of the whole associatedHandles map and clears the map in the listener.
     * @return The clone of the associatedHandles map.
     */
    public synchronized Map<Object, ResourceHandle> getAssociatedHandlesAndClearMap() {
        logger.log(Level.FINE, "LocalTxConnectionEventListener getAssociatedHandlesAndClearMap, this=" + this);

        // Clone the associatedHandles, because we will clear the list in this method
        IdentityHashMap<Object, ResourceHandle> result = (IdentityHashMap<Object, ResourceHandle>) associatedHandles.clone();

        // Clear the associatedHandles
        associatedHandles.clear();

        return result;
    }
}
