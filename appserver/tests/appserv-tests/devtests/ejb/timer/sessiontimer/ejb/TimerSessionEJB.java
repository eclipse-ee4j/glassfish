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

package com.sun.s1asdev.ejb.timer.sessiontimer;

import jakarta.ejb.TimedObject;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerHandle;
import jakarta.ejb.TimerService;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.EJBContext;
import java.rmi.RemoteException;
import javax.naming.*;

public class TimerSessionEJB implements TimedObject, SessionBean
{
        private SessionContext context;
    private TimerSingleton singleton;

        public void ejbCreate() throws RemoteException {

            singleton = (TimerSingleton)
                context.lookup("java:module/TimerSingleton!com.sun.s1asdev.ejb.timer.sessiontimer.TimerSingleton");

        }

        public void ejbRemove() throws RemoteException {}

        public void setSessionContext(SessionContext sc) {
                context = sc;
        }

        // business method to create a timer
        public TimerHandle createTimer(int ms) {

            try {
                System.out.println("Calling getMessageContext");
                context.getMessageContext();
            } catch(IllegalStateException ise) {
                System.out.println("getMessageContext() successfully threw illegalStateException");
            }

            TimerService timerService = context.getTimerService();
            Timer timer = timerService.createTimer(ms, "created timer");
            return timer.getHandle();
        }

        // timer callback method
        public void ejbTimeout(Timer timer) {
            try {
                System.out.println("Calling getMessageContext");
                context.getMessageContext();
            } catch(IllegalStateException ise) {
                System.out.println("getMessageContext() successfully threw illegalStateException");
            }

            singleton.setTimeoutReceived();

        }

        public void ejbActivate() {}
        public void ejbPassivate() {}
}
