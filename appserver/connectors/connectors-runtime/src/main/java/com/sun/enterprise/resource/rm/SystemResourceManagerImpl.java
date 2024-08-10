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
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.logging.LogDomains;

import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;

import java.util.logging.Logger;

import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * SystemResourceManagerImpl manages the resource requests from system
 *
 * @author Binod PG
 */
public class SystemResourceManagerImpl implements ResourceManager {

    private static final Logger LOG = LogDomains.getLogger(SystemResourceManagerImpl.class, LogDomains.RSR_LOGGER);

    /**
     * Returns the transaction component is participating.
     *
     * @return Handle to the <code>Transaction</code> object.
     * @throws PoolingException If exception is thrown while getting the transaction.
     */
    @Override
    public Transaction getTransaction() throws PoolingException {
        try {
            return ConnectorRuntime.getRuntime().getTransaction();
        } catch (Exception ex) {
            LOG.log(SEVERE, "poolmgr.unexpected_exception", ex);
            throw new PoolingException(ex.toString(), ex);
        }
    }

    /**
     * Return null for System Resource.
     */
    @Override
    public Object getComponent() {
        return null;
    }

    /**
     * Register the <code>ResourceHandle</code> in the transaction
     *
     * @param handle <code>ResourceHandle</code> object
     * @throws PoolingException If there is any error while enlisting.
     */
    @Override
    public void enlistResource(ResourceHandle handle) throws PoolingException {
        try {
            JavaEETransactionManager transactionManager = getTransactionManager();
            Transaction transaction = transactionManager.getTransaction();
            if (transaction != null) {
                transactionManager.enlistResource(transaction, handle);
            }
        } catch (Exception ex) {
            LOG.log(SEVERE, "poolmgr.unexpected_exception", ex);
            throw new PoolingException(ex.toString(), ex);
        }
    }

    /**
     * Dont do any thing for System Resource.
     */
    @Override
    public void registerResource(ResourceHandle handle) throws PoolingException {
    }

    @Override
    public void rollBackTransaction() {
        try {
            Transaction transaction = getCurrentTransaction();
            if (transaction != null) {
                transaction.setRollbackOnly();
            }
        } catch (SystemException ex) {
            LOG.log(WARNING, "poolmgr.system_exception", ex);
        } catch (IllegalStateException ex) {
            LOG.log(FINEST, "Ignoring IllegalStateException.", ex);
        }
    }

    /**
     * delist the <code>ResourceHandle</code> from the transaction
     *
     * @param h <code>ResourceHandle</code> object
     * @param xaresFlag flag indicating transaction success. This can be XAResource.TMSUCCESS or XAResource.TMFAIL
     */
    @Override
    public void delistResource(ResourceHandle h, int xaresFlag) {
        try {
            JavaEETransactionManager transactionManager = getTransactionManager();
            Transaction transaction = transactionManager.getTransaction();
            if (transaction != null) {
                transactionManager.delistResource(transaction, h, xaresFlag);
            }
        } catch (SystemException ex) {
            LOG.log(WARNING, "poolmgr.system_exception", ex);
        } catch (IllegalStateException ex) {
            LOG.log(FINEST, "Ignoring IllegalStateException.", ex);
        }
    }

    /**
     * Dont do any thing for System Resource.
     */
    @Override
    public void unregisterResource(ResourceHandle resource, int xaresFlag) {
    }

    private static JavaEETransactionManager getTransactionManager() {
        return ConnectorRuntime.getRuntime().getTransactionManager();
    }

    private static Transaction getCurrentTransaction() throws SystemException {
        return getTransactionManager().getTransaction();
    }
}
