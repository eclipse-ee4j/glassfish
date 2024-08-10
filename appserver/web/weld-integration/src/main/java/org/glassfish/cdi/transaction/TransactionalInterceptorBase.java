/*
 * Copyright (c) 2012, 2020jakarta Oracle and/or its affiliates. All rights reserved.
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
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * Base class for all interceptors providing common logic for exception handling, etc.
 *
 * @author Paul Parkinson
 */
public class TransactionalInterceptorBase implements Serializable {

    private static final long serialVersionUID = 706603825748958619L;

    @LogMessagesResourceBundle
    public static final String SHARED_LOGMESSAGE_RESOURCE = "org.glassfish.cdi.LogMessages";

    @LoggerInfo(subsystem = "AS-CDI-JTA", description = "CDI-JTA", publish = true)
    public static final String CDI_JTA_LOGGER_SUBSYSTEM_NAME = "jakarta.enterprise.resource.jta";
    private static final Logger _logger = Logger.getLogger(CDI_JTA_LOGGER_SUBSYSTEM_NAME, SHARED_LOGMESSAGE_RESOURCE);

    @LogMessageInfo(message = "Encountered NamingException while attempting to acquire "
            + "transaction manager for Transactional annotation interceptors {0}", action = "Fix the issue for the Naming exception", cause = "Transaction annotation processing for the Naming", level = "SEVERE")
    public static final String CDI_JTA_NAME_EXCEPTION = "AS-JTA-00001";

    @LogMessageInfo(message = "About to setRollbackOnly from @Transactional interceptor on " + "transaction: {0}", level = "INFO")
    public static final String CDI_JTA_SETROLLBACK = "AS-JTA-00002";

    @LogMessageInfo(message = "No ComponentInvocation present for @Transactional annotation "
            + "processing. Restriction on use of UserTransaction will not be enforced.", level = "WARNING")
    public static final String CDI_JTA_NOCOMPONENT = "AS-JTA-00003";

    @LogMessageInfo(message = "In MANDATORY TransactionalInterceptor", level = "INFO")
    public static final String CDI_JTA_MANDATORY = "AS-JTA-00004";

    @LogMessageInfo(message = "In NEVER TransactionalInterceptor", level = "INFO")
    public static final String CDI_JTA_NEVER = "AS-JTA-00005";

    @LogMessageInfo(message = "In NOT_SUPPORTED TransactionalInterceptor", level = "INFO")
    public static final String CDI_JTA_NOTSUPPORTED = "AS-JTA-00006";

    @LogMessageInfo(message = "Managed bean with Transactional annotation and TxType of NOT_SUPPORTED "
            + "called inside a transaction context. Suspending transaction...", level = "INFO")
    public static final String CDI_JTA_MBNOTSUPPORTED = "AS-JTA-00007";

    @LogMessageInfo(message = "Managed bean with Transactional annotation and TxType of NOT_SUPPORTED "
            + "called inside a transaction context.  Suspending transaction failed due to {0}", level = "INFO")
    public static final String CDI_JTA_MBNOTSUPPORTEDTX = "AS-JTA-00008";

    @LogMessageInfo(message = "In REQUIRED TransactionalInterceptor", level = "INFO")
    public static final String CDI_JTA_REQUIRED = "AS-JTA-00009";

    @LogMessageInfo(message = "Managed bean with Transactional annotation and TxType of REQUIRED "
            + "called outside a transaction context.  Beginning a transaction...", level = "INFO")
    public static final String CDI_JTA_MBREQUIRED = "AS-JTA-00010";

    @LogMessageInfo(message = "Managed bean with Transactional annotation and TxType of REQUIRED "
            + "encountered exception during begin {0}", level = "INFO")
    public static final String CDI_JTA_MBREQUIREDBT = "AS-JTA-00011";

    @LogMessageInfo(message = "Managed bean with Transactional annotation and TxType of REQUIRED "
            + "encountered exception during commit {0}", level = "INFO")
    public static final String CDI_JTA_MBREQUIREDCT = "AS-JTA-00012";

    @LogMessageInfo(message = "In REQUIRES_NEW TransactionalInterceptor", level = "INFO")
    public static final String CDI_JTA_REQNEW = "AS-JTA-00013";

    @LogMessageInfo(message = "Managed bean with Transactional annotation and TxType of REQUIRES_NEW "
            + "called inside a transaction context.  Suspending before beginning a transaction...", level = "INFO")
    public static final String CDI_JTA_MBREQNEW = "AS-JTA-00014";

    @LogMessageInfo(message = "Managed bean with Transactional annotation and TxType of REQUIRES_NEW "
            + "encountered exception during begin {0}", level = "INFO")
    public static final String CDI_JTA_MBREQNEWBT = "AS-JTA-00015";

    @LogMessageInfo(message = "Managed bean with Transactional annotation and TxType of REQUIRES_NEW "
            + "encountered exception during commit {0}", level = "INFO")
    public static final String CDI_JTA_MBREQNEWCT = "AS-JTA-00016";

    @LogMessageInfo(message = "Managed bean with Transactional annotation and TxType of REQUIRED "
            + "encountered exception during resume {0}", level = "INFO")
    public static final String CDI_JTA_MBREQNEWRT = "AS-JTA-00017";

    @LogMessageInfo(message = "In SUPPORTS TransactionalInterceptor", level = "INFO")
    public static final String CDI_JTA_SUPPORTS = "AS-JTA-00018";

    private static TransactionManager testTransactionManager;
    volatile private static TransactionManager transactionManager;
    transient private TransactionOperationsManager preexistingTransactionOperationsManager;
    private static final TransactionOperationsManager transactionalTransactionOperationsManagerTransactionMethodsAllowed = new TransactionalTransactionOperationsManagerTransactionMethodsAllowed();
    private static final TransactionOperationsManager transactionalTransactionOperationsManagerTransactionMethodsNotAllowed = new TransactionalTransactionOperationsManagerTransactionMethodsNotAllowed();

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
                        transactionManager = (TransactionManager) new InitialContext().lookup("java:appserver/TransactionManager");
                    }
                }
            } catch (NamingException e) {
                _logger.log(SEVERE, CDI_JTA_NAME_EXCEPTION, e);
                throw new RuntimeException("Unable to obtain TransactionManager for Transactional Interceptor", e);
            }
        }

        return transactionManager;
    }

    static void setTestTransactionManager(TransactionManager transactionManager) {
        testTransactionManager = transactionManager;
    }

    boolean isLifeCycleMethod(InvocationContext ctx) {
        return ctx.getMethod().getAnnotation(PostConstruct.class) != null || ctx.getMethod().getAnnotation(PreDestroy.class) != null;
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
            _logger.log(INFO, "Error during transaction processing", runtimeException);
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
            _logger.log(INFO, "Error during transaction processing", checkedException);
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
            _logger.log(INFO, CDI_JTA_SETROLLBACK, transaction);
            getTransactionManager().setRollbackOnly();
        }
    }

    void setTransactionalTransactionOperationsManger(boolean userTransactionMethodsAllowed) {
        if (testTransactionManager != null) {
            return; //test
        }

        ComponentInvocation currentInvocation = getCurrentInvocation();
        if (currentInvocation == null) {
            _logger.log(WARNING, CDI_JTA_NOCOMPONENT);
            return;
        }

        preexistingTransactionOperationsManager = (TransactionOperationsManager) currentInvocation.getTransactionOperationsManager();
        currentInvocation.setTransactionOperationsManager(
                userTransactionMethodsAllowed ? transactionalTransactionOperationsManagerTransactionMethodsAllowed
                        : transactionalTransactionOperationsManagerTransactionMethodsNotAllowed);
    }

    void resetTransactionOperationsManager() {
        if (testTransactionManager != null) {
            return; //test
        }

        ComponentInvocation currentInvocation = getCurrentInvocation();
        if (currentInvocation == null) {

            // There should always be a currentInvocation and so this would seem a bug
            // but not a fatal one as app should not be relying on this, so log warning only
            _logger.log(WARNING, "TransactionalInterceptorBase.markThreadAsTransactional currentInvocation==null");
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
