/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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

import java.util.concurrent.TimeUnit;

import org.glassfish.concurrent.runtime.deployer.cfg.ConcurrentServiceCfg;
import org.glassfish.concurrent.runtime.deployer.cfg.ContextServiceCfg;
import org.glassfish.concurrent.runtime.deployer.cfg.ManagedExecutorServiceCfg;
import org.glassfish.concurrent.runtime.deployer.cfg.ManagedThreadFactoryCfg;
import org.glassfish.enterprise.concurrent.ContextServiceImpl;
import org.glassfish.enterprise.concurrent.ManagedExecutorServiceImpl;
import org.glassfish.enterprise.concurrent.ManagedThreadFactoryImpl;
import org.glassfish.enterprise.concurrent.internal.ManagedThreadPoolExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.sun.enterprise.deployment.annotation.handlers.StandardContextType.Classloader;
import static com.sun.enterprise.deployment.annotation.handlers.StandardContextType.JNDI;
import static com.sun.enterprise.deployment.annotation.handlers.StandardContextType.Remaining;
import static com.sun.enterprise.deployment.annotation.handlers.StandardContextType.Security;
import static com.sun.enterprise.deployment.annotation.handlers.StandardContextType.WorkArea;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.glassfish.tests.utils.ReflectionUtils.getField;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ConcurrentRuntimeTest {

    private ManagedThreadFactoryCfg managedThreadFactoryCfg;
    private ManagedExecutorServiceCfg managedExecutorServiceCfg;
    private ConcurrentRuntime runtime;

    @BeforeEach
    public void before() {
        managedThreadFactoryCfg = createMock(ManagedThreadFactoryCfg.class);
        managedExecutorServiceCfg = createMock(ManagedExecutorServiceCfg.class);
        runtime = new ConcurrentRuntime();
    }


    @Test
    public void testParseContextInfo() throws Exception {
        ConcurrentServiceCfg serviceConfig = new ConcurrentServiceCfg("concurrent/ctxSrv", "Classloader, JNDI, Security, WorkArea", true, null);
        ContextServiceCfg contextServiceConfig = new ContextServiceCfg(serviceConfig);
        ContextServiceImpl contextService = runtime.getContextService(contextServiceConfig);
        ContextSetupProviderImpl provider = (ContextSetupProviderImpl) contextService.getContextSetupProvider();
        ContextSetup contextSetup = provider.getContextSetup();
        contextSetup.reloadProviders(Thread.currentThread().getContextClassLoader());
        assertAll(
            () -> assertTrue(contextSetup.isPropagated(Classloader)),
            () -> assertTrue(contextSetup.isPropagated(JNDI)),
            () -> assertTrue(contextSetup.isPropagated(Security)),
            () -> assertTrue(contextSetup.isPropagated(WorkArea)),
            () -> assertFalse(contextSetup.isPropagated(Remaining))
        );
    }


    @Test
    public void testParseContextInfo_lowerCase() throws Exception {
        ConcurrentServiceCfg serviceConfig = new ConcurrentServiceCfg("concurrent/ctxSrv", "classloader, jndi, security, workarea", true, null);
        ContextServiceCfg contextServiceConfig = new ContextServiceCfg(serviceConfig);
        ContextServiceImpl contextService = runtime.getContextService(contextServiceConfig);
        ContextSetupProviderImpl provider = (ContextSetupProviderImpl) contextService.getContextSetupProvider();
        ContextSetup contextSetup = provider.getContextSetup();
        contextSetup.reloadProviders(Thread.currentThread().getContextClassLoader());
        assertAll(
            () -> assertTrue(contextSetup.isPropagated(Classloader)),
            () -> assertTrue(contextSetup.isPropagated(JNDI)),
            () -> assertTrue(contextSetup.isPropagated(Security)),
            () -> assertTrue(contextSetup.isPropagated(WorkArea)),
            () -> assertFalse(contextSetup.isPropagated(Remaining))
        );
    }


    @Test
    public void testParseContextInfo_upperCase() throws Exception {
        ConcurrentServiceCfg serviceConfig = new ConcurrentServiceCfg("concurrent/ctxSrv", "CLASSLOADER, JNDI, SECURITY, WORKAREA", true, null);
        ContextServiceCfg contextServiceConfig = new ContextServiceCfg(serviceConfig);
        ContextServiceImpl contextService = runtime.getContextService(contextServiceConfig);
        ContextSetupProviderImpl provider = (ContextSetupProviderImpl) contextService.getContextSetupProvider();
        ContextSetup contextSetup = provider.getContextSetup();
        contextSetup.reloadProviders(Thread.currentThread().getContextClassLoader());
        assertAll(
            () -> assertTrue(contextSetup.isPropagated(Classloader)),
            () -> assertTrue(contextSetup.isPropagated(JNDI)),
            () -> assertTrue(contextSetup.isPropagated(Security)),
            () -> assertTrue(contextSetup.isPropagated(WorkArea)),
            () -> assertFalse(contextSetup.isPropagated(Remaining))
        );
    }


    @Test
    public void testParseContextInfo_disabled() throws Exception {
        ConcurrentServiceCfg serviceConfig = new ConcurrentServiceCfg("concurrent/ctxSrv", "Classloader, JNDI, Security, WorkArea", false, null);
        ContextServiceCfg contextServiceConfig = new ContextServiceCfg(serviceConfig);
        ContextServiceImpl contextService = runtime.getContextService(contextServiceConfig);
        ContextSetupProviderImpl provider = (ContextSetupProviderImpl) contextService.getContextSetupProvider();
        ContextSetup contextSetup = provider.getContextSetup();
        contextSetup.reloadProviders(Thread.currentThread().getContextClassLoader());
        assertAll(
            () -> assertTrue(contextSetup.isPropagated(Classloader)),
            () -> assertTrue(contextSetup.isPropagated(JNDI)),
            () -> assertTrue(contextSetup.isPropagated(Security)),
            () -> assertTrue(contextSetup.isPropagated(WorkArea)),
            () -> assertFalse(contextSetup.isPropagated(Remaining))
        );
    }


    @Test
    public void testParseContextInfo_invalid() throws Exception {
        ConcurrentServiceCfg serviceConfig = new ConcurrentServiceCfg("concurrent/ctxSrv", "JNDI, blah, beh, JNDI, WorkArea, WorkArea, ", true, null);
        ContextServiceCfg contextServiceConfig = new ContextServiceCfg(serviceConfig);
        ContextServiceImpl contextService = runtime.getContextService(contextServiceConfig);
        ContextSetupProviderImpl provider = (ContextSetupProviderImpl) contextService.getContextSetupProvider();
        ContextSetup contextSetup = provider.getContextSetup();
        contextSetup.reloadProviders(Thread.currentThread().getContextClassLoader());
        assertAll(
            () -> assertTrue(contextSetup.isPropagated(Classloader)),
            () -> assertTrue(contextSetup.isPropagated(JNDI)),
            () -> assertTrue(contextSetup.isPropagated(Security)),
            () -> assertTrue(contextSetup.isPropagated(WorkArea)),
            () -> assertFalse(contextSetup.isPropagated(Remaining))
        );
    }


    @Test
    public void testParseContextInfo_null() throws Exception {
        ConcurrentServiceCfg serviceConfig = new ConcurrentServiceCfg("concurrent/ctxSrv", null, true, null);
        ContextServiceCfg contextServiceConfig = new ContextServiceCfg(serviceConfig);
        ContextServiceImpl contextService = runtime.getContextService(contextServiceConfig);
        ContextSetupProviderImpl provider = (ContextSetupProviderImpl) contextService.getContextSetupProvider();
        ContextSetup contextSetup = provider.getContextSetup();
        contextSetup.reloadProviders(Thread.currentThread().getContextClassLoader());
        assertAll(
            () -> assertTrue(contextSetup.isPropagated(Classloader)),
            () -> assertTrue(contextSetup.isPropagated(JNDI)),
            () -> assertTrue(contextSetup.isPropagated(Security)),
            () -> assertTrue(contextSetup.isPropagated(WorkArea)),
            () -> assertFalse(contextSetup.isPropagated(Remaining))
        );
    }


    @Test
    public void testParseContextInfo_empty() throws Exception {
        ConcurrentServiceCfg serviceConfig = new ConcurrentServiceCfg("concurrent/ctxSrv", null, false, null);
        ContextServiceCfg contextServiceConfig = new ContextServiceCfg(serviceConfig);
        ContextServiceImpl contextService = runtime.getContextService(contextServiceConfig);
        ContextSetupProviderImpl provider = (ContextSetupProviderImpl) contextService.getContextSetupProvider();
        ContextSetup contextSetup = provider.getContextSetup();
        contextSetup.reloadProviders(Thread.currentThread().getContextClassLoader());
        assertAll(
            () -> assertTrue(contextSetup.isPropagated(Classloader)),
            () -> assertTrue(contextSetup.isPropagated(JNDI)),
            () -> assertTrue(contextSetup.isPropagated(Security)),
            () -> assertTrue(contextSetup.isPropagated(WorkArea)),
            () -> assertFalse(contextSetup.isPropagated(Remaining))
        );
    }


    @Test
    public void testCreateManagedThreadFactory() throws Exception {
        final int THREAD_PRIORITY = 8;
        ConcurrentServiceCfg serviceConfig = new ConcurrentServiceCfg("concurrent/mtf", "Classloader, JNDI, Security", true, "context");
        expect(managedThreadFactoryCfg.getServiceConfig()).andReturn(serviceConfig).anyTimes();
        expect(managedThreadFactoryCfg.getThreadPriority()).andReturn(THREAD_PRIORITY).anyTimes();
        replay(managedThreadFactoryCfg);

        ManagedThreadFactoryImpl managedThreadFactory = runtime.getManagedThreadFactory(managedThreadFactoryCfg);
        ContextServiceImpl contextService = getField(managedThreadFactory, "contextService",
            ManagedThreadFactoryImpl.class);
        ContextSetupProviderImpl provider = (ContextSetupProviderImpl) contextService.getContextSetupProvider();
        ContextSetup contextSetup = provider.getContextSetup();
        contextSetup.reloadProviders(Thread.currentThread().getContextClassLoader());
        assertAll(
            () -> assertTrue(contextSetup.isPropagated(Classloader)),
            () -> assertTrue(contextSetup.isPropagated(JNDI)),
            () -> assertTrue(contextSetup.isPropagated(Security)),
            () -> assertFalse(contextSetup.isPropagated(WorkArea)),
            () -> assertFalse(contextSetup.isPropagated(Remaining))
        );

        int threadPriority = getField(managedThreadFactory, "priority", ManagedThreadFactoryImpl.class);
        assertEquals(THREAD_PRIORITY, threadPriority);
    }


    @Test
    public void testCreateManagedExecutorService() throws Exception {
        final int THREAD_PRIORITY = 3;
        final int HUNG_AFTER_SECONDS = 100;
        final int CORE_POOL_SIZE = 1;
        final int MAXIMUM_POOL_SIZE = 5;
        final boolean LONG_RUNNING_TASKS = true;
        final long KEEP_ALIVE_SECONDS = 88L;
        final long THREAD_LIFE_TIME_SECONDS = 99L;
        final int TASK_QUEUE_CAPACITY = 12345;
        ConcurrentServiceCfg serviceConfig = new ConcurrentServiceCfg("concurrent/mes", "Classloader, JNDI, Security", true, "context");
        expect(managedExecutorServiceCfg.getServiceConfig()).andReturn(serviceConfig).anyTimes();
        expect(managedExecutorServiceCfg.getThreadPriority()).andReturn(THREAD_PRIORITY).anyTimes();
        expect(managedExecutorServiceCfg.getHungAfterSeconds()).andReturn(HUNG_AFTER_SECONDS).anyTimes();
        expect(managedExecutorServiceCfg.getCorePoolSize()).andReturn(CORE_POOL_SIZE).anyTimes();
        expect(managedExecutorServiceCfg.getMaximumPoolSize()).andReturn(MAXIMUM_POOL_SIZE).anyTimes();
        expect(managedExecutorServiceCfg.isLongRunningTasks()).andReturn(LONG_RUNNING_TASKS).anyTimes();
        expect(managedExecutorServiceCfg.getKeepAliveSeconds()).andReturn(KEEP_ALIVE_SECONDS).anyTimes();
        expect(managedExecutorServiceCfg.getThreadLifeTimeSeconds()).andReturn(THREAD_LIFE_TIME_SECONDS).anyTimes();
        expect(managedExecutorServiceCfg.getTaskQueueCapacity()).andReturn(TASK_QUEUE_CAPACITY).anyTimes();
        replay(managedExecutorServiceCfg);

        ManagedExecutorServiceImpl mes = runtime.getManagedExecutorService(managedExecutorServiceCfg);
        ManagedThreadFactoryImpl managedThreadFactory = mes.getManagedThreadFactory();

        assertEquals(HUNG_AFTER_SECONDS * 1000, managedThreadFactory.getHungTaskThreshold());

        ManagedThreadPoolExecutor executor = getField(mes, "threadPoolExecutor");
        assertEquals(CORE_POOL_SIZE, executor.getCorePoolSize());
        assertEquals(KEEP_ALIVE_SECONDS, executor.getKeepAliveTime(TimeUnit.SECONDS));
        assertEquals(MAXIMUM_POOL_SIZE, executor.getMaximumPoolSize());

        long threadLifeTime = getField(executor, "threadLifeTime");
        assertEquals(THREAD_LIFE_TIME_SECONDS, threadLifeTime);

        ContextSetupProviderImpl provider = (ContextSetupProviderImpl) mes.getContextSetupProvider();
        ContextSetup contextSetup = provider.getContextSetup();
        contextSetup.reloadProviders(Thread.currentThread().getContextClassLoader());
        assertAll(
            () -> assertTrue(contextSetup.isPropagated(Classloader)),
            () -> assertTrue(contextSetup.isPropagated(JNDI)),
            () -> assertTrue(contextSetup.isPropagated(Security)),
            () -> assertFalse(contextSetup.isPropagated(WorkArea)),
            () -> assertFalse(contextSetup.isPropagated(Remaining))
        );

        int threadPriority = getField(managedThreadFactory, "priority", ManagedThreadFactoryImpl.class);
        assertEquals(THREAD_PRIORITY, threadPriority);
    }
}
