/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import static com.sun.enterprise.glassfish.bootstrap.Constants.NO_FORCED_SHUTDOWN;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.glassfish.api.admin.ProcessEnvironment.ProcessType.Embedded;
import static org.glassfish.api.admin.ProcessEnvironment.ProcessType.Server;
import static org.glassfish.api.admin.ServerEnvironment.Status.stopped;
import static org.glassfish.api.admin.ServerEnvironment.Status.stopping;
import static org.glassfish.api.event.EventTypes.SERVER_SHUTDOWN;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.FutureProvider;
import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.event.EventListener.Event;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.common.util.Constants;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InstanceLifecycleEvent;
import org.glassfish.hk2.api.InstanceLifecycleEventType;
import org.glassfish.hk2.api.InstanceLifecycleListener;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.ChangeableRunLevelFuture;
import org.glassfish.hk2.runlevel.ErrorInformation;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.RunLevelFuture;
import org.glassfish.hk2.runlevel.RunLevelListener;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.internal.api.InitRunLevel;
import org.glassfish.internal.api.InternalSystemAdministrator;
import org.glassfish.internal.api.PostStartupRunLevel;
import org.glassfish.kernel.KernelLoggerInfo;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Service;

import com.sun.appserv.server.util.Version;
import com.sun.enterprise.module.HK2Module;
import com.sun.enterprise.module.ModuleState;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.util.Result;
import com.sun.enterprise.v3.common.DoNothingActionReporter;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

/**
 * Main class for Glassfish startup This class spawns a non-daemon Thread when the start() is called. Having a
 * non-daemon thread allows us to control lifecycle of server JVM. The thead is stopped when stop() is called.
 *
 * @author Jerome Dochez, sahoo@sun.com
 */
@Service
@Rank(Constants.DEFAULT_IMPLEMENTATION_RANK) // This should be the default impl if no name is specified
public class AppServerStartup implements PostConstruct, ModuleStartup {

    StartupContext context;

    final static Logger logger = KernelLoggerInfo.getLogger();

    final static Level level = Level.FINE;

    @Inject
    ServerEnvironmentImpl serverEnvironment;

    @Inject
    ServiceLocator serviceLocator;

    @Inject
    ModulesRegistry systemRegistry;

    @Override
    @Inject
    public void setStartupContext(StartupContext context) {
        this.context = context;
    }

    @Inject
    ExecutorService executor;

    @Inject
    Events events;

    @Inject
    CommonClassLoaderServiceImpl commonCLS;

    @Inject
    SystemTasks pidWriter;

    @Inject
    RunLevelController runLevelController;

    @Inject
    Provider<CommandRunner> commandRunnerProvider;

    @Inject
    private AppInstanceListener appInstanceListener;

    private MasterRunLevelListener masterListener;

    private long platformInitTime;

    private String platform = System.getProperty("GlassFish_Platform");

    /**
     * A keep alive thread that keeps the server JVM from going down as long as GlassFish kernel is up.
     */
    private Thread serverThread;
    private boolean shutdownSignal = false;

    private final static String THREAD_POLICY_PROPERTY = "org.glassfish.startupThreadPolicy";
    private final static String MAX_STARTUP_THREAD_PROPERTY = "org.glassfish.maxStartupThreads";

    private final static String POLICY_FULLY_THREADED = "FULLY_THREADED";
    private final static String POLICY_USE_NO_THREADS = "USE_NO_THREADS";

    private final static int DEFAULT_STARTUP_THREADS = 4;
    private final static String FELIX_PLATFORM = "Felix";
    private final static String STATIC_PLATFORM = "Static";

    @Override
    public void postConstruct() {
        masterListener = new MasterRunLevelListener(runLevelController);
        String threadPolicy = System.getProperty(THREAD_POLICY_PROPERTY);
        if (threadPolicy != null) {
            if (POLICY_FULLY_THREADED.equals(threadPolicy)) {
                logger.fine("Using startup thread policy FULLY_THREADED at behest of system property");
                runLevelController.setThreadingPolicy(RunLevelController.ThreadingPolicy.FULLY_THREADED);
            } else if (POLICY_USE_NO_THREADS.equals(threadPolicy)) {
                logger.fine("Using startup thread policy USE_NO_THREADS at behest of system property");
                runLevelController.setThreadingPolicy(RunLevelController.ThreadingPolicy.USE_NO_THREADS);
            } else {
                logger.warning("Unknown threading policy " + threadPolicy + ".  Will use the current policy of "
                        + runLevelController.getThreadingPolicy());
            }
        } else {
            if (platform == null || !(platform.equals(FELIX_PLATFORM) || platform.equals(STATIC_PLATFORM))) {
                runLevelController.setThreadingPolicy(RunLevelController.ThreadingPolicy.USE_NO_THREADS);
            }
        }

        int numThreads = Integer.getInteger(MAX_STARTUP_THREAD_PROPERTY, DEFAULT_STARTUP_THREADS);
        if (numThreads > 0) {
            logger.fine("Startup controller will use " + numThreads + " + threads");
            runLevelController.setMaximumUseableThreads(numThreads);
        } else {
            logger.fine("Startup controller will use infinite threads");
        }

    }

    @Override
    public synchronized void start() {
        ClassLoader origCL = Thread.currentThread().getContextClassLoader();
        try {
            // See issue #5596 to know why we set context CL as common CL.
            Thread.currentThread().setContextClassLoader(commonCLS.getCommonClassLoader());
            doStart();
        } finally {
            // reset the context classloader. See issue GLASSFISH-15775
            Thread.currentThread().setContextClassLoader(origCL);
        }
    }

    private void doStart() {

        run();

        final CountDownLatch latch = new CountDownLatch(1);

        // spwan a non-daemon thread that waits indefinitely for shutdown request.
        // This stops the VM process from exiting.
        serverThread = new Thread("GlassFish Kernel Main Thread") {
            @Override
            public void run() {
                logger.logp(level, "AppServerStartup", "run", "[{0}] started", new Object[] { this });

                // notify the other thread to continue now that a non-daemon
                // thread has started.
                latch.countDown();

                synchronized (this) {
                    while (!shutdownSignal) {
                        try {
                            wait(); // Wait indefinitely until shutdown is requested
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                logger.logp(level, "AppServerStartup", "run", "[{0}] exiting", new Object[] { this });
            }
        };

        // by default a thread inherits daemon status of parent thread.
        // Since this method can be called by non-daemon threads (e.g.,
        // PackageAdmin service in case of an update of bundles), we
        // have to explicitly set the daemon status to false.
        serverThread.setDaemon(false);
        serverThread.start();

        // wait until we have spawned a non-daemon thread
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        if (context == null) {
            System.err.println("Startup context not provided, cannot continue");
            return;
        }

        if (platform == null) {
            platform = "Embedded";
        }

        platformInitTime = System.currentTimeMillis();

        if (logger.isLoggable(level)) {
            logger.log(level, "Startup class : {0}", getClass().getName());
        }

        // prepare the global variables
        DynamicConfigurationService dcs = serviceLocator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        config.addActiveDescriptor(BuilderHelper.createConstantDescriptor(logger));
        config.addActiveDescriptor(BuilderHelper.createConstantDescriptor(masterListener));

        config.addUnbindFilter(BuilderHelper.createContractFilter(ProcessEnvironment.class.getName()));

        config.addActiveDescriptor(BuilderHelper.createConstantDescriptor(
                serverEnvironment.isEmbedded() ? new ProcessEnvironment(Embedded) : new ProcessEnvironment(Server)));
        config.commit();

        // activate the run level services
        masterListener.reset();

        long initFinishTime = 0L;
        long startupFinishTime = 0L;

        if (!proceedTo(InitRunLevel.VAL)) {
            appInstanceListener.stopRecordingTimes();
            return;
        }

        if (!logger.isLoggable(level)) {
            // Stop recording the times, no-one cares
            appInstanceListener.stopRecordingTimes();
        } else {
            initFinishTime = System.currentTimeMillis();
            logger.log(level, "Init level done in " + (initFinishTime - context.getCreationTime()) + " ms");
        }

        appInstanceListener.startRecordingFutures();
        if (!proceedTo(StartupRunLevel.VAL) || !postStartupJob()) {
            appInstanceListener.stopRecordingTimes();
            return;
        }

        if (logger.isLoggable(level)) {
            startupFinishTime = System.currentTimeMillis();
            logger.log(level, "Startup level done in " + (startupFinishTime - initFinishTime) + " ms");
        }

        if (!proceedTo(PostStartupRunLevel.VAL)) {
            appInstanceListener.stopRecordingTimes();
            return;
        }

        if (logger.isLoggable(level)) {

            long postStartupFinishTime = System.currentTimeMillis();
            logger.log(level, "PostStartup level done in " + (postStartupFinishTime - startupFinishTime) + " ms");
        }
    }

    private boolean postStartupJob() {
        LinkedList<Future<Result<Thread>>> futures = appInstanceListener.getFutures();

        serverEnvironment.setStatus(ServerEnvironment.Status.starting);
        events.send(new Event<>(EventTypes.SERVER_STARTUP), false);

        // finally let's calculate our starting times
        long nowTime = System.currentTimeMillis();
        logger.log(Level.INFO, KernelLoggerInfo.startupEndMessage, new Object[] { Version.getProductIdInfo(), platform,
                platformInitTime - context.getCreationTime(), nowTime - platformInitTime, nowTime - context.getCreationTime(), });

        printModuleStatus(systemRegistry, level);

        String wallClockStart = System.getProperty("WALL_CLOCK_START");
        if (wallClockStart != null) {
            try {
                // it will only be set when called from AsadminMain and the env. variable AS_DEBUG is set to true
                Instant realstart = Instant.parse(wallClockStart);
                long duration = Duration.between(realstart, Instant.now()).toMillis();
                logger.log(Level.INFO, KernelLoggerInfo.startupTotalTime, duration);
            } catch (Exception e) {
                // do nothing.
            }
        }

        for (Future<Result<Thread>> future : futures) {
            try {
                try {
                    // wait for an eventual status, otherwise ignore
                    if (future.get(3, SECONDS).isFailure()) {
                        final Throwable t = future.get().exception();
                        logger.log(Level.SEVERE, KernelLoggerInfo.startupFatalException, t);
                        events.send(new Event<>(SERVER_SHUTDOWN), false);
                        shutdown();
                        return false;
                    }
                } catch (TimeoutException e) {
                    logger.log(Level.WARNING, KernelLoggerInfo.startupWaitTimeout, e);
                }
            } catch (Throwable t) {
                logger.log(Level.SEVERE, KernelLoggerInfo.startupException, t);
            }
        }

        serverEnvironment.setStatus(ServerEnvironment.Status.started);
        events.send(new Event<>(EventTypes.SERVER_READY), false);
        pidWriter.writePidFile();

        return true;
    }

    public static void printModuleStatus(ModulesRegistry registry, Level level) {
        if (!logger.isLoggable(level) || registry == null) {
            return;
        }

        StringBuilder sb = new StringBuilder("HK2Module Status Report Begins\n");
        // first started :

        for (HK2Module m : registry.getModules()) {
            if (m.getState() == ModuleState.READY) {
                sb.append(m).append("\n");
            }
        }
        sb.append("\n");
        // then resolved
        for (HK2Module m : registry.getModules()) {
            if (m.getState() == ModuleState.RESOLVED) {
                sb.append(m).append("\n");
            }
        }
        sb.append("\n");
        // finally installed
        for (HK2Module m : registry.getModules()) {
            if (m.getState() != ModuleState.READY && m.getState() != ModuleState.RESOLVED) {
                sb.append(m).append("\n");
            }
        }
        sb.append("HK2Module Status Report Ends");
        logger.log(level, sb.toString());
    }

    // TODO(Sahoo): Revisit this method after discussing with Jerome.
    private void shutdown() {
        CommandRunner runner = commandRunnerProvider.get();

        if (runner != null) {
            final ParameterMap params = new ParameterMap();

            // By default we don't want to shutdown forcefully, as that will cause the VM to exit and that's not
            // a very good behavior for a code known to be embedded in other processes.
            final boolean noForcedShutdown = Boolean.parseBoolean(context.getArguments().getProperty(NO_FORCED_SHUTDOWN, "true"));
            if (noForcedShutdown) {
                params.set("force", "false");
            }

            final InternalSystemAdministrator kernelIdentity = serviceLocator.getService(InternalSystemAdministrator.class);
            if (serverEnvironment.isDas()) {
                runner.getCommandInvocation("stop-domain", new DoNothingActionReporter(), kernelIdentity.getSubject())
                      .parameters(params)
                      .execute();
            } else {
                runner.getCommandInvocation("_stop-instance", new DoNothingActionReporter(), kernelIdentity.getSubject())
                      .parameters(params)
                      .execute();
            }
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public synchronized void stop() {
        if (serverEnvironment.getStatus() == stopped) {
            // During shutdown because of shutdown hooks, we can be stopped multiple times.
            // In such a case, ignore any subsequent stop operations.
            logger.fine("Already stopped, so just returning");
            return;
        }

        serverEnvironment.setStatus(stopping);

        try {
            events.send(new Event<>(EventTypes.PREPARE_SHUTDOWN), false);
        } catch (Exception e) {
            logger.log(Level.SEVERE, KernelLoggerInfo.exceptionDuringShutdown, e);
        }

        // deactivate the run level services
        try {
            proceedTo(InitRunLevel.VAL);
        } catch (Exception e) {
            logger.log(Level.SEVERE, KernelLoggerInfo.exceptionDuringShutdown, e);
        }

        // first send the shutdown event synchronously
        serverEnvironment.setStatus(stopped);
        try {
            events.send(new Event<>(SERVER_SHUTDOWN), false);
        } catch (Exception e) {
            logger.log(Level.SEVERE, KernelLoggerInfo.exceptionDuringShutdown, e);
        }

        try {
            runLevelController.proceedTo(0);
        } catch (Exception e) {
            logger.log(Level.SEVERE, KernelLoggerInfo.exceptionDuringShutdown, e);
        }

        logger.info(KernelLoggerInfo.shutdownFinished);

        // notify the server thread that we are done, so that it can come out.
        if (serverThread != null) {
            synchronized (serverThread) {
                shutdownSignal = true;

                serverThread.notify();
            }
            try {
                serverThread.join(0);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Proceed to the given run level using the given {@link AppServerActivator}.
     *
     * @param runLevel the run level to proceed to
     * @param activator an {@link AppServerActivator activator} used to activate/deactivate the services
     * @return false if an error occurred that required server shutdown; true otherwise
     */
    private boolean proceedTo(int runLevel) {
        try {
            runLevelController.proceedTo(runLevel);
        } catch (Exception e) {
            logger.log(Level.SEVERE, KernelLoggerInfo.shutdownRequired, e);
            shutdown();
            return false;
        }

        return !masterListener.isForcedShutdown();
    }

    @Service
    public static class AppInstanceListener implements InstanceLifecycleListener {
        private static final Filter FILTER = new Filter() {

            @Override
            public boolean matches(Descriptor d) {
                if (d.getScope() != null && d.getScope().equals(RunLevel.class.getName())) {
                    return true;
                }

                return false;
            }

        };

        @Inject
        private Provider<RunLevelController> controllerProvider;

        private volatile RunLevelController controller;

        private Map<String, Long> startTimes = new HashMap<>();
        private LinkedHashMap<String, Long> recordedTimes = new LinkedHashMap<>();
        private LinkedList<Future<Result<Thread>>> futures = null;

        @Override
        public Filter getFilter() {
            return FILTER;
        }

        @Override
        public void lifecycleEvent(InstanceLifecycleEvent lifecycleEvent) {
            if (InstanceLifecycleEventType.PRE_PRODUCTION.equals(lifecycleEvent.getEventType())) {
                doPreProduction(lifecycleEvent.getActiveDescriptor());
                return;
            }

            if (InstanceLifecycleEventType.POST_PRODUCTION.equals(lifecycleEvent.getEventType())) {
                doPostProduction(lifecycleEvent);
                return;
            }

            if (InstanceLifecycleEventType.PRE_DESTRUCTION.equals(lifecycleEvent.getEventType())) {
                doPreDestruction(lifecycleEvent.getActiveDescriptor());
                return;
            }

            // All other events ignored
        }

        private RunLevelController getController() {
            if (controller != null) {
                return controller;
            }

            synchronized (this) {
                if (controller != null) {
                    return controller;
                }

                controller = controllerProvider.get();
                return controller;
            }
        }

        private void stopRecordingTimes() {
            startTimes = null;
            recordedTimes = null;
        }

        private void startRecordingFutures() {
            futures = new LinkedList<>();
        }

        private LinkedList<Future<Result<Thread>>> getFutures() {
            LinkedList<Future<Result<Thread>>> retVal = futures;
            futures = null;
            return retVal;
        }

        private void doPreProduction(ActiveDescriptor<?> descriptor) {
            if (startTimes != null) {
                startTimes.put(descriptor.getImplementation(), System.currentTimeMillis());
            }

            if ((getController().getCurrentRunLevel() > InitRunLevel.VAL) && logger.isLoggable(level)) {
                logger.log(level, "Running service " + descriptor.getImplementation());
            }
        }

        @SuppressWarnings("unchecked")
        private void doPostProduction(InstanceLifecycleEvent event) {
            ActiveDescriptor<?> descriptor = event.getActiveDescriptor();

            if (startTimes != null && recordedTimes != null) {

                Long startupTime = startTimes.remove(descriptor.getImplementation());
                if (startupTime == null) {
                    return;
                }

                recordedTimes.put(descriptor.getImplementation(), (System.currentTimeMillis() - startupTime));
            }

            if ((getController().getCurrentRunLevel() > InitRunLevel.VAL) && logger.isLoggable(level)) {
                logger.log(level, "Service " + descriptor.getImplementation() + " finished " + event.getLifecycleObject());
            }

            if (futures != null) {
                Object startup = event.getLifecycleObject();
                if (startup instanceof FutureProvider) {
                    FutureProvider<Result<Thread>> futureProvider = (FutureProvider<Result<Thread>>) startup;

                    futures.addAll(futureProvider.getFutures());
                }
            }
        }

        private void doPreDestruction(ActiveDescriptor<?> descriptor) {
            if (logger.isLoggable(level)) {
                logger.log(level, "Releasing service {0}", descriptor.getImplementation());
            }
        }

        private LinkedHashMap<String, Long> getAllRecordedTimes() {
            LinkedHashMap<String, Long> retVal = recordedTimes;

            stopRecordingTimes(); // Do not hold onto data that will never be needed again

            return retVal;
        }

    }

    @Singleton
    private class MasterRunLevelListener implements RunLevelListener {
        private final RunLevelController controller;

        private boolean forcedShutdown = false;

        private MasterRunLevelListener(RunLevelController controller) {
            this.controller = controller;
        }

        private void reset() {
            forcedShutdown = false;
        }

        private boolean isForcedShutdown() {
            return forcedShutdown;
        }

        @Override
        public void onCancelled(RunLevelFuture future, int previousProceedTo) {
            logger.log(Level.INFO, KernelLoggerInfo.shutdownRequested);

            if (future.isDown()) {
                return;
            }

            forcedShutdown = true;
            shutdown();
        }

        @Override
        public void onError(RunLevelFuture future, ErrorInformation info) {
            if (future.isDown()) {
                // TODO: Need a log message
                logger.log(Level.WARNING, "An error occured when the system was coming down", info.getError());
                return;
            }

            logger.log(Level.INFO, KernelLoggerInfo.shutdownRequested, info.getError());

            if (controller.getCurrentRunLevel() >= InitRunLevel.VAL) {
                logger.log(Level.SEVERE, KernelLoggerInfo.startupFailure, info.getError());
                events.send(new Event<>(SERVER_SHUTDOWN), false);
            }

            forcedShutdown = true;
            shutdown();
        }

        @Override
        public void onProgress(ChangeableRunLevelFuture future, int achievedLevel) {
            if (achievedLevel == PostStartupRunLevel.VAL) {
                if (logger.isLoggable(level)) {
                    printModuleStatus(systemRegistry, level);

                    LinkedHashMap<String, Long> recordedTimes = appInstanceListener.getAllRecordedTimes();

                    int lcv = 0;
                    if (recordedTimes != null) {
                        for (Map.Entry<String, Long> service : recordedTimes.entrySet()) {
                            logger.log(level, "Service(" + lcv++ + ") : " + service.getKey() + " took " + service.getValue() + " ms");
                        }
                    }
                }
            }
        }

    }
}
