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

package com.sun.appserv.connectors.internal.spi;

import jakarta.resource.spi.ConnectionEvent;

/**
 * Inteface for resource adapters to signal that the connection being closed is bad.
 * <i>Custom api</i>
 * @author  Jagadish Ramu
 */
public interface BadConnectionEventListener {

    /**
     * Error code used to indicate that the pool is reconfigured
     * and the client can retry to do operations based on
     * based on new configuration.
     * Used for <i>dynamic-resource-reconfiguration<i>
     */
    public static final String POOL_RECONFIGURED_ERROR_CODE = "POOL-RECONFIGURED-1";

    /**
     * Resource adapters will signal that the connection being closed is bad.
     * <i>Custom api</i>
     * @param evt ConnectionEvent
     */
    public void badConnectionClosed(ConnectionEvent evt);

    /**
     * Resource adapters will signal that the connection is being aborted.
     * @param evt ConnectionEvent
     */
    public void connectionAbortOccurred(ConnectionEvent evt);
}
