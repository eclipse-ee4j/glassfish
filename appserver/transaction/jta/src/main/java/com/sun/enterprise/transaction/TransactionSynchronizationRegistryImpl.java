/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.transaction;

import com.sun.enterprise.util.i18n.StringManager;

import jakarta.inject.Inject;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;

import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

import static jakarta.transaction.Status.STATUS_MARKED_ROLLBACK;
import static jakarta.transaction.Status.STATUS_NO_TRANSACTION;
import static jakarta.transaction.Status.STATUS_ROLLING_BACK;

@Service
@ContractsProvided({ TransactionSynchronizationRegistryImpl.class, TransactionSynchronizationRegistry.class }) // Needed because we can't change spec provided class
public class TransactionSynchronizationRegistryImpl implements TransactionSynchronizationRegistry {

    @Inject
    private transient TransactionManager transactionManager;

    private static StringManager sm = StringManager.getManager(TransactionSynchronizationRegistryImpl.class);

    public TransactionSynchronizationRegistryImpl() {
    }

    public TransactionSynchronizationRegistryImpl(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * Return an opaque object to represent the transaction bound to the current thread at the time this method is called.
     * The returned object overrides <code>hashCode</code> and <code>equals</code> methods to allow its use as the key in a
     * <code>java.util.HashMap</code> for use by the caller. If there is no transaction currently active, null is returned.
     *
     * <P>
     * The returned object will return the same <code>hashCode</code> and compare equal to all other objects returned by
     * calling this method from any component executing in the same transaction context in the same application server.
     *
     * <P>
     * The <code>toString</code> method returns a <code>String</code> that might be usable by a human reader to usefully
     * understand the transaction context. The result of the <code>toString</code> method is otherwise not defined.
     * Specifically, there is no forward or backward compatibility guarantee for the result returned by the
     * <code>toString</code> method.
     *
     * <P>
     * The object is not necessarily serializable, and is not useful outside the virtual machine from which it was obtained.
     *
     * @return An object representing the current transaction, or null if no transaction is active.
     */
    @Override
    public Object getTransactionKey() {
        try {
            return transactionManager.getTransaction();
        } catch (SystemException ex) {
            return null;
        }
    }

    /**
     * Add an object to the map of resources being managed for the current transaction. The supplied key must be of a
     * caller- defined class so as not to conflict with other users. The class of the key must guarantee that the
     * <code>hashCode</code> and <code>equals</code> methods are suitable for keys in a map. The key and value are not
     * examined or used by the implementation.
     *
     * @param key The key for looking up the associated value object.
     *
     * @param value The value object to keep track of.
     *
     * @exception IllegalStateException Thrown if the current thread is not associated with a transaction.
     */
    @Override
    public void putResource(Object key, Object value) {
        try {
            getTransaction().putUserResource(key, value);
        } catch (SystemException ex) {
            throw new IllegalStateException(sm.getString("enterprise_distributedtx.no_transaction"));
        }
    }

    /**
     * Get an object from the map of resources being managed for the current transaction. The key must have been supplied
     * earlier by a call to <code>putResouce</code> in the same transaction. If the key cannot be found in the current
     * resource map, null is returned.
     *
     * @param key The key for looking up the associated value object.
     *
     * @return The value object, or null if not found.
     *
     * @exception IllegalStateException Thrown if the current thread is not associated with a transaction.
     */
    @Override
    public Object getResource(Object key) {
        try {
            return getTransaction().getUserResource(key);
        } catch (SystemException ex) {
            throw new IllegalStateException(sm.getString("enterprise_distributedtx.no_transaction"));
        }
    }

    /**
     * Register a <code>Synchronization</code> instance with special ordering semantics. The <code>beforeCompletion</code>
     * method on the registered <code>Synchronization</code> will be called after all user and system component
     * <code>beforeCompletion</code> callbacks, but before the 2-phase commit process starts. This allows user and system
     * components to flush state changes to the caching manager, during their <code>SessionSynchronization</code> callbacks,
     * and allows managers to flush state changes to Connectors, during the callbacks registered with this method.
     * Similarly, the <code>afterCompletion</code> callback will be called after 2-phase commit completes but before any
     * user and system <code>afterCompletion</code> callbacks.
     *
     * <P>
     * The <code>beforeCompletion</code> callback will be invoked in the transaction context of the current transaction
     * bound to the thread of the caller of this method, which is the same transaction context active at the time this
     * method is called. Allowable methods include access to resources, for example, Connectors. No access is allowed to
     * user components, for example, timer services or bean methods, as these might change the state of POJOs, or plain old
     * Java objects, being managed by the caching manager.
     *
     * <P>
     * The <code>afterCompletion</code> callback will be invoked in an undefined transaction context. No access is permitted
     * to resources or user components as defined above. Resources can be closed, but no transactional work can be performed
     * with them.
     *
     * <P>
     * Other than the transaction context, no component J2EE context is active during either of the callbacks.
     *
     * @param sync The synchronization callback object.
     *
     * @exception IllegalStateException Thrown if the current thread is not associated with a transaction.
     */
    @Override
    public void registerInterposedSynchronization(Synchronization sync) {
        try {
            getTransaction().registerInterposedSynchronization(sync);
        } catch (SystemException ex) {
            throw new IllegalStateException(sm.getString("enterprise_distributedtx.no_transaction"));
        } catch (RollbackException ex) {
            throw new IllegalStateException(sm.getString("enterprise_distributedtx.mark_rollback"));
        }
    }

    /**
     * Returns the status of the transaction bound to the current thread. This is the result of executing
     * <code>getStatus</code> method on the <code>TransactionManager</code>, in the current transaction context.
     *
     * @return The status of the current transaction.
     */
    @Override
    public int getTransactionStatus() {
        try {
            return transactionManager.getStatus();
        } catch (SystemException ex) {
            return STATUS_NO_TRANSACTION;
        }
    }

    /**
     * Set the <code>rollbackOnly</code> status of the transaction bound to the current thread.
     *
     * @exception IllegalStateException Thrown if the current thread is not associated with a transaction.
     */
    @Override
    public void setRollbackOnly() {
        try {
            transactionManager.setRollbackOnly();
        } catch (SystemException ex) {
            throw new IllegalStateException(sm.getString("enterprise_distributedtx.no_transaction"));
        }
    }

    /**
     * Get the <code>rollbackOnly</code> status of the transaction bound to the current thread.
     *
     * @return true, if the current transaction is marked for rollback only.
     *
     * @exception IllegalStateException Thrown if the current thread is not associated with a transaction.
     */
    @Override
    public boolean getRollbackOnly() {
        int status = getTransactionStatus();
        if (status == STATUS_NO_TRANSACTION) {
            throw new IllegalStateException(sm.getString("enterprise_distributedtx.no_transaction"));
        }

        if (status == STATUS_MARKED_ROLLBACK || status == STATUS_ROLLING_BACK) {
            return true;
        }

        return false;
    }

    private JavaEETransactionImpl getTransaction() throws SystemException {
        JavaEETransactionImpl transaction = (JavaEETransactionImpl) transactionManager.getTransaction();
        if (transaction == null) {
            throw new IllegalStateException(sm.getString("enterprise_distributedtx.no_transaction"));
        }

        return transaction;
    }
}
