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

import jakarta.transaction.Transaction;

/**
 * Interface definition for the Resource Manager. Depending on the ResourceSpec, PoolManager selects appropriate
 * Resource Manager.
 *
 * @author Binod PG
 */
public interface ResourceManager {

    /**
     * Returns the current Transaction, resource should be dealing with.
     *
     * @return An instance of Transaction object.
     * @throws PoolingException If there is any error in getting the transaction
     */
    Transaction getTransaction() throws PoolingException;

    /**
     * Get the component involved in invocation. Returns null , if there is no component is involved in the current
     * invocation.
     *
     * @return object handle
     */
    Object getComponent();

    /**
     * Enlist the Resource handle to the transaction.
     *
     * @param h Resource to be enlisted.
     * @throws PoolingException If there is any error in enlisting.
     */
    void enlistResource(ResourceHandle h) throws PoolingException;

    /**
     * Register the resource for a transaction's house keeping activities.
     *
     * @param handle Resource to be registered.
     * @throws PoolingException If there is any error in registering.
     */
    void registerResource(ResourceHandle handle) throws PoolingException;

    /**
     * Set the transaction for rolling back.
     */
    void rollBackTransaction();

    /**
     * Delist the resource from the transaction.
     *
     * @param resource Resource to be delisted.
     * @param xaresFlag flag indicating transaction success. This can be XAResource.TMSUCCESS or XAResource.TMFAIL
     */
    void delistResource(ResourceHandle resource, int xaresFlag);

    /**
     * Unregister the resource from a transaction's list.
     *
     * @param resource Resource to be unregistered.
     * @param xaresFlag flag indicating transaction success. This can be XAResource.TMSUCCESS or XAResource.TMFAIL
     */
    void unregisterResource(ResourceHandle resource, int xaresFlag);
}
