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

package com.acme.ejb32.timer.getalltimers;

import jakarta.annotation.Resource;
import jakarta.ejb.Schedule;
import jakarta.ejb.Schedules;
import jakarta.ejb.Singleton;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

@Singleton
public class SingletonTimeoutEJB {
    @Resource
    TimerService ts;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void createTimerForTimeout() {
        ts.createIntervalTimer(1, 1000*5, new TimerConfig("Sglt.timeout", true));
        ts.createIntervalTimer(1, 1000*5, new TimerConfig("Sglt.timeout.nonpersist", false));
    }

    @Timeout
    private void timeout(Timer t) {
        System.out.println("SingletonTimeoutEJB.timeout for " + t.getInfo().toString());
    }

    @Schedules({
            @Schedule(second="*/5", minute="*", hour="*", info="Sglt.schedule.anno"),
            @Schedule(second="*/5", minute="*", hour="*", info="Sglt.schedule.anno.nonpersist", persistent = false)
    })
    private void schedule(Timer t) {
        System.out.println("SingletonTimeoutEJB.schedule for " + t.getInfo().toString());
    }

}
