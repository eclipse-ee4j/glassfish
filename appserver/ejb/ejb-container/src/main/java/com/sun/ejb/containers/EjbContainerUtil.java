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

import org.glassfish.ejb.config.EjbTimerService;
import com.sun.enterprise.container.common.spi.util.JavaEEIOUtils;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deployment.EjbDescriptor;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.server.ServerEnvironmentImpl;
import com.sun.enterprise.admin.monitor.callflow.Agent;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.jvnet.hk2.annotations.Contract;

import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.Synchronization;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.ThreadPoolExecutor;
import org.glassfish.ejb.config.EjbContainer;

import org.glassfish.enterprise.iiop.api.GlassFishORBHelper;
import org.glassfish.flashlight.provider.ProbeProviderFactory;

/**
 * @author Mahesh Kannan
 *         Date: Feb 10, 2008
 */
@Contract
public interface EjbContainerUtil {

    // FIXME temporary constant for EJB Container's name - should get
    // removed once Deployment teams changes to add ContainerType are complete
    String EJB_CONTAINER_NAME = "ejb";

    public static final String DEFAULT_THREAD_POOL_NAME = "__ejb-thread-pool";

    // Used by the TimerService upgrade
    public long MINIMUM_TIMER_DELIVERY_INTERVAL = 1000;

    // Used by the TimerService upgrade
    public String TIMER_SERVICE_UPGRADED = "ejb-timer-service-upgraded";

    public GlassFishORBHelper getORBHelper();

    public ServiceLocator getServices();

    public  EjbTimerService getEjbTimerService(String target);

    public  void registerContainer(BaseContainer container);

    public  void unregisterContainer(BaseContainer container);

    public  BaseContainer getContainer(long id);

    public  EjbDescriptor getDescriptor(long id);

    public  ClassLoader getClassLoader(long id);

    public  Timer getTimer();

    public  void setInsideContainer(boolean bool);

    public  boolean isInsideContainer();

    public  InvocationManager getInvocationManager();

    public  InjectionManager getInjectionManager();

    public  GlassfishNamingManager getGlassfishNamingManager();

    public  ComponentEnvManager getComponentEnvManager();

    public  ComponentInvocation getCurrentInvocation();

    public JavaEETransactionManager getTransactionManager();

    public ServerContext getServerContext();

    public  ContainerSynchronization getContainerSync(Transaction jtx)
        throws RollbackException, SystemException;

    public void removeContainerSync(Transaction tx);

    public void registerPMSync(Transaction jtx, Synchronization sync)
        throws RollbackException, SystemException;

    public EjbContainer getEjbContainer();

    public ServerEnvironmentImpl getServerEnvironment();

    public Agent getCallFlowAgent();

    public Vector getBeans(Transaction jtx);

    public Object getActiveTxCache(Transaction jtx);

    public void setActiveTxCache(Transaction jtx, Object cache);

    public void addWork(Runnable task);

    public EjbDescriptor ejbIdToDescriptor(long ejbId);

    public boolean isEJBLite();

    public boolean isEmbeddedServer();

    public ProbeProviderFactory getProbeProviderFactory();

    public boolean isDas();

    public ThreadPoolExecutor getThreadPoolExecutor(String poolName);

    public JavaEEIOUtils getJavaEEIOUtils();

    public Deployment getDeployment();
}
