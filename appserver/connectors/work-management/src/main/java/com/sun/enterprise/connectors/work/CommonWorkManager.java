/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors.work;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.corba.ee.spi.threadpool.NoSuchThreadPoolException;
import com.sun.corba.ee.spi.threadpool.ThreadPool;
import com.sun.corba.ee.spi.threadpool.ThreadPoolManager;
import com.sun.enterprise.connectors.work.context.WorkContextHandlerImpl;
import com.sun.enterprise.connectors.work.monitor.WorkManagementProbeProvider;
import com.sun.enterprise.connectors.work.monitor.WorkManagementStatsProvider;

import jakarta.resource.spi.work.ExecutionContext;
import jakarta.resource.spi.work.Work;
import jakarta.resource.spi.work.WorkCompletedException;
import jakarta.resource.spi.work.WorkException;
import jakarta.resource.spi.work.WorkListener;
import jakarta.resource.spi.work.WorkManager;
import jakarta.resource.spi.work.WorkRejectedException;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.enterprise.iiop.util.S1ASThreadPoolManager;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.logging.annotation.LogMessageInfo;


/**
 * WorkManager implementation.
 *
 * @author Binod P.G
 */

public final class CommonWorkManager implements WorkManager {

    private ThreadPoolManager tpm;
    private ThreadPool tp;

    private static final Logger logger = LogFacade.getLogger();

    private WorkManagementProbeProvider probeProvider = null;
    private WorkManagementStatsProvider statsProvider = null;
    private String dottedNamesHierarchy;

    private ConnectorRuntime runtime;
    private String raName ;
    private ClassLoader rarClassLoader;

    @LogMessageInfo(
            message = "Failed to get the thread-pool [ {0} ] for resource adapter [ {1} ].",
            comment = "Failed to find thread pool",
            level = "SEVERE",
            cause = "Could not find a thread pool according to the pool ID.",
            action = "Check the thread-pool-id property in Resource Adapter Config.",
            publish = true)
    private static final String RAR_THREAD_POOL_NOT_FOUND = "AS-RAR-05001";

    @LogMessageInfo(
            message = "Failed to get the default thread-pool for resource adapter [ {0} ].",
            comment = "Failed to find the default thread pool.",
            level = "SEVERE",
            cause = "Could not find the default thread pool for resource adatper.",
            action = "Check the thread-pool-id property in Resource Adapter Config.",
            publish = true)
    private static final String RAR_DEFAULT_THREAD_POOL_NOT_FOUND = "AS-RAR-05002";

    /**
     * Private constructor.
     *
     * @param threadPoolId Id of the thread pool.
     * @throws ConnectorRuntimeException if thread pool is not accessible
     */
    public CommonWorkManager(String threadPoolId, ConnectorRuntime runtime, String raName, ClassLoader cl)
            throws ConnectorRuntimeException {

        if (runtime.isServer() || runtime.isEmbedded()) {
            this.runtime = runtime;
            this.raName = raName;
            this.rarClassLoader = cl;
            tpm = S1ASThreadPoolManager.getThreadPoolManager();

            if (threadPoolId == null || threadPoolId.isEmpty()) {
                tp = tpm.getDefaultThreadPool();
            } else {
                try {
                    tp = tpm.getThreadPool(threadPoolId);
                    if(logger.isLoggable(Level.FINEST)){
                        logger.finest("Got the thread pool [ "+threadPoolId+" ] for WorkManager of RAR [ "+raName+" ]");
                    }
                } catch (NoSuchThreadPoolException e) {
                    logger.log(Level.SEVERE, RAR_THREAD_POOL_NOT_FOUND, new Object[]{threadPoolId, raName});
                    ConnectorRuntimeException cre = new ConnectorRuntimeException(e.getMessage());
                    cre.initCause(e);
                    throw cre;
                }
            }
            if (tp == null) {
                // in case the default thread-pool was not available.
                // Set the message appropriately.
                String format = null;
                format = logger.getResourceBundle().getString(RAR_DEFAULT_THREAD_POOL_NOT_FOUND);
                if (format.trim().equals("")) {
                    format = "Failed to get the default thread-pool for resource adapter "+raName+".";
                } else {
                    format = MessageFormat.format(format, raName);
                }
                ConnectorRuntimeException cre =  new ConnectorRuntimeException(format);
                logger.log(Level.SEVERE, RAR_DEFAULT_THREAD_POOL_NOT_FOUND, raName);
                throw cre;
            }
            registerWithMonitoringService();
        }
    }

    private void registerWithMonitoringService() {

        if(ConnectorsUtil.belongsToSystemRA(raName)){
            if(!ConnectorsUtil.isJMSRA(raName)){
                return ;
            }
        }
        probeProvider = new WorkManagementProbeProvider();
        String monitoringModuleName = ConnectorConstants.MONITORING_CONNECTOR_SERVICE_MODULE_NAME;

        if(ConnectorsUtil.isJMSRA(raName)){
            dottedNamesHierarchy = ConnectorConstants.MONITORING_JMS_SERVICE +
                ConnectorConstants.MONITORING_SEPARATOR + ConnectorConstants.MONITORING_WORK_MANAGEMENT;
            monitoringModuleName =  ConnectorConstants.MONITORING_JMS_SERVICE_MODULE_NAME;
        }else{
            dottedNamesHierarchy = ConnectorConstants.MONITORING_CONNECTOR_SERVICE +
                    ConnectorConstants.MONITORING_SEPARATOR + raName + ConnectorConstants.MONITORING_SEPARATOR +
                    ConnectorConstants.MONITORING_WORK_MANAGEMENT;

        }
        statsProvider = new WorkManagementStatsProvider(raName);

        StatsProviderManager.register(monitoringModuleName,PluginPoint.SERVER, dottedNamesHierarchy, statsProvider);

        if(logger.isLoggable(Level.FINE)){
            logger.log(Level.FINE, "Registered work-monitoring stats [ "+dottedNamesHierarchy+" ]  " +
                  "for [ " + raName + " ] with monitoring-stats-registry.");
        }

    }

    private void deregisterFromMonitoringService(){

        if(ConnectorsUtil.belongsToSystemRA(raName)){
            if(!ConnectorsUtil.isJMSRA(raName)){
                return ;
            }
        }
        if (statsProvider != null) {
            StatsProviderManager.unregister(statsProvider);
            if(logger.isLoggable(Level.FINE)){
                logger.log(Level.FINE, "De-registered work-monitoring stats [ "+dottedNamesHierarchy+" ]" +
                    "  for [ " + raName + " ] from monitoring-stats-registry.");
            }
        }
    }

    public void cleanUp(){
        if (runtime != null && runtime.isServer()) {
            deregisterFromMonitoringService();
        }
    }

    /**
     * Executes the work instance.
     *
     * @param work work instance from resource adapter
     * @throws WorkException if there is an exception while executing work.
     */
    public void doWork(Work work)
            throws WorkException {
        doWork(work, -1, null, null);
    }

    /**
     * Executes the work instance. The calling thread will wait until the
     * end of work execution.
     *
     * @param work         work instance from resource adapter
     * @param startTimeout Timeout for the work.
     * @param execContext  Execution context in which the work will be executed.
     * @param workListener Listener from RA that will listen to work events.
     * @throws WorkException if there is an exception while executing work.
     */
    public void doWork(Work work, long startTimeout,
                       ExecutionContext execContext, WorkListener workListener)
            throws WorkException {
        WorkContextHandlerImpl contextHandler = createWorkContextHandler();
        validateWork(work, WorkCoordinator.getExecutionContext(execContext, work), contextHandler);

        if (logger.isLoggable(Level.FINEST)) {
            String msg = "doWork for [" + work.toString() + "] START";
            logger.log(Level.FINEST, msg);
        }

        WorkCoordinator wc = new WorkCoordinator
                (work, startTimeout, execContext, tp.getAnyWorkQueue(), workListener,
                        this.probeProvider, runtime, raName, contextHandler);
        wc.submitWork(WorkCoordinator.WAIT_UNTIL_FINISH);
        wc.lock();

        WorkException we = wc.getException();
        if (we != null) {
            throw we;
        }

        if (logger.isLoggable(Level.FINEST)) {
            String msg = "doWork for [" + work.toString() + "] END";
            logger.log(Level.FINEST, msg);
        }
    }

    /**
     * Executes the work instance. The calling thread will wait until the
     * start of work execution.
     *
     * @param work work instance from resource adapter
     * @throws WorkException if there is an exception while executing work.
     */
    public long startWork(Work work) // startTimeout = INDEFINITE
            throws WorkException {
        //block the current application thread
        //find a thread to run work
        //notify the application thread when done

        return startWork(work, -1, null, null);
    }

    /**
     * Executes the work instance. The calling thread will wait until the
     * start of work execution.
     *
     * @param work         work instance from resource adapter
     * @param startTimeout Timeout for the work.
     * @param execContext  Execution context in which the work will be executed.
     * @param workListener Listener from RA that will listen to work events.
     * @throws WorkException if there is an exception while executing work.
     */
    public long startWork(Work work, long startTimeout,
                          ExecutionContext execContext, WorkListener workListener)
            throws WorkException {

        WorkContextHandlerImpl contextHandler = createWorkContextHandler();
        validateWork(work, WorkCoordinator.getExecutionContext(execContext, work), contextHandler);

        if (logger.isLoggable(Level.FINEST)) {
            String msg = "startWork for [" + work.toString() + "] START";
            logger.log(Level.FINEST, msg);
        }

        long acceptanceTime = System.currentTimeMillis();

        WorkCoordinator wc = new WorkCoordinator
                (work, startTimeout, execContext, tp.getAnyWorkQueue(), workListener,
                        this.probeProvider, runtime, raName, contextHandler);
        wc.submitWork(WorkCoordinator.WAIT_UNTIL_START);
        wc.lock();

        WorkException we = wc.getException();
        if (we != null) {
            throw we;
        }

        if (logger.isLoggable(Level.FINEST)) {
            String msg = "startWork for [" + work.toString() + "] END";
            logger.log(Level.FINEST, msg);
        }
        long startTime = System.currentTimeMillis();

        return (startTime - acceptanceTime);
    }

    /**
     * prvides work-context-handler to handle the submitted work-contexts
     * @return work-context-handler
     */
    private WorkContextHandlerImpl createWorkContextHandler() {
        WorkContextHandlerImpl contextHandler = new WorkContextHandlerImpl(runtime, rarClassLoader);
        return contextHandler;
    }

    /**
     * Executes the work instance. Calling thread will continue after scheduling
     * the work
     *
     * @param work work instance from resource adapter
     * @throws WorkException if there is an exception while executing work.
     */
    public void scheduleWork(Work work) // startTimeout = INDEFINITE
            throws WorkException {
        scheduleWork(work, -1, null, null);
        return;
    }

    /**
     * Executes the work instance. Calling thread will continue after scheduling
     * the work
     *
     * @param work         work instance from resource adapter
     * @param startTimeout Timeout for the work.
     * @param execContext  Execution context in which the work will be executed.
     * @param workListener Listener from RA that will listen to work events.
     * @throws WorkException if there is an exception while executing work.
     */
    public void scheduleWork(Work work, long startTimeout,
                             ExecutionContext execContext, WorkListener workListener)
            throws WorkException {

        WorkContextHandlerImpl contextHandler = createWorkContextHandler();
        validateWork(work, WorkCoordinator.getExecutionContext(execContext, work), contextHandler);
        if (logger.isLoggable(Level.FINEST)) {
            String msg = "scheduleWork for [" + work.toString() + "] START";
            logger.log(Level.FINEST, msg);
        }

        WorkCoordinator wc = new WorkCoordinator
                (work, startTimeout, execContext, tp.getAnyWorkQueue(), workListener,
                        this.probeProvider, runtime, raName, contextHandler);
        wc.submitWork(WorkCoordinator.NO_WAIT);
        wc.lock();

        WorkException we = wc.getException();
        if (we != null) {
            throw we;
        }

        if (logger.isLoggable(Level.FINEST)) {
            String msg = "scheduleWork for [" + work.toString() + "] END";
            logger.log(Level.FINEST, msg);
        }
        return;
    }

    /**
     * validates the work-contexts provided in the work
     * @param workToBeValidated work instance
     * @param context execution-context (if present)
     * @param contextHandler work-context-handler
     * @throws WorkCompletedException when work processing fails
     * @throws WorkRejectedException when work cannot be processed
     */
    private void validateWork(Work workToBeValidated, ExecutionContext context, WorkContextHandlerImpl contextHandler)
            throws WorkCompletedException, WorkRejectedException {
        contextHandler.validateWork(workToBeValidated, context);
    }

}
