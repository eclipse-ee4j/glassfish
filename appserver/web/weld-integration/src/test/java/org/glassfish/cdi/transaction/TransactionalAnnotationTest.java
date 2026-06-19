/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021, 2026 Contributors to the Eclipse Foundation
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

import jakarta.enterprise.inject.Stereotype;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.TransactionRequiredException;
import jakarta.transaction.Transactional;
import jakarta.transaction.TransactionalException;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static java.lang.annotation.ElementType.TYPE;
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
        InvocationContext ctx = new TestInvocationContext(BeanMandatory.class.getMethod("foo", String.class), null);
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
        InvocationContext ctx = new TestInvocationContext(BeanNever.class.getMethod("foo", String.class), null);
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
        InvocationContext ctx = new TestInvocationContext(BeanNotSupported.class.getMethod("foo", String.class), null);
        transactionalInterceptorNOT_SUPPORTED.transactional(ctx);
    }

    public void testTransactionalInterceptorREQUIRED() throws Exception {
        TransactionalInterceptorRequired transactionalInterceptorREQUIRED = new TransactionalInterceptorRequired();
        jakarta.transaction.TransactionManager transactionManager = new TransactionManager();
        TransactionalInterceptorBase.setTestTransactionManager(transactionManager);
        InvocationContext ctx = new TestInvocationContext(BeanRequired.class.getMethod("foo", String.class), null);
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
        InvocationContext ctx = new TestInvocationContext(BeanRequiresNew.class.getMethod("foo", String.class), null);
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
        InvocationContext ctx = new TestInvocationContext(BeanSupports.class.getMethod("foo", String.class), null);
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
            InvocationContext ctx = new TestInvocationContext(
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
            InvocationContext ctx = new TestInvocationContext(
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
            InvocationContext ctx = new TestInvocationContext(
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
            InvocationContext ctx = new TestInvocationContext(
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
            InvocationContext ctx = new TestInvocationContext(
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

    /**
     * Reproduces issue #26017: when {@code @Transactional(rollbackOn = ...)} is carried by a CDI stereotype rather than
     * declared directly on the bean, its {@code rollbackOn} must still trigger a rollback for the listed checked
     * exceptions. The resolved binding (including those contributed by stereotypes) is exposed through
     * {@link jakarta.interceptor.InvocationContext#getInterceptorBinding(Class)}, which the interceptor consults.
     */
    @Test
    public void testRollbackOnFromStereotypeCheckedException() throws Exception {
        TransactionalInterceptorRequired transactionalInterceptorREQUIRED = new TransactionalInterceptorRequired();
        jakarta.transaction.TransactionManager transactionManager = new TransactionManager();
        TransactionalInterceptorBase.setTestTransactionManager(transactionManager);

        // @Transactional is meta-annotated on the stereotype, not on the bean itself.
        Transactional transactionalFromStereotype = StereotypeWithTransactional.class.getAnnotation(Transactional.class);
        Set<Annotation> interceptorBindings = Set.of(transactionalFromStereotype);

        InvocationContext ctx = new TestInvocationContext(
            StereotypedBean.class.getMethod("doSomething"), null) {

            @Override
            public Object getTarget() {
                return new StereotypedBean();
            }

            @Override
            public Set<Annotation> getInterceptorBindings() {
                return interceptorBindings;
            }

            @Override
            public Object proceed() throws Exception {
                throw new StereotypeCheckedException();
            }
        };

        transactionManager.begin();
        assertThrows(StereotypeCheckedException.class, () -> transactionalInterceptorREQUIRED.transactional(ctx));
        assertThrows(RollbackException.class, transactionManager::commit);
    }

    static class StereotypeCheckedException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    @Transactional(rollbackOn = StereotypeCheckedException.class)
    @Stereotype
    @Target(TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface StereotypeWithTransactional {
    }

    @StereotypeWithTransactional
    static class StereotypedBean {
        public void doSomething() throws StereotypeCheckedException {
            throw new StereotypeCheckedException();
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
