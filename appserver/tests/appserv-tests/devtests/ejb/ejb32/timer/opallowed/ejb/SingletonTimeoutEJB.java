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

package com.acme.ejb32.timer.opallowed;

import jakarta.annotation.Resource;
import jakarta.ejb.Schedule;
import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.Schedules;
import jakarta.ejb.Singleton;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerHandle;
import jakarta.ejb.TimerService;

@Singleton
public class SingletonTimeoutEJB implements SingletonTimeoutLocal, SingletonTimeout {
    @Resource
    TimerService ts;

    Timer t;

    public TimerHandle createTimer(String info) {
        boolean created = false;
        for(Timer timer:ts.getTimers()) {
            if(timer.getInfo().equals(info)) {
                created = true;
                break;
            }
        }
        if(!created) {
            ScheduleExpression scheduleExpression = new ScheduleExpression().second("*/5").minute("*").hour("*");
            t = ts.createCalendarTimer(scheduleExpression, new TimerConfig(info, true));
        }
        return t.getHandle();
    }

    public Timer createLocalTimer(String info) {
        boolean created = false;
        for(Timer timer:ts.getTimers()) {
            if(timer.getInfo().equals(info)) {
                created = true;
                break;
            }
        }
        if(!created) {
            ScheduleExpression scheduleExpression = new ScheduleExpression().second("*/5").minute("*").hour("*");
            t = ts.createCalendarTimer(scheduleExpression, new TimerConfig(info, true));
        }
        return t;
    }

    @Override
    public void cancelFromHelper() {
        TimeoutHelper.cancelTimer(createTimer("helper"));
    }

    @Timeout
    public void timeout(Timer t) {
        String info = t.getInfo().toString();
        System.out.println(info + " is timeout");
    }

    @Schedules({
            @Schedule(second="*/5", minute="*", hour="*", info="Sglt.schedule.anno"),
    })
    private void schedule(Timer t) {
        System.out.println("SingletonTimeoutEJB.schedule for " + t.getInfo().toString());
    }

}
