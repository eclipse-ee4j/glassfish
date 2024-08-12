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

import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.logging.LogDomains;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        ResourceGateway gateway = null;
        if (className != null) {
            gateway = initializeCustomResourceGatewayInPrivilegedMode(className);
        } else {
            gateway = new ResourceGateway();
        }
        return gateway;
    }

    private static ResourceGateway initializeCustomResourceGatewayInPrivilegedMode(final String className) throws PoolingException {
        Object result = AccessController.doPrivileged(new PrivilegedAction() {
            @Override
            public Object run() {

                Object result = null;
                try {
                    result = initializeCustomResourceGateway(className);
                } catch (Exception e) {
                    _logger.log(Level.WARNING, "pool.resource.gateway.init.failure", className);
                    _logger.log(Level.WARNING, "pool.resource.gateway.init.failure", e);
                }
                return result;
            }
        });
        if (result != null) {
            return (ResourceGateway) result;
        } else {
            throw new PoolingException("Unable to initalize custom ResourceGateway : " + className);
        }
    }

    private static ResourceGateway initializeCustomResourceGateway(String className) throws Exception {
        ResourceGateway gateway;
        Class class1 = Class.forName(className);
        gateway = (ResourceGateway) class1.newInstance();
        return gateway;
    }

    protected static void debug(String debugStatement) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, debugStatement);
        }
    }
}
