/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.TransactionRequiredException;
import jakarta.transaction.Transactional;
import jakarta.transaction.TransactionalException;

import java.sql.SQLException;
import java.sql.SQLWarning;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * User: paulparkinson Date: 12/10/12 Time: 3:50 PM
 */
public class TransactionalAnnotationTest {

    @Test
    public void testTransactionalInterceptorMANDATORY() throws Exception {
        TransactionalInterceptorMandatory transactionalInterceptorMANDATORY = new TransactionalInterceptorMandatory();
        jakarta.transaction.TransactionManager transactionManager = new TransactionManager();
        TransactionalInterceptorBase.setTestTransactionManager(transactionManager);
        jakarta.interceptor.InvocationContext ctx = new InvocationContext(BeanMandatory.class.getMethod("foo", String.class), null);
        try {
            transactionalInterceptorMANDATORY.transactional(ctx);
            fail("should have thrown TransactionRequiredException due to " + "transactionalInterceptorMANDATORY and no tx in place");
        } catch (TransactionalException transactionalException) {
            assertThat("transactionalException.getCause()", transactionalException.getCause(),
                instanceOf(TransactionRequiredException.class));
        }
        transactionManager.begin();
        transactionalInterceptorMANDATORY.transactional(ctx);
        transactionManager.commit();
        try {
            transactionalInterceptorMANDATORY.transactional(ctx);
            fail("should have thrown TransactionRequiredException due to " + "transactionalInterceptorMANDATORY and no tx in place");
        } catch (TransactionalException transactionalException) {
            assertThat("transactionalException.getCause()", transactionalException.getCause(),
                instanceOf(TransactionRequiredException.class));
        }
    }

    public void testTransactionalInterceptorNEVER() throws Exception {
        TransactionalInterceptorNever transactionalInterceptorNEVER = new TransactionalInterceptorNever();
        jakarta.transaction.TransactionManager transactionManager = new TransactionManager();
        TransactionalInterceptorBase.setTestTransactionManager(transactionManager);
        jakarta.interceptor.InvocationContext ctx = new InvocationContext(BeanNever.class.getMethod("foo", String.class), null);
        transactionalInterceptorNEVER.transactional(ctx);
        transactionManager.begin();
        try {
            transactionalInterceptorNEVER.transactional(ctx);
            fail("should have thrown InvalidTransactionException due to " + "TransactionalInterceptorNEVER and  tx in place");
        } catch (TransactionalException transactionalException) {
            assertThat("transactionalException.getCause()", transactionalException.getCause(),
                instanceOf(InvalidTransactionException.class));
        } finally {
            transactionManager.rollback();
        }
    }

    public void testTransactionalInterceptorNOT_SUPPORTED() throws Exception {
        TransactionalInterceptorNotSupported transactionalInterceptorNOT_SUPPORTED = new TransactionalInterceptorNotSupported();
        jakarta.transaction.TransactionManager transactionManager = new TransactionManager();
        TransactionalInterceptorBase.setTestTransactionManager(transactionManager);
        jakarta.interceptor.InvocationContext ctx = new InvocationContext(BeanNotSupported.class.getMethod("foo", String.class), null);
        transactionalInterceptorNOT_SUPPORTED.transactional(ctx);
    }

    public void testTransactionalInterceptorREQUIRED() throws Exception {
        TransactionalInterceptorRequired transactionalInterceptorREQUIRED = new TransactionalInterceptorRequired();
        jakarta.transaction.TransactionManager transactionManager = new TransactionManager();
        TransactionalInterceptorBase.setTestTransactionManager(transactionManager);
        jakarta.interceptor.InvocationContext ctx = new InvocationContext(BeanRequired.class.getMethod("foo", String.class), null);
        transactionalInterceptorREQUIRED.transactional(ctx);
        transactionManager.begin();
        transactionalInterceptorREQUIRED.transactional(ctx);
        transactionManager.commit();
        //todo equality check
    }

    public void testTransactionalInterceptorREQUIRES_NEW() throws Exception {
        TransactionalInterceptorRequiresNew transactionalInterceptorREQUIRES_NEW = new TransactionalInterceptorRequiresNew();
        jakarta.transaction.TransactionManager transactionManager = new TransactionManager();
        TransactionalInterceptorBase.setTestTransactionManager(transactionManager);
        jakarta.interceptor.InvocationContext ctx = new InvocationContext(BeanRequiresNew.class.getMethod("foo", String.class), null);
        transactionalInterceptorREQUIRES_NEW.transactional(ctx);
        transactionManager.begin();
        transactionalInterceptorREQUIRES_NEW.transactional(ctx);
        transactionManager.commit();
        //todo equality check
    }

    public void testTransactionalInterceptorSUPPORTS() throws Exception {
        TransactionalInterceptorSupports transactionalInterceptorSUPPORTS = new TransactionalInterceptorSupports();
        jakarta.transaction.TransactionManager transactionManager = new TransactionManager();
        TransactionalInterceptorBase.setTestTransactionManager(transactionManager);
        jakarta.interceptor.InvocationContext ctx = new InvocationContext(BeanSupports.class.getMethod("foo", String.class), null);
        transactionalInterceptorSUPPORTS.transactional(ctx);
        transactionManager.begin();
        transactionalInterceptorSUPPORTS.transactional(ctx);
        transactionManager.commit();
    }

    public void testSpecRollbackOnDontRollbackOnSample() throws Exception {
        TransactionalInterceptorRequired transactionalInterceptorREQUIRED = new TransactionalInterceptorRequired();
        jakarta.transaction.TransactionManager transactionManager = new TransactionManager();
        TransactionalInterceptorBase.setTestTransactionManager(transactionManager);
        {
            jakarta.interceptor.InvocationContext ctx = new InvocationContext(
                BeanSpecExampleOfRollbackDontRollback.class.getMethod("throwSQLException"), null) {

                @Override
                public Object getTarget() {
                    return new BeanSpecExampleOfRollbackDontRollback();
                }

                @Override
                public Object proceed() throws Exception {
                    throw new SQLException("test SQLException");
                }
            };
            transactionManager.begin();
            assertThrows(SQLException.class, () -> transactionalInterceptorREQUIRED.transactional(ctx));
            assertThrows(RollbackException.class, transactionManager::commit);
        }
        {
            // Now with a child of SQLException
            InvocationContext ctx = new InvocationContext(
                BeanSpecExampleOfRollbackDontRollback.class.getMethod("throwSQLException"), null) {

                @Override
                public Object getTarget() {
                    return new BeanSpecExampleOfRollbackDontRollback();
                }

                @Override
                public Object proceed() throws Exception {
                    throw new SQLExceptionExtension();
                }
            };
            transactionManager.begin();
            assertThrows(SQLExceptionExtension.class, () -> transactionalInterceptorREQUIRED.transactional(ctx));
            assertThrows(RollbackException.class, transactionManager::commit);
        }
        {

            // now with a child of SQLException but one that is specified as dontRollback
            InvocationContext ctx = new InvocationContext(
                BeanSpecExampleOfRollbackDontRollback.class.getMethod("throwSQLWarning"), null) {

                @Override
                public Object proceed() throws Exception {
                    throw new SQLWarning("test SQLWarning");
                }

                @Override
                public Object getTarget() {
                    return new BeanSpecExampleOfRollbackDontRollback();
                }
            };
            transactionManager.begin();
            assertThrows(SQLWarning.class, () -> transactionalInterceptorREQUIRED.transactional(ctx));
            transactionManager.commit();
        }
        {
            // now with a child of SQLWarning but one that is specified as rollback
            // ie testing this
            // @Transactional(
            //  rollbackOn = {SQLException.class, SQLWarningExtension.class},
            //  dontRollbackOn = {SQLWarning.class})
            //   where dontRollbackOn=SQLWarning overrides rollbackOn=SQLException,
            //   but rollbackOn=SQLWarningExtension overrides dontRollbackOn=SQLWarning
            // ie...
            //        SQLException isAssignableFrom SQLWarning
            //        SQLWarning isAssignableFrom SQLWarningExtensionExtension
            //        SQLWarningExtensionExtension isAssignableFrom SQLWarningExtension
            InvocationContext ctx = new InvocationContext(
                BeanSpecExampleOfRollbackDontRollbackExtension.class.getMethod("throwSQLWarning"), null) {

                @Override
                public Object proceed() throws Exception {
                    throw new SQLWarningExtension();
                }

                @Override
                public Object getTarget() {
                    return new BeanSpecExampleOfRollbackDontRollbackExtension();
                }
            };
            transactionManager.begin();
            assertThrows(SQLWarningExtension.class, () -> transactionalInterceptorREQUIRED.transactional(ctx));
            assertThrows(RollbackException.class, transactionManager::commit);
        }
        {
            // same as above test but with extension just to show continued inheritance...
            InvocationContext ctx = new InvocationContext(
                BeanSpecExampleOfRollbackDontRollbackExtension.class.getMethod("throwSQLWarning"), null) {

                @Override
                public Object proceed() throws Exception {
                    throw new SQLWarningExtensionExtension();
                }

                @Override
                public Object getTarget() {
                    return new BeanSpecExampleOfRollbackDontRollbackExtension();
                }

            };
            transactionManager.begin();
            assertThrows(SQLWarningExtensionExtension.class, () -> transactionalInterceptorREQUIRED.transactional(ctx));
            assertThrows(RollbackException.class, transactionManager::commit);
        }
    }

    class SQLExceptionExtension extends SQLException {
    }

    class SQLWarningExtension extends SQLWarning {
    }

    class SQLWarningExtensionExtension extends SQLWarningExtension {
    }

    @Transactional(rollbackOn = { SQLException.class, SQLWarningExtension.class }, dontRollbackOn = { SQLWarning.class })
    class BeanSpecExampleOfRollbackDontRollbackExtension extends BeanSpecExampleOfRollbackDontRollback {

    }

}
