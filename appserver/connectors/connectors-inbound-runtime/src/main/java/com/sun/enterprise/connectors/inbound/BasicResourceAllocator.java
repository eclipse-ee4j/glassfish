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

package com.sun.enterprise.connectors.inbound;

import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.XAResourceWrapper;
import com.sun.enterprise.resource.allocator.AbstractConnectorAllocator;
import com.sun.logging.LogDomains;
import com.sun.appserv.connectors.internal.api.PoolingException;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.resource.ResourceException;
import javax.transaction.xa.XAResource;

public final class BasicResourceAllocator extends AbstractConnectorAllocator {

    private static final Logger logger =
            LogDomains.getLogger(BasicResourceAllocator.class, LogDomains.RSR_LOGGER);

    private static final String JMS_RESOURCE_FACTORY = "JMS";

    public BasicResourceAllocator() {
    }

    public ResourceHandle createResource()
            throws PoolingException {
        throw new UnsupportedOperationException();
    }

    public ResourceHandle createResource(XAResource xaResource)
            throws PoolingException {

        ResourceHandle resourceHandle = null;
        ResourceSpec spec =
                new ResourceSpec(JMS_RESOURCE_FACTORY,
                        ResourceSpec.JMS);

        if (xaResource != null) {

            logger.logp(Level.FINEST,
                    "BasicResourceAllocator", "createResource",
                    "NOT NULL", xaResource);

            try {
                resourceHandle = new ResourceHandle(
                        null,  //no object present
                        spec,
                        this, null);

                if (logger.isLoggable(Level.FINEST)) {
                    xaResource = new XAResourceWrapper(xaResource);
                }

                resourceHandle.fillInResourceObjects(null, xaResource);

            } catch (Exception e) {
                throw (PoolingException) (new PoolingException()).initCause(e);
            }
        } else {
            logger.logp(Level.FINEST,
                    "BasicResourceAllocator", "createResource", "NULL");
        }

        return resourceHandle;
    }


    public void closeUserConnection(ResourceHandle resourceHandle)
            throws PoolingException {
        throw new UnsupportedOperationException();
    }


    public boolean matchConnection(ResourceHandle resourceHandle) {
        return false;
    }

    public boolean supportsReauthentication() {
        return false;
    }

    public void cleanup(ResourceHandle resourceHandle)
            throws PoolingException {
        throw new UnsupportedOperationException();
    }

    public Set getInvalidConnections(Set connectionSet) throws ResourceException {
        throw new UnsupportedOperationException();
    }

    public boolean isConnectionValid(ResourceHandle resource) {
        throw new UnsupportedOperationException();
    }

}
