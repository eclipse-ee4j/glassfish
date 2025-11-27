/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.admin.monitor.callflow.Agent;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.container.common.spi.util.JavaEEIOUtils;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;

import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;

import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.ThreadPoolExecutor;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.ejb.config.EjbContainer;
import org.glassfish.ejb.config.EjbTimerService;
import org.glassfish.enterprise.iiop.api.GlassFishORBLocator;
import org.glassfish.flashlight.provider.ProbeProviderFactory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Contract;

/**
 * @author Mahesh Kannan
 */
@Contract
public interface EjbContainerUtil {

    // FIXME temporary constant for EJB Container's name - should get
    // removed once Deployment teams changes to add ContainerType are complete
    String EJB_CONTAINER_NAME = "ejb";

    String DEFAULT_THREAD_POOL_NAME = "__ejb-thread-pool";

    // Used by the TimerService upgrade
    long MINIMUM_TIMER_DELIVERY_INTERVAL = 1000;

    // Used by the TimerService upgrade
    String TIMER_SERVICE_UPGRADED = "ejb-timer-service-upgraded";

    GlassFishORBLocator getOrbLocator();

    ServiceLocator getServices();

    EjbTimerService getEjbTimerService(String target);

    void registerContainer(BaseContainer container);

    void unregisterContainer(BaseContainer container);

    BaseContainer getContainer(long id);

    EjbDescriptor getDescriptor(long id);

    ClassLoader getClassLoader(long id);

    Timer getTimer();

    void setInsideContainer(boolean bool);

    boolean isInsideContainer();

    InvocationManager getInvocationManager();

    InjectionManager getInjectionManager();

    GlassfishNamingManager getGlassfishNamingManager();

    ComponentEnvManager getComponentEnvManager();

    ComponentInvocation getCurrentInvocation();

    JavaEETransactionManager getTransactionManager();

    EjbAsyncInvocationManager getEjbAsyncInvocationManager();

    ServerContext getServerContext();

    ContainerSynchronization getContainerSync(Transaction jtx) throws RollbackException, SystemException;

    void removeContainerSync(Transaction tx);

    void registerPMSync(Transaction jtx, Synchronization sync) throws RollbackException, SystemException;

    EjbContainer getEjbContainer();

    ServerEnvironmentImpl getServerEnvironment();

    Agent getCallFlowAgent();

    Vector getBeans(Transaction jtx);

    Object getActiveTxCache(Transaction jtx);

    void setActiveTxCache(Transaction jtx, Object cache);

    void addWork(Runnable task);

    EjbDescriptor ejbIdToDescriptor(long ejbId);

    boolean isEJBLite();

    boolean isEmbeddedServer();

    ProbeProviderFactory getProbeProviderFactory();

    boolean isDas();

    ThreadPoolExecutor getThreadPoolExecutor(String poolName);

    JavaEEIOUtils getJavaEEIOUtils();

    Deployment getDeployment();
}
