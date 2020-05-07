/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.acme;

import jakarta.annotation.Resource;
import jakarta.ejb.Schedule;
import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.Schedules;
import jakarta.ejb.Singleton;
import jakarta.ejb.Stateless;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;
import java.util.HashSet;

@Stateless
public class StlesTimerBean {
    @Resource
    TimerService ts;

    @Schedules({
            @Schedule(second = "*/5", minute = "*", hour = "*", info = "StlesTimerBean.timer01.p"),
            @Schedule(second = "*/5", minute = "*", hour = "*", info = "StlesTimerBean.timer01.t", persistent = false)
    })
    private void scheduledTimeout(Timer t) {
        test(t, "StlesTimerBean.timer01");
    }

    @Timeout
    private void programmaticTimeout(Timer t) {
        test(t, "StlesTimerBean.timer02");
    }

    private void test(Timer t, String name) {
        if (((String) t.getInfo()).startsWith(name)) {
            System.err.println("In StlesTimerBean:timeout___ " + t.getInfo() + " - persistent: " + t.isPersistent());
        } else {
            throw new RuntimeException("Wrong " + t.getInfo() + " timer was called");
        }

    }

    public void createProgrammaticTimer() {
        System.err.println("In StlesTimerBean:createProgrammaticTimer__ ");
        for (Timer t : ts.getTimers()) {
            if (t.getInfo().toString().contains("StlesTimerBean.timer02")) {
                System.err.println("programmatic timers are already created for StlesTimerBean");
                return;
            }
        }
        ts.createTimer(1000, 5000, "StlesTimerBean.timer02.p");
        ScheduleExpression scheduleExpression = new ScheduleExpression().minute("*").hour("*").second("*/5");
        ts.createCalendarTimer(scheduleExpression, new TimerConfig("StlesTimerBean.timer02.t", false));
    }
}
