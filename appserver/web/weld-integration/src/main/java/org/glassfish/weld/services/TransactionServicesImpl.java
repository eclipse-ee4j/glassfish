/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld.services;

import com.sun.enterprise.transaction.api.JavaEETransactionManager;

import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.hk2.api.ServiceLocator;
import org.jboss.weld.transaction.spi.TransactionServices;

import static jakarta.transaction.Status.STATUS_ACTIVE;
import static jakarta.transaction.Status.STATUS_COMMITTING;
import static jakarta.transaction.Status.STATUS_MARKED_ROLLBACK;
import static jakarta.transaction.Status.STATUS_PREPARED;
import static jakarta.transaction.Status.STATUS_PREPARING;
import static jakarta.transaction.Status.STATUS_ROLLING_BACK;
import static jakarta.transaction.Status.STATUS_UNKNOWN;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT;

public class TransactionServicesImpl implements TransactionServices {

    private final JavaEETransactionManager transactionManager;

    public TransactionServicesImpl(ServiceLocator services) {
        transactionManager = services.getService(JavaEETransactionManager.class);
        if (transactionManager == null) {
            throw new RuntimeException("Unable to retrieve transaction mgr.");
        }
    }

    @Override
    public boolean isTransactionActive() {
        try {
            int curStatus = transactionManager.getStatus();
            if (curStatus == STATUS_ACTIVE ||
                    curStatus == STATUS_MARKED_ROLLBACK ||
                    curStatus == STATUS_PREPARED ||
                    curStatus == STATUS_UNKNOWN ||
                    curStatus == STATUS_PREPARING ||
                    curStatus == STATUS_COMMITTING ||
                    curStatus == STATUS_ROLLING_BACK) {
                return true;
            }
            return false;
        } catch (SystemException e) {
            throw new RuntimeException("Unable to determine transaction status", e);
        }
    }

    @Override
    public void registerSynchronization(Synchronization observer) {
        try {
            transactionManager.registerSynchronization(observer);
        } catch (Exception e) {
            throw new RuntimeException("Unable to register synchronization " + observer + " for current transaction", e);
        }
    }

    @Override
    public UserTransaction getUserTransaction() {
        try {
            return (UserTransaction) new InitialContext().lookup(JNDI_CTX_JAVA_COMPONENT + "UserTransaction");
        } catch (NamingException e) {
            return null;
        }
    }

    @Override
    public void cleanup() {
    }
}
