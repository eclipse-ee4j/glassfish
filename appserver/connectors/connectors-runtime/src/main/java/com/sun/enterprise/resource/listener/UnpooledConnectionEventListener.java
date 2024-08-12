/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import com.sun.logging.LogDomains;

import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.ManagedConnection;

import java.util.logging.Logger;

/**
 * This class is a ConnectionEventListener for handling the "close" of a ManagedConnection that is not acquired through
 * the appserver's pool.
 *
 * <p>
 * The ManagedConnection is simply destroyed after close is called. Such an "unpooled"
 * ManagedConnection is obtained for testConnectionPool and the ConnectorRuntime.getConnection() APIs
 *
 * @author Aditya Gore
 * @since SJSAS 8.1
 */
public class UnpooledConnectionEventListener extends ConnectionEventListener {

    private static Logger _logger = LogDomains.getLogger(UnpooledConnectionEventListener.class, LogDomains.RSR_LOGGER);

    @Override
    public void connectionClosed(ConnectionEvent connectionEvent) {
        ManagedConnection managedConnection = (ManagedConnection) connectionEvent.getSource();
        try {
            managedConnection.destroy();
        } catch (Throwable re) {
            _logger.fine("error while destroying Unpooled Managed Connection");
        }
        _logger.fine("UnpooledConnectionEventListener: Connection closed");
    }

    /**
     * Resource adapters will signal that the connection being closed is bad.
     *
     * @param connectionEvent ConnectionEvent
     */
    @Override
    public void badConnectionClosed(ConnectionEvent connectionEvent) {
        ManagedConnection managedConnection = (ManagedConnection) connectionEvent.getSource();
        managedConnection.removeConnectionEventListener(this);
        connectionClosed(connectionEvent);
    }

    @Override
    public void connectionErrorOccurred(ConnectionEvent evt) {
        // no-op
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

}
