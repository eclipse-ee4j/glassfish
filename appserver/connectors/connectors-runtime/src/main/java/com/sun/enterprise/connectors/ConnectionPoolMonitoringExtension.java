/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.resource.pool.monitor.ConnectionPoolAppProbeProvider;
import com.sun.enterprise.resource.pool.monitor.ConnectionPoolProbeProvider;

import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.jvnet.hk2.annotations.Contract;

/**
 * @author Shalini M
 */

@Contract
public interface ConnectionPoolMonitoringExtension {
    void registerPool(PoolInfo poolInfo);

    void unregisterPool(PoolInfo poolInfo);

    /**
     * Register connection pool for Application based monitoring
     * @param appName
     * @return
     */
    ConnectionPoolAppProbeProvider registerConnectionPool(PoolInfo poolInfo,
                                                          String appName);
    void unRegisterConnectionPool();

    ConnectionPoolProbeProvider createProbeProvider();

}
