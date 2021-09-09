/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import org.glassfish.concurrent.runtime.deployer.ContextServiceConfig;
import org.glassfish.concurrent.runtime.deployer.ManagedExecutorServiceConfig;
import org.glassfish.concurrent.runtime.deployer.ManagedThreadFactoryConfig;
import org.glassfish.enterprise.concurrent.ContextServiceImpl;
import org.glassfish.enterprise.concurrent.ManagedExecutorServiceImpl;
import org.glassfish.enterprise.concurrent.ManagedThreadFactoryImpl;
import org.glassfish.enterprise.concurrent.internal.ManagedThreadPoolExecutor;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.glassfish.tests.utils.ReflectionUtils.getField;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConcurrentRuntimeTest {

    private ContextServiceConfig contextServiceConfig;
    private ManagedThreadFactoryConfig managedThreadFactoryConfig;
    private ManagedExecutorServiceConfig managedExecutorServiceConfig;

    @BeforeEach
    public void before() {
        contextServiceConfig = createMock(ContextServiceConfig.class);
        managedThreadFactoryConfig = createMock(ManagedThreadFactoryConfig.class);
        managedExecutorServiceConfig = createMock(ManagedExecutorServiceConfig.class);
    }

    @Test
    public void testParseContextInfo() throws Exception {
        expect(contextServiceConfig.getJndiName()).andReturn("concurrent/ctxSrv").anyTimes();
        expect(contextServiceConfig.getContextInfo()).andReturn("Classloader, JNDI, Security, WorkArea").anyTimes();
        expect(contextServiceConfig.getContextInfoEnabled()).andReturn("true");
        replay(contextServiceConfig);

        ConcurrentRuntime concurrentRuntime = new ConcurrentRuntime();

        ResourceInfo resource = new ResourceInfo("test");
        ContextServiceImpl contextService = concurrentRuntime.getContextService(resource, contextServiceConfig);
        ContextSetupProviderImpl contextSetupProvider = (ContextSetupProviderImpl) contextService.getContextSetupProvider();
        assertTrue((Boolean) getField(contextSetupProvider, "classloading"));
        assertTrue((Boolean) getField(contextSetupProvider, "naming"));
        assertTrue((Boolean) getField(contextSetupProvider, "security"));
        assertTrue((Boolean) getField(contextSetupProvider, "workArea"));
    }

    @Test
    public void testParseContextInfo_lowerCase() throws Exception {
        expect(contextServiceConfig.getJndiName()).andReturn("concurrent/ctxSrv").anyTimes();
        expect(contextServiceConfig.getContextInfo()).andReturn("classloader, jndi, security, workarea").anyTimes();
        expect(contextServiceConfig.getContextInfoEnabled()).andReturn("true");
        replay(contextServiceConfig);

        ConcurrentRuntime concurrentRuntime = new ConcurrentRuntime();

        ResourceInfo resource = new ResourceInfo("test");
        ContextServiceImpl contextService = concurrentRuntime.getContextService(resource, contextServiceConfig);
        ContextSetupProviderImpl contextSetupProvider = (ContextSetupProviderImpl) contextService.getContextSetupProvider();
        assertTrue((Boolean) getField(contextSetupProvider, "classloading"));
        assertTrue((Boolean) getField(contextSetupProvider, "naming"));
        assertTrue((Boolean) getField(contextSetupProvider, "security"));
        assertTrue((Boolean) getField(contextSetupProvider, "workArea"));
    }

    @Test
    public void testParseContextInfo_upperCase() throws Exception {
        expect(contextServiceConfig.getJndiName()).andReturn("concurrent/ctxSrv").anyTimes();
        expect(contextServiceConfig.getContextInfo()).andReturn("CLASSLOADER, JNDI, SECURITY, WORKAREA").anyTimes();
        expect(contextServiceConfig.getContextInfoEnabled()).andReturn("true");
        replay(contextServiceConfig);

        ConcurrentRuntime concurrentRuntime = new ConcurrentRuntime();

        ResourceInfo resource = new ResourceInfo("test");
        ContextServiceImpl contextService = concurrentRuntime.getContextService(resource, contextServiceConfig);
        ContextSetupProviderImpl contextSetupProvider = (ContextSetupProviderImpl) contextService.getContextSetupProvider();
        assertTrue((Boolean) getField(contextSetupProvider, "classloading"));
        assertTrue((Boolean) getField(contextSetupProvider, "naming"));
        assertTrue((Boolean) getField(contextSetupProvider, "security"));
        assertTrue((Boolean) getField(contextSetupProvider, "workArea"));
    }

    @Test
    public void testParseContextInfo_disabled() throws Exception {
        expect(contextServiceConfig.getJndiName()).andReturn("concurrent/ctxSrv").anyTimes();
        expect(contextServiceConfig.getContextInfo()).andReturn("Classloader, JNDI, Security, WorkArea").anyTimes();
        expect(contextServiceConfig.getContextInfoEnabled()).andReturn("false");
        replay(contextServiceConfig);

        ConcurrentRuntime concurrentRuntime = new ConcurrentRuntime();

        ResourceInfo resource = new ResourceInfo("test");
        ContextServiceImpl contextService = concurrentRuntime.getContextService(resource, contextServiceConfig);
        ContextSetupProviderImpl contextSetupProvider = (ContextSetupProviderImpl) contextService.getContextSetupProvider();
        assertFalse((Boolean) getField(contextSetupProvider, "classloading"));
        assertFalse((Boolean) getField(contextSetupProvider, "naming"));
        assertFalse((Boolean) getField(contextSetupProvider, "security"));
        assertFalse((Boolean) getField(contextSetupProvider, "workArea"));
    }

    @Test
    public void testParseContextInfo_invalid() throws Exception {
        expect(contextServiceConfig.getJndiName()).andReturn("concurrent/ctxSrv").anyTimes();
        expect(contextServiceConfig.getContextInfo()).andReturn("JNDI, blah, beh, JNDI, WorkArea, WorkArea, ").anyTimes();
        expect(contextServiceConfig.getContextInfoEnabled()).andReturn("true");
        replay(contextServiceConfig);

        ConcurrentRuntime concurrentRuntime = new ConcurrentRuntime();

        ResourceInfo resource = new ResourceInfo("test");
        ContextServiceImpl contextService = concurrentRuntime.getContextService(resource, contextServiceConfig);
        ContextSetupProviderImpl contextSetupProvider = (ContextSetupProviderImpl) contextService.getContextSetupProvider();
        assertFalse((Boolean) getField(contextSetupProvider, "classloading"));
        assertTrue((Boolean) getField(contextSetupProvider, "naming"));
        assertFalse((Boolean) getField(contextSetupProvider, "security"));
        assertTrue((Boolean) getField(contextSetupProvider, "workArea"));
    }

    @Test
    public void testParseContextInfo_null() throws Exception {
        expect(contextServiceConfig.getJndiName()).andReturn("concurrent/ctxSrv").anyTimes();
        expect(contextServiceConfig.getContextInfo()).andReturn(null).anyTimes();
        expect(contextServiceConfig.getContextInfoEnabled()).andReturn("true");
        replay(contextServiceConfig);

        ConcurrentRuntime concurrentRuntime = new ConcurrentRuntime();

        ResourceInfo resource = new ResourceInfo("test");
        ContextServiceImpl contextService = concurrentRuntime.getContextService(resource, contextServiceConfig);
        ContextSetupProviderImpl contextSetupProvider = (ContextSetupProviderImpl) contextService.getContextSetupProvider();
        assertTrue((Boolean) getField(contextSetupProvider, "classloading"));
        assertTrue((Boolean) getField(contextSetupProvider, "naming"));
        assertTrue((Boolean) getField(contextSetupProvider, "security"));
        assertTrue((Boolean) getField(contextSetupProvider, "workArea"));
    }

    @Test
    public void testParseContextInfo_empty() throws Exception {
        expect(contextServiceConfig.getJndiName()).andReturn("concurrent/ctxSrv").anyTimes();
        expect(contextServiceConfig.getContextInfo()).andReturn("").anyTimes();
        expect(contextServiceConfig.getContextInfoEnabled()).andReturn("true");
        replay(contextServiceConfig);

        ConcurrentRuntime concurrentRuntime = new ConcurrentRuntime();

        ResourceInfo resource = new ResourceInfo("test");
        ContextServiceImpl contextService = concurrentRuntime.getContextService(resource, contextServiceConfig);
        ContextSetupProviderImpl contextSetupProvider = (ContextSetupProviderImpl) contextService.getContextSetupProvider();
        assertFalse((Boolean) getField(contextSetupProvider, "classloading"));
        assertFalse((Boolean) getField(contextSetupProvider, "naming"));
        assertFalse((Boolean) getField(contextSetupProvider, "security"));
        assertFalse((Boolean) getField(contextSetupProvider, "workArea"));
    }

    @Test
    public void testCreateManagedThreadFactory() throws Exception {
        final int THREAD_PRIORITY = 8;

        expect(managedThreadFactoryConfig.getJndiName()).andReturn("concurrent/mtf").anyTimes();
        expect(managedThreadFactoryConfig.getContextInfo()).andReturn("Classloader, JNDI, Security").anyTimes();
        expect(managedThreadFactoryConfig.getContextInfoEnabled()).andReturn("true").anyTimes();
        expect(managedThreadFactoryConfig.getThreadPriority()).andReturn(THREAD_PRIORITY).anyTimes();
        replay(managedThreadFactoryConfig);

        ConcurrentRuntime concurrentRuntime = new ConcurrentRuntime();

        ResourceInfo resource = new ResourceInfo("test");
        ManagedThreadFactoryImpl managedThreadFactory = concurrentRuntime.getManagedThreadFactory(resource, managedThreadFactoryConfig);
        ContextServiceImpl contextService = (ContextServiceImpl) getField(managedThreadFactory, "contextService");
        ContextSetupProviderImpl contextSetupProvider = (ContextSetupProviderImpl) contextService.getContextSetupProvider();
        assertTrue((Boolean) getField(contextSetupProvider, "classloading"));
        assertTrue((Boolean) getField(contextSetupProvider, "naming"));
        assertTrue((Boolean) getField(contextSetupProvider, "security"));
        assertFalse((Boolean) getField(contextSetupProvider, "workArea"));

        int threadPriority = (Integer)getField(managedThreadFactory, "priority");
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


        expect(managedExecutorServiceConfig.getJndiName()).andReturn("concurrent/mes").anyTimes();
        expect(managedExecutorServiceConfig.getContextInfo()).andReturn("Classloader, JNDI, Security").anyTimes();
        expect(managedExecutorServiceConfig.getContextInfoEnabled()).andReturn("true").anyTimes();
        expect(managedExecutorServiceConfig.getThreadPriority()).andReturn(THREAD_PRIORITY).anyTimes();
        expect(managedExecutorServiceConfig.getHungAfterSeconds()).andReturn(HUNG_AFTER_SECONDS).anyTimes();
        expect(managedExecutorServiceConfig.getCorePoolSize()).andReturn(CORE_POOL_SIZE).anyTimes();
        expect(managedExecutorServiceConfig.getMaximumPoolSize()).andReturn(MAXIMUM_POOL_SIZE).anyTimes();
        expect(managedExecutorServiceConfig.isLongRunningTasks()).andReturn(LONG_RUNNING_TASKS).anyTimes();
        expect(managedExecutorServiceConfig.getKeepAliveSeconds()).andReturn(KEEP_ALIVE_SECONDS).anyTimes();
        expect(managedExecutorServiceConfig.getThreadLifeTimeSeconds()).andReturn(THREAD_LIFE_TIME_SECONDS).anyTimes();
        expect(managedExecutorServiceConfig.getTaskQueueCapacity()).andReturn(TASK_QUEUE_CAPACITY).anyTimes();
        replay(managedExecutorServiceConfig);

        ConcurrentRuntime concurrentRuntime = new ConcurrentRuntime();

        ResourceInfo resource = new ResourceInfo("test");
        ManagedExecutorServiceImpl mes = concurrentRuntime.getManagedExecutorService(resource, managedExecutorServiceConfig);

        ManagedThreadFactoryImpl managedThreadFactory = mes.getManagedThreadFactory();

        assertEquals(HUNG_AFTER_SECONDS * 1000, managedThreadFactory.getHungTaskThreshold());

        ManagedThreadPoolExecutor executor = (ManagedThreadPoolExecutor) getField(mes, "threadPoolExecutor");
        assertEquals(CORE_POOL_SIZE, executor.getCorePoolSize());
        assertEquals(KEEP_ALIVE_SECONDS, executor.getKeepAliveTime(TimeUnit.SECONDS));
        assertEquals(MAXIMUM_POOL_SIZE, executor.getMaximumPoolSize());

        long threadLifeTime = (Long)getField(executor, "threadLifeTime");
        assertEquals(THREAD_LIFE_TIME_SECONDS, threadLifeTime);

        ContextSetupProviderImpl contextSetupProvider = (ContextSetupProviderImpl) mes.getContextSetupProvider();
        assertTrue((Boolean) getField(contextSetupProvider, "classloading"));
        assertTrue((Boolean) getField(contextSetupProvider, "naming"));
        assertTrue((Boolean) getField(contextSetupProvider, "security"));
        assertFalse((Boolean) getField(contextSetupProvider, "workArea"));

        int threadPriority = (Integer)getField(managedThreadFactory, "priority");
        assertEquals(THREAD_PRIORITY, threadPriority);
    }
}
