/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.cdi.transaction;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;

import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import static org.glassfish.cdi.transaction.TransactionScopedCDIUtil.DESTORYED_EVENT;
import static org.glassfish.cdi.transaction.TransactionScopedCDIUtil.log;
import static org.glassfish.cdi.transaction.TransactionScopedContextImpl.TRANSACTION_SYNCHRONIZATION_REGISTRY_JNDI_NAME;

/**
 * A wrapper for contextual instances of {@link jakarta.transaction.TransactionScoped} beans. We need this wrapper so
 * that the contextual instance can be destroyed when the transaction completes.
 *
 * @author <a href="mailto:j.j.snyder@oracle.com">JJ Snyder</a>
 */
public class TransactionScopedBean<T> implements Synchronization {
    private T contextualInstance;
    private Contextual<T> contextual;
    private CreationalContext<T> creationalContext;
    private TransactionScopedContextImpl transactionScopedContext;

    public TransactionScopedBean(Contextual<T> contextual, CreationalContext<T> creationalContext, TransactionScopedContextImpl transactionScopedContext) {
        this.contextual = contextual;
        this.creationalContext = creationalContext;
        this.transactionScopedContext = transactionScopedContext;
        contextualInstance = contextual.create(creationalContext);
    }

    public T getContextualInstance() {
        return contextualInstance;
    }

    @Override
    public void beforeCompletion() {
        // empty on purpose
    }

    /**
     * Destroy the contextual instance.
     */
    @Override
    public void afterCompletion(int i) {
        try {
            TransactionSynchronizationRegistry transactionSynchronizationRegistry = getTransactionSynchronizationRegistry();
            // We can't do "getResource" on TransactionSynchronizationRegistry at this stage in completion
            if (transactionSynchronizationRegistry != null) {
                if (transactionScopedContext != null) {
                    // Get list of TransactionScopedBeans for this Transaction
                    Set<TransactionScopedBean<?>> transactionScopedBeanSet =
                        transactionScopedContext.beansPerTransaction.get(transactionSynchronizationRegistry);

                    if (transactionScopedBeanSet != null) {
                        // Remove the current TransactionScopedBean from list as we are destroying it now
                        if (transactionScopedBeanSet.contains(this)) {
                            transactionScopedBeanSet.remove(this);
                        }

                        // If current TransactionScopedBean is last in list, fire destroyed event and remove transaction entry from main Map
                        if (transactionScopedBeanSet.size() == 0) {
                            TransactionScopedCDIUtil.fireEvent(DESTORYED_EVENT);
                            transactionScopedContext.beansPerTransaction.remove(transactionSynchronizationRegistry);
                        }
                        // Not updating entry in main Map with leftover TransactionScopedBeans as it should happen by reference
                    }
                }
            }
        } catch (NamingException ne) {
            log("Can't get instance of TransactionSynchronizationRegistry to process TransactionScoped Destroyed CDI Event!");
            ne.printStackTrace();
        } finally {
            contextual.destroy(contextualInstance, creationalContext);
        }
    }

    private TransactionSynchronizationRegistry getTransactionSynchronizationRegistry() throws NamingException {
        TransactionSynchronizationRegistry transactionSynchronizationRegistry;
        try {
            transactionSynchronizationRegistry = (TransactionSynchronizationRegistry)
                new InitialContext().lookup(TRANSACTION_SYNCHRONIZATION_REGISTRY_JNDI_NAME);
        } catch (NamingException ne) {
            throw ne;
        }

        // Not checking for transaction status, it would be 6, as its in afterCompletion
        return transactionSynchronizationRegistry;
    }
}
