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

package com.sun.appserv.connectors.internal.api;

import jakarta.transaction.Transaction;

import org.jvnet.hk2.annotations.Contract;


/**
 * TransactedPoolManager manages jdbc and connector connection pool
 * @author Jagadish Ramu
 */
@Contract
public interface TransactedPoolManager {

    /**
     * Indicate that a resource is enlisted.
     * @param tran Transaction to which the resource is enlisted
     * @param res Resource that is enlisted
     * @throws IllegalStateException when unable to enlist the resource
     */
    void resourceEnlisted(Transaction tran, ResourceHandle res) throws IllegalStateException;

    /**
     * registers the provided resource with the component & enlists the resource in the transaction
     * @param handle resource-handle
     * @throws PoolingException when unable to register the resource
     */
    void registerResource(ResourceHandle handle) throws PoolingException;

    /**
     * unregisters the resource from the component and delists the resource from the transaction
     * @param resource resource-handle
     * @param xaresFlag
     */
    void unregisterResource(ResourceHandle resource, int xaresFlag);
}

