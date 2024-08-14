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

package com.sun.enterprise.connectors;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.logging.LogDomains;

import jakarta.resource.spi.ConnectionManager;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;

/* Authors : Binod P G, Aditya Gore
 *
 */
public class ConnectionManagerFactory {

    public static ConnectionManager getAvailableConnectionManager(
            PoolInfo poolInfo, boolean forceNoLazyAssoc, ResourceInfo resourceInfo)
            throws ConnectorRuntimeException {

        ConnectorRegistry registry = ConnectorRegistry.getInstance();
        PoolMetaData pmd = registry.getPoolMetaData(poolInfo);
        boolean isLazyEnlist = pmd.isLazyEnlistable();
        boolean isLazyAssoc = pmd.isLazyAssociatable();

        ConnectionManagerImpl mgr = null;

        if (isLazyAssoc && !forceNoLazyAssoc) {
            logFine("Creating LazyAssociatableConnectionManager");
            mgr = new LazyAssociatableConnectionManagerImpl(poolInfo, resourceInfo);
        }else if (isLazyEnlist) {
            logFine("Creating LazyEnlistableConnectionManager");
            mgr = new LazyEnlistableConnectionManagerImpl(poolInfo, resourceInfo);
        } else {
            logFine("Creating plain ConnectionManager");
            mgr = new ConnectionManagerImpl(poolInfo, resourceInfo);
        }
        return mgr;
    }

    private static void logFine(String message) {
        Logger _logger = LogDomains.getLogger(ConnectionManagerFactory.class, LogDomains.RSR_LOGGER);
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine(message);
        }
    }
}
