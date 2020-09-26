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

package com.sun.s1asdev.ejb31.timer.reschedule_after_failures;

import jakarta.ejb.*;
import jakarta.interceptor.InvocationContext;
import jakarta.annotation.Resource;

import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

@Stateless
public class StlesEJB implements Stles {

    @Resource
    private TimerService timerSvc;

    private static volatile int i = 0;
    private static volatile boolean b = false;

    public void createTimers() throws Exception {

        Calendar now = new GregorianCalendar();
        int month = (now.get(Calendar.MONTH) + 1); // Calendar starts with 0
        int dayOfMonth = now.get(Calendar.DAY_OF_MONTH);

        System.out.println("createTimers(): creating timer with 8 sec ");
        ScheduleExpression se = new ScheduleExpression().second("*/8").minute("*").hour("*");
        TimerConfig tc = new TimerConfig("timer-8-sec", true);
        timerSvc.createCalendarTimer(se, tc);
    }

    public void verifyTimers() {
        if (!b) {
            throw new EJBException("Timer was not rescheduled!");
        }
    }

    @Timeout
    public void timeout(Timer t) {

        System.out.println("in StlesEJB:timeout "  + t.getInfo() + " - persistent: " + t.isPersistent());
        if (i < 2) {
            i++;
            throw new RuntimeException("Failing number " + i);
        }
        b = true;
    }

}
