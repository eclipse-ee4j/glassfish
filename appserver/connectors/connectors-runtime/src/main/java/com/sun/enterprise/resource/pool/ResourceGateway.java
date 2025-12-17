/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.logging.LogDomains;

import java.util.logging.Logger;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

/**
 * Resource gateway used to restrict the resource access. eg: based on priority.
 *
 * @author Jagadish Ramu
 */
public class ResourceGateway {

    protected final static Logger _logger = LogDomains.getLogger(ResourceGateway.class, LogDomains.RSR_LOGGER);

    /**
     * indicates whether resource access is allowed or not.
     *
     * @return boolean
     */
    public boolean allowed() {
        return true;
    }

    /**
     * used to indicate the gateway that a resource is acquired.
     */
    public void acquiredResource() {
        // no-op
    }

    public static ResourceGateway getInstance(String className) throws PoolingException {
        if (className == null) {
            return new ResourceGateway();
        }

        return initializeCustomResourceGatewayInPrivilegedMode(className);
    }

    private static ResourceGateway initializeCustomResourceGatewayInPrivilegedMode(final String className) throws PoolingException {
        ResourceGateway gateway = null;

        try {
            gateway = initializeCustomResourceGateway(className);
        } catch (Exception e) {
            _logger.log(WARNING, "pool.resource.gateway.init.failure", className);
            _logger.log(WARNING, "pool.resource.gateway.init.failure", e);
        }

        if (gateway == null) {
            throw new PoolingException("Unable to initalize custom ResourceGateway : " + className);
        }

        return gateway;
    }

    private static ResourceGateway initializeCustomResourceGateway(String className) throws Exception {
        return (ResourceGateway)
            Class.forName(className)
                 .getDeclaredConstructor()
                 .newInstance();
    }

    protected static void debug(String debugStatement) {
        _logger.log(FINE, debugStatement);
    }
}
