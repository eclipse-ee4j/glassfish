/*
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.containers;

import com.sun.ejb.base.io.EJBObjectInputStreamHandler;
import com.sun.ejb.base.io.EJBObjectOutputStreamHandler;
import com.sun.enterprise.container.common.spi.util.JavaEEIOUtils;
import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.admin.monitor.callflow.Agent;
import com.sun.enterprise.util.Utility;
import com.sun.logging.LogDomains;
import org.glassfish.ejb.spi.CMPDeployer;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.config.ReferenceContainer;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.enterprise.iiop.api.GlassFishORBHelper;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.server.ServerEnvironmentImpl;
import org.glassfish.flashlight.provider.ProbeProviderFactory;

import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.api.ServiceLocator;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.Synchronization;

import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.ejb.config.EjbContainer;
import org.glassfish.ejb.config.EjbTimerService;

/**
 * @author Mahesh Kannan
 *         Date: Feb 10, 2008
 */
@Service
public class EjbContainerUtilImpl
    implements PostConstruct, PreDestroy, EjbContainerUtil {

    private static Logger _logger = LogDomains.getLogger(EjbContainerUtilImpl.class, LogDomains.EJB_LOGGER);

    private ThreadPoolExecutor defaultThreadPoolExecutor;

    @Inject
    private ServiceLocator services;

    @Inject
    private ServerContext serverContext;

    @Inject
    JavaEEIOUtils javaEEIOUtils;

    private  Map<Long, BaseContainer> id2Container
            = new ConcurrentHashMap<Long, BaseContainer>();

    private  Timer _timer;

    private  boolean _insideContainer = true;

    @Inject
    private  InvocationManager _invManager;

    @Inject
    private  InjectionManager _injectionManager;

    @Inject
    private  GlassfishNamingManager _gfNamingManager;

    @Inject
    private  ComponentEnvManager _compEnvManager;

    @Inject
    private JavaEETransactionManager txMgr;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config serverConfig;

    private EjbContainer ejbContainer;

    @Inject
    private GlassFishORBHelper orbHelper;

    @Inject
    private ServerEnvironmentImpl env;

    @Inject @Optional
    private Agent callFlowAgent;

    @Inject
    private ProcessEnvironment processEnv;

    @Inject
    private EjbAsyncInvocationManager ejbAsyncInvocationManager;

    @Inject
    ProbeProviderFactory probeProviderFactory;

    @Inject
    Domain domain;

    @Inject
    Provider<Deployment> deploymentProvider;

    @Inject
    private Provider<CMPDeployer> cmpDeployerProvider;

    private  static EjbContainerUtil _me;

    public void postConstruct() {
        ejbContainer = serverConfig.getExtensionByType(EjbContainer.class);

        ClassLoader ejbImplClassLoader = EjbContainerUtilImpl.class.getClassLoader();
        if (callFlowAgent == null) {
            callFlowAgent = (Agent) Proxy.newProxyInstance(ejbImplClassLoader,
                    new Class[] {Agent.class},
                    new InvocationHandler() {
                        public Object invoke(Object proxy, Method m, Object[] args) {
                            return null;
                        }
                    });
        }

        defaultThreadPoolExecutor = createThreadPoolExecutor(DEFAULT_THREAD_POOL_NAME);

        //avoid starting JDK timer in application class loader.  The life of _timer
        //field is longer than deployed apps, and any reference to app class loader
        //in JDK timer thread will cause class loader leak.  Issue 17468
        ClassLoader originalClassLoader = null;
        try {
            originalClassLoader = Utility.setContextClassLoader(ejbImplClassLoader);
            _timer = new Timer(true);
        } finally {
            if (originalClassLoader != null) {
                Utility.setContextClassLoader(originalClassLoader);
            }
        }

        EJBObjectOutputStreamHandler.setJavaEEIOUtils(javaEEIOUtils);
        javaEEIOUtils.addGlassFishOutputStreamHandler(new EJBObjectOutputStreamHandler());
        javaEEIOUtils.addGlassFishInputStreamHandler(new EJBObjectInputStreamHandler());
        _me = this;
    }

    public void preDestroy() {
        if( defaultThreadPoolExecutor != null ) {
            defaultThreadPoolExecutor.shutdown();
            defaultThreadPoolExecutor = null;
        }
        EJBTimerService.onShutdown();
        EJBTimerService.unsetEJBTimerService();
    }

    public GlassFishORBHelper getORBHelper() {
        return orbHelper;
    }

    public ServiceLocator getServices() {
        return services;
    }

    public static boolean isInitialized() {
        return (_me != null);
    }

    public static EjbContainerUtil getInstance() {
        if (_me == null) {
            // This situation shouldn't happen. Print the error message
            // and the stack trace to know how did we get here.

            // Create the instance first to access the logger.
            _logger.log(Level.WARNING,
                    "Internal error: EJBContainerUtilImpl is null, creating ...",
                    new Throwable());
            _me = Globals.getDefaultHabitat().getService(
                    EjbContainerUtilImpl.class);
        }
        return _me;
    }

    public  static Logger getLogger() {
        return _logger;
    }

    public  void registerContainer(BaseContainer container) {
        id2Container.put(container.getContainerId(), container);
    }

    public  void unregisterContainer(BaseContainer container) {
        id2Container.remove(container.getContainerId());
    }

    public  BaseContainer getContainer(long id) {
        return id2Container.get(id);
    }

    public  EjbDescriptor getDescriptor(long id) {
        BaseContainer container = id2Container.get(id);
        return (container != null) ? container.getEjbDescriptor() : null;
    }

    public  ClassLoader getClassLoader(long id) {
        BaseContainer container = id2Container.get(id);
        return (container != null) ? container.getClassLoader() : null;
    }

    public  Timer getTimer() {
        return _timer;
    }

    public  void setInsideContainer(boolean bool) {
        _insideContainer = bool;
    }

    public  boolean isInsideContainer() {
        return _insideContainer;
    }

    public  InvocationManager getInvocationManager() {
        return _invManager;
    }

    public  InjectionManager getInjectionManager() {
        return _injectionManager;
    }

    public  GlassfishNamingManager getGlassfishNamingManager() {
        return _gfNamingManager;
    }

    public  ComponentEnvManager getComponentEnvManager() {
        return _compEnvManager;
    }

    public  ComponentInvocation getCurrentInvocation() {
        return _invManager.getCurrentInvocation();
    }

    public JavaEETransactionManager getTransactionManager() {
        return txMgr;
    }

    public ServerContext getServerContext() {
        return serverContext;
    }

    public EjbAsyncInvocationManager getEjbAsyncInvocationManager() {
        return ejbAsyncInvocationManager;
    }

    private TxData getTxData(JavaEETransaction tx) {
        TxData txData = tx.getContainerData();

        if ( txData == null ) {
            txData = new TxData();
            tx.setContainerData(txData);
        }

        return txData;
    }

    public  ContainerSynchronization getContainerSync(Transaction jtx)
        throws RollbackException, SystemException
    {
        JavaEETransaction tx = (JavaEETransaction) jtx;
        TxData txData = getTxData(tx);

        if( txData.sync == null ) {
            txData.sync = new ContainerSynchronization(tx, this);
            tx.registerSynchronization(txData.sync);
        }

        return txData.sync;
    }

    public void removeContainerSync(Transaction tx) {
        //No op
    }

    public void registerPMSync(Transaction jtx, Synchronization sync)
            throws RollbackException, SystemException {

        getContainerSync(jtx).addPMSynchronization(sync);
    }

    public EjbContainer getEjbContainer() {
        return ejbContainer;
    }

    public ServerEnvironmentImpl getServerEnvironment() {
        return env;
    }

    public  Vector getBeans(Transaction jtx) {
        JavaEETransaction tx = (JavaEETransaction) jtx;
        TxData txData = getTxData(tx);

        if( txData.beans == null ) {
            txData.beans = new Vector();
        }

        return txData.beans;

    }

    public Object getActiveTxCache(Transaction jtx) {
        JavaEETransaction tx = (JavaEETransaction) jtx;
        TxData txData = getTxData(tx);

        return txData.activeTxCache;
    }

    public void setActiveTxCache(Transaction jtx, Object cache) {
        JavaEETransaction tx = (JavaEETransaction) jtx;
        TxData txData = getTxData(tx);

        txData.activeTxCache = cache;
    }

    public Agent getCallFlowAgent() {
        return callFlowAgent;
    }

    public void addWork(Runnable task) {
        if (defaultThreadPoolExecutor != null) {
            defaultThreadPoolExecutor.submit(task);
        }
    }

    public EjbDescriptor ejbIdToDescriptor(long ejbId) {
        throw new RuntimeException("Not supported yet");
    }

    public boolean isEJBLite() {
        return (cmpDeployerProvider.get() == null);
    }

    public boolean isEmbeddedServer() {
        return processEnv.getProcessType().isEmbedded();
    }

    public Deployment getDeployment() {
        return deploymentProvider.get();
    }

    // Various pieces of data associated with a tx.  Store directly
    // in J2EETransaction to avoid repeated Map<tx, data> lookups.
    private static class TxData {
        ContainerSynchronization sync;
        Vector beans;
        Object activeTxCache;
    }

    public EjbTimerService getEjbTimerService(String target) {
        EjbTimerService ejbt = null;
        if (target == null) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("Looking for current instance ejb-container config");
            }
            ejbt = getEjbContainer().getEjbTimerService();
        } else {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("Looking for " + target + " ejb-container config");
            }
            ReferenceContainer rc =  domain.getReferenceContainerNamed(target);
            if (rc != null) {
                Config config = domain.getConfigNamed(rc.getReference());
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine("Found " + config);
                }
                if (config != null) {
                    ejbt = config.getExtensionByType(EjbContainer.class).getEjbTimerService();
                }
            }
        }

        return ejbt;
    }

    public ProbeProviderFactory getProbeProviderFactory() {
        return probeProviderFactory;
    }

   /**
    * Embedded is a single-instance like DAS
    */
    public boolean isDas() {
        return env.isDas() || env.isEmbedded();
    }

    private ThreadPoolExecutor createThreadPoolExecutor(String poolName) {
        ThreadPoolExecutor result = null;
        String val = ejbContainer.getPropertyValue(RuntimeTagNames.THREAD_CORE_POOL_SIZE);
        int corePoolSize = val != null ? Integer.parseInt(val.trim())
                : EjbContainer.DEFAULT_THREAD_CORE_POOL_SIZE;

        val = ejbContainer.getPropertyValue(RuntimeTagNames.THREAD_MAX_POOL_SIZE);
        int maxPoolSize = val != null ? Integer.parseInt(val.trim())
                : EjbContainer.DEFAULT_THREAD_MAX_POOL_SIZE;

        val = ejbContainer.getPropertyValue(RuntimeTagNames.THREAD_KEEP_ALIVE_SECONDS);
        long keepAliveSeconds = val != null ? Long.parseLong(val.trim())
                : EjbContainer.DEFAULT_THREAD_KEEP_ALIVE_SECONDS;

        val = ejbContainer.getPropertyValue(RuntimeTagNames.THREAD_QUEUE_CAPACITY);
        int queueCapacity = val != null ? Integer.parseInt(val.trim())
                : EjbContainer.DEFAULT_THREAD_QUEUE_CAPACITY;

        val = ejbContainer.getPropertyValue(RuntimeTagNames.ALLOW_CORE_THREAD_TIMEOUT);
        boolean allowCoreThreadTimeout = val != null ? Boolean.parseBoolean(val.trim())
                : EjbContainer.DEFAULT_ALLOW_CORE_THREAD_TIMEOUT;

        val = ejbContainer.getPropertyValue(RuntimeTagNames.PRESTART_ALL_CORE_THREADS);
        boolean preStartAllCoreThreads = val != null ? Boolean.parseBoolean(val.trim())
                : EjbContainer.DEFAULT_PRESTART_ALL_CORE_THREADS;

        BlockingQueue workQueue = queueCapacity > 0
                ? new LinkedBlockingQueue<Runnable>(queueCapacity)
                : new SynchronousQueue(true);

        result = new EjbThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveSeconds, workQueue, poolName);

        if(allowCoreThreadTimeout) {
            result.allowCoreThreadTimeOut(true);
        }
        if (preStartAllCoreThreads) {
            result.prestartAllCoreThreads();
        }

        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("Created " + result.toString());

        }
        return result;
    }

    public ThreadPoolExecutor getThreadPoolExecutor(String poolName) {
        if(poolName == null) {
            return defaultThreadPoolExecutor;
        }
        return null;
//        TODO retrieve the named ThreadPoolExecutor
    }

    public JavaEEIOUtils getJavaEEIOUtils() {
        return javaEEIOUtils;
    }
}
