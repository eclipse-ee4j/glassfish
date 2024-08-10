/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.pool;

import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.logging.LogDomains;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.resourcebase.resources.api.PoolInfo;

/**
 * Connection leak detector, book keeps the caller stack-trace during getConnection()<br>
 * Once the leak-timeout expires, assumes a connection leak and prints the caller stack-trace<br>
 * Also, reclaims the connection if connection-leak-reclaim in ON<br>
 *
 * @author Kshitiz Saxena, Jagadish Ramu
 */
public class ConnectionLeakDetector {
    private final HashMap<ResourceHandle, StackTraceElement[]> connectionLeakThreadStackHashMap;
    private final HashMap<ResourceHandle, ConnectionLeakTask> connectionLeakTimerTaskHashMap;
    private boolean connectionLeakTracing;
    private long connectionLeakTimeoutInMillis;
    private boolean connectionLeakReclaim;
    private final PoolInfo connectionPoolInfo;
    private final Map<ResourceHandle, ConnectionLeakListener> listeners;

    // Lock on HashMap to trace connection leaks
    private final Object connectionLeakLock;

    private final static Logger _logger = LogDomains.getLogger(ConnectionLeakDetector.class, LogDomains.RSR_LOGGER);

    public ConnectionLeakDetector(PoolInfo poolInfo, boolean leakTracing, long leakTimeoutInMillis, boolean leakReclaim) {
        connectionPoolInfo = poolInfo;
        connectionLeakThreadStackHashMap = new HashMap<>();
        connectionLeakTimerTaskHashMap = new HashMap<>();
        listeners = new HashMap<>();
        connectionLeakLock = new Object();
        connectionLeakTracing = leakTracing;
        connectionLeakTimeoutInMillis = leakTimeoutInMillis;
        connectionLeakReclaim = leakReclaim;
    }

    public void reset(boolean leakTracing, long leakTimeoutInMillis, boolean leakReclaim) {
        if (!connectionLeakTracing && leakTracing) {
            clearAllConnectionLeakTasks();
        }
        connectionLeakTracing = leakTracing;
        connectionLeakTimeoutInMillis = leakTimeoutInMillis;
        connectionLeakReclaim = leakReclaim;
    }

    private void registerListener(ResourceHandle handle, ConnectionLeakListener listener) {
        listeners.put(handle, listener);
    }

    private void unRegisterListener(ResourceHandle handle) {
        listeners.remove(handle);
    }

    /**
     * starts connection leak tracing
     *
     * @param resourceHandle Resource which needs to be traced
     * @param listener Leak Listener
     */
    public void startConnectionLeakTracing(ResourceHandle resourceHandle, ConnectionLeakListener listener) {
        if (connectionLeakTracing) {
            synchronized (connectionLeakLock) {
                if (!connectionLeakThreadStackHashMap.containsKey(resourceHandle)) {
                    connectionLeakThreadStackHashMap.put(resourceHandle, Thread.currentThread().getStackTrace());
                    ConnectionLeakTask connectionLeakTask = new ConnectionLeakTask(resourceHandle);
                    connectionLeakTimerTaskHashMap.put(resourceHandle, connectionLeakTask);
                    registerListener(resourceHandle, listener);
                    if (getTimer() != null) {
                        getTimer().schedule(connectionLeakTask, connectionLeakTimeoutInMillis);
                    }
                }
            }
        }
    }

    /**
     * stops connection leak tracing
     *
     * @param resourceHandle Resource which needs to be traced
     * @param listener Leak Listener
     */
    public void stopConnectionLeakTracing(ResourceHandle resourceHandle, ConnectionLeakListener listener) {
        if (connectionLeakTracing) {
            synchronized (connectionLeakLock) {
                if (connectionLeakThreadStackHashMap.containsKey(resourceHandle)) {
                    connectionLeakThreadStackHashMap.remove(resourceHandle);
                    ConnectionLeakTask connectionLeakTask = connectionLeakTimerTaskHashMap.remove(resourceHandle);
                    connectionLeakTask.cancel();
                    getTimer().purge();
                    unRegisterListener(resourceHandle);
                }
            }
        }
    }

    /**
     * Logs the potential connection leaks
     *
     * @param resourceHandle Resource that is not returned by application
     */
    private void potentialConnectionLeakFound(ResourceHandle resourceHandle) {
        synchronized (connectionLeakLock) {
            if (connectionLeakThreadStackHashMap.containsKey(resourceHandle)) {
                StackTraceElement[] threadStack = connectionLeakThreadStackHashMap.remove(resourceHandle);
                ConnectionLeakListener connLeakListener = listeners.get(resourceHandle);
                connLeakListener.potentialConnectionLeakFound();
                printConnectionLeakTrace(threadStack, connLeakListener);
                connectionLeakTimerTaskHashMap.remove(resourceHandle);
                if (connectionLeakReclaim) {
                    resourceHandle.markForReclaim(true);
                    connLeakListener.reclaimConnection(resourceHandle);
                }
                // Unregister here as the listeners would still be present in the map.
                unRegisterListener(resourceHandle);
            }
        }
    }

    /**
     * Prints the stack trace of thread leaking connection to server logs
     *
     * @param threadStackTrace Application(caller) thread stack trace
     */
    private void printConnectionLeakTrace(StackTraceElement[] threadStackTrace, ConnectionLeakListener connLeakListener) {
        StringBuffer stackTrace = new StringBuffer(1024);
        stackTrace.append("A potential connection leak detected for connection pool ").append(connectionPoolInfo);
        stackTrace.append(". The stack trace of the thread is provided below:\n");
        for (int i = 2; i < threadStackTrace.length; i++) {
            stackTrace.append(threadStackTrace[i]);
            stackTrace.append('\n');
        }
        connLeakListener.printConnectionLeakTrace(stackTrace);
        _logger.log(Level.WARNING, stackTrace.toString());
    }

    /**
     * Clear all connection leak tracing tasks in case of connection leak tracing being turned off
     */
    private void clearAllConnectionLeakTasks() {
        synchronized (connectionLeakLock) {
            for (Entry<ResourceHandle, ConnectionLeakTask> connectionLeakTaskEntry : connectionLeakTimerTaskHashMap.entrySet()) {
                connectionLeakTaskEntry.getValue().cancel();
            }
            if (getTimer() != null) {
                getTimer().purge();
            }
            connectionLeakThreadStackHashMap.clear();
            connectionLeakTimerTaskHashMap.clear();
        }
    }

    private Timer getTimer() {
        return ConnectorRuntime.getRuntime().getTimer();
    }

    private class ConnectionLeakTask extends TimerTask {

        private final ResourceHandle resourceHandle;

        ConnectionLeakTask(ResourceHandle resourceHandle) {
            this.resourceHandle = resourceHandle;
        }

        @Override
        public void run() {
            potentialConnectionLeakFound(resourceHandle);
        }
    }
}
