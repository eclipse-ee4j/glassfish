/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.transaction.xa;

import jakarta.transaction.*;
import javax.transaction.xa.*;

import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.transaction.spi.JavaEETransactionManagerDelegate;
import com.sun.enterprise.transaction.spi.TransactionalResource;
import com.sun.enterprise.transaction.JavaEETransactionManagerSimplified;
import com.sun.enterprise.transaction.JavaEETransactionImpl;

import com.sun.enterprise.util.i18n.StringManager;

import org.jvnet.hk2.annotations.Service;

/**
 ** Implementation of JavaEETransactionManagerDelegate that supports XA
 * transactions without OTS.
 *
 * @author Marina Vatkina
 */
@Service
public class JavaEETransactionManagerXADelegate
            implements JavaEETransactionManagerDelegate {

    private JavaEETransactionManagerSimplified tm;

    // Sting Manager for Localization
    private static StringManager sm
           = StringManager.getManager(JavaEETransactionManagerSimplified.class);

    private boolean lao = true;

    public boolean useLAO() {
         return lao;
    }

    public void setUseLAO(boolean b) {
        lao = b;
    }

    /** XXX Throw an exception if called ??? XXX
     *  it might be a JTS imported global tx or an error
     */
    public void commitDistributedTransaction() throws
            RollbackException, HeuristicMixedException,
            HeuristicRollbackException, SecurityException,
            IllegalStateException, SystemException {}

    /** XXX Throw an exception if called ??? XXX
     *  it might be a JTS imported global tx or an error
     */
    public void rollbackDistributedTransaction() throws IllegalStateException,
            SecurityException, SystemException {}

    public int getStatus() throws SystemException {
        JavaEETransaction tx = tm.getCurrentTransaction();
        if ( tx != null && tx.isLocalTx())
            return tx.getStatus();
        else
            return jakarta.transaction.Status.STATUS_NO_TRANSACTION;
    }

    public Transaction getTransaction()
            throws SystemException {
        return  tm.getCurrentTransaction();
    }

    public boolean enlistDistributedNonXAResource(Transaction tran, TransactionalResource h)
           throws RollbackException, IllegalStateException, SystemException {
        throw new IllegalStateException(sm.getString("enterprise_distributedtx.nonxa_usein_jts"));
    }

    public boolean enlistLAOResource(Transaction tran, TransactionalResource h)
           throws RollbackException, IllegalStateException, SystemException {

        return false;
    }

    public void setRollbackOnlyDistributedTransaction()
            throws IllegalStateException, SystemException {
        /** XXX Throw an exception ??? XXX **/
    }

    public Transaction suspend(JavaEETransaction tx) throws SystemException {
        if ( tx != null )
            tm.setCurrentTransaction(null);
        return tx;
    }

    public void resume(Transaction tx)
        throws InvalidTransactionException, IllegalStateException,
        SystemException {
        /** XXX Throw an exception ??? XXX **/
    }

    public void removeTransaction(Transaction tx) {}

    public int getOrder() {
        return 2;
    }

    public void setTransactionManager(JavaEETransactionManager tm) {
        this.tm = (JavaEETransactionManagerSimplified)tm;
    }
}
