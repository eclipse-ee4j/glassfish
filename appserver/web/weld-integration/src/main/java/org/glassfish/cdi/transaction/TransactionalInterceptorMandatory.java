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
import jakarta.transaction.TransactionRequiredException;
import jakarta.transaction.Transactional;
import jakarta.transaction.TransactionalException;

import java.util.logging.Logger;

import static jakarta.interceptor.Interceptor.Priority.PLATFORM_BEFORE;
import static jakarta.transaction.Transactional.TxType.MANDATORY;
import static java.util.logging.Level.INFO;

/**
 * Transactional annotation Interceptor class for Mandatory transaction type, ie
 * jakarta.transaction.Transactional.TxType.MANDATORY If called outside a transaction context,
 * TransactionRequiredException will be thrown If called inside a transaction context, managed bean method execution
 * will then continue under that context.
 *
 * @author Paul Parkinson
 */
@Priority(PLATFORM_BEFORE + 200)
@Interceptor()
@Transactional(MANDATORY)
public class TransactionalInterceptorMandatory extends TransactionalInterceptorBase {

    private static final long serialVersionUID = 884559632546224653L;
    private static final Logger _logger = Logger.getLogger(CDI_JTA_LOGGER_SUBSYSTEM_NAME, SHARED_LOGMESSAGE_RESOURCE);

    @AroundInvoke
    public Object transactional(InvocationContext ctx) throws Exception {
        _logger.log(INFO, CDI_JTA_MANDATORY);
        if (isLifeCycleMethod(ctx)) {
            return proceed(ctx);
        }

        setTransactionalTransactionOperationsManger(false);
        try {
            if (getTransactionManager().getTransaction() == null) {
                throw new TransactionalException("TransactionRequiredException thrown from TxType.MANDATORY transactional interceptor.",
                        new TransactionRequiredException("Managed bean with Transactional annotation and TxType of "
                                + "MANDATORY called outside of a transaction context"));
            }
            return proceed(ctx);
        } finally {
            resetTransactionOperationsManager();
        }
    }

}
