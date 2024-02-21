/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.server;

import com.sun.appserv.server.util.Version;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.single.StaticModulesRegistry;
import com.sun.enterprise.util.Result;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.glassfish.api.FutureProvider;
import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.hk2.runlevel.RunLevelContext;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.internal.AsyncRunLevelContext;
import org.glassfish.hk2.runlevel.internal.RunLevelControllerImpl;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorBuilder;
import org.glassfish.internal.api.InitRunLevel;
import org.glassfish.internal.api.PostStartupRunLevel;
import org.glassfish.kernel.event.EventsImpl;
import org.glassfish.main.core.apiexporter.APIExporterImpl;
import org.glassfish.server.ServerEnvironmentImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import static java.util.Collections.singletonList;
import static org.glassfish.api.admin.ServerEnvironment.Status.starting;
import static org.glassfish.api.event.EventTypes.PREPARE_SHUTDOWN;
import static org.glassfish.api.event.EventTypes.SERVER_READY;
import static org.glassfish.api.event.EventTypes.SERVER_SHUTDOWN;
import static org.glassfish.api.event.EventTypes.SERVER_STARTUP;
import static org.glassfish.hk2.runlevel.RunLevel.RUNLEVEL_MODE_META_TAG;
import static org.glassfish.hk2.runlevel.RunLevel.RUNLEVEL_VAL_META_TAG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AppServerStartup tests.
 * <p>
 * If test failed, look into logs, because exceptions are not thrown outside the {@link AppServerStartup}
 * implementation.
 *
 * @author Tom Beerbower
 */
public class AppServerStartupTest {

    /**
     * The AppServerStartup instance to test.
     */
    private AppServerStartup appServerStartup;

    /**
     * The test results.
     */
    private static Results results;

    /**
     * Map of exceptions to be thrown from the postConstruct.
     */
    private static Map<Class<?>, RuntimeException> mapPostConstructExceptions;

    /**
     * List of {@link Future}s returned from {@link FutureProvider#getFutures()} by the Startup services during progression
     * to the start up run level.
     */
    private static List<TestFuture> listFutures;

    private ServiceLocator testLocator;

    /**
     * Reset the results prior to each test.
     */
    @BeforeEach
    public void beforeTest() {
        testLocator = ServiceLocatorFactory.getInstance().create("AppServerStartupTest");
        DynamicConfigurationService dcs = testLocator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        config.addActiveDescriptor(BuilderHelper.createConstantDescriptor(new TestSystemTasks()));

        // These are services that would normally be started by hk2 core
        config.addActiveDescriptor(AppServerStartup.AppInstanceListener.class);

        AbstractActiveDescriptor<?> descriptor = BuilderHelper.createConstantDescriptor(new TestModulesRegistry());
        descriptor.addContractType(ModulesRegistry.class);
        config.addActiveDescriptor(descriptor);

        descriptor = BuilderHelper.createConstantDescriptor(new ExecutorServiceFactory().provide());
        descriptor.addContractType(ExecutorService.class);
        config.addActiveDescriptor(descriptor);

        config.addActiveDescriptor(BuilderHelper.createConstantDescriptor(new ServerEnvironmentImpl()));
        config.addActiveDescriptor(BuilderHelper.createConstantDescriptor(new EventsImpl()));
        config.addActiveDescriptor(BuilderHelper.createConstantDescriptor(new Version()));
        config.addActiveDescriptor(BuilderHelper.createConstantDescriptor(new StartupContext()));

        config.bind(BuilderHelper.link(RunLevelControllerImpl.class).to(RunLevelController.class).build());

        config.addUnbindFilter(BuilderHelper.createContractFilter(RunLevelContext.class.getName()));
        config.bind(BuilderHelper.link(RunLevelContext.class).named(RunLevelContext.CONTEXT_NAME).to(Context.class).in(Singleton.class)
                .build());

        config.addUnbindFilter(BuilderHelper.createContractFilter(AsyncRunLevelContext.class.getName()));
        config.bind(BuilderHelper.link(AsyncRunLevelContext.class).in(Singleton.class).build());

        config.bind(BuilderHelper.link(AppServerStartup.class).build());

        descriptor = BuilderHelper.createConstantDescriptor(testLocator);
        descriptor.addContractType(ServiceLocator.class);
        config.addActiveDescriptor(descriptor);

        bindService(config, TestInitRunLevelService.class);
        bindService(config, TestStartupService.class);
        bindService(config, TestStartupRunLevelService.class);
        bindService(config, TestPostStartupRunLevelService.class);

        bindService(config, CommonClassLoaderServiceImpl.class);
        bindService(config, APIClassLoaderServiceImpl.class);

        bindService(config, APIExporterImpl.class);
        config.commit();

        appServerStartup = testLocator.getService(AppServerStartup.class);
        assertNotNull(appServerStartup);

        mapPostConstructExceptions = new HashMap<>();
        listFutures = new LinkedList<>();
        results = new Results(appServerStartup.runLevelController);

        appServerStartup.events.register(results);
        assertEquals(starting, appServerStartup.serverEnvironment.getStatus());
    }

    /**
     * Ensure that things are stopped after the test... if not then call stop.
     */
    @AfterEach
    public void afterTest() {
        if (appServerStartup != null) {
            // Force a stop to ensure that the services are released
            if (appServerStartup.runLevelController.getCurrentRunLevel() > 0) {
                appServerStartup.serverEnvironment.setStatus(ServerEnvironment.Status.started);
                appServerStartup.stop();
            }

            appServerStartup.events.unregister(results);
        }

        results = null;
        listFutures = null;
        mapPostConstructExceptions = null;

        ServiceLocatorFactory.getInstance().destroy(testLocator);
        testLocator = null;
    }

    /**
     * Call the {@link AppServerStartup#run} method and make sure that the run level services are constructed and destroyed
     * at the proper run levels.
     */
    @Test
    public void testRunLevelServices() {
        // Create the list of Futures returned from TestStartupService
        listFutures.add(new TestFuture());
        listFutures.add(new TestFuture());
        listFutures.add(new TestFuture());

        testRunAppServerStartup();

        assertEquals(ServerEnvironment.Status.started, appServerStartup.serverEnvironment.getStatus());

        assertThat(results.getListEvents(), hasSize(2));
        assertEquals(SERVER_STARTUP, results.getListEvents().get(0));
        assertEquals(SERVER_READY, results.getListEvents().get(1));

        // assert that the run level services have been constructed
        assertTrue(results.isConstructed(TestInitRunLevelService.class, InitRunLevel.VAL));
        assertTrue(results.isConstructed(TestStartupService.class, StartupRunLevel.VAL));
        assertTrue(results.isConstructed(TestStartupRunLevelService.class, StartupRunLevel.VAL));
        assertTrue(results.isConstructed(TestPostStartupRunLevelService.class, PostStartupRunLevel.VAL));

        appServerStartup.stop();
        assertEquals(ServerEnvironment.Status.stopped, appServerStartup.serverEnvironment.getStatus());

        assertThat(results.getListEvents(), hasSize(4));
        assertEquals(PREPARE_SHUTDOWN, results.getListEvents().get(2));
        assertEquals(SERVER_SHUTDOWN, results.getListEvents().get(3));

        // assert that the run level services have been destroyed
        assertTrue(results.isDestroyed(TestPostStartupRunLevelService.class, PostStartupRunLevel.VAL));
        assertTrue(results.isDestroyed(TestStartupService.class, StartupRunLevel.VAL));
        assertTrue(results.isDestroyed(TestStartupRunLevelService.class, StartupRunLevel.VAL));
        assertTrue(results.isDestroyed(TestInitRunLevelService.class, InitRunLevel.VAL));
    }

    /**
     * Test the {@link AppServerStartup#run} method with an exception thrown from an init service that should cause a
     * failure during init. Make sure that the init run level services are constructed at the proper run levels.
     */
    @Test
    public void testRunLevelServicesWithInitException() {
        testRunLevelServicesWithException(TestInitRunLevelService.class);

        // make sure that the server has not been started
        assertEquals(starting, appServerStartup.serverEnvironment.getStatus());

        // assert that the run level services have been constructed
        assertTrue(results.isConstructed(TestInitRunLevelService.class, InitRunLevel.VAL));
        // assert that startup & post-startup services are not constructed since the failure occurs during init
        assertFalse(results.isConstructed(TestStartupService.class));
        assertFalse(results.isConstructed(TestStartupRunLevelService.class));
        assertFalse(results.isConstructed(TestPostStartupRunLevelService.class));
    }

    /**
     * Test the {@link AppServerStartup#run} method with an exception thrown from a startup service that should cause a
     * failure during startup. Make sure that the init and startup run level services are constructed at the proper run
     * levels.
     */
    @Test
    public void testRunLevelServicesWithStartupException() {
        testRunLevelServicesWithException(TestStartupService.class);

        // make sure that the server has not been started
        assertEquals(starting, appServerStartup.serverEnvironment.getStatus());

        assertTrue(results.getListEvents().contains(SERVER_SHUTDOWN));

        // assert that the run level services have been constructed
        assertTrue(results.isConstructed(TestInitRunLevelService.class, InitRunLevel.VAL));
        assertTrue(results.isConstructed(TestStartupService.class, StartupRunLevel.VAL));
        assertTrue(results.isConstructed(TestStartupRunLevelService.class, StartupRunLevel.VAL));

        // assert that the post-startup service is not constructed since shutdown occurs during startup
        assertFalse(results.isConstructed(TestPostStartupRunLevelService.class));
    }

    /**
     * Test the {@link AppServerStartup#run} method with an exception thrown from a post-startup service that should cause a
     * failure during post-startup. Make sure that the run level services are constructed at the proper run levels.
     */
    @Test
    public void testRunLevelServicesWithPostStartupException() {
        testRunLevelServicesWithException(TestPostStartupRunLevelService.class);

        assertEquals(ServerEnvironment.Status.started, appServerStartup.serverEnvironment.getStatus());

        // assert that the run level services have been constructed
        assertTrue(results.isConstructed(TestInitRunLevelService.class, InitRunLevel.VAL));
        assertTrue(results.isConstructed(TestStartupService.class, StartupRunLevel.VAL));
        assertTrue(results.isConstructed(TestStartupRunLevelService.class, StartupRunLevel.VAL));
        assertTrue(results.isConstructed(TestPostStartupRunLevelService.class, PostStartupRunLevel.VAL));
    }

    /**
     * Test the {@link AppServerStartup#run} method with an exception thrown from a {@link Future} should cause a failed
     * result during startup. Make sure that the init and startup run level services are constructed at the proper run
     * levels. Also ensure that the failed {@link Future} causes a shutdown.
     */
    @Test
    public void testRunLevelServicesWithFuturesException() {

        // create the list of Futures returned from TestStartupService
        listFutures.add(new TestFuture());
        listFutures.add(new TestFuture(new Exception("Exception from Future.")));
        listFutures.add(new TestFuture());

        testRunAppServerStartup();

        // make sure that the server has not been started
        assertEquals(starting, appServerStartup.serverEnvironment.getStatus());

        assertThat(results.getListEvents(), hasItem(SERVER_SHUTDOWN));

        // assert that the run level services have been constructed
        assertTrue(results.isConstructed(TestInitRunLevelService.class, InitRunLevel.VAL));
        assertTrue(results.isConstructed(TestStartupService.class, StartupRunLevel.VAL));
        assertTrue(results.isConstructed(TestStartupRunLevelService.class, StartupRunLevel.VAL));

        // assert that the post-startup service is not constructed since shutdown occurs during startup
        assertFalse(results.isConstructed(TestPostStartupRunLevelService.class));
    }

    private void bindService(DynamicConfiguration configurator, Class<?> service) {
        final DescriptorBuilder descriptorBuilder = BuilderHelper.link(service);

        final RunLevel runLevel = service.getAnnotation(RunLevel.class);
        if (runLevel != null) {
            descriptorBuilder.to(RunLevel.class)
                    .has(RUNLEVEL_VAL_META_TAG, singletonList(((Integer) runLevel.value()).toString()))
                    .has(RUNLEVEL_MODE_META_TAG, singletonList(((Integer) runLevel.mode()).toString()));

            descriptorBuilder.in(RunLevel.class);
        }

        Class<?> clazz = service;
        while (clazz != null) {
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> element : interfaces) {
                descriptorBuilder.to(element);
            }
            clazz = clazz.getSuperclass();
        }

        final Named named = service.getAnnotation(Named.class);
        if (named != null) {
            descriptorBuilder.named(named.value());
        }

        configurator.bind(descriptorBuilder.build());
    }

    /**
     * Helper method to call {@link AppServerStartup#run()}. Sets up an exception to be thrown from
     * {@link PostConstruct#postConstruct()} of the given class.
     *
     * @param badServiceClass the service class that the exception will be thrown from
     */
    private void testRunLevelServicesWithException(Class badServiceClass) {
        // set an exception to be thrown from TestStartupService.postConstruct()
        mapPostConstructExceptions.put(badServiceClass,
                new RuntimeException("Exception from " + badServiceClass.getSimpleName() + ".postConstruct"));

        // create the list of Futures returned from TestStartupService
        listFutures.add(new TestFuture());

        testRunAppServerStartup();
    }

    /**
     * Helper method to run the app server after asserting that the results are clean.
     */
    private void testRunAppServerStartup() {
        // assert that we have clean results to start
        assertFalse(results.isConstructed(TestInitRunLevelService.class));
        assertFalse(results.isConstructed(TestStartupService.class));
        assertFalse(results.isConstructed(TestStartupRunLevelService.class));
        assertFalse(results.isConstructed(TestPostStartupRunLevelService.class));

        appServerStartup.run();
    }

    /**
     * Test results
     */
    private static class Results implements EventListener {
        /**
         * Map of constructed run level services to run levels.
         */
        private final Map<Class<?>, Integer> mapConstructedLevels = new HashMap<>();

        /**
         * Map of destroyed run level services to run levels.
         */
        private final Map<Class<?>, Integer> mapDestroyedLevels = new HashMap<>();

        /**
         * List of server events.
         */
        private final List<EventTypes<?>> listEvents = new LinkedList<>();

        /**
         * The run level service.
         */
        private final RunLevelController RunLevelController
        ;

        public Results(RunLevelController rls) {
            this.RunLevelController = rls;
        }

        public void recordConstruction(Class<?> cl) {
            mapConstructedLevels.put(cl, RunLevelController.getCurrentProceeding().getProposedLevel());
        }

        public void recordDestruction(Class<?> cl) {
            mapDestroyedLevels.put(cl, RunLevelController.getCurrentRunLevel() + 1);
        }

        public boolean isConstructed(Class<?> cl) {
            return mapConstructedLevels.keySet().contains(cl);
        }

        public boolean isConstructed(Class<?> cl, Integer runLevel) {
            Integer recLevel = mapConstructedLevels.get(cl);
            return recLevel != null && recLevel.equals(runLevel);
        }

        public boolean isDestroyed(Class<?> cl) {
            return mapDestroyedLevels.keySet().contains(cl);
        }

        public boolean isDestroyed(Class<?> cl, Integer runLevel) {
            Integer recLevel = mapDestroyedLevels.get(cl);
            return recLevel != null && recLevel.equals(runLevel);
        }

        public List<EventTypes<?>> getListEvents() {
            return listEvents;
        }

        @Override
        public void event(Event<?> event) {
            listEvents.add(event.type());
        }
    }

    // ----- test services inner classes -------------------------------------

    /**
     * Abstract service that will update the test results from {@link PostConstruct#postConstruct()}.
     */
    public static abstract class TestService implements PostConstruct, PreDestroy {
        @Override
        public void postConstruct() {
            AppServerStartupTest.results.recordConstruction(this.getClass());
            if (mapPostConstructExceptions != null) {
                RuntimeException ex = mapPostConstructExceptions.get(getClass());
                if (ex != null) {
                    throw ex;
                }
            }
        }

        @Override
        public void preDestroy() {
            AppServerStartupTest.results.recordDestruction(this.getClass());
        }
    }

    /**
     * Init service annotated with the new style {@link InitRunLevel} annotation.
     */
    @Service
    @RunLevel(InitRunLevel.VAL)
    public static class TestInitRunLevelService extends TestService {
    }

    /**
     * Startup service that implements the old style Startup interface.
     */
    @RunLevel(StartupRunLevel.VAL)
    @Service
    public static class TestStartupService extends TestService implements FutureProvider {
        // Make sure the other one starts first
        @Inject
        private TestStartupRunLevelService dependency;

        @Override
        public List getFutures() {
            return listFutures;
        }
    }

    /**
     * Startup service annotated with the new style {@link StartupRunLevel} annotation.
     */
    @Service
    @RunLevel(StartupRunLevel.VAL)
    public static class TestStartupRunLevelService extends TestService {
    }

    /**
     * Post-startup service annotated with the new style {@link PostStartupRunLevel} annotation.
     */
    @Service
    @RunLevel(PostStartupRunLevel.VAL)
    public static class TestPostStartupRunLevelService extends TestService {
    }

    // ----- TestSystemTasks inner classes -----------------------------------

    /**
     * Test {@link SystemTasks} implementation.
     */
    public static class TestSystemTasks implements SystemTasks {
        @Override
        public void writePidFile() {
            // do nothing.
        }
    }

    // ----- TestModulesRegistry inner classes -------------------------------

    /**
     * Test {@link ModulesRegistry} implementation.
     */
    public static class TestModulesRegistry extends StaticModulesRegistry {

        public TestModulesRegistry() {
            super(TestModulesRegistry.class.getClassLoader());
        }
    }

    // ----- TestFuture inner classes ----------------------------------------

    /**
     * Future implementation used for test Startup implementations that also implement {@link FutureProvider}.
     */
    public static class TestFuture implements Future<Result<Thread>> {

        private boolean canceled;
        private boolean done;
        private Exception resultException;

        public TestFuture() {
        }

        public TestFuture(Exception resultException) {
            this.resultException = resultException;
        }

        @Override
        public boolean cancel(boolean b) {
            if (done) {
                return false;
            }
            canceled = done = true;
            return true;
        }

        @Override
        public boolean isCancelled() {
            return canceled;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public Result<Thread> get() throws InterruptedException, ExecutionException {

            Result<Thread> result = resultException == null ? new Result<>(Thread.currentThread()) : new Result<>(resultException);
            done = true;

            return result;
        }

        @Override
        public Result<Thread> get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }
    }
}
