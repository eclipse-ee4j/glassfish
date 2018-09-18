/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.logging.annotation.LoggerInfo;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionalException;
import java.util.logging.Logger;

/**
 * Transactional annotation Interceptor class for RequiresNew transaction type,
 * ie javax.transaction.Transactional.TxType.REQUIRES_NEW
 * If called outside a transaction context, a new JTA transaction will begin,
 * the managed bean method execution will then continue inside this transaction context,
 * and the transaction will be committed.
 * If called inside a transaction context, the current transaction context will be suspended,
 * a new JTA transaction will begin, the managed bean method execution will then continue
 * inside this transaction context, the transaction will be committed, and the previously
 * suspended transaction will be resumed.
 *
 * @author Paul Parkinson
 */
@javax.annotation.Priority(Interceptor.Priority.PLATFORM_BEFORE + 200)
@Interceptor
@javax.transaction.Transactional(javax.transaction.Transactional.TxType.REQUIRES_NEW)
public class TransactionalInterceptorRequiresNew extends TransactionalInterceptorBase {

    private static final Logger _logger = Logger.getLogger(CDI_JTA_LOGGER_SUBSYSTEM_NAME, SHARED_LOGMESSAGE_RESOURCE);

    @AroundInvoke
    public Object transactional(InvocationContext ctx) throws Exception {
        _logger.log(java.util.logging.Level.INFO, CDI_JTA_REQNEW);
        if (isLifeCycleMethod(ctx)) return proceed(ctx);
        setTransactionalTransactionOperationsManger(false);
        try {
            Transaction suspendedTransaction = null;
            if (getTransactionManager().getTransaction() != null) {
                _logger.log(java.util.logging.Level.INFO, CDI_JTA_MBREQNEW);
                suspendedTransaction = getTransactionManager().suspend();
                //todo catch, wrap in new transactional exception and throw
            }
            try {
                getTransactionManager().begin();
            } catch (Exception exception) {
                String messageString =
                        "Managed bean with Transactional annotation and TxType of REQUIRES_NEW " +
                                "encountered exception during begin " +
                                exception;
                _logger.log(java.util.logging.Level.INFO, CDI_JTA_MBREQNEWBT, exception);
                throw new TransactionalException(messageString, exception);
            }
            Object proceed = null;
            try {
                proceed = proceed(ctx);
            } finally {
                try {
                    // Exception handling for proceed method call above can set TM/TRX as setRollbackOnly
                    if(getTransactionManager().getTransaction().getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                        getTransactionManager().rollback();
                    } else {
                        getTransactionManager().commit();
                    }
                } catch (Exception exception) {
                    String messageString =
                            "Managed bean with Transactional annotation and TxType of REQUIRES_NEW " +
                                    "encountered exception during commit " +
                                    exception;
                    _logger.log(java.util.logging.Level.INFO, CDI_JTA_MBREQNEWCT, exception);
                    throw new TransactionalException(messageString, exception);
                }
                if (suspendedTransaction != null) {
                    try {
                        getTransactionManager().resume(suspendedTransaction);
                    } catch (Exception exception) {
                        String messageString =
                                "Managed bean with Transactional annotation and TxType of REQUIRED " +
                                        "encountered exception during resume " +
                                        exception;
                        _logger.log(java.util.logging.Level.INFO, CDI_JTA_MBREQNEWRT, exception);
                        throw new TransactionalException(messageString, exception);
                    }
                }
            }
            return proceed;

        } finally {
            resetTransactionOperationsManager();
        }
    }
}
