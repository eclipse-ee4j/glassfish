/*
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

import static java.util.Collections.emptyList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.concurrent.LogFacade;
import org.glassfish.enterprise.concurrent.spi.ContextHandle;
import org.glassfish.enterprise.concurrent.spi.ContextSetupProvider;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.deployment.Deployment;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.util.Utility;

import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.ManagedTask;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;
import jakarta.enterprise.concurrent.spi.ThreadContextRestorer;
import jakarta.enterprise.concurrent.spi.ThreadContextSnapshot;
import jakarta.transaction.Status;
import jakarta.transaction.Transaction;

public class ContextSetupProviderImpl implements ContextSetupProvider {

    private transient InvocationManager invocationManager;
    private transient Deployment deployment;
    private transient ComponentEnvManager compEnvMgr;
    private transient Applications applications;
    // transactionManager should be null for ContextService since it uses TransactionSetupProviderImpl
    private transient JavaEETransactionManager transactionManager;

    private static final Logger logger  = LogFacade.getLogger();

    static final long serialVersionUID = -1095988075917755802L;

    public static final String CONTEXT_TYPE_CLASSLOADING = "CLASSLOADING"; // Concurrency 3.0: N/A
    public static final String CONTEXT_TYPE_SECURITY = "SECURITY"; // Concurrency 3.0: SECURITY
    public static final String CONTEXT_TYPE_NAMING = "NAMING"; // Concurrency 3.0: APPLICATION
    public static final String CONTEXT_TYPE_WORKAREA = "WORKAREA"; // Concurrency 3.0: TRANSACTION

    static enum CONTEXT_TYPE {CLASSLOADING, SECURITY, NAMING, WORKAREA}

    private boolean classloading, security, naming;

    private final Set<String> contextPropagate;
    private final Set<String> contextClear;
    private final Set<String> contextUnchanged;
    private Map<String, ThreadContextProvider> allThreadContextProviders = null;
    /**
     * Points to the context, which contains ALL_REMAINING.
     */
    private final Set<String> allRemaining;


    public ContextSetupProviderImpl(InvocationManager invocationManager,
            Deployment deployment,
            ComponentEnvManager compEnvMgr,
            ApplicationRegistry applicationRegistry,
            Applications applications,
            JavaEETransactionManager transactionManager,
            Set<String> propagated,
            Set<String> cleared,
            Set<String> unchanged) {
        this.invocationManager = invocationManager;
        this.deployment = deployment;
        this.compEnvMgr = compEnvMgr;
        this.applications = applications;
        this.transactionManager = transactionManager;

        contextPropagate = new HashSet<>(propagated);
        contextClear = new HashSet<>(cleared);
        contextUnchanged = new HashSet<>(unchanged);

        // process ALL_REMAINING
        if (contextPropagate.contains(ContextServiceDefinition.ALL_REMAINING)) {
            allRemaining = contextPropagate;
        } else if (contextClear.contains(ContextServiceDefinition.ALL_REMAINING)) {
            allRemaining = contextClear;
        } else if (contextUnchanged.contains(ContextServiceDefinition.ALL_REMAINING)) {
            allRemaining = contextUnchanged;
        } else {
            allRemaining = contextPropagate; // By default, propagate contexts
        }

        // Put standard "providers" to Remaining if not specified
        addToRemainingIfNotPresent(CONTEXT_TYPE_CLASSLOADING);
        addToRemainingIfNotPresent(CONTEXT_TYPE_SECURITY);
        addToRemainingIfNotPresent(CONTEXT_TYPE_NAMING);
        addToRemainingIfNotPresent(CONTEXT_TYPE_WORKAREA);

        for (String contextType : propagated) {
            switch (contextType) {
                case CONTEXT_TYPE_CLASSLOADING:
                    classloading = true;
                    break;
                case CONTEXT_TYPE_SECURITY:
                    security = true;
                    break;
                case CONTEXT_TYPE_NAMING:
                    naming = true;
                    break;
                case CONTEXT_TYPE_WORKAREA:
                    break;
            }
        }
    }

    @Override
    public ContextHandle saveContext(ContextService contextService) {
        return saveContext(contextService, null);
    }

    @Override
    public ContextHandle saveContext(ContextService contextService, Map<String, String> contextObjectProperties) {
        // Capture the current thread context
        ClassLoader contextClassloader = null;
        SecurityContext currentSecurityContext = null;
        ComponentInvocation savedInvocation = null;
        if (classloading) {
            contextClassloader = Utility.getClassLoader();
        }
        if (security) {
            currentSecurityContext = SecurityContext.getCurrent();
        }

        // TODO: put initialization of providers to better place; caching is a problem due to different classloaders
        allThreadContextProviders = new HashMap<>();
        for (ThreadContextProvider service : ServiceLoader.load(jakarta.enterprise.concurrent.spi.ThreadContextProvider.class, Utility.getClassLoader())) {
            String serviceName = service.getThreadContextType();
            if (contextPropagate.contains(serviceName) || contextClear.contains(serviceName) || contextUnchanged.contains(serviceName)) {
                allThreadContextProviders.put(serviceName, service);
            } else {
                if (allRemaining != null) {
                    allRemaining.add(serviceName);
                    allThreadContextProviders.put(serviceName, service);
                }
            }
        }
        // check, if there is no unexpected provider name
        verifyProviders(contextPropagate);
        verifyProviders(contextClear);
        verifyProviders(contextUnchanged);

        ComponentInvocation currentInvocation = invocationManager.getCurrentInvocation();
        if (currentInvocation != null) {

            if (contextPropagate.contains(CONTEXT_TYPE_NAMING)) {
                savedInvocation = createComponentInvocation(currentInvocation);
            }
            if (contextClear.contains(CONTEXT_TYPE_NAMING)) {
                savedInvocation = new ComponentInvocation();
            }

            savedInvocation = createComponentInvocation(currentInvocation);
        }

        boolean useTransactionOfExecutionThread =
            (transactionManager == null && useTransactionOfExecutionThread(contextObjectProperties)) ||
            contextUnchanged.contains(CONTEXT_TYPE_WORKAREA);

        // store the snapshots of the current state
        List<ThreadContextSnapshot> threadContextSnapshots = new ArrayList<>();
        // remember values from propagate and clear lists
        contextPropagate.stream()
                .map((provider) -> allThreadContextProviders.get(provider))
                .filter(snapshot -> snapshot != null) // ignore standard providers like CONTEXT_TYPE_CLASSLOADING
                .map(snapshot -> snapshot.currentContext(contextObjectProperties))
                .forEach(snapshot -> threadContextSnapshots.add(snapshot));
        contextClear.stream()
                .map((provider) -> allThreadContextProviders.get(provider))
                .filter(snapshot -> snapshot != null)
                .map(snapshot -> snapshot.clearedContext(contextObjectProperties))
                .forEach(snapshot -> threadContextSnapshots.add(snapshot));

        return new InvocationContext(savedInvocation, contextClassloader, currentSecurityContext, useTransactionOfExecutionThread,
                threadContextSnapshots, emptyList());
    }

    @Override
    public ContextHandle setup(ContextHandle contextHandle) throws IllegalStateException {
        if (! (contextHandle instanceof InvocationContext)) {
            logger.log(Level.SEVERE, LogFacade.UNKNOWN_CONTEXT_HANDLE);
            return null;
        }
        InvocationContext handle = (InvocationContext) contextHandle;
        String appName = null;

        ComponentInvocation invocation = handle.getInvocation();
        ClassLoader backupClassLoader = null;
        if (invocation != null) {
            appName = invocation.getAppName();
            if (appName == null && invocation.getJNDIEnvironment() != null) {
                appName = DOLUtils.getApplicationFromEnv((JndiNameEnvironment) invocation.getJNDIEnvironment()).getRegistrationName();
            }
            if (appName == null) {
                // try to get environment from component ID
                if (invocation.getComponentId() != null && compEnvMgr != null) {
                    JndiNameEnvironment currJndiEnv = compEnvMgr.getJndiNameEnvironment(invocation.getComponentId());
                    if (currJndiEnv != null) {
                        com.sun.enterprise.deployment.Application appInfo = DOLUtils.getApplicationFromEnv(currJndiEnv);
                        if (appInfo != null) {
                            appName = appInfo.getRegistrationName();
                            // cache JNDI environment
                            invocation.setJNDIEnvironment(currJndiEnv);
                            backupClassLoader = appInfo.getClassLoader();
                        }
                    }
                }
            }
        }

        // Check whether the application component submitting the task is still running. Throw IllegalStateException if not.
        if (appName != null && !isApplicationEnabled(appName)) { // appName == null in case of the server context
            throw new IllegalStateException("Module " + appName + " is disabled");
        }

        ClassLoader resetClassLoader = null;
        if (handle.getContextClassLoader() != null) {
            resetClassLoader = Utility.setContextClassLoader(handle.getContextClassLoader());
        } else if (backupClassLoader != null) {
            resetClassLoader = Utility.setContextClassLoader(backupClassLoader);
        }

        SecurityContext resetSecurityContext = null;
        if (handle.getSecurityContext() != null && !contextUnchanged.contains(CONTEXT_TYPE_SECURITY)) {
            resetSecurityContext = SecurityContext.getCurrent();
            SecurityContext.setCurrent(handle.getSecurityContext());
        }

        if (invocation != null) {
            // Each invocation needs a ResourceTableKey that returns a unique hashCode for TransactionManager
            invocation.setResourceTableKey(new PairKey(invocation.getInstance(), Thread.currentThread()));
            invocationManager.preInvoke(invocation);
        }
        // Ensure that there is no existing transaction in the current thread
        if (transactionManager != null && contextClear.contains(CONTEXT_TYPE_WORKAREA)) {
            transactionManager.clearThreadTx();
        }

        // execute thread contexts snapshots to begin
        List<ThreadContextRestorer> restorers = Collections.emptyList();
        if (handle.getThreadContextSnapshots() != null) {
            restorers = handle.getThreadContextSnapshots().stream()
                    .map((ThreadContextSnapshot snapshot) -> snapshot.begin())
                    .collect(Collectors.toList());
        }

        return new InvocationContext(invocation, resetClassLoader, resetSecurityContext, handle.isUseTransactionOfExecutionThread(),
                Collections.emptyList(), restorers);
    }


    @Override
    public void reset(ContextHandle contextHandle) {
        if (! (contextHandle instanceof InvocationContext)) {
            logger.log(Level.SEVERE, LogFacade.UNKNOWN_CONTEXT_HANDLE);
            return;
        }
        InvocationContext handle = (InvocationContext) contextHandle;
        if (handle.getContextClassLoader() != null) {
            Utility.setContextClassLoader(handle.getContextClassLoader());
        }
        if (handle.getSecurityContext() != null) {
            SecurityContext.setCurrent(handle.getSecurityContext());
        }
        if (handle.getInvocation() != null && !handle.isUseTransactionOfExecutionThread()) {
            invocationManager.postInvoke(((InvocationContext)contextHandle).getInvocation());
        }
        if (transactionManager != null) {
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
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ex.toString());
                }
            }
          transactionManager.clearThreadTx();
        }
    }

    private void verifyProviders(Set<String> providers) {
        Iterator<String> providerIter = providers.iterator();
        while (providerIter.hasNext()) {
            String provider = providerIter.next();
            switch (provider) {
                case CONTEXT_TYPE_CLASSLOADING:
                case CONTEXT_TYPE_SECURITY:
                case CONTEXT_TYPE_NAMING:
                case CONTEXT_TYPE_WORKAREA:
                case ContextServiceDefinition.ALL_REMAINING:
                    // OK, they are known
                    break;
                default:
                    if (!allThreadContextProviders.containsKey(provider)) {
                        logger.severe("Thread context provider '" + provider + "' is not registered in WEB-APP/services/jakarta.enterprise.concurrent.spi.ThreadContextProvider and will be ignored!");
                        providerIter.remove();
                    }
                    break;
            }
        }
    }

    private void addToRemainingIfNotPresent(String contextType) {
        if (!(contextPropagate.contains(contextType) || contextClear.contains(contextType) || contextUnchanged.contains(contextType))) {
            // such context type is not present in any context
            allRemaining.add(contextType);
        }
    }

    private boolean isApplicationEnabled(String appId) {
        if (appId != null) {
            Application app = applications.getApplication(appId);
            if (app != null)
                return deployment.isAppEnabled(app);
        }
        return false;
    }

    private ComponentInvocation createComponentInvocation(ComponentInvocation currInv) {
        ComponentInvocation newInv = currInv.clone();
        newInv.setResourceTableKey(null);
        newInv.instance = currInv.getInstance();
        if (!naming) {
            newInv.setJNDIEnvironment(null);
        }

        return newInv;
    }

    private boolean useTransactionOfExecutionThread(Map<String, String> executionProperties) {
        return (ManagedTask.USE_TRANSACTION_OF_EXECUTION_THREAD.equals(getTransactionExecutionProperty(executionProperties)));
    }

    private String getTransactionExecutionProperty(Map<String, String> executionProperties) {
        if (executionProperties != null && executionProperties.get(ManagedTask.TRANSACTION) != null) {
            return executionProperties.get(ManagedTask.TRANSACTION);
        }
        return ManagedTask.SUSPEND;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeBoolean(transactionManager == null);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        boolean nullTransactionManager = in.readBoolean();
        ConcurrentRuntime concurrentRuntime = ConcurrentRuntime.getRuntime();
        // re-initialize these fields
        invocationManager = concurrentRuntime.getInvocationManager();
        deployment = concurrentRuntime.getDeployment();
        applications = concurrentRuntime.getApplications();
        compEnvMgr = concurrentRuntime.getCompEnvMgr();
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

