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


import org.glassfish.api.logging.Task;
import java.util.TimerTask;

public class LogRotationTimerTask extends TimerTask {
    private long timerValue;
    Task task;


    public LogRotationTimerTask(Task task, long timeInMinutes ) {
        timerValue = timeInMinutes * 60 * 1000;
        this.task = task;
    }

    public long getRotationTimerValue( ) {
        return timerValue;
    }

    public long getRotationTimerValueInMinutes( ) {
        // We are just converting the value from milliseconds back to
        // minutes
        return timerValue/60000;
    }

    public void run( ) {
        task.run();
    }
}

