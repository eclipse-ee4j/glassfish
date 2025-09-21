/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.pool.monitor;

import org.glassfish.api.naming.SimpleJndiName;

/**
 * Listen to events related to jdbc/connector connection pool monitoring
 * grouped by applications.
 * The methods invoke the probe providers internally to
 * provide the monitoring related information grouped by applications.
 *
 * @author Shalini M
 */
public class ConnectionPoolAppEmitterImpl {

    private final String appName;
    private final String poolName;
    private final ConnectionPoolAppProbeProvider poolAppProbeProvider;

    public ConnectionPoolAppEmitterImpl(SimpleJndiName poolName, String appName,
        ConnectionPoolAppProbeProvider provider) {
        this.appName = appName;
        this.poolName = poolName.toString();
        this.poolAppProbeProvider = provider;
    }

    public String getPoolName() {
        return this.poolName;
    }

    public String getAppName() {
        return this.appName;
    }

    /**
     * Fires probe event related to the fact the given connection pool has
     * got a connection used event.
     */
    public void connectionUsed() {
        poolAppProbeProvider.connectionUsedEvent(poolName, appName);
    }

    /**
     * Fires probe event related to the fact the given connection pool has
     * got a decrement connection used event.
     *
     */
    public void decrementConnectionUsed() {
        poolAppProbeProvider.decrementConnectionUsedEvent(poolName, appName);
    }

    /**
     * Fires probe event that a connection has been acquired by the application
     * for the given connection pool.
     */
    public void connectionAcquired() {
        poolAppProbeProvider.connectionAcquiredEvent(poolName, appName);
    }

    /**
     * Fires probe event that a connection is released for the given
     * connection pool.
     */
    public void connectionReleased() {
        poolAppProbeProvider.connectionReleasedEvent(poolName, appName);
    }
}
