/*
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

package com.sun.enterprise.server.logging;

import java.util.Timer;

public class LogRotationTimer {
    private Timer rotationTimer;

    private LogRotationTimerTask rotationTimerTask;

    private static LogRotationTimer instance = new LogRotationTimer();

    private LogRotationTimer() {
        rotationTimer = new Timer("log-rotation-timer");
    }

    public static LogRotationTimer getInstance() {
        return instance;
    }

    public void startTimer(LogRotationTimerTask timerTask) {
        rotationTimerTask = timerTask;
        rotationTimer.schedule(rotationTimerTask,
                timerTask.getRotationTimerValue());
    }

    public void stopTimer() {
        rotationTimer.cancel();
    }

    public void restartTimer() {
        // We will restart the timer only if the timerTask is set which
        // means user has set a value for LogRotation based on Time
        if (rotationTimerTask != null) {
            rotationTimerTask.cancel();
            rotationTimerTask = new LogRotationTimerTask(
                    // This is wierd, We need to have a fresh TimerTask object
                    // to reschedule the work.
                    rotationTimerTask.task,
                    rotationTimerTask.getRotationTimerValueInMinutes());
            rotationTimer.schedule(rotationTimerTask,
                    rotationTimerTask.getRotationTimerValue());
        }
    }

    public void restartTimerForDayBasedRotation() {
        // We will restart the timer only if the timerTask is set which
        // means user has set a value for LogRotation based on Time
        if (rotationTimerTask != null) {
            rotationTimerTask.cancel();
            rotationTimerTask = new LogRotationTimerTask(
                    // This is wierd, We need to have a fresh TimerTask object
                    // to reschedule the work.
                    rotationTimerTask.task,
                    60 * 24);
            rotationTimer.schedule(rotationTimerTask,
                    1000 * 60 * 60 * 24);
        }
    }

}
