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

package com.sun.enterprise.resource.rm;

import java.util.logging.*;

import jakarta.transaction.Transaction;
import jakarta.transaction.SystemException;

import com.sun.logging.*;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.resource.*;

/**
 * SystemResourceManagerImpl manages the resource requests from system
 *
 * @author Binod PG
 */
public class SystemResourceManagerImpl implements ResourceManager {


    private static Logger _logger ;
    static {
        _logger = LogDomains.getLogger(SystemResourceManagerImpl.class, LogDomains.RSR_LOGGER);
    }


    /**
     * Returns the transaction component is participating.
     *
     * @return Handle to the <code>Transaction</code> object.
     * @exception <code>PoolingException<code> If exception is thrown
     *         while getting the transaction.
     */
    public Transaction getTransaction() throws PoolingException {
        try {
            return ConnectorRuntime.getRuntime().getTransaction();
        } catch (Exception ex) {
            _logger.log(Level.SEVERE,"poolmgr.unexpected_exception",ex);
            throw new PoolingException(ex.toString(), ex);
        }
    }

    /**
     * Return null for System Resource.
     */
    public Object getComponent() {
        return null;
    }

    /**
     * Register the <code>ResourceHandle</code> in the transaction
     *
     * @param handle    <code>ResourceHandle</code> object
     * @exception <code>PoolingException</code> If there is any error while
     *        enlisting.
     */
    public void enlistResource(ResourceHandle handle) throws PoolingException{
        try {
            JavaEETransactionManager tm = ConnectorRuntime.getRuntime().getTransactionManager();
            Transaction tran = tm.getTransaction();
        if (tran != null) {
                tm.enlistResource(tran, handle);
            }
    } catch (Exception ex) {
            _logger.log(Level.SEVERE,"poolmgr.unexpected_exception",ex);
            throw new PoolingException(ex.toString(), ex);
        }
    }

    /**
     * Dont do any thing for System Resource.
     */
    public void registerResource(ResourceHandle handle)
         throws PoolingException {
    }

    public void rollBackTransaction() {
        try {
            JavaEETransactionManager tm = ConnectorRuntime.getRuntime().getTransactionManager();
            Transaction tran = tm.getTransaction();
        if ( tran != null ) {
                tran.setRollbackOnly();
        }
        } catch (SystemException ex) {
            _logger.log(Level.WARNING,"poolmgr.system_exception",ex);
        } catch (IllegalStateException ex) {
            // ignore
        }
    }

    /**
     * delist the <code>ResourceHandle</code> from the transaction
     *
     * @param h    <code>ResourceHandle</code> object
     * @param xaresFlag flag indicating transaction success. This can
     *        be XAResource.TMSUCCESS or XAResource.TMFAIL
     * @exception <code>PoolingException</code>
     */
    public void delistResource(ResourceHandle h, int xaresFlag) {
        try {
        JavaEETransactionManager tm = ConnectorRuntime.getRuntime().getTransactionManager();
            Transaction tran = tm.getTransaction();
        if (tran != null) {
                tm.delistResource(tran, h, xaresFlag);
            }
        } catch (SystemException ex) {
            _logger.log(Level.WARNING,"poolmgr.system_exception",ex);
        } catch (IllegalStateException ex) {
            // ignore
        }
    }

    /**
     * Dont do any thing for System Resource.
     */
    public void unregisterResource(ResourceHandle resource,
                                   int xaresFlag) {
    }
}
