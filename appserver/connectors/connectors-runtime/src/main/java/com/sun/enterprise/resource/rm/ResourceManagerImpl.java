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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationException;

import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * Resource Manager for any resource request from a component.
 *
 * @author Binod PG
 */
public class ResourceManagerImpl implements ResourceManager {

    private static final Logger LOG = LogDomains.getLogger(ResourceManagerImpl.class, LogDomains.RSR_LOGGER);

    /**
     * Returns the transaction component is participating.
     *
     * @return Handle to the <code>Transaction</code> object.
     * @throws PoolingException
     */
    @Override
    public Transaction getTransaction() throws PoolingException {
        ComponentInvocation componentInvocation = getCurrentInvocation();
        if (componentInvocation == null) {
            try {
                return ConnectorRuntime.getRuntime().getTransaction();
            } catch (Exception ex) {
                return null;
            }
        }

        return (Transaction) componentInvocation.getTransaction();
    }

    /**
     * Returns the component invoking resource request.
     *
     * @return Handle to the component.
     */
    @Override
    public Object getComponent() {
        ComponentInvocation componentInvocation = getCurrentInvocation();
        if (componentInvocation == null) {
            return null;
        }

        return componentInvocation.getInstance();
    }

    /**
     * Enlist the <code>ResourceHandle</code> in the transaction
     *
     * @param h <code>ResourceHandle</code> object
     * @throws PoolingException
     */
    @Override
    public void enlistResource(ResourceHandle resourceHandle) throws PoolingException {
        registerResource(resourceHandle);
    }

    /**
     * Register the <code>ResourceHandle</code> in the transaction
     *
     * @param handle <code>ResourceHandle</code> object
     * @throws PoolingException
     */
    @Override
    public void registerResource(ResourceHandle handle) throws PoolingException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "ResourceManagerImpl.registerResource START: handle=" + handle + ", resource="
                    + handle.getResource() + ", transactional=" + handle.isTransactional());
        }

        try {
            JavaEETransactionManager transactionManager = getTransactionManager();

            // Enlist if necessary
            if (handle.isTransactional()) {
                ComponentInvocation componentInvocation = getCurrentInvocation();

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "ResourceManagerImpl.registerResource: componentInvocation=" + componentInvocation);
                }

                Transaction transaction = null;
                if (componentInvocation == null) {

                    // Go to the tm and get the transaction
                    // This is mimicking the current behavior of the SystemResourceManagerImpl registerResource
                    // method/ in that, you return the transaction from the TxManager
                    try {
                        transaction = transactionManager.getTransaction();
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "ResourceManagerImpl.registerResource: A transaction=" + transaction);
                        }
                    } catch (Exception e) {
                        transaction = null;
                        LOG.log(INFO, e.getMessage());
                    }
                } else {
                    transaction = (Transaction) componentInvocation.getTransaction();
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "ResourceManagerImpl.registerResource: B transaction=" + transaction);
                    }
                    transactionManager.registerComponentResource(handle);
                }

                if (transaction != null) {
                    try {
                        transactionManager.enlistResource(transaction, handle);
                    } catch (Exception ex) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Exception while trying to enlist resource " + ex.getMessage());
                        }

                        // If transactional, remove the connection handle from the
                        // component's resource list as there has been exception attempting
                        // to enlist the resource
                        if (componentInvocation != null) {
                            LOG.fine("Attempting to unregister component resource");
                            transactionManager.unregisterComponentResource(handle);
                        }

                        throw ex;
                    }
                }
            }

        } catch (Exception ex) {
            LOG.log(SEVERE, "poolmgr.component_register_exception", ex);
            throw new PoolingException(ex.toString(), ex);
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "ResourceManagerImpl.registerResource END: handle=" + handle + ", resource="
                    + handle.getResource() + ", transactional=" + handle.isTransactional());
        }
    }

    // Overridden by the LazyEnlistableResourceManager to be a No-Op
    protected void enlist(JavaEETransactionManager transactionManager, Transaction transaction, ResourceHandle resourceHandle) throws PoolingException {
        try {
            transactionManager.enlistResource(transaction, resourceHandle);
        } catch (Exception e) {
            throw new PoolingException(e.getMessage(), e);
        }
    }

    /**
     * Get's the component's transaction and marks it for rolling back.
     */
    @Override
    public void rollBackTransaction() {
        JavaEETransactionManager transactionManager = getTransactionManager();
        Transaction transaction = null;
        try {
            ComponentInvocation componentInvocation = getCurrentInvocation();
            if (componentInvocation == null) {

                // Go to the transactionManager and get the transaction.
                // This is mimicking the current behavior of
                // the SystemResourceManagerImpl registerResource method
                // in that, you return the transaction from the TxManager

                try {
                    transaction = transactionManager.getTransaction();
                } catch (Exception e) {
                    transaction = null;
                    LOG.log(INFO, e.getMessage());
                }

            } else {
                transaction = (Transaction) componentInvocation.getTransaction();
            }

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
     * @param resource <code>ResourceHandle</code> object
     * @param xaresFlag flag indicating transaction success. This can be XAResource.TMSUCCESS or XAResource.TMFAIL
     * @exception <code>PoolingException</code>
     */
    @Override
    public void delistResource(ResourceHandle resource, int xaresFlag) {
        unregisterResource(resource, xaresFlag);
    }

    /**
     * Unregister the <code>ResourceHandle</code> from the transaction TODO: document what the resource state should be:
     * enlisted? busy? TODO: move documentation to the interface
     *
     * @param resource <code>ResourceHandle</code> object
     * @param xaresFlag flag indicating transaction success. This can be XAResource.TMSUCCESS or XAResource.TMFAIL
     * @throws PoolingException
     */
    @Override
    public void unregisterResource(ResourceHandle resource, int xaresFlag) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE,
                    "ResourceManagerImpl.unregisterResource START: handle=" + resource + ", resource="
                            + resource.getResource()
                            + ", transactional=" + resource.isTransactional());
        }
        JavaEETransactionManager transactionManager = getTransactionManager();

        Transaction transaction = null;

        try {
            // Delist with TMSUCCESS if necessary
            if (resource.isTransactional()) {

                ComponentInvocation componentInvocation = getCurrentInvocation();
                if (componentInvocation == null) {

                    // Go to the transactionManager and get the transaction
                    // This is mimicking the current behavior of
                    // the SystemResourceManagerImpl registerResource method
                    // in that, you return the transaction from the TxManager
                    try {
                        transaction = transactionManager.getTransaction();
                    } catch (Exception e) {
                        transaction = null;
                        LOG.log(INFO, e.getMessage());
                    }
                } else {
                    transaction = (Transaction) componentInvocation.getTransaction();
                    transactionManager.unregisterComponentResource(resource);
                }

                if (transaction != null && resource.isEnlisted()) {
                    // TODO: delistResource seems to return always true, or throws an exception
                    // which is ok, but return type could be removed! Since it is not used here to see
                    // if something went wrong.
                    transactionManager.delistResource(transaction, resource, xaresFlag);
                }
            }
        } catch (SystemException ex) {
            LOG.log(WARNING, "poolmgr.system_exception", ex);
        } catch (IllegalStateException ex) {
            // Transaction aborted. Do nothing
            LOG.log(FINEST, "Ignoring IllegalStateException.", ex);
        } catch (InvocationException ex) {
            LOG.log(FINEST, "Ignoring InvocationException.", ex);
            // UnregisterResource is called outside of component context
            // likely to be container-forced destroy. Do nothing
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "ResourceManagerImpl.unregisterResource END: handle=" + resource + ", resource="
                    + resource.getResource()
                    + ", transactional=" + resource.isTransactional());
        }
    }

    private static ComponentInvocation getCurrentInvocation() {
        return ConnectorRuntime.getRuntime().getInvocationManager().getCurrentInvocation();
    }

    private static JavaEETransactionManager getTransactionManager() {
        return ConnectorRuntime.getRuntime().getTransactionManager();
    }
}
