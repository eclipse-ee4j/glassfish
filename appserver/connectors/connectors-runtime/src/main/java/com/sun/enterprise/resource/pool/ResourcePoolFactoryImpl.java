/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.pool;

import com.sun.appserv.connectors.internal.api.ConnectorConstants.PoolType;
import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.logging.LogDomains;

import java.util.Hashtable;
import java.util.logging.Logger;

import org.glassfish.resourcebase.resources.api.PoolInfo;

import static java.util.logging.Level.FINE;

/**
 * Factory to create appropriate connection pool.
 *
 * @author Aditya Gore
 */
public class ResourcePoolFactoryImpl {

    // Property to take care of switching off connection pooling in ACC
    // since 9.1
    private static final String SWITCH_OFF_ACC_CONNECTION_POOLING = "com.sun.enterprise.connectors.SwitchoffACCConnectionPooling";
    private static String switchOffACCConnectionPoolingProperty = System.getProperty(SWITCH_OFF_ACC_CONNECTION_POOLING);

    private static Logger _logger = LogDomains.getLogger(ResourcePoolFactoryImpl.class, LogDomains.RSR_LOGGER);

    public static ResourcePool newInstance(PoolInfo poolInfo, PoolType poolType, Hashtable env) throws PoolingException {
        if (ConnectorRuntime.getRuntime().isACCRuntime()) {
            if ("TRUE".equalsIgnoreCase(switchOffACCConnectionPoolingProperty)) {
                return new UnpooledResource(poolInfo, env);
            }
        }

        if (poolType == PoolType.POOLING_DISABLED) {
            return new UnpooledResource(poolInfo, env);
        }

        final ResourcePool pool;
        if (poolType == PoolType.ASSOCIATE_WITH_THREAD_POOL) {
            pool = new AssocWithThreadResourcePool(poolInfo, env);
        } else {
            pool = new ConnectionPool(poolInfo, env);
        }

        _logger.log(FINE, "Created a pool of type: {0}", poolType);
        return pool;
    }
}
