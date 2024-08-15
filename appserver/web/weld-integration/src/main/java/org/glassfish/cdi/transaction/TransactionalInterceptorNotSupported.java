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

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.Transaction;
import jakarta.transaction.Transactional;
import jakarta.transaction.TransactionalException;

import java.util.logging.Logger;

import static jakarta.interceptor.Interceptor.Priority.PLATFORM_BEFORE;
import static jakarta.transaction.Transactional.TxType.NOT_SUPPORTED;
import static java.util.logging.Level.INFO;

/**
 * Transactional annotation Interceptor class for NotSupported transaction type, ie
 * jakarta.transaction.Transactional.TxType.NOT_SUPPORTED If called outside a transaction context, managed bean method
 * execution will then continue outside a transaction context. If called inside a transaction context, the current
 * transaction context will be suspended, the managed bean method execution will then continue outside a transaction
 * context, and the previously suspended transaction will be resumed.
 *
 * @author Paul Parkinson
 */
@Priority(PLATFORM_BEFORE + 200)
@Interceptor
@Transactional(NOT_SUPPORTED)
public class TransactionalInterceptorNotSupported extends TransactionalInterceptorBase {

    private static final long serialVersionUID = 2905721637911698354L;
    private static final Logger _logger = Logger.getLogger(CDI_JTA_LOGGER_SUBSYSTEM_NAME, SHARED_LOGMESSAGE_RESOURCE);

    @AroundInvoke
    public Object transactional(InvocationContext ctx) throws Exception {
        _logger.log(INFO, CDI_JTA_NOTSUPPORTED);
        if (isLifeCycleMethod(ctx)) {
            return proceed(ctx);
        }

        setTransactionalTransactionOperationsManger(true);
        try {
            Transaction transaction = null;
            if (getTransactionManager().getTransaction() != null) {
                _logger.log(INFO, CDI_JTA_MBNOTSUPPORTED);
                try {
                    transaction = getTransactionManager().suspend();
                } catch (Exception exception) {
                    _logger.log(INFO, CDI_JTA_MBNOTSUPPORTEDTX, exception);
                    throw new TransactionalException(
                        "Managed bean with Transactional annotation and TxType of NOT_SUPPORTED " +
                        "called inside a transaction context.  Suspending transaction failed due to " + exception,
                        exception);
                }
            }

            Object proceed = null;
            try {
                proceed = proceed(ctx);
            } finally {
                if (transaction != null) {
                    try {
                        getTransactionManager().resume(transaction);
                    } catch (Exception exception) {
                        throw new TransactionalException(
                            "Managed bean with Transactional annotation and TxType of NOT_SUPPORTED " +
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
