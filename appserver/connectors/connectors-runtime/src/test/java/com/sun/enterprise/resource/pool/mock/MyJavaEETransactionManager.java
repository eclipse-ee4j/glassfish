/*
 * Copyright (c) 2025 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.pool.mock;

import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.spi.TransactionalResource;

import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;

import java.util.HashMap;
import java.util.Map;

/**
 * We cannot depend on the real JavaEETransactionManagerSimplified implementation due to dependency limitations
 */
public class MyJavaEETransactionManager extends JavaEETransactionManagerMock {

    Map<TransactionalResource, Boolean> delistIsCalled = new HashMap<>();
    private Transaction javaEETransaction;

    public MyJavaEETransactionManager(JavaEETransaction javaEETransaction) {
        this.javaEETransaction = javaEETransaction;
    }

    @Override
    public Transaction getTransaction() throws SystemException {
        // Assuming only 1 transaction used in each unit test, return it
        return javaEETransaction;
    }

    @Override
    public boolean delistResource(Transaction tran, TransactionalResource resource, int flag) throws IllegalStateException, SystemException {
        // Store state for unit test validation
        delistIsCalled.put(resource, Boolean.TRUE);

        // Return delist success
        return true;
    }

    public boolean isDelistIsCalled(TransactionalResource resource) {
        return delistIsCalled.getOrDefault(resource, Boolean.FALSE);
    }
}