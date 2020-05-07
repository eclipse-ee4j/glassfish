/*
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
import jakarta.resource.spi.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

/**
 * This class is a ConnectionEventListener for handling the "close" of a
 * ManagedConnection that is not acquired through the appserver's pool.
 * The ManagedConnection is simply destroyed after close is called
 * Such an "unpooled" ManagedConnection is obtained for testConnectionPool
 * and the ConnectorRuntime.getConnection() APIs
 *
 * @author Aditya Gore
 * @since SJSAS 8.1
 */
public class UnpooledConnectionEventListener extends ConnectionEventListener {


    private static Logger _logger = LogDomains.getLogger(UnpooledConnectionEventListener.class,LogDomains.RSR_LOGGER);

    public void connectionClosed(ConnectionEvent evt) {
        ManagedConnection mc = (ManagedConnection) evt.getSource();
        try {
            mc.destroy();
        } catch (Throwable re) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("error while destroying Unpooled Managed Connection");
            }
        }
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("UnpooledConnectionEventListener: Connection closed");
        }
    }

    /**
     * Resource adapters will signal that the connection being closed is bad.
     * @param evt ConnectionEvent
     */
    public void badConnectionClosed(ConnectionEvent evt){
        ManagedConnection mc = (ManagedConnection) evt.getSource();
        mc.removeConnectionEventListener(this);
        connectionClosed(evt);
    }

    public void connectionErrorOccurred(ConnectionEvent evt) {
        //no-op
    }

    public void localTransactionStarted(ConnectionEvent evt) {
            // no-op
    }

    public void localTransactionCommitted(ConnectionEvent evt) {
         // no-op
    }

    public void localTransactionRolledback(ConnectionEvent evt) {
        // no-op
    }

}

