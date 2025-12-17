/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation.
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
import com.sun.enterprise.admin.monitor.callflow.Agent;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.container.common.spi.util.JavaEEIOUtils;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.util.Utility;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.ReferenceContainer;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.ejb.config.EjbContainer;
import org.glassfish.ejb.config.EjbTimerService;
import org.glassfish.ejb.spi.CMPDeployer;
import org.glassfish.enterprise.iiop.api.GlassFishORBLocator;
import org.glassfish.flashlight.provider.ProbeProviderFactory;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Mahesh Kannan
 *         Date: Feb 10, 2008
 */
@Service
public class EjbContainerUtilImpl implements PostConstruct, PreDestroy, EjbContainerUtil {

    private static Logger _logger = LogDomains.getLogger(EjbContainerUtilImpl.class, LogDomains.EJB_LOGGER);

    private ThreadPoolExecutor defaultThreadPoolExecutor;

    @Inject
    private ServiceLocator services;

    @Inject
    private ServerContext serverContext;

    @Inject
    private JavaEEIOUtils javaEEIOUtils;

    private final Map<Long, BaseContainer> id2Container = new ConcurrentHashMap<>();

    private Timer _timer;

    private boolean _insideContainer = true;

    @Inject
    private InvocationManager _invManager;

    @Inject
    private InjectionManager _injectionManager;

    @Inject
    private GlassfishNamingManager _gfNamingManager;

    @Inject
    private ComponentEnvManager _compEnvManager;

    @Inject
    private JavaEETransactionManager txMgr;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config serverConfig;

    private EjbContainer ejbContainer;

    @Inject
    private GlassFishORBLocator orbLocator;

    @Inject
    private ServerEnvironmentImpl env;

    @Inject @Optional
    private Agent callFlowAgent;

    @Inject
    private ProcessEnvironment processEnv;

    @Inject
    private EjbAsyncInvocationManager ejbAsyncInvocationManager;

    @Inject
    private ProbeProviderFactory probeProviderFactory;

    @Inject
    private Domain domain;

    @Inject
    private Provider<Deployment> deploymentProvider;

    @Inject
    private Provider<CMPDeployer> cmpDeployerProvider;

    private  static EjbContainerUtil _me;

    @Override
    public void postConstruct() {
        ejbContainer = serverConfig.getExtensionByType(EjbContainer.class);

        ClassLoader ejbImplClassLoader = EjbContainerUtilImpl.class.getClassLoader();
        if (callFlowAgent == null) {
            callFlowAgent = (Agent) Proxy.newProxyInstance(ejbImplClassLoader,
                    new Class[] {Agent.class},
                    new InvocationHandler() {
                        @Override
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

    @Override
    public void preDestroy() {
        if (defaultThreadPoolExecutor != null) {
            defaultThreadPoolExecutor.shutdown();
            defaultThreadPoolExecutor = null;
        }
        EJBTimerService.onShutdown();
        EJBTimerService.unsetEJBTimerService();
    }

    @Override
    public GlassFishORBLocator getOrbLocator() {
        return orbLocator;
    }

    @Override
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

    @Override
    public  void registerContainer(BaseContainer container) {
        id2Container.put(container.getContainerId(), container);
    }

    @Override
    public  void unregisterContainer(BaseContainer container) {
        id2Container.remove(container.getContainerId());
    }

    @Override
    public  BaseContainer getContainer(long id) {
        return id2Container.get(id);
    }

    @Override
    public  EjbDescriptor getDescriptor(long id) {
        BaseContainer container = id2Container.get(id);
        return (container != null) ? container.getEjbDescriptor() : null;
    }

    @Override
    public  ClassLoader getClassLoader(long id) {
        BaseContainer container = id2Container.get(id);
        return (container != null) ? container.getClassLoader() : null;
    }

    @Override
    public  Timer getTimer() {
        return _timer;
    }

    @Override
    public  void setInsideContainer(boolean bool) {
        _insideContainer = bool;
    }

    @Override
    public  boolean isInsideContainer() {
        return _insideContainer;
    }

    @Override
    public  InvocationManager getInvocationManager() {
        return _invManager;
    }

    @Override
    public  InjectionManager getInjectionManager() {
        return _injectionManager;
    }

    @Override
    public  GlassfishNamingManager getGlassfishNamingManager() {
        return _gfNamingManager;
    }

    @Override
    public  ComponentEnvManager getComponentEnvManager() {
        return _compEnvManager;
    }

    @Override
    public  ComponentInvocation getCurrentInvocation() {
        return _invManager.getCurrentInvocation();
    }

    @Override
    public JavaEETransactionManager getTransactionManager() {
        return txMgr;
    }

    @Override
    public ServerContext getServerContext() {
        return serverContext;
    }

    @Override
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

    @Override
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

    @Override
    public void removeContainerSync(Transaction tx) {
        //No op
    }

    @Override
    public void registerPMSync(Transaction jtx, Synchronization sync)
            throws RollbackException, SystemException {

        getContainerSync(jtx).addPMSynchronization(sync);
    }

    @Override
    public EjbContainer getEjbContainer() {
        return ejbContainer;
    }

    @Override
    public ServerEnvironmentImpl getServerEnvironment() {
        return env;
    }

    @Override
    public  Vector getBeans(Transaction jtx) {
        JavaEETransaction tx = (JavaEETransaction) jtx;
        TxData txData = getTxData(tx);

        if( txData.beans == null ) {
            txData.beans = new Vector();
        }

        return txData.beans;

    }

    @Override
    public Object getActiveTxCache(Transaction jtx) {
        JavaEETransaction tx = (JavaEETransaction) jtx;
        TxData txData = getTxData(tx);

        return txData.activeTxCache;
    }

    @Override
    public void setActiveTxCache(Transaction jtx, Object cache) {
        JavaEETransaction tx = (JavaEETransaction) jtx;
        TxData txData = getTxData(tx);

        txData.activeTxCache = cache;
    }

    @Override
    public Agent getCallFlowAgent() {
        return callFlowAgent;
    }

    @Override
    public void addWork(Runnable task) {
        if (defaultThreadPoolExecutor != null) {
            defaultThreadPoolExecutor.submit(task);
        }
    }

    @Override
    public EjbDescriptor ejbIdToDescriptor(long ejbId) {
        throw new RuntimeException("Not supported yet");
    }

    @Override
    public boolean isEJBLite() {
        return (cmpDeployerProvider.get() == null);
    }

    @Override
    public boolean isEmbeddedServer() {
        return processEnv.getProcessType().isEmbedded();
    }

    @Override
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

    @Override
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

    @Override
    public ProbeProviderFactory getProbeProviderFactory() {
        return probeProviderFactory;
    }

   /**
    * Embedded is a single-instance like DAS
    */
    @Override
    public boolean isDas() {
        return env.isDas() || env.isEmbedded();
    }

    private ThreadPoolExecutor createThreadPoolExecutor(String poolName) {
        ThreadPoolExecutor result = null;
        String val = ejbContainer.getPropertyValue(RuntimeTagNames.THREAD_CORE_POOL_SIZE);
        int corePoolSize = initCorePoolSize(val);

        val = ejbContainer.getPropertyValue(RuntimeTagNames.THREAD_MAX_POOL_SIZE);
        int maxPoolSize = initMaxPoolSize(val, corePoolSize);

        val = ejbContainer.getPropertyValue(RuntimeTagNames.THREAD_KEEP_ALIVE_SECONDS);
        long keepAliveSeconds = initKeepAliveSeconds(val);

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

    static int initCorePoolSize(String propertyValue) {
        int corePoolSize = EjbContainer.DEFAULT_THREAD_CORE_POOL_SIZE;
        if (propertyValue == null) {
            return corePoolSize;
        }
        try {
            int configCorePoolSize = Integer.parseInt(propertyValue.trim());
            if (configCorePoolSize >= 0) {
                corePoolSize = configCorePoolSize;
            } else {
                _logger.warning(RuntimeTagNames.THREAD_CORE_POOL_SIZE
                        + " < 0 using default value "
                        + EjbContainer.DEFAULT_THREAD_CORE_POOL_SIZE);
            }
        } catch (NumberFormatException e) {
            _logger.warning(RuntimeTagNames.THREAD_CORE_POOL_SIZE
                    + " is not a number, using default value "
                    + EjbContainer.DEFAULT_THREAD_CORE_POOL_SIZE);
        }
        return corePoolSize;
    }

    static int initMaxPoolSize(String propertyValue, final int corePoolSize) {
        int maxPoolSize = EjbContainer.DEFAULT_THREAD_MAX_POOL_SIZE;
        if (propertyValue == null) {
            return maxPoolSize;
        }
        try {
            int configMaxPoolSize = Integer.parseInt(propertyValue.trim());
            if (configMaxPoolSize > 0) {
                maxPoolSize = configMaxPoolSize;
            } else {
                _logger.warning(RuntimeTagNames.THREAD_MAX_POOL_SIZE
                        + " <= 0 using default value "
                        + EjbContainer.DEFAULT_THREAD_MAX_POOL_SIZE);
            }
        } catch (NumberFormatException e) {
            _logger.warning(RuntimeTagNames.THREAD_MAX_POOL_SIZE
                    + " is not a number, using default value "
                    + EjbContainer.DEFAULT_THREAD_MAX_POOL_SIZE);
        }
        if (corePoolSize > maxPoolSize) {
            maxPoolSize = corePoolSize;
            _logger.warning(RuntimeTagNames.THREAD_MAX_POOL_SIZE
                    + " < " + RuntimeTagNames.THREAD_CORE_POOL_SIZE
                    + " using " + RuntimeTagNames.THREAD_MAX_POOL_SIZE
                    + "=" + corePoolSize);
        }
        return maxPoolSize;
    }

    static long initKeepAliveSeconds(String propertyValue) {
        long keepAliveSeconds = EjbContainer.DEFAULT_THREAD_KEEP_ALIVE_SECONDS;
        if (propertyValue == null) {
            return keepAliveSeconds;
        }
        try {
            long configKeepAliveSeconds = Long.parseLong(propertyValue.trim());
            if (configKeepAliveSeconds >= 0) {
                keepAliveSeconds = configKeepAliveSeconds;
            } else {
                _logger.warning(RuntimeTagNames.THREAD_KEEP_ALIVE_SECONDS
                        + " < 0 using default value "
                        + EjbContainer.DEFAULT_THREAD_KEEP_ALIVE_SECONDS);
            }
        } catch (NumberFormatException e) {
            _logger.warning(RuntimeTagNames.THREAD_KEEP_ALIVE_SECONDS
                    + " is not a number, using default value "
                    + EjbContainer.DEFAULT_THREAD_KEEP_ALIVE_SECONDS);
        }
        return keepAliveSeconds;
    }

    @Override
    public ThreadPoolExecutor getThreadPoolExecutor(String poolName) {
        if(poolName == null) {
            return defaultThreadPoolExecutor;
        }
        return null;
//        TODO retrieve the named ThreadPoolExecutor
    }

    @Override
    public JavaEEIOUtils getJavaEEIOUtils() {
        return javaEEIOUtils;
    }
}
