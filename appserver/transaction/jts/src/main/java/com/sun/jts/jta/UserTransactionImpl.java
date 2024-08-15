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

package com.sun.jts.jta;

import com.sun.logging.LogDomains;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionRolledbackException;

import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.Reference;
/**
 * This class implements the jakarta.transaction.UserTransaction interface
 * which defines methods that allow an application to explicitly manage
 * transaction boundaries.
 *
 * @author Ram Jeyaraman
 * @version 1.0 Feb 09, 1999
 */
public class UserTransactionImpl implements jakarta.transaction.UserTransaction,
    javax.naming.Referenceable, java.io.Serializable {

    // Instance variables

    private transient TransactionManager transactionManager;

    /*
        Logger to log transaction messages
    */
    static Logger _logger = LogDomains.getLogger(UserTransactionImpl.class, LogDomains.TRANSACTION_LOGGER);
    // Constructor

    public UserTransactionImpl() {}

    // Implementation of jakarta.transaction.UserTransaction interface

    /**
     * Create a new transaction and associate it with the current thread.
     *
     * @exception IllegalStateException Thrown if the thread is already
     *    associated with a transaction.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition
     *
     */
    public void begin() throws NotSupportedException, SystemException {
        if (transactionManager == null) init();
        this.transactionManager.begin();
    }

    /**
     * Complete the transaction associated with the current thread. When this
     * method completes, the thread becomes associated with no transaction.
     *
     * @exception TransactionRolledbackException Thrown to indicate that
     *    the transaction has been rolled back rather than committed.
     *
     * @exception HeuristicMixedException Thrown to indicate that a heuristic
     *    decision was made and that some relevant updates have been committed
     *    while others have been rolled back.
     *
     * @exception HeuristicRollbackException Thrown to indicate that a
     *    heuristic decision was made and that all relevant updates have been
     *    rolled back.
     *
     * @exception SecurityException Thrown to indicate that the thread is
     *    not allowed to commit the transaction.
     *
     * @exception IllegalStateException Thrown if the current thread is
     *    not associated with a transaction.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition
    */
    public void commit() throws RollbackException,
    HeuristicMixedException, HeuristicRollbackException, SecurityException,
    IllegalStateException, SystemException {
        if (transactionManager == null) init();
        this.transactionManager.commit();
    }

    /**
     * Roll back the transaction associated with the current thread. When this
     * method completes, the thread becomes associated with no transaction.
     *
     * @exception SecurityException Thrown to indicate that the thread is
     *    not allowed to roll back the transaction.
     *
     * @exception IllegalStateException Thrown if the current thread is
     *    not associated with a transaction.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition
     *
     */
    public void rollback() throws IllegalStateException, SecurityException,
        SystemException {
        if (transactionManager == null) init();
        this.transactionManager.rollback();
    }

    /**
     * Modify the transaction associated with the current thread such that
     * the only possible outcome of the transaction is to roll back the
     * transaction.
     *
     * @exception IllegalStateException Thrown if the current thread is
     *    not associated with a transaction.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition
     *
     */
    public void setRollbackOnly() throws IllegalStateException,
        SystemException {
        if (transactionManager == null) init();
        this.transactionManager.setRollbackOnly();
    }

    /**
     * Obtain the status of the transaction associated with the current thread.
     *
     * @return The transaction status. If no transaction is associated with
     *    the current thread, this method returns the Status.NoTransaction
     *    value.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition
     *
     */
    public int getStatus() throws SystemException {
        if (transactionManager == null) init();
        return this.transactionManager.getStatus();
    }

    /**
     * Modify the timeout value that is associated with transactions started
     * by subsequent invocations of the begin method.
     *
     * <p> If an application has not called this method, the transaction
     * service uses some default value for the transaction timeout.
     *
     * @param seconds The value of the timeout in seconds. If the value is zero,
     *        the transaction service restores the default value. If the value
     *        is negative a SystemException is thrown.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition.
     *
     */
    public void setTransactionTimeout(int seconds) throws SystemException {
        if (transactionManager == null) init();
        this.transactionManager.setTransactionTimeout(seconds);
    }

    // Implementation of the javax.naming.Referenceable interface

    /**
     * This method is used by JNDI to store a referenceable object.
     */
    public Reference getReference() throws NamingException {
        //_logger.log(Level.FINE,"Referenceable object invoked");
        return new Reference(this.getClass().getName(),
            UserTransactionFactory.class.getName(), null);
    }

    // serializable interface related


    private void init() {
        this.transactionManager =
            TransactionManagerImpl.getTransactionManagerImpl();
    }
}

