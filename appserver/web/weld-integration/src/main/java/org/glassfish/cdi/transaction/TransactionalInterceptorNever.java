/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.Transactional;
import jakarta.transaction.TransactionalException;

import java.lang.System.Logger;

import static jakarta.interceptor.Interceptor.Priority.PLATFORM_BEFORE;
import static jakarta.transaction.Transactional.TxType.NEVER;
import static java.lang.System.Logger.Level.TRACE;

/**
 * Transactional annotation Interceptor class for Never transaction type, ie
 * jakarta.transaction.Transactional.TxType.NEVER If called outside a transaction context, managed bean method execution
 * will then continue outside a transaction context. If called inside a transaction context, InvalidTransactionException
 * will be thrown
 *
 * @author Paul Parkinson
 */
@Priority(PLATFORM_BEFORE + 200)
@Interceptor
@Transactional(NEVER)
public class TransactionalInterceptorNever extends TransactionalInterceptorBase {
    private static final long serialVersionUID = -7206478787594554608L;
    private static final Logger LOG = System.getLogger(TransactionalInterceptorNever.class.getName());


    @AroundInvoke
    public Object transactional(InvocationContext ctx) throws Exception {
        LOG.log(TRACE, "Processing transactional context of type: {0}", NEVER);
        if (isLifeCycleMethod(ctx)) {
            return proceed(ctx);
        }

        setTransactionalTransactionOperationsManger(true);
        try {
            if (getTransactionManager().getTransaction() != null) {
                throw new TransactionalException(
                    "InvalidTransactionException thrown from TxType.NEVER transactional interceptor.",
                    new InvalidTransactionException("Managed bean with Transactional annotation and TxType of NEVER "
                        + "called inside a transaction context"));
            }
            return proceed(ctx);
        } finally {
            resetTransactionOperationsManager();
        }
    }
}
