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

package org.glassfish.tests.ejb.timertest;

import jakarta.ejb.*;
import javax.naming.InitialContext;

/**
 * @author Marina Vatkina
 */
@Stateless
public class SimpleEjb {

    private static volatile boolean timeoutWasCalled = false;
    private static volatile boolean autotimeoutWasCalled = false;

    public void createTimer() throws Exception {
        System.err.println("In SimpleEjb:createTimer()");
        TimerService timerSvc = (TimerService) new InitialContext().lookup("java:comp/TimerService");
        Timer t = timerSvc.createSingleActionTimer(2, new TimerConfig("timer01", false));
    }

    public boolean verifyTimer() {
        return timeoutWasCalled && autotimeoutWasCalled;
    }

    @Timeout
    private void timeout(Timer t) {

        System.err.println("in SimpleEjb: timeout "  + t.getInfo());
        timeoutWasCalled = true;
    }

    @Schedule(second="*", minute="*", hour="*", persistent=false)
    public void autotest() {
        System.err.println("IN AUTO-TIMEOUT!!!");
        autotimeoutWasCalled = true;
    }
}
