/*
 * Copyright (c) 2021-2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.deployment.annotation.handlers.ContextualResourceDefinition;
import com.sun.enterprise.deployment.types.ConcurrencyContextType;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.concurrent.runtime.deployer.cfg.ConcurrentServiceCfg;
import org.glassfish.concurrent.runtime.deployer.cfg.ContextServiceCfg;
import org.glassfish.concurrent.runtime.deployer.cfg.ManagedExecutorServiceCfg;
import org.glassfish.concurrent.runtime.deployer.cfg.ManagedScheduledExecutorServiceCfg;
import org.glassfish.concurrent.runtime.deployer.cfg.ManagedThreadFactoryCfg;
import org.glassfish.concurro.AbstractManagedExecutorService;
import org.glassfish.concurro.AbstractManagedThread;
import org.glassfish.concurro.ContextServiceImpl;
import org.glassfish.concurro.ManagedExecutorServiceImpl;
import org.glassfish.concurro.ManagedScheduledExecutorServiceImpl;
import org.glassfish.concurro.ManagedThreadFactoryImpl;
import org.glassfish.concurro.spi.ContextHandle;
import org.glassfish.concurro.spi.ContextSetupProvider;
import org.glassfish.concurro.spi.TransactionSetupProvider;
import org.glassfish.concurro.virtualthreads.VirtualThreadsManagedExecutorService;
import org.glassfish.concurro.virtualthreads.VirtualThreadsManagedScheduledExecutorService;
import org.glassfish.concurro.virtualthreads.VirtualThreadsManagedThreadFactory;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.resourcebase.resources.api.GenericResourceInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.naming.ResourceNamingService;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.deployment.types.StandardContextType.Classloader;
import static com.sun.enterprise.deployment.types.StandardContextType.JNDI;
import static com.sun.enterprise.deployment.types.StandardContextType.Security;
import static com.sun.enterprise.deployment.types.StandardContextType.WorkArea;
import static java.util.Collections.emptySet;

/**
 * This class provides API to create various Concurrency Utilities objects
 */
@Service
@Singleton
public class ConcurrentRuntime {

    private static final Logger LOG = LogFacade.getLogger();
    private static ConcurrentRuntime singletonInstance;

    private final Map<SimpleJndiName, AbstractManagedExecutorService> managedExecutorServiceMap = new HashMap<>();
    private final Map<SimpleJndiName, AbstractManagedExecutorService> managedScheduledExecutorServiceMap = new HashMap<>();
    private final Map<SimpleJndiName, ContextServiceImpl> contextServiceMap = new HashMap<>();
    private final Map<SimpleJndiName, ManagedThreadFactoryImpl> managedThreadFactoryMap = new HashMap<>();

    private ScheduledExecutorService internalScheduler;

    @Inject
    private InvocationManager invocationManager;
    @Inject
    private Deployment deployment;
    @Inject
    private Applications applications;
    @Inject
    private JavaEETransactionManager transactionManager;
    @Inject
    private ApplicationRegistry applicationRegistry;
    @Inject
    private ResourceNamingService resourceNamingService;

    /**
     * Returns the ConcurrentRuntime instance.
     * It follows singleton pattern and only one instance exists at any point
     * of time. External entities need to call this method to get
     * ConcurrentRuntime instance
     *
     * @return ConcurrentRuntime instance
     */
    public static ConcurrentRuntime getRuntime() {
        if (singletonInstance == null) {
            throw new RuntimeException("ConcurrentRuntime not initialized");
        }
        return singletonInstance;
    }

    /**
     * Constructor should be private to follow singleton pattern, but package access for unit testing.
     */
    ConcurrentRuntime() {
        singletonInstance = this;
    }

    InvocationManager getInvocationManager() {
        return invocationManager;
    }

    Deployment getDeployment() {
        return deployment;
    }

    Applications getApplications() {
        return applications;
    }

    JavaEETransactionManager getTransactionManager() {
        return transactionManager;
    }

    ApplicationRegistry getApplicationRegistry() {
        return applicationRegistry;
    }


    public synchronized AbstractManagedExecutorService getManagedExecutorService(ManagedExecutorServiceCfg config) {
        LOG.log(Level.FINEST, "getManagedExecutorService(config={0})", config);
        SimpleJndiName jndiName = config.getServiceConfig().getJndiName();
        if (managedExecutorServiceMap != null && managedExecutorServiceMap.containsKey(jndiName)) {
            return managedExecutorServiceMap.get(jndiName);
        }
        ContextServiceImpl contextService = getContextService(config.getServiceConfig(), true);
        AbstractManagedExecutorService mes = createManagedExecutorService(config, contextService);
        managedExecutorServiceMap.put(jndiName, mes);
        return mes;
    }


    public synchronized AbstractManagedExecutorService createManagedExecutorService(ManagedExecutorServiceCfg config, ContextServiceImpl contextService) {
        LOG.log(Level.FINE, "createManagedExecutorService(config={0}, contextService={1})",
            new Object[] {config, contextService});
        SimpleJndiName jndiName = config.getServiceConfig().getJndiName();
        GlassFishManagedThreadFactory managedThreadFactory = new GlassFishManagedThreadFactory(
                toManagedThreadFactoryName(jndiName),
                null,
                config.getThreadPriority());
        AbstractManagedExecutorService mes = null;
        if (config.getUseVirtualThreads()) {
            try {
                mes = new VirtualThreadsManagedExecutorService(jndiName.toString(),
                        null,
                        config.getHungAfterSeconds() * 1_000L, // in milliseconds
                        config.isLongRunningTasks(),
                        config.getMaximumPoolSize(),
                        config.getTaskQueueCapacity(),
                        contextService,
                        AbstractManagedExecutorService.RejectPolicy.ABORT);
            } catch (Exception e) {
                LOG.severe(() -> "Unable to create ManagedExecutorService with virtual threads: " + e.getMessage() + ", using fallback");
            }
        }
        if (mes == null) {
            mes = new ManagedExecutorServiceImpl(jndiName.toString(),
                    managedThreadFactory,
                    config.getHungAfterSeconds() * 1_000L, // in millis
                    config.isLongRunningTasks(),
                    config.getCorePoolSize(),
                    config.getMaximumPoolSize(),
                    config.getKeepAliveSeconds(), TimeUnit.SECONDS,
                    config.getThreadLifeTimeSeconds(),
                    config.getTaskQueueCapacity(),
                    contextService,
                    AbstractManagedExecutorService.RejectPolicy.ABORT);
        }
        if (config.getHungAfterSeconds() > 0L && !config.isLongRunningTasks()) {
            scheduleInternalTimer(config.getHungLoggerInitialDelaySeconds(), config.getHungLoggerIntervalSeconds(), config.isHungLoggerPrintOnce());
        }
        return mes;
    }


    public synchronized AbstractManagedExecutorService getManagedScheduledExecutorService(ManagedScheduledExecutorServiceCfg config) {
        LOG.log(Level.FINE, "getManagedScheduledExecutorService(config={0})", config);
        SimpleJndiName jndiName = config.getServiceConfig().getJndiName();
        if (managedScheduledExecutorServiceMap != null && managedScheduledExecutorServiceMap.containsKey(jndiName)) {
            return managedScheduledExecutorServiceMap.get(jndiName);
        }
        ContextServiceImpl contextService = getContextService(config.getServiceConfig(), true);
        AbstractManagedExecutorService mes = createManagedScheduledExecutorService(config, contextService);
        managedScheduledExecutorServiceMap.put(jndiName, mes);
        if (config.getHungAfterSeconds() > 0L && !config.isLongRunningTasks()) {
            scheduleInternalTimer(config.getHungLoggerInitialDelaySeconds(), config.getHungLoggerIntervalSeconds(), config.isHungLoggerPrintOnce());
        }
        return mes;
    }

    public AbstractManagedExecutorService createManagedScheduledExecutorService(
            ManagedScheduledExecutorServiceCfg config, ContextServiceImpl contextService) {
        SimpleJndiName jndiName = config.getServiceConfig().getJndiName();
        GlassFishManagedThreadFactory managedThreadFactory = new GlassFishManagedThreadFactory(
                toManagedThreadFactoryName(jndiName),
                null,
                config.getThreadPriority());
        AbstractManagedExecutorService mes = null;
        if (config.getUseVirtualThreads()) {
            try {
                mes = new VirtualThreadsManagedScheduledExecutorService(jndiName.toString(),
                        null,
                        config.getHungAfterSeconds() * 1_000L, // in milliseconds
                        config.isLongRunningTasks(),
                        Integer.MAX_VALUE,
                        Integer.MAX_VALUE,
                        contextService,
                        AbstractManagedExecutorService.RejectPolicy.ABORT);
            } catch (Exception e) {
                LOG.severe(() -> "Unable to create ManagedExecutorService with virtual threads: " + e.getMessage() + ", using fallback to platform threads");
            }
        }
        if (mes == null) {
            mes = new ManagedScheduledExecutorServiceImpl(jndiName.toString(),
                    managedThreadFactory,
                    config.getHungAfterSeconds() * 1000L,
                    config.isLongRunningTasks(),
                    config.getCorePoolSize(),
                    config.getKeepAliveSeconds(), TimeUnit.SECONDS,
                    config.getThreadLifeTimeSeconds(),
                    contextService,
                    AbstractManagedExecutorService.RejectPolicy.ABORT);
        }
        return mes;
    }

    public synchronized ManagedThreadFactoryImpl getManagedThreadFactory(ManagedThreadFactoryCfg config) {
        LOG.log(Level.FINE, "getManagedThreadFactory(config={0})", config);
        SimpleJndiName jndiName = config.getServiceConfig().getJndiName();
        if (managedThreadFactoryMap != null && managedThreadFactoryMap.containsKey(jndiName)) {
            return managedThreadFactoryMap.get(jndiName);
        }
        ContextServiceImpl contextService = getContextService(config.getServiceConfig(), true);
        ManagedThreadFactoryImpl managedThreadFactory = createManagedThreadFactory(config, contextService);
        managedThreadFactoryMap.put(jndiName, managedThreadFactory);
        return managedThreadFactory;
    }


    public ManagedThreadFactoryImpl createManagedThreadFactory(ManagedThreadFactoryCfg config, ContextServiceImpl contextService) {
        SimpleJndiName jndiName = config.getServiceConfig().getJndiName();
        if (config.getUseVirtualThreads()) {
            ManagedThreadFactoryImpl virtFactory = new VirtualThreadsManagedThreadFactory(jndiName.toString(), contextService);
            return virtFactory;
        } else {
            return new GlassFishManagedThreadFactory(jndiName, contextService, config.getThreadPriority());
        }
    }


    public ContextServiceImpl findOrCreateContextService(ContextualResourceDefinition definition, String applicationName, String moduleName) {
        SimpleJndiName jndiName = toContextServiceName(definition.getContext(), definition.getJndiName());
        LOG.log(Level.FINEST, "findOrCreateContextService(jndiName={0}, applicationName={1}, moduleName={2})",
            new Object[] {definition, applicationName, moduleName});
        ResourceInfo contextResourceInfo = new ResourceInfo(jndiName, applicationName, moduleName);
        ContextServiceImpl lookup1 = lookup(contextResourceInfo, jndiName);
        if (lookup1 != null) {
            return lookup1;
        }
        ContextServiceImpl lookup2 = lookup(definition.getContext());
        if (lookup2 != null) {
            return lookup2;
        }
        Set<ConcurrencyContextType> provided = Set.of(Classloader, JNDI, Security, WorkArea);
        ConcurrentServiceCfg config = new ConcurrentServiceCfg(jndiName, provided);
        return contextServiceMap.computeIfAbsent(jndiName, n -> createContextService(jndiName, config, true));
    }


    public synchronized ContextServiceImpl getContextService(ContextServiceCfg config) {
        return contextServiceMap.computeIfAbsent(config.getServiceConfig().getJndiName(),
            n -> createContextService(config));
    }


    public synchronized ContextServiceImpl createContextService(ContextServiceCfg config) {
        LOG.log(Level.FINE, "createContextService(config={0})", config);
        boolean keepTxUnchanged = config.getUnchangedContexts().contains(WorkArea);
        boolean clearTx = config.getClearedContexts().contains(WorkArea);
        TransactionSetupProvider txSetupProvider = createTxSetupProvider(keepTxUnchanged, clearTx);
        ContextSetupProvider ctxSetupProvider = new ContextSetupProviderImpl(config.getPropagatedContexts(),
            config.getClearedContexts(), config.getUnchangedContexts());
        SimpleJndiName jndiName = config.getServiceConfig().getJndiName();
        return new ContextServiceImpl(jndiName.toString(), ctxSetupProvider, txSetupProvider);
    }


    public synchronized ContextServiceImpl getContextService(ConcurrentServiceCfg config, boolean cleanupTransaction) {
        SimpleJndiName contextServiceJndiName = toContextServiceName(config.getContext(), config.getJndiName());
        return contextServiceMap.computeIfAbsent(contextServiceJndiName,
            n -> createContextService(contextServiceJndiName, config, cleanupTransaction));
    }


    /**
     * Shut down the runtime service.
     *
     * @param jndiName
     */
    public void shutdownManagedExecutorService(SimpleJndiName jndiName) {
        AbstractManagedExecutorService mes = removeManagedExecutorService(jndiName);
        if (mes != null) {
            mes.shutdownNow();
        }
    }


    /**
     * Shut down the runtime service.
     *
     * @param jndiName
     */
    public void shutdownScheduledManagedExecutorService(SimpleJndiName jndiName) {
        AbstractManagedExecutorService mses = removeManagedScheduledExecutorService(jndiName);
        if (mses != null) {
            mses.shutdownNow();
        }
    }


    /**
     * Stop the runtime thread factory.
     *
     * @param jndiName
     */
    public void shutdownManagedThreadFactory(SimpleJndiName jndiName) {
        ManagedThreadFactoryImpl mtf = removeManagedThreadFactory(jndiName);
        if (mtf != null) {
            mtf.stop();
        }
    }


    /**
     * Remove the context service from the internal map.
     *
     * @param jndiName
     */
    public synchronized void shutdownContextService(SimpleJndiName jndiName) {
        contextServiceMap.remove(jndiName);
    }


    private ContextServiceImpl createContextService(SimpleJndiName contextServiceJndiName, ConcurrentServiceCfg config,
        boolean cleanupTransaction) {
        LOG.log(Level.FINE, "createContextService(contextServiceJndiName={0}, config={1}, cleanupTransaction={2})",
            new Object[] {contextServiceJndiName, config, cleanupTransaction});
        // if the context service is not known, create it
        final Set<ConcurrencyContextType> propagated = config.getContextInfo();
        final Set<ConcurrencyContextType> cleared;
        final boolean clearTx;
        if (cleanupTransaction && !propagated.contains(WorkArea)) {
            // pass the cleanup transaction in list of cleared handlers
            cleared = Set.of(WorkArea);
            clearTx = true;
        } else {
            cleared = emptySet();
            clearTx = false;
        }
        TransactionSetupProvider txSetupProvider = createTxSetupProvider(false, clearTx);
        ContextSetupProvider ctxSetupProvider = new ContextSetupProviderImpl(propagated, cleared, emptySet());
        return new ContextServiceImpl(contextServiceJndiName.toString(), ctxSetupProvider, txSetupProvider);
    }


    private void scheduleInternalTimer(long initialDelay, long interval, boolean logOnce) {
        if (internalScheduler != null) {
            return;
        }
        final SimpleJndiName name = new SimpleJndiName("Glassfish-Internal");
        ManagedThreadFactoryImpl managedThreadFactory = new GlassFishManagedThreadFactory(
                toManagedThreadFactoryName(name),
                null,
                Thread.NORM_PRIORITY);
        ConcurrentServiceCfg config = new ConcurrentServiceCfg(toContextServiceName(name), Classloader, null);
        ContextServiceImpl contextService = getContextService(config, false);
        internalScheduler = new ManagedScheduledExecutorServiceImpl(name.toString(),
                managedThreadFactory,
                0L,
                false,
                1,
                60, TimeUnit.SECONDS,
                0L,
                contextService,
                AbstractManagedExecutorService.RejectPolicy.ABORT);
        internalScheduler.scheduleAtFixedRate(
            new HungTasksLogger(logOnce), initialDelay, interval, TimeUnit.SECONDS);
    }


    private TransactionSetupProvider createTxSetupProvider(boolean keepTransactionUnchanged, boolean clearTransaction) {
        return new TransactionSetupProviderImpl(transactionManager, keepTransactionUnchanged, clearTransaction);
    }


    private ContextServiceImpl lookup(GenericResourceInfo contextResourceInfo, SimpleJndiName jndiName) {
        try {
            return (ContextServiceImpl) resourceNamingService.lookup(contextResourceInfo, jndiName);
        } catch (NamingException e) {
            return null;
        }
    }


    private ContextServiceImpl lookup(String jndiName) {
        LOG.log(Level.FINEST, "lookup(jndiName={0})", jndiName);
        try {
            return InitialContext.doLookup(jndiName);
        } catch (Exception e) {
            return null;
        }
    }


    private synchronized AbstractManagedExecutorService removeManagedExecutorService(SimpleJndiName jndiName) {
        return managedExecutorServiceMap.remove(jndiName);
    }


    private synchronized AbstractManagedExecutorService removeManagedScheduledExecutorService(SimpleJndiName jndiName) {
        return managedScheduledExecutorServiceMap.remove(jndiName);
    }


    private synchronized ManagedThreadFactoryImpl removeManagedThreadFactory(SimpleJndiName jndiName) {
        return managedThreadFactoryMap.remove(jndiName);
    }


    private static SimpleJndiName toContextServiceName(String configuredContextJndiName, SimpleJndiName parentObjectJndiName) {
        return configuredContextJndiName == null ? toContextServiceName(parentObjectJndiName)
            : new SimpleJndiName(configuredContextJndiName);
    }


    private static SimpleJndiName toContextServiceName(final SimpleJndiName jndiName) {
        return new SimpleJndiName(jndiName + "-ContextService");
    }


    private static SimpleJndiName toManagedThreadFactoryName(SimpleJndiName jndiName) {
        return new SimpleJndiName(jndiName + "-ManagedThreadFactory");
    }


    class HungTasksLogger implements Runnable {

        private final Boolean logOnce;
        private final Map<String, Collection<Thread>> cachedHungThreadsMap = new HashMap<>();

        HungTasksLogger(Boolean logOnce) {
            this.logOnce = logOnce;
        }


        @Override
        public void run() {
            ArrayList<AbstractManagedExecutorService> executorServices = new ArrayList<>();
            synchronized (ConcurrentRuntime.this) {
                if (managedExecutorServiceMap != null) {
                    Collection<AbstractManagedExecutorService> mesColl = managedExecutorServiceMap.values();
                    executorServices.addAll(mesColl);
                }
                if (managedScheduledExecutorServiceMap != null) {
                    Collection<AbstractManagedExecutorService> msesColl = managedScheduledExecutorServiceMap.values();
                    executorServices.addAll(msesColl);
                }
            }
            for (AbstractManagedExecutorService mes : executorServices) {
                Collection<Thread> hungThreads = mes.getHungThreads();
                logHungThreads(hungThreads, mes.getManagedThreadFactory(), mes.getName());
            }
        }


        private void logHungThreads(Collection<Thread> hungThreads, ManagedThreadFactoryImpl mtf,
            String mesName) {
            if (!logOnce) {
                logRawHungThreads(hungThreads, mtf, mesName);
                return;
            }
            if (hungThreads == null) {
                cachedHungThreadsMap.remove(mesName);
                return;
            }
            Collection<Thread> targetHungThreads = new HashSet<>();
            targetHungThreads.addAll(hungThreads);
            Collection<Thread> cachedHungThreads = cachedHungThreadsMap.get(mesName);
            if (cachedHungThreads != null) {
                targetHungThreads.removeAll(cachedHungThreads);
            }
            logRawHungThreads(targetHungThreads, mtf, mesName);
            cachedHungThreadsMap.put(mesName, hungThreads);
        }


        private void logRawHungThreads(Collection<Thread> hungThreads, ManagedThreadFactoryImpl mtf,
            String mesName) {
            if (hungThreads != null) {
                for (Thread hungThread : hungThreads) {

                    String taskIdentityName = "virtual";
                    long taskRunTime = 0l;
                    if (hungThread instanceof AbstractManagedThread managedThread) {
                        taskIdentityName = managedThread.getTaskIdentityName();
                        taskRunTime = managedThread.getTaskRunTime(System.currentTimeMillis()) / 1000;
                    }

                    Object[] params = {taskIdentityName, hungThread.getName(),
                            taskRunTime, mtf.getHungTaskThreshold() / 1000,
                        mesName};
                    LOG.log(Level.WARNING, LogFacade.UNRESPONSIVE_TASK, params);
                }
            }
        }
    }

    private static class GlassFishManagedThreadFactory extends ManagedThreadFactoryImpl {

        private static final Logger LOG = Logger.getLogger(GlassFishManagedThreadFactory.class.getName());

        GlassFishManagedThreadFactory(SimpleJndiName name, ContextServiceImpl contextService, int threadPriority) {
            super(name.toString(), contextService, threadPriority);
        }


        @Override
        protected Thread createThread(Runnable runnable, ContextHandle contextHandleForSetup) {
            LOG.log(Level.FINE, "createThread(runnable={0}, contextHandleForSetup={1})",
                new Object[] {runnable, contextHandleForSetup});
            Thread thread = Thread.currentThread();
            ClassLoader originalCL = thread.getContextClassLoader();
            thread.setContextClassLoader(null);
            try {
                return super.createThread(runnable, contextHandleForSetup);
            } finally {
                thread.setContextClassLoader(originalCL);
            }
        }
    }
}
