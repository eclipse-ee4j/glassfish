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

package com.sun.enterprise.resource.rm;

import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.logging.LogDomains;

import jakarta.transaction.Transaction;

import java.util.logging.Logger;

/**
 * Resource Manager for a resource request from a component that is not to be associated with a transaction.
 *
 * @author Aditya Gore
 */
public class NoTxResourceManagerImpl implements ResourceManager {

    private static final Logger LOG = LogDomains.getLogger(NoTxResourceManagerImpl.class, LogDomains.RSR_LOGGER);

    /**
     * Returns null since this connection is outside any tx context
     *
     * @throws PoolingException when unable to get current transaction
     */
    @Override
    public Transaction getTransaction() throws PoolingException {
        return null;
    }

    /**
     * Returns the component invoking resource request.
     *
     * @return Handle to the component
     */
    @Override
    public Object getComponent() {
        return null;
    }

    /**
     * Enlist the <code>ResourceHandle</code> in the transaction This implementation of the method is expected to be a no-op
     *
     * @param h <code>ResourceHandle</code> object
     * @throws PoolingException when unable to enlist resource
     */
    @Override
    public void enlistResource(ResourceHandle h) throws PoolingException {
        LOG.fine("NoTxResourceManagerImpl :: enlistResource called");
    }

    /**
     * Register the <code>ResourceHandle</code> in the transaction This implementation of the method is expected to be a
     * no-op
     *
     * @param handle <code>ResourceHandle</code> object
     * @throws PoolingException when unable to register resource
     */
    @Override
    public void registerResource(ResourceHandle handle) throws PoolingException {
        LOG.fine("NoTxResourceManagerImpl :: registerResource called");
    }

    /**
     * Get's the component's transaction and marks it for rolling back. This implementation of the method is expected to be
     * a no-op
     */
    @Override
    public void rollBackTransaction() {
        LOG.fine("rollBackTransaction called in NoTxResourceManagerImpl");
    }

    /**
     * delist the <code>ResourceHandle</code> from the transaction This implementation of the method is expected to be a
     * no-op
     *
     * @param resource <code>ResourceHandle</code> object
     * @param xaresFlag flag indicating transaction success. This can be XAResource.TMSUCCESS or XAResource.TMFAIL
     */
    @Override
    public void delistResource(ResourceHandle resource, int xaresFlag) {
        LOG.fine("NoTxResourceManagerImpl :: delistResource called");
    }

    /**
     * Unregister the <code>ResourceHandle</code> from the transaction This implementation of the method is expected to be a
     * no-op
     *
     * @param resource <code>ResourceHandle</code> object
     * @param xaresFlag flag indicating transaction success. This can be XAResource.TMSUCCESS or XAResource.TMFAIL
     */
    @Override
    public void unregisterResource(ResourceHandle resource, int xaresFlag) {
        LOG.fine("NoTxResourceManagerImpl :: unregisterResource called");
    }
}
