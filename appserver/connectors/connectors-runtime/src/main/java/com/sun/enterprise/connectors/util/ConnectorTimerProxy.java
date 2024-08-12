/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.connectors.util;

import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.logging.LogDomains;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectorTimerProxy extends Timer {

    private volatile static ConnectorTimerProxy connectorTimer;
    private Timer timer;
    private boolean timerException = false;
    private final Object getTimerLock = new Object();

    private final static Logger _logger = LogDomains.getLogger(ConnectorTimerProxy.class,
            LogDomains.RSR_LOGGER);

    private ConnectorTimerProxy(boolean isDaemon) {
        super(isDaemon);
    }

    private Timer getTimer() {
        synchronized (getTimerLock) {
            if (timer == null || timerException) {
                ClassLoader loader = null;
                try {
                    loader = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(
                            ConnectorRuntime.getRuntime().getConnectorClassLoader());
                    timer = new Timer("connector-timer-proxy", true);
                } finally {
                    Thread.currentThread().setContextClassLoader(loader);
                    timerException = false;
                }
            }
        }
        return timer;
    }

    public static final ConnectorTimerProxy getProxy() {
        if(connectorTimer == null) {
            synchronized (ConnectorTimerProxy.class) {
                if (connectorTimer == null) {
                    connectorTimer = new ConnectorTimerProxy(true);
                }
            }
        }
        return connectorTimer;
    }

    /**
     * Proxy method to schedule a timer task at fixed rate.
     * The unchecked exceptions are caught here and in such cases, the timer
     * is recreated and task is rescheduled.
     * @param task
     * @param delay
     * @param period
     */
    @Override
    public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
        timer = getTimer();
        try {
            timer.scheduleAtFixedRate(task, delay, period);
        } catch(Exception ex) {
            handleTimerException(ex);
            timer.scheduleAtFixedRate(task, delay, period);
        }
    }

    @Override
    public void cancel() {
        timer = getTimer();
        try {
            timer.cancel();
        } catch(Exception ex) {
            _logger.log(Level.WARNING, "exception_cancelling_timer", ex.getMessage());
        }
    }

    @Override
    public int purge() {
        int status = 0;
        timer = getTimer();
        try {
            status = timer.purge();
        } catch(Exception ex) {
            _logger.log(Level.WARNING, "exception_purging_timer",  ex.getMessage());
        }
        return status;
    }

    /**
     * Proxy method to schedule a timer task after a specified delay.
     * The unchecked exceptions are caught here and in such cases, the timer
     * is recreated and task is rescheduled.
     * @param task
     * @param delay
     * @param period
     */
    @Override
    public void schedule(TimerTask task, long delay) {
        timer = getTimer();
        try {
            timer.schedule(task, delay);
        } catch(Exception ex) {
            handleTimerException(ex);
            timer.schedule(task, delay);
        }
    }

    /**
     * Proxy method to schedule a timer task at the specified time.
     * The unchecked exceptions are caught here and in such cases, the timer
     * is recreated and task is rescheduled.
     * @param task
     * @param delay
     * @param period
     */
    @Override
    public void schedule(TimerTask task, Date time) {
        timer = getTimer();
        try {
            timer.schedule(task, time);
        } catch(Exception ex) {
            handleTimerException(ex);
            timer.schedule(task, time);
        }
    }

    /**
     * Proxy method to schedule a timer task for repeated fixed-delay execution,
     * beginning after the specified delay.
     * The unchecked exceptions are caught here and in such cases, the timer
     * is recreated and task is rescheduled.
     * @param task
     * @param delay
     * @param period
     */
    @Override
    public void schedule(TimerTask task, long delay, long period) {
        timer = getTimer();
        try {
            timer.schedule(task, delay, period);
        } catch(Exception ex) {
            handleTimerException(ex);
            timer.schedule(task, delay, period);
        }
    }

    /**
     * Proxy method to schedule a timer task for repeated fixed-delay execution,
     * beginning after the specified delay.
     * The unchecked exceptions are caught here and in such cases, the timer
     * is recreated and task is rescheduled.
     * @param task
     * @param delay
     * @param period
     */
    @Override
    public void schedule(TimerTask task, Date firstTime, long period) {
        timer = getTimer();
        try {
            timer.schedule(task, firstTime, period);
        } catch(Exception ex) {
            handleTimerException(ex);
            timer.schedule(task, firstTime, period);
        }
    }

    /**
     * Proxy method to schedule a timer task for repeated fixed-rate execution,
     * beginning after the specified delay.
     * The unchecked exceptions are caught here and in such cases, the timer
     * is recreated and task is rescheduled.
     * @param task
     * @param delay
     * @param period
     */
    @Override
    public void scheduleAtFixedRate(TimerTask task, Date firstTime, long period) {
        timer = getTimer();
        try {
            timer.scheduleAtFixedRate(task, firstTime, period);
        } catch(Exception ex) {
            handleTimerException(ex);
            timer.scheduleAtFixedRate(task, firstTime, period);
        }
    }

    /**
     * Handle any exception occured during scheduling timer.
     *
     * In case of unchecked exceptions, the timer is recreated to be used
     * by the subsequent requests for scheduling.
     * @param ex exception that was caught
     */
    private void handleTimerException(Exception ex) {
        _logger.log(Level.WARNING, "exception_scheduling_timer", ex.getMessage());

        //In case of unchecked exceptions, timer needs to recreated.
        _logger.info("Recreating Timer and scheduling at fixed rate");
        timerException = true;
        timer = getTimer();
    }
}
