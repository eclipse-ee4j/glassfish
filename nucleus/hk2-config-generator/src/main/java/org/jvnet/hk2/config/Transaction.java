/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple transaction mechanism for config-api objects
 *
 * @author Jerome Dochez
 */
public class Transaction {

    final LinkedList<Transactor> participants = new LinkedList<Transactor>();

    /**
     * Enlists a new participant in this transaction
     *
     * @param t new participant to this transaction
     *
     */
    synchronized void addParticipant(Transactor t) {
        // add participants first so the lastly created elements are processed before the parent
        // is modified, this is especially important when sub elements have key attributes the parent
        // need (or the habitat).
        participants.addFirst(t);
    }

    /**
     * Rollbacks all participants to this transaction.
     */
    public synchronized void rollback() {
        for (Transactor t : participants) {
            t.abort(this);
        }
    }

    /**
     * Tests if the transaction participants will pass the prepare phase successfully.
     * The prepare phase will invoke validation of the changes, so a positive return
     * usually means the changes are valid and unless a external factor arise, the
     * {@link #commit()} method will succeed.
     *
     * @return true if the participants changes are valid, false otherwise
     * @throws TransactionFailure if the changes are not valid
     */
    public boolean canCommit() throws TransactionFailure {
        for (Transactor t : participants) {
            if (!t.canCommit(this)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Commits all participants to this transaction
     *
     * @return list of PropertyChangeEvent for the changes that were applied to the
     * participants during the transaction.
     * @throws RetryableException if the transaction cannot commit at this time but
     * could succeed later.
     * @throws TransactionFailure if the transaction commit failed.
     */
    public synchronized List<PropertyChangeEvent> commit()
        throws RetryableException, TransactionFailure {

        if (!canCommit()) {
            throw new RetryableException();
        }
        LinkedList<PropertyChangeEvent> transactionChanges = new LinkedList<PropertyChangeEvent>();
        for (Transactor t : participants) {
            for (PropertyChangeEvent evt : t.commit(this)) {
                // adding the last committed changes last to reverse the list of participants
                // as participants are always added first, we want the events to be consistent
                // with the transactional code meaning FIFO (first element modified/created generates
                // first events).
                transactionChanges.addFirst(evt);
            }
        }
        // any ConfigBean in our transactor should result in sending transaction events, but only once.
        for (Transactor t : participants) {
            if (t instanceof WriteableView) {
                ((WriteableView) t).getMasterView().getHabitat().<Transactions>getService(Transactions.class).addTransaction(transactionChanges);
                break;
            }
        }
        return transactionChanges;
    }

    /**
     * Returns the transaction associated with a writable view
     * @param source the proxy to the writable view
     * @return the transaction object for that view or null if not transaction is in progress
     */
    public static <T extends ConfigBeanProxy> Transaction getTransaction(final T source) {
        Object sourceBean = Proxy.getInvocationHandler(source);
        if (sourceBean instanceof WriteableView) {
            return ((WriteableView) sourceBean).getTransaction();
        }
        return null;
    }

    /**
     * Enroll a configuration object in a transaction and returns a writeable view of it
     *
     * @param source the configured interface implementation
     * @return the new interface implementation providing write access
     * @throws TransactionFailure if the object cannot be enrolled (probably already enrolled in
     * another transaction).
     */
    public <T extends ConfigBeanProxy> T enroll(final T source)
        throws TransactionFailure {
        T configBeanProxy = ConfigSupport.revealProxy(source);
        ConfigView sourceBean = (ConfigView) Proxy.getInvocationHandler(configBeanProxy);
        WriteableView writeableView = ConfigSupport.getWriteableView(configBeanProxy, (ConfigBean) sourceBean.getMasterView());
        if (!writeableView.join(this)) {
            throw new TransactionFailure("Cannot join transaction : " + sourceBean.getProxyType());
        }
        return (T) writeableView.getProxy(sourceBean.getProxyType());
    }

}
