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

import com.sun.enterprise.transaction.TransactionManagerHelper;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.TransactionalException;

import java.util.logging.Logger;

import static jakarta.interceptor.Interceptor.Priority.PLATFORM_BEFORE;
import static jakarta.transaction.Status.STATUS_MARKED_ROLLBACK;
import static jakarta.transaction.Transactional.TxType.REQUIRES_NEW;
import static java.util.logging.Level.INFO;

/**
 * Transactional annotation Interceptor class for RequiresNew transaction type, ie
 * jakarta.transaction.Transactional.TxType.REQUIRES_NEW If called outside a transaction context, a new JTA transaction
 * will begin, the managed bean method execution will then continue inside this transaction context, and the transaction
 * will be committed. If called inside a transaction context, the current transaction context will be suspended, a new
 * JTA transaction will begin, the managed bean method execution will then continue inside this transaction context, the
 * transaction will be committed, and the previously suspended transaction will be resumed.
 *
 * @author Paul Parkinson
 */
@Priority(PLATFORM_BEFORE + 200)
@Interceptor
@Transactional(REQUIRES_NEW)
public class TransactionalInterceptorRequiresNew extends TransactionalInterceptorBase {

    private static final long serialVersionUID = -3843074402046047130L;
    private static final Logger _logger = Logger.getLogger(CDI_JTA_LOGGER_SUBSYSTEM_NAME, SHARED_LOGMESSAGE_RESOURCE);

    @AroundInvoke
    public Object transactional(InvocationContext ctx) throws Exception {
        _logger.log(INFO, CDI_JTA_REQNEW);
        if (isLifeCycleMethod(ctx)) {
            return proceed(ctx);
        }

        setTransactionalTransactionOperationsManger(false);

        try {
            Transaction suspendedTransaction = null;
            if (getTransactionManager().getTransaction() != null) {
                _logger.log(INFO, CDI_JTA_MBREQNEW);
                suspendedTransaction = getTransactionManager().suspend();
                //todo catch, wrap in new transactional exception and throw
            }
            try {
                getTransactionManager().begin();
                TransactionManager tm = getTransactionManager();
                if(tm instanceof TransactionManagerHelper){
                    ((TransactionManagerHelper)tm).preInvokeTx(true);
                }
            } catch (Exception exception) {
                _logger.log(INFO, CDI_JTA_MBREQNEWBT, exception);
                throw new TransactionalException(
                    "Managed bean with Transactional annotation and TxType of REQUIRES_NEW " +
                    "encountered exception during begin " + exception,
                    exception);
            }

            Object proceed = null;
            try {
                proceed = proceed(ctx);
            } finally {
                try {
                    TransactionManager tm = getTransactionManager();
                    if(tm instanceof TransactionManagerHelper){
                        ((TransactionManagerHelper)tm).postInvokeTx(false, true);
                    }
                    // Exception handling for proceed method call above can set TM/TRX as setRollbackOnly
                    if (getTransactionManager().getTransaction().getStatus() == STATUS_MARKED_ROLLBACK) {
                        getTransactionManager().rollback();
                    } else {
                        getTransactionManager().commit();
                    }
                } catch (Exception exception) {
                    _logger.log(INFO, CDI_JTA_MBREQNEWCT, exception);
                    throw new TransactionalException(
                        "Managed bean with Transactional annotation and TxType of REQUIRES_NEW " +
                        "encountered exception during commit " + exception,
                        exception);
                }

                if (suspendedTransaction != null) {
                    try {
                        getTransactionManager().resume(suspendedTransaction);
                    } catch (Exception exception) {
                        _logger.log(INFO, CDI_JTA_MBREQNEWRT, exception);
                        throw new TransactionalException(
                            "Managed bean with Transactional annotation and TxType of REQUIRED " +
                            "encountered exception during resume " + exception,
                            exception);
                    }
                }
            }

            return proceed;

        } finally {
            resetTransactionOperationsManager();
        }
    }
}
