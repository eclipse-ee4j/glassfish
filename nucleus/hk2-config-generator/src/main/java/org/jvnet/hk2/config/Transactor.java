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
import java.util.List;

/**
 * Any object that want to be part of a configuration transaction
 * should implement this interface.
 *
 * @author Jerome Dochez
 */
public interface Transactor {

    /**
     * Enter a new Transaction, this method should return false if this object
     * is already enlisted in another transaction, or cannot be enlisted with
     * the passed transaction. If the object returns true, the object
     * is enlisted in the passed transaction and cannot be enlisted in another
     * transaction until either commit or abort has been issued.
     *
     * @param t the transaction to enlist with
     * @return true if the enlisting with the passed transaction was accepted,
     * false otherwise
     */
    boolean join(Transaction t);

    /**
     * Returns true of this Transaction can be committed on this object
     *
     * @param t is the transaction to commit, should be the same as the
     * one passed during the join(Transaction t) call.
     *
     * @return true if the transaction committing would be successful
     * @throws TransactionFailure if the changes cannot be validated
     */
    boolean canCommit(Transaction t) throws TransactionFailure;

    /**
     * Commit this Transaction.
     *
     * @param t the transaction commiting.
     * @return list of applied property changes
     * @throws TransactionFailure if the transaction commit failed
     */
    List<PropertyChangeEvent> commit(Transaction t) throws TransactionFailure;

    /**
     * Aborts this Transaction, reverting the state

     * @param t the aborting transaction
     */
    void abort(Transaction t);
}
