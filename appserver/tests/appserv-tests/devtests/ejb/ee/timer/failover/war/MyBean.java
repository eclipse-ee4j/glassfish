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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.acme;

import java.util.Collection;
import java.util.HashSet;
import jakarta.ejb.*;
import jakarta.annotation.Resource;

/**
 *
 * @author marina vatkina
 */

@Singleton
public class MyBean {

    @Resource TimerService ts;
    private volatile int counter = 0;
    private HashSet timers = new HashSet();
    private volatile boolean second_timer_running = false;

    @Schedule(second="*/5", minute="*", hour="*", info = "timer01")
    private void scheduledtimeout(Timer t) {
        test(t, "timer01");
    }

    @Timeout
    private void requestedtimeout(Timer t) {
        second_timer_running = true;
        test(t, "timer02");
    }

    private void test(Timer t, String name) {
        if (((String)t.getInfo()).startsWith(name)) {
            System.err.println("In ___MyBean:timeout___ "  + t.getInfo() + " - persistent: " + t.isPersistent());
            timers.add(t.getInfo());
            counter++;
        } else {
            throw new RuntimeException("Wrong " + t.getInfo() + " timer was called");
        }

    }

    public boolean timeoutReceived(String param) {
        System.err.println("In ___MyBean:timeoutReceived___ " + counter + " times");
        boolean result = (counter > 0);
        if (!second_timer_running)  {
            ts.createTimer(1000, 5000, "timer02 " + param);
        } else {
            result = result && (timers.size() == 3);
        }

        counter = 0;
        return result;
    }
}
