/*
 * Copyright (c) 2021-2025 Contributors to the Eclipse Foundation
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

import org.glassfish.concurrent.config.ContextService;
import org.glassfish.concurrent.config.ManagedExecutorService;
import org.glassfish.concurrent.config.ManagedThreadFactory;
import org.glassfish.concurrent.runtime.deployer.cfg.ContextServiceCfg;
import org.glassfish.concurrent.runtime.deployer.cfg.ManagedExecutorServiceCfg;
import org.glassfish.concurrent.runtime.deployer.cfg.ManagedThreadFactoryCfg;
import org.glassfish.concurro.AbstractManagedExecutorService;
import org.glassfish.concurro.ContextServiceImpl;
import org.glassfish.concurro.ManagedThreadFactoryImpl;
import org.glassfish.concurro.internal.ManagedThreadPoolExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.sun.enterprise.deployment.types.StandardContextType.Classloader;
import static com.sun.enterprise.deployment.types.StandardContextType.JNDI;
import static com.sun.enterprise.deployment.types.StandardContextType.Remaining;
import static com.sun.enterprise.deployment.types.StandardContextType.Security;
import static com.sun.enterprise.deployment.types.StandardContextType.WorkArea;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.glassfish.tests.utils.ReflectionUtils.getField;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ConcurrentRuntimeTest {

    private ConcurrentRuntime runtime;

    @BeforeEach
    public void before() {
        runtime = new ConcurrentRuntime();
    }


    @Test
    public void testParseContextInfo() throws Exception {
        ContextService serviceConfig = createMock(ContextService.class);
        expect(serviceConfig.getContextInfo()).andReturn(null).anyTimes();
        expect(serviceConfig.getContextInfoEnabled()).andReturn("true").anyTimes();
        expect(serviceConfig.getJndiName()).andReturn("concurrent/ctxSrv").anyTimes();
        replay(serviceConfig);

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
            () -> assertTrue(contextSetup.isPropagated(Remaining))
        );
    }


    @Test
    public void testCreateManagedThreadFactory() throws Exception {
        ManagedThreadFactory managedThreadFactoryCfg = createMock(ManagedThreadFactory.class);
        expect(managedThreadFactoryCfg.getContext()).andReturn("context").anyTimes();
        expect(managedThreadFactoryCfg.getContextInfo()).andReturn("Classloader, jndi, Security").anyTimes();
        expect(managedThreadFactoryCfg.getContextInfoEnabled()).andReturn("true").anyTimes();
        expect(managedThreadFactoryCfg.getJndiName()).andReturn("concurrent/ctxSrv").anyTimes();
        expect(managedThreadFactoryCfg.getThreadPriority()).andReturn("5").anyTimes();
        expect(managedThreadFactoryCfg.getUseVirtualThreads()).andReturn("false").anyTimes();
        replay(managedThreadFactoryCfg);

        ManagedThreadFactoryCfg cfg = new ManagedThreadFactoryCfg(managedThreadFactoryCfg);
        ManagedThreadFactoryImpl managedThreadFactory = runtime.getManagedThreadFactory(cfg);
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
        assertEquals(5, threadPriority);
    }


    @Test
    public void testCreateManagedExecutorService() throws Exception {
        ManagedExecutorService config = createMock(ManagedExecutorService.class);
        expect(config.getContext()).andReturn("context").anyTimes();
        expect(config.getContextInfo()).andReturn("Classloader, jndi, Security").anyTimes();
        expect(config.getContextInfoEnabled()).andReturn("true").anyTimes();
        expect(config.getJndiName()).andReturn("concurrent/ctxSrv").anyTimes();
        expect(config.getHungAfterSeconds()).andReturn("100").anyTimes();
        expect(config.getHungLoggerPrintOnce()).andReturn("true").anyTimes();
        expect(config.getHungLoggerInitialDelaySeconds()).andReturn("true").anyTimes();
        expect(config.getHungLoggerIntervalSeconds()).andReturn("true").anyTimes();
        expect(config.getLongRunningTasks()).andReturn("true").anyTimes();
        expect(config.getTaskQueueCapacity()).andReturn("12345").anyTimes();
        expect(config.getCorePoolSize()).andReturn("1").anyTimes();
        expect(config.getMaximumPoolSize()).andReturn("5").anyTimes();
        expect(config.getKeepAliveSeconds()).andReturn("88").anyTimes();
        expect(config.getThreadPriority()).andReturn("3").anyTimes();
        expect(config.getThreadLifetimeSeconds()).andReturn("99").anyTimes();
        expect(config.getUseVirtualThreads()).andReturn("false").anyTimes();
        replay(config);

        ManagedExecutorServiceCfg managedExecutorServiceCfg = new ManagedExecutorServiceCfg(config);
        AbstractManagedExecutorService mes = runtime.getManagedExecutorService(managedExecutorServiceCfg);
        ManagedThreadFactoryImpl managedThreadFactory = mes.getManagedThreadFactory();

        assertEquals(100_000L, managedThreadFactory.getHungTaskThreshold());

        ManagedThreadPoolExecutor executor = getField(mes, "threadPoolExecutor");
        assertEquals(1, executor.getCorePoolSize());
        assertEquals(88, executor.getKeepAliveTime(TimeUnit.SECONDS));
        assertEquals(5, executor.getMaximumPoolSize());

        long threadLifeTime = getField(executor, "threadLifeTime");
        assertEquals(99, threadLifeTime);

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
        assertEquals(3, threadPriority);
    }
}
