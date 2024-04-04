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

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnection;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;

import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import org.glassfish.api.invocation.ComponentInvocation;

import static com.sun.logging.LogDomains.RSR_LOGGER;
import static java.util.logging.Level.WARNING;

/**
 * This class is used for lazy enlistment of a resource
 *
 * @author Aditya Gore
 */
public class LazyEnlistableResourceManagerImpl extends ResourceManagerImpl {
    private static final Logger LOG = LogDomains.getLogger(LazyEnlistableResourceManagerImpl.class, RSR_LOGGER);

    @Override
    protected void enlist(JavaEETransactionManager tm, Transaction transaction, ResourceHandle h) {
        // do nothing
    }

    /**
     * Overridden to suspend lazyenlistment.
     *
     * @param handle
     * @throws PoolingException
     */
    @Override
    public void registerResource(ResourceHandle handle) throws PoolingException {
        handle.setEnlistmentSuspended(true);
        super.registerResource(handle);
    }

    /**
     * This is called by the PoolManager (in turn by the LazyEnlistableConnectionManager) when a lazy enlistment is sought.
     *
     * @param managedConnection ManagedConnection
     * @throws ResourceException
     */
    public void lazyEnlist(ManagedConnection managedConnection) throws ResourceException {
        LOG.fine("Entering lazyEnlist");

        JavaEETransactionManager transactionManager = ConnectorRuntime.getRuntime().getTransactionManager();
        Transaction transaction = null;

        try {
            transaction = transactionManager.getTransaction();
            if (transaction == null) {
                LOG.fine(" Transaction null - not enlisting ");
                return;
            }
        } catch (SystemException se) {
            throw new ResourceException(se.getMessage(), se);
        }

        List<? extends ComponentInvocation> invList = ConnectorRuntime.getRuntime().getInvocationManager().getAllInvocations();

        ResourceHandle resourceHandle = null;
        for (int j = invList.size(); j > 0; j--) {
            ComponentInvocation componentInvocation = invList.get(j - 1);
            Object comp = componentInvocation.getInstance();

            List l = transactionManager.getResourceList(comp, componentInvocation);

            ListIterator it = l.listIterator();
            while (it.hasNext()) {
                ResourceHandle hand = (ResourceHandle) it.next();
                ManagedConnection toEnlist = hand.getResource();
                if (managedConnection.equals(toEnlist)) {
                    resourceHandle = hand;
                    break;
                }
            }
        }

        // NOTE: Notice that here we are always assuming that the connection we
        // are trying to enlist was acquired in this component only. This
        // might be inadequate in situations where component A acquires a connection
        // and passes it on to a method of component B, and the lazyEnlist is
        // triggered in B
        // At this point however, we will only support the straight and narrow
        // case where a connection is acquired and then used in the same component.
        // The other case might or might not work
        if (resourceHandle != null && resourceHandle.getResourceState().isUnenlisted()) {
            try {
                // Enable enlistment to be able to enlist the resource.
                resourceHandle.setEnlistmentSuspended(false);
                transactionManager.enlistResource(transaction, resourceHandle);

                // Suspend it back
                resourceHandle.setEnlistmentSuspended(true);
            } catch (Exception e) {
                // In the rare cases where enlistResource throws exception, we
                // should return the resource to the pool
                ConnectorRuntime.getRuntime().getPoolManager().putbackDirectToPool(resourceHandle,
                        resourceHandle.getResourceSpec().getPoolInfo());

                LOG.log(WARNING, "poolmgr.err_enlisting_res_in_getconn", resourceHandle.getResourceSpec().getPoolInfo());
                LOG.fine("rm.enlistResource threw Exception. Returning resource to pool");

                // And rethrow the exception
                throw new ResourceException(e);
            }
        }
    }

}
