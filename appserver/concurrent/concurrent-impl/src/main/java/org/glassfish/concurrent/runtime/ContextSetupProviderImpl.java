/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.concurrent.runtime;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.deployment.types.ConcurrencyContextType;
import com.sun.enterprise.deployment.types.StandardContextType;
import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.util.Utility;

import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.spi.ThreadContextSnapshot;
import jakarta.transaction.Status;
import jakarta.transaction.Transaction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.enterprise.concurrent.spi.ContextHandle;
import org.glassfish.enterprise.concurrent.spi.ContextSetupProvider;
import org.glassfish.internal.deployment.Deployment;

import static com.sun.enterprise.deployment.types.StandardContextType.Classloader;
import static com.sun.enterprise.deployment.types.StandardContextType.JNDI;
import static com.sun.enterprise.deployment.types.StandardContextType.Security;
import static com.sun.enterprise.deployment.types.StandardContextType.WorkArea;
import static jakarta.enterprise.concurrent.ManagedTask.SUSPEND;
import static jakarta.enterprise.concurrent.ManagedTask.TRANSACTION;
import static jakarta.enterprise.concurrent.ManagedTask.USE_TRANSACTION_OF_EXECUTION_THREAD;


/**
 * Order of calls:
 * <ol>
 * <li>{@link #saveContext(ContextService)} or {@link #saveContext(ContextService, Map)}
 * <li>{@link #setup}
 * <li>{@link #reset(ContextHandle)}
 * </ol>
 */
public class ContextSetupProviderImpl implements ContextSetupProvider {

    private static final long serialVersionUID = -2043616384112372091L;
    private static final Logger LOG  = LogFacade.getLogger();

    private transient InvocationManager invocationManager;
    private transient Deployment deployment;
    private transient Applications applications;

    // transactionManager should be null for ContextService since it uses TransactionSetupProviderImpl
    private transient JavaEETransactionManager transactionManager;

    private final ContextSetup setup;

    public ContextSetupProviderImpl(Set<ConcurrencyContextType> propagated, Set<ConcurrencyContextType> cleared,
        Set<ConcurrencyContextType> unchanged) {
        this.setup = new ContextSetup(propagated, cleared, unchanged);
        ConcurrentRuntime runtime = ConcurrentRuntime.getRuntime();
        this.invocationManager = runtime.getInvocationManager();
        this.deployment = runtime.getDeployment();
        this.applications = runtime.getApplications();
        this.transactionManager = runtime.getTransactionManager();
    }


    public ContextSetup getContextSetup() {
        return this.setup;
    }


    @Override
    public ContextHandle saveContext(ContextService contextService) {
        return saveContext(contextService, Map.of());
    }


    @Override
    public ContextHandle saveContext(ContextService contextService, Map<String, String> executionProperties) {
        LOG.log(Level.FINEST, "saveContext(contextService={0}, executionProperties={1})",
            new Object[] {contextService, executionProperties});
        ClassLoader classLoader = Utility.getClassLoader();
        setup.reloadProviders(classLoader);
        final ClassLoader contextClassloader;
        if (setup.isPropagated(Classloader)) {
            contextClassloader = classLoader;
        } else {
            contextClassloader = null;
        }
        final SecurityContext securityContext;
        if (setup.isPropagated(Security)) {
            securityContext = SecurityContext.getCurrent();
        } else {
            securityContext = null;
        }
        return createInvocationContext(executionProperties, contextClassloader, securityContext);
    }


    @Override
    public ContextHandle setup(ContextHandle contextHandle) {
        LOG.log(Level.FINEST, "setup(contextHandle={0})", contextHandle);
        if (! (contextHandle instanceof InvocationContext)) {
            LOG.log(Level.SEVERE, LogFacade.UNKNOWN_CONTEXT_HANDLE);
            return null;
        }
        InvocationContext invocationCtx = (InvocationContext) contextHandle;
        ComponentInvocation invocation = invocationCtx.getInvocation();
        final String appName = invocation == null ? null : invocation.getAppName();

        if (!isApplicationEnabled(appName)) {
            throw new IllegalStateException("Module " + appName + " is disabled");
        }

        final ClassLoader resetClassLoader;
        if (invocationCtx.getContextClassLoader() != null) {
            resetClassLoader = Utility.setContextClassLoader(invocationCtx.getContextClassLoader());
        } else {
            resetClassLoader = null;
        }

        final SecurityContext resetSecurityContext;
        if (invocationCtx.getSecurityContext() == null || setup.isUnchanged(Security)) {
            resetSecurityContext = null;
        } else {
            resetSecurityContext = SecurityContext.getCurrent();
            SecurityContext.setCurrent(invocationCtx.getSecurityContext());
        }
        if (invocation != null) {
            // Each invocation needs a ResourceTableKey that returns a unique hashCode for TransactionManager
            invocation.setResourceTableKey(new PairKey(invocation.getInstance(), Thread.currentThread()));
            invocationManager.preInvoke(invocation);
        }
        // Ensure that there is no existing transaction in the current thread
        if (transactionManager != null && setup.isClear(StandardContextType.WorkArea)) {
            transactionManager.clearThreadTx();
        }

        ThreadMgmtData threadManagement = ThreadMgmtData.createNextGeneration(invocationCtx.getContextData());
        return new InvocationContext(invocation, resetClassLoader, resetSecurityContext,
            invocationCtx.isUseTransactionOfExecutionThread(), threadManagement);
    }


    @Override
    public void reset(ContextHandle contextHandle) {
        LOG.log(Level.FINEST, "reset(contextHandle={0})", contextHandle);
        if (! (contextHandle instanceof InvocationContext)) {
            LOG.log(Level.SEVERE, LogFacade.UNKNOWN_CONTEXT_HANDLE);
            return;
        }
        InvocationContext invocationContext = (InvocationContext) contextHandle;
        invocationContext.getContextData().endContext();
        if (invocationContext.getContextClassLoader() != null) {
            Utility.setContextClassLoader(invocationContext.getContextClassLoader());
        }
        if (invocationContext.getSecurityContext() != null) {
            SecurityContext.setCurrent(invocationContext.getSecurityContext());
        }
        if (invocationContext.getInvocation() != null && !invocationContext.isUseTransactionOfExecutionThread()) {
            invocationManager.postInvoke(invocationContext.getInvocation());
        }
        if (setup.isClear(WorkArea) && transactionManager != null) {
            // clean up after user if a transaction is still active
            // This is not required by the Concurrency spec
            Transaction transaction = transactionManager.getCurrentTransaction();
            if (transaction != null) {
                try {
                    int status = transaction.getStatus();
                    if (status == Status.STATUS_ACTIVE) {
                        transactionManager.commit();
                    } else if (status == Status.STATUS_MARKED_ROLLBACK) {
                        transactionManager.rollback();
                    }
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, "Cannot commit or rollback the transaction or get it's status.", ex);
                }
            }
            transactionManager.clearThreadTx();
        }
    }


    private ContextHandle createInvocationContext(final Map<String, String> executionProperties,
        final ClassLoader classloader, final SecurityContext securityCtx) {
        final boolean useTxOfExecutionThread = useTransactionOfExecutionThread(executionProperties);
        final List<ThreadContextSnapshot> threadCtxSnapshots = setup.getThreadContextSnapshots(executionProperties);
        final ComponentInvocation invocation = getSavedInvocation();
        final ThreadMgmtData threadMgmtData = new ThreadMgmtData(threadCtxSnapshots);
        return new InvocationContext(invocation, classloader, securityCtx, useTxOfExecutionThread, threadMgmtData);
    }


    private boolean useTransactionOfExecutionThread(Map<String, String> executionProperties) {
        return (transactionManager == null
            && USE_TRANSACTION_OF_EXECUTION_THREAD.equals(getTransactionExecutionProperty(executionProperties)))
            || setup.isUnchanged(WorkArea);
    }


    private String getTransactionExecutionProperty(Map<String, String> executionProperties) {
        if (executionProperties == null || executionProperties.isEmpty()) {
            return SUSPEND;
        }
        return executionProperties.getOrDefault(TRANSACTION, SUSPEND);
    }


    private ComponentInvocation getSavedInvocation() {
        ComponentInvocation currentInvocation = invocationManager.getCurrentInvocation();
        if (currentInvocation == null) {
            return null;
        }
        if (setup.isClear(JNDI)) {
            return new ComponentInvocation();
        }
        if (setup.isPropagated(JNDI)) {
            return cloneComponentInvocation(currentInvocation);
        }
        return null;
    }


    /**
     * Check whether the application component submitting the task is still running.
     * Throw IllegalStateException if not.
     */
    private boolean isApplicationEnabled(String appId) {
        if (appId == null) {
            return false;
        }
        Application app = applications.getApplication(appId);
        if (app != null) {
            return deployment.isAppEnabled(app);
        }
        // we know the application name but don't have an application yet, it is starting.
        return true;
    }


    private ComponentInvocation cloneComponentInvocation(ComponentInvocation currentInvocation) {
        ComponentInvocation clonedInvocation = currentInvocation.clone();
        clonedInvocation.setResourceTableKey(null);
        clonedInvocation.clearRegistry();
        clonedInvocation.instance = currentInvocation.getInstance();
        if (!setup.isPropagated(JNDI)) {
            clonedInvocation.setJNDIEnvironment(null);
        }
        return clonedInvocation;
    }


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeBoolean(transactionManager == null);
    }


    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        boolean nullTransactionManager = in.readBoolean();
        ConcurrentRuntime concurrentRuntime = ConcurrentRuntime.getRuntime();
        // re-initialize transient fields
        invocationManager = concurrentRuntime.getInvocationManager();
        deployment = concurrentRuntime.getDeployment();
        applications = concurrentRuntime.getApplications();
        if (!nullTransactionManager) {
            transactionManager = concurrentRuntime.getTransactionManager();
        }
    }

    private static class PairKey {
        private Object instance = null;
        private Thread thread = null;
        int hCode = 0;

        private PairKey(Object inst, Thread thr) {
            instance = inst;
            thread = thr;
            if (inst != null) {
                hCode = 7 * inst.hashCode();
            }
            if (thr != null) {
                hCode += thr.hashCode();
            }
        }

        @Override
        public int hashCode() {
            return hCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            boolean eq = false;
            if (obj != null && obj instanceof PairKey) {
                PairKey p = (PairKey)obj;
                if (instance != null) {
                    eq = (instance.equals(p.instance));
                } else {
                    eq = (p.instance == null);
                }

                if (eq) {
                    if (thread != null) {
                        eq = (thread.equals(p.thread));
                    } else {
                        eq = (p.thread == null);
                    }
                }
            }
            return eq;
        }
    }
}

