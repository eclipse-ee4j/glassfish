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

import static jakarta.transaction.Status.STATUS_ACTIVE;
import static jakarta.transaction.Status.STATUS_COMMITTING;
import static jakarta.transaction.Status.STATUS_MARKED_ROLLBACK;
import static jakarta.transaction.Status.STATUS_PREPARED;
import static jakarta.transaction.Status.STATUS_PREPARING;
import static jakarta.transaction.Status.STATUS_ROLLING_BACK;
import static jakarta.transaction.Status.STATUS_UNKNOWN;
import static java.util.Collections.synchronizedSet;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.transaction.TransactionScoped;
import jakarta.transaction.TransactionSynchronizationRegistry;

/**
 * The Context implementation for obtaining contextual instances of {@link TransactionScoped} beans.
 *
 * <p/>
 * The contextual instances are destroyed when the transaction completes.
 *
 * <p/>
 * Any attempt to call a method on a {@link TransactionScoped} bean when a transaction is not active will result in a
 * {@Link jakarta.enterprise.context.ContextNotActiveException}.
 *
 * <p/>
 * A CDI Event: @Initialized(TransactionScoped.class) is fired with {@link TransactionScopedCDIEventPayload}, when the
 * context is initialized for the first time and @Destroyed(TransactionScoped.class) is fired with
 * {@link TransactionScopedCDIEventPayload}, when the context is destroyed at the end. Currently this payload is empty
 * i.e. it doesn't contain any information.
 *
 * @author <a href="mailto:j.j.snyder@oracle.com">JJ Snyder</a>
 * @author <a href="mailto:arjav.desai@oracle.com">Arjav Desai</a>
 */
public class TransactionScopedContextImpl implements Context {
    public static final String TRANSACTION_SYNCHRONIZATION_REGISTRY_JNDI_NAME = "java:comp/TransactionSynchronizationRegistry";

    ConcurrentHashMap<TransactionSynchronizationRegistry, Set<TransactionScopedBean<?>>> beansPerTransaction;

    public TransactionScopedContextImpl() {
        beansPerTransaction = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<TransactionSynchronizationRegistry, Set<TransactionScopedBean<?>>> getBeansPerTransaction() {
        return beansPerTransaction;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return TransactionScoped.class;
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        TransactionSynchronizationRegistry transactionSynchronizationRegistry = getTransactionSynchronizationRegistry();
        Object beanId = getContextualId(contextual);
        T contextualInstance = getContextualInstance(beanId, transactionSynchronizationRegistry);
        if (contextualInstance == null) {
            contextualInstance = createContextualInstance(contextual, beanId, creationalContext, transactionSynchronizationRegistry);
        }

        return contextualInstance;
    }

    @Override
    public <T> T get(Contextual<T> contextual) {
        return getContextualInstance(getContextualId(contextual), getTransactionSynchronizationRegistry());
    }


    /**
     * Determines if this context object is active.
     *
     * @return true if there is a current global transaction and its status is
     * {@Link jakarta.transaction.Status.STATUS_ACTIVE} false otherwise
     */
    @Override
    public boolean isActive() {
        try {
            //Just calling it but not checking for != null on return value as its already done inside method
            getTransactionSynchronizationRegistry();
            return true;
        } catch (ContextNotActiveException ignore) {
        }

        return false;
    }

    private Object getContextualId(Contextual<?> contextual) {
        if (contextual instanceof PassivationCapable) {
            PassivationCapable passivationCapable = (PassivationCapable) contextual;
            return passivationCapable.getId();
        }

        return contextual;
    }

    private <T> T createContextualInstance(Contextual<T> contextual, Object contextualId, CreationalContext<T> creationalContext, TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
        TransactionScopedBean<T> transactionScopedBean = new TransactionScopedBean<>(contextual, creationalContext, this);
        transactionSynchronizationRegistry.putResource(contextualId, transactionScopedBean);
        transactionSynchronizationRegistry.registerInterposedSynchronization(transactionScopedBean);

        // Adding TransactionScopedBean as Set, per transactionSynchronizationRegistry, which is unique per transaction
        // Setting synchronizedSet so that even is there are multiple transaction for an app its safe
        Set<TransactionScopedBean<?>> transactionScopedBeanSet = beansPerTransaction.get(transactionSynchronizationRegistry);
        if (transactionScopedBeanSet != null) {
            transactionScopedBeanSet = synchronizedSet(transactionScopedBeanSet);
        } else {
            transactionScopedBeanSet = synchronizedSet(new HashSet<>());

            // Fire this event only for the first initialization of context and not for every TransactionScopedBean in a Transaction
            TransactionScopedCDIUtil.fireEvent(TransactionScopedCDIUtil.INITIALIZED_EVENT);

            // Adding transactionScopedBeanSet in Map for the first time for this transactionSynchronizationRegistry key
            beansPerTransaction.put(transactionSynchronizationRegistry, transactionScopedBeanSet);
        }
        transactionScopedBeanSet.add(transactionScopedBean);

        // Not updating entry in main Map with new TransactionScopedBeans as it should happen by reference
        return transactionScopedBean.getContextualInstance();
    }

    private <T> T getContextualInstance(Object beanId, TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
        Object obj = transactionSynchronizationRegistry.getResource(beanId);
        TransactionScopedBean<T> transactionScopedBean = (TransactionScopedBean<T>) obj;
        if (transactionScopedBean != null) {
            return transactionScopedBean.getContextualInstance();
        }

        return null;
    }

    private TransactionSynchronizationRegistry getTransactionSynchronizationRegistry() {
        TransactionSynchronizationRegistry transactionSynchronizationRegistry;
        try {
            transactionSynchronizationRegistry = (TransactionSynchronizationRegistry)
                    new InitialContext().lookup(TRANSACTION_SYNCHRONIZATION_REGISTRY_JNDI_NAME);
        } catch (NamingException ne) {
            throw new ContextNotActiveException("Could not get TransactionSynchronizationRegistry", ne);
        }

        int status = transactionSynchronizationRegistry.getTransactionStatus();
        if (status == STATUS_ACTIVE ||
                status == STATUS_MARKED_ROLLBACK ||
                status == STATUS_PREPARED ||
                status == STATUS_UNKNOWN ||
                status == STATUS_PREPARING ||
                status == STATUS_COMMITTING ||
                status == STATUS_ROLLING_BACK) {

            return transactionSynchronizationRegistry;
        }

        throw new ContextNotActiveException("TransactionSynchronizationRegistry status is not active.");
    }

}
