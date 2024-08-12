/*
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

package org.glassfish.tests.embedded.ejb.basic;

import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerService;

import javax.naming.InitialContext;

@Stateless
public class TimerEjb {

    private static volatile boolean timeoutWasCalled = false;
    private static volatile boolean autotimeoutWasCalled = false;

    public void createTimer() throws Exception {
        System.err.println("In SimpleEjb:createTimer()");
        TimerService timerSvc = (TimerService) new InitialContext().lookup(
                "java:comp/TimerService");
        timerSvc.createTimer(2, "timer01");
    }

    public boolean verifyTimer() {
        return timeoutWasCalled && autotimeoutWasCalled;
    }

    @Timeout
    private void timeout(Timer t) {

        System.err.println("in SimpleEjb: timeout " + t.getInfo());
        timeoutWasCalled = true;
    }

    @Schedule(second = "*", minute = "*", hour = "*")
    public void autotest() {
        System.err.println("IN AUTO-TIMEOUT!!!");
        autotimeoutWasCalled = true;
    }
}
