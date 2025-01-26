/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.resource.pool;

import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceState;
import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.logging.LogDomains;

import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.resourcebase.resources.api.PoolInfo;

/**
 * Transaction helper for the pool to check various states of a resource that is taking part in the transaction.
 *
 * @author Jagadish Ramu
 */
public class PoolTxHelper {

    private PoolInfo poolInfo;

    protected final static Logger _logger = LogDomains.getLogger(PoolTxHelper.class, LogDomains.RSR_LOGGER);

    public PoolTxHelper(PoolInfo poolInfo) {
        this.poolInfo = poolInfo;
    }

    /**
     * Check whether the local resource can be put back to pool If true, unenlist the resource
     *
     * @param h ResourceHandle to be verified
     * @return true if the resource handle is eligible for reuse, otherwise false. NOTE in case of true, this method alters
     * the handle state to enlisted=false
     */
    public boolean isLocalResourceEligibleForReuse(ResourceHandle h) {
        boolean result = false;
        if ((!isLocalResourceInTransaction(h))) {
            try {
                enforceDelistment(h);
            } catch (SystemException se) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE,
                            "Exception while delisting the local resource [ of pool : " + poolInfo + " ] " + "forcibily from transaction",
                            se);
                }
                return result;
            }

            h.getResourceState().setEnlisted(false);
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Pool: isLocalResourceEligibleForReuse, eligible=true, enlisted changed to true for handle=" + h);
            }
            result = true;
        }

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Pool: isLocalResourceEligibleForReuse, eligible=false, handle=" + h);
        }
        return result;
    }

    /**
     * Remove the resource from book-keeping
     *
     * @param h ResourceHandle to be delisted
     * @throws jakarta.transaction.SystemException when not able to delist the resource
     */
    private void enforceDelistment(ResourceHandle h) throws SystemException {
        JavaEETransaction txn = (JavaEETransaction) ConnectorRuntime.getRuntime().getTransaction();
        if (txn != null) {
            Set set = txn.getResources(poolInfo);
            if (set != null) {
                set.remove(h);
            }
        }
    }

    /**
     * Check whether a local transaction is in progress.
     *
     * @return true if a local transaction is in progress.
     */
    public boolean isLocalTransactionInProgress() {
        boolean result = false;
        try {
            JavaEETransaction txn = (JavaEETransaction) ConnectorRuntime.getRuntime().getTransaction();
            if (txn != null) {
                result = txn.isLocalTx();
            }
        } catch (SystemException e) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE,
                        "Exception while checking whether a local " + "transaction is in progress while using pool : " + poolInfo, e);
            }
        }
        return result;
    }

    /**
     * Check whether the local resource in question is the one participating in transaction.
     *
     * @param h ResourceHandle
     * @return true if the resource is participating in the transaction
     */
    public boolean isLocalResourceInTransaction(ResourceHandle h) {
        boolean result = true;
        try {
            JavaEETransaction txn = (JavaEETransaction) ConnectorRuntime.getRuntime().getTransaction();
            if (txn != null) {
                result = isNonXAResourceInTransaction(txn, h);
            }
        } catch (SystemException e) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Exception while checking whether the resource [ of pool : " + poolInfo + " ] "
                        + "is nonxa and is enlisted in transaction : ", e);
            }
        }
        return result;
    }

    /**
     * Check whether the resource is non-xa
     *
     * @param resource Resource to be verified
     * @return boolean indicating whether the resource is non-xa
     */
    public boolean isNonXAResource(ResourceHandle resource) {
        return !resource.getResourceSpec().isXA();
    }

    /**
     * Check whether the non-xa resource is enlisted in transaction.
     *
     * @param tran Transaction
     * @param resource Resource to be verified
     * @return boolean indicating whether thegiven non-xa resource is in transaction
     */
    private boolean isNonXAResourceInTransaction(JavaEETransaction tran, ResourceHandle resource) {
        return resource.equals(tran.getNonXAResource());
    }

    /**
     * Check whether the resource is non-xa, free and is enlisted in transaction.
     *
     * @param tran Transaction
     * @param resource Resource to be verified
     * @return boolean indicating whether the resource is free, non-xa and is enlisted in transaction
     */
    public boolean isNonXAResourceAndFree(JavaEETransaction tran, ResourceHandle resource) {
        return resource.getResourceState().isFree() && isNonXAResource(resource) && isNonXAResourceInTransaction(tran, resource);
    }

    /**
     * this method is called when a resource is enlisted in transation tran
     *
     * @param tran Transaction to which the resource need to be enlisted
     * @param resource Resource to be enlisted in the transaction
     */
    public void resourceEnlisted(Transaction tran, ResourceHandle resource) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Pool: resourceEnlisted START, tran=" + tran + ", resource=" + resource + ", poolInfo=" + poolInfo);
        }
        try {
            JavaEETransaction j2eetran = (JavaEETransaction) tran;
            Set set = j2eetran.getResources(poolInfo);
            if (set == null) {
                set = new HashSet();
                j2eetran.setResources(set, poolInfo);
            }
            set.add(resource);
        } catch (ClassCastException e) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Pool [ " + poolInfo + " ]: resourceEnlisted: " + "transaction is not J2EETransaction but a "
                        + tran.getClass().getName(), e);
            }
        }
        ResourceState state = resource.getResourceState();
        state.setEnlisted(true);
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Pool: resourceEnlisted END, tran=" + tran + ", resource=" + resource);
        }
    }

    /**
     * this method is called when transaction tran is completed
     *
     * @param tran transaction which has completed
     * @param status transaction status
     * @param poolInfo Pool name
     * @return delisted resources
     */
    public List<ResourceHandle> transactionCompleted(Transaction tran, int status, PoolInfo poolInfo) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Pool: transactionCompleted START, tran= " + tran + ", poolInfo=" + poolInfo);
        }

        JavaEETransaction j2eetran;
        List<ResourceHandle> delistedResources = new ArrayList<>();
        try {
            j2eetran = (JavaEETransaction) tran;
        } catch (ClassCastException e) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE,
                        "Pool: transactionCompleted: " + "transaction is not J2EETransaction but a " + tran.getClass().getName(), e);
            }
            return delistedResources;
        }
        Set set = j2eetran.getResources(poolInfo);

        if (set == null) {
            return delistedResources;
        }

        Iterator iter = set.iterator();
        while (iter.hasNext()) {
            ResourceHandle resource = (ResourceHandle) iter.next();
            ResourceState state = resource.getResourceState();
            state.setEnlisted(false);
            delistedResources.add(resource);
            iter.remove();
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Pool: transactionCompleted: " + resource);
            }
        }

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Pool: transactionCompleted END, tran= " + tran);
        }
        return delistedResources;
    }
}
