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

package com.sun.s1asdev.ejb.timer.timerserialization.ejb;

import jakarta.ejb.TimedObject;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerHandle;
import jakarta.ejb.TimerService;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;

public class TimerSLSBEJB
    implements TimedObject, SessionBean
{
        private SessionContext context;

        public void ejbCreate() {}

        public void setSessionContext(SessionContext sc) {
                context = sc;
        }

        // business method to create a timer
        public Timer createTimer(int ms) {
                TimerService timerService = context.getTimerService();
                Timer timer = timerService.createTimer(ms, "created timer");
                return timer;
        }

        // timer callback method
        public void ejbTimeout(Timer timer) {
                System.out.println("TimerSLSB::ejbTimeout() invoked");
        }

        public void ejbRemove() {}

        public void ejbActivate() {
        System.out.println ("In TimerSLSB.activate()");
    }

        public void ejbPassivate() {
        System.out.println ("In TimerSLSB.passivate()");
    }
}
