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

import com.sun.enterprise.transaction.spi.TransactionOperationsManager;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.reflect.Method;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.logging.annotation.LoggerInfo;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Base class for all interceptors providing common logic for exception handling, etc.
 *
 * @author Paul Parkinson
 */
public class TransactionalInterceptorBase implements Serializable {

    private static final long serialVersionUID = 706603825748958619L;

    @LoggerInfo(subsystem = "AS-CDI-JTA", description = "CDI-JTA", publish = true)
    private static final String CDI_JTA_LOGGER_SUBSYSTEM_NAME = "org.glassfish.cdi.transaction";

    /**
     * {@value #CDI_JTA_LOGGER_SUBSYSTEM_NAME}
     */
    private static final Logger LOG = System.getLogger(TransactionalInterceptorBase.class.getName());

    private static TransactionManager testTransactionManager;
    private static volatile TransactionManager transactionManager;
    private static final TransactionOperationsManager txOperationsManagerTransactionMethodsAllowed
        = new TransactionalTransactionOperationsManagerTransactionMethodsAllowed();
    private static final TransactionOperationsManager txOperationsManagerTransactionMethodsNotAllowed
        = new TransactionalTransactionOperationsManagerTransactionMethodsNotAllowed();

    private transient TransactionOperationsManager preexistingTransactionOperationsManager;

    /**
     * Must not return null
     *
     * @return TransactionManager
     */
    public TransactionManager getTransactionManager() {
        if (testTransactionManager != null) {
            return testTransactionManager;
        }

        if (transactionManager == null) {
            try {
                synchronized (TransactionalInterceptorBase.class) {
                    if (transactionManager == null) {
                        transactionManager = (TransactionManager) new InitialContext()
                            .lookup("java:appserver/TransactionManager");
                    }
                }
            } catch (NamingException e) {
                throw new RuntimeException(
                    "Unable to obtain TransactionManager for Transactional Interceptor: " + e.getMessage(), e);
            }
        }

        return transactionManager;
    }

    static void setTestTransactionManager(TransactionManager transactionManager) {
        testTransactionManager = transactionManager;
    }

    boolean isLifeCycleMethod(InvocationContext ctx) {
        Method method = ctx.getMethod();
        return method.getAnnotation(PostConstruct.class) != null || method.getAnnotation(PreDestroy.class) != null;
    }

    public Object proceed(InvocationContext ctx) throws Exception {
        Transactional transactionalAnnotation = ctx.getMethod().getAnnotation(Transactional.class);
        Class<?>[] rollbackOn = null;
        Class<?>[] dontRollbackOn = null;

        if (transactionalAnnotation != null) { //if at method level
            rollbackOn = transactionalAnnotation.rollbackOn();
            dontRollbackOn = transactionalAnnotation.dontRollbackOn();
        } else { //if not, at class level
            Class<?> targetClass = ctx.getTarget().getClass();
            transactionalAnnotation = targetClass.getAnnotation(Transactional.class);
            if (transactionalAnnotation != null) {
                rollbackOn = transactionalAnnotation.rollbackOn();
                dontRollbackOn = transactionalAnnotation.dontRollbackOn();
            }
        }

        Object object;
        try {
            object = ctx.proceed();
        } catch (RuntimeException runtimeException) {
            LOG.log(ERROR, "Error during transaction processing", runtimeException);
            Class<?> dontRollbackOnClass = getClassInArrayClosestToClassOrNull(dontRollbackOn, runtimeException.getClass());
            if (dontRollbackOnClass == null) { //proceed as default...
                markRollbackIfActiveTransaction();
                throw runtimeException;
            }

            // Spec states "if both elements are specified, dontRollbackOn takes precedence."
            if (dontRollbackOnClass.equals(runtimeException.getClass())
                    || dontRollbackOnClass.isAssignableFrom(runtimeException.getClass())) {
                throw runtimeException;
            }

            Class<?> rollbackOnClass = getClassInArrayClosestToClassOrNull(rollbackOn, runtimeException.getClass());
            if (rollbackOnClass != null) {

                // Both rollback and dontrollback are isAssignableFrom exception.
                // Check if one isAssignableFrom the other, dontRollbackOn takes precedence if not
                if (rollbackOnClass.isAssignableFrom(dontRollbackOnClass)) {
                    throw runtimeException;
                }
                if (dontRollbackOnClass.isAssignableFrom(rollbackOnClass)) {
                }
            }

            // This means dontRollbackOnClass is "not null" and rollbackOnClass is "null"
            // Default for un-checked exception is to mark transaction for rollback
            markRollbackIfActiveTransaction();
            throw runtimeException;
        } catch (Exception checkedException) {
            LOG.log(ERROR, "Error during transaction processing", checkedException);
            Class<?> rollbackOnClass = getClassInArrayClosestToClassOrNull(rollbackOn, checkedException.getClass());
            if (rollbackOnClass == null) { //proceed as default...
                throw checkedException;
            }

            // Spec states "if both elements are specified, dontRollbackOn takes precedence."
            Class<?> dontRollbackOnClass = getClassInArrayClosestToClassOrNull(dontRollbackOn, checkedException.getClass());
            if (dontRollbackOnClass != null) {

                // Both rollback and dontrollback are isAssignableFrom exception.
                // Check if one isAssignableFrom the other, dontRollbackOn takes precedence if not
                if (rollbackOnClass.isAssignableFrom(dontRollbackOnClass)) {
                    throw checkedException;
                }
                if (dontRollbackOnClass.isAssignableFrom(rollbackOnClass)) {
                    markRollbackIfActiveTransaction();
                    throw checkedException;
                }
            }

            if (rollbackOnClass.equals(checkedException.getClass()) || rollbackOnClass.isAssignableFrom(checkedException.getClass())) {
                markRollbackIfActiveTransaction();
            }

            // This means dontRollbackOnClass is null but rollbackOnClass is "not null"
            // Default for checked exception is to "not" mark transaction for rollback
            throw checkedException;
        }
        return object;
    }

    /**
     * We want the exception in the array that is closest/lowest in hierarchy to the exception So if c extends b which
     * extends a the return of getClassInArrayClosestToClassOrNull( {a,b} , c} will be b
     *
     * @param exceptionArray rollbackOn or dontRollbackOn exception array
     * @param exception actual exception thrown for comparison
     * @return exception in the array that is closest/lowest in hierarchy to the exception or null if non exists
     */
    private Class<?> getClassInArrayClosestToClassOrNull(Class<?>[] exceptionArray, Class<?> exception) {
        if (exceptionArray == null || exception == null) {
            return null;
        }

        Class<?> closestMatch = null;
        for (Class<?> exceptionArrayElement : exceptionArray) {
            if (exceptionArrayElement.equals(exception)) {
                return exceptionArrayElement;
            }
            if (exceptionArrayElement.isAssignableFrom(exception)) {
                if (closestMatch == null || closestMatch.isAssignableFrom(exceptionArrayElement)) {
                    closestMatch = exceptionArrayElement;
                }
            }
        }

        return closestMatch;
    }

    private void markRollbackIfActiveTransaction() throws SystemException {
        Transaction transaction = getTransactionManager().getTransaction();
        if (transaction != null) {
            LOG.log(DEBUG, "About to setRollbackOnly from @Transactional interceptor on transaction: {0}", transaction);
            getTransactionManager().setRollbackOnly();
        }
    }

    void setTransactionalTransactionOperationsManger(boolean userTransactionMethodsAllowed) {
        if (testTransactionManager != null) {
            // test
            return;
        }

        ComponentInvocation currentInvocation = getCurrentInvocation();
        if (currentInvocation == null) {
            LOG.log(WARNING, "No ComponentInvocation present for @Transactional annotation processing."
                + " Restriction on use of UserTransaction will not be enforced.");
            return;
        }

        preexistingTransactionOperationsManager = (TransactionOperationsManager) currentInvocation.getTransactionOperationsManager();
        currentInvocation.setTransactionOperationsManager(
                userTransactionMethodsAllowed
                ? txOperationsManagerTransactionMethodsAllowed
                : txOperationsManagerTransactionMethodsNotAllowed);
    }

    void resetTransactionOperationsManager() {
        if (testTransactionManager != null) {
            return; //test
        }

        ComponentInvocation currentInvocation = getCurrentInvocation();
        if (currentInvocation == null) {

            // There should always be a currentInvocation and so this would seem a bug
            // but not a fatal one as app should not be relying on this, so log warning only
            LOG.log(WARNING, "TransactionalInterceptorBase.resetTransactionOperationsManager currentInvocation is null");
            return;
        }

        currentInvocation.setTransactionOperationsManager(preexistingTransactionOperationsManager);
    }

    ComponentInvocation getCurrentInvocation() {
        ServiceLocator serviceLocator = Globals.getDefaultHabitat();
        InvocationManager invocationManager = serviceLocator == null ? null : serviceLocator.getService(InvocationManager.class);
        return invocationManager == null ? null : invocationManager.getCurrentInvocation();
    }

    private static final class TransactionalTransactionOperationsManagerTransactionMethodsAllowed implements TransactionOperationsManager {

        @Override
        public boolean userTransactionMethodsAllowed() {
            return true;
        }

        @Override
        public void userTransactionLookupAllowed() throws NameNotFoundException {
        }

        @Override
        public void doAfterUtxBegin() {
        }
    }

    private static final class TransactionalTransactionOperationsManagerTransactionMethodsNotAllowed implements TransactionOperationsManager {

        @Override
        public boolean userTransactionMethodsAllowed() {
            return false;
        }

        @Override
        public void userTransactionLookupAllowed() throws NameNotFoundException {
        }

        @Override
        public void doAfterUtxBegin() {
        }
    }
}
