/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.gjc.util;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.gjc.monitoring.StatementLeakProbeProvider;
import com.sun.logging.LogDomains;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.glassfish.resourcebase.resources.api.PoolInfo;

import static java.util.logging.Level.WARNING;

/**
 * Statement leak detector that prints the stack trace of the thread when a
 * statement object is leaked. Once the leak timeout expires, a statement leak
 * is assumed and the caller stack trace is printed. When statement-leak-reclaim
 * is set to true, the statement object is reclaimed.
 *
 * @author Shalini M
 */
public class StatementLeakDetector {
    private final HashMap<Statement, StackTraceElement[]> statementLeakThreadStackHashMap;
    private final HashMap<Statement, StatementLeakTask> statementLeakTimerTaskHashMap;
    private final PoolInfo poolInfo;
    private boolean statementLeakTracing;
    private long statementLeakTimeoutInMillis;
    private boolean statementLeakReclaim;
    // Lock on HashMap to trace statement leaks
    private final Object statementLeakLock;
    private final Map<Statement, StatementLeakListener> listeners;
    private final static Logger _logger = LogDomains.getLogger(StatementLeakDetector.class, LogDomains.RSR_LOGGER);
    private final static StringManager localStrings = StringManager.getManager(StatementLeakDetector.class);
    private final Timer timer;
    private StatementLeakProbeProvider stmtLeakProbeProvider = null;

    public StatementLeakDetector(PoolInfo poolInfo, boolean leakTracing, long leakTimeoutInMillis, boolean leakReclaim,
            Timer timer) {
        this.poolInfo = poolInfo;
        statementLeakThreadStackHashMap = new HashMap<>();
        statementLeakTimerTaskHashMap = new HashMap<>();
        listeners = new HashMap<>();
        statementLeakLock = new Object();
        statementLeakTracing = leakTracing;
        statementLeakTimeoutInMillis = leakTimeoutInMillis;
        statementLeakReclaim = leakReclaim;
        this.timer = timer;
        stmtLeakProbeProvider = new StatementLeakProbeProvider();
    }

    public void reset(boolean leakTracing, long leakTimeoutInMillis, boolean leakReclaim) {
        if (!statementLeakTracing && leakTracing) {
            clearAllStatementLeakTasks();
        }
        statementLeakTracing = leakTracing;
        statementLeakTimeoutInMillis = leakTimeoutInMillis;
        statementLeakReclaim = leakReclaim;
    }

    private void registerListener(Statement stmt, StatementLeakListener listener) {
        listeners.put(stmt, listener);
    }

    private void unRegisterListener(Statement stmt) {
        listeners.remove(stmt);
    }

    /**
     * Starts statement leak tracing
     *
     * @param stmt Statement which needs to be traced
     * @param listener Leak Listener
     */
    public void startStatementLeakTracing(Statement stmt, StatementLeakListener listener) {
        synchronized (statementLeakLock) {
            if (!statementLeakThreadStackHashMap.containsKey(stmt)) {
                statementLeakThreadStackHashMap.put(stmt, Thread.currentThread().getStackTrace());
                StatementLeakTask statementLeakTask = new StatementLeakTask(stmt);
                statementLeakTimerTaskHashMap.put(stmt, statementLeakTask);
                registerListener(stmt, listener);
                if (timer != null) {
                    timer.schedule(statementLeakTask, statementLeakTimeoutInMillis);
                    _logger.finest("Scheduled Statement leak tracing timer task");
                }
            }
        }
    }

    /**
     * Stops statement leak tracing
     *
     * @param stmt Statement which needs to be traced
     * @param listener Leak Listener
     */
    public void stopStatementLeakTracing(Statement stmt, StatementLeakListener listener) {
        synchronized (statementLeakLock) {
            if (statementLeakThreadStackHashMap.containsKey(stmt)) {
                statementLeakThreadStackHashMap.remove(stmt);
                StatementLeakTask statementLeakTask = statementLeakTimerTaskHashMap.remove(stmt);
                statementLeakTask.cancel();
                timer.purge();
                _logger.finest("Stopped Statement leak tracing timer task");
                unRegisterListener(stmt);
            }
        }
    }

    /**
     * Logs the potential statement leaks
     *
     * @param stmt Statement that is not closed by application
     */
    private void potentialStatementLeakFound(Statement stmt) {
        synchronized (statementLeakLock) {
            if (statementLeakThreadStackHashMap.containsKey(stmt)) {
                StackTraceElement[] threadStack = statementLeakThreadStackHashMap.remove(stmt);
                StatementLeakListener stmtLeakListener = listeners.get(stmt);
                stmtLeakProbeProvider.potentialStatementLeakEvent(poolInfo.getName().toString(),
                    poolInfo.getApplicationName(), poolInfo.getModuleName());
                printStatementLeakTrace(threadStack);
                statementLeakTimerTaskHashMap.remove(stmt);
                if (statementLeakReclaim) {
                    try {
                        stmtLeakListener.reclaimStatement();
                    } catch (SQLException ex) {
                        Object[] params = new Object[] { poolInfo, ex };
                        _logger.log(WARNING, "statement.leak.detector_reclaim_statement_failure", params);
                    }
                }
                // Unregister here as the listeners would still be present in the map.
                unRegisterListener(stmt);
            }
        }
    }

    /**
     * Prints the stack trace of thread leaking statement to server logs
     *
     * @param threadStackTrace Application(caller) thread stack trace
     */
    private void printStatementLeakTrace(StackTraceElement[] threadStackTrace) {
        StringBuffer stackTrace = new StringBuffer();
        String msg = localStrings
                .getStringWithDefault(
                        "potential.statement.leak.msg", "A potential statement leak detected for connection pool "
                                + poolInfo + ". The stack trace of the thread is provided below : ",
                        new Object[] { poolInfo });
        stackTrace.append(msg);
        stackTrace.append("\n");
        for (int i = 2; i < threadStackTrace.length; i++) {
            stackTrace.append(threadStackTrace[i].toString());
            stackTrace.append("\n");
        }
        _logger.log(WARNING, stackTrace.toString(), "ConnectionPoolName=" + poolInfo);
    }

    /**
     * Clear all statement leak tracing tasks in case of statement leak tracing
     * being turned off
     */
    public void clearAllStatementLeakTasks() {
        synchronized (statementLeakLock) {
            for (Entry<Statement, StatementLeakTask> entry : statementLeakTimerTaskHashMap.entrySet()) {
                StatementLeakTask statementLeakTask = entry.getValue();
                statementLeakTask.cancel();
            }
            if (timer != null) {
                timer.purge();
            }
            statementLeakThreadStackHashMap.clear();
            statementLeakTimerTaskHashMap.clear();
        }
    }

    private class StatementLeakTask extends TimerTask {

        private final Statement statement;

        StatementLeakTask(Statement stmt) {
            this.statement = stmt;
        }

        @Override
        public void run() {
            potentialStatementLeakFound(statement);
        }
    }

}
