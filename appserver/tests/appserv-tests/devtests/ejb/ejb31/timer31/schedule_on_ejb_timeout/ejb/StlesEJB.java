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

package com.sun.s1asdev.ejb31.timer.schedule_on_ejb_timeout;

import jakarta.ejb.*;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

public class StlesEJB implements Stles, SessionBean, TimedObject {

    private SessionContext context;
    private static Set timers = new HashSet();

    public void ejbTimeout(Timer timer) {
        System.out.println(timer.getInfo() + " expired");
        timers.add(timer.getInfo());
    }

    public void createTimer() throws Exception {
        ScheduleExpression expression = new ScheduleExpression().second("*/2").minute("*").hour("*");
        TimerConfig config = new TimerConfig("Timer01", false);
        context.getTimerService().createCalendarTimer(expression, config);
    }

    public void setSessionContext(SessionContext context) throws EJBException, RemoteException {
        this.context = context;
    }

    public void ejbRemove() throws EJBException, RemoteException {}
    public void ejbActivate() throws EJBException, RemoteException { }
    public void ejbPassivate() throws EJBException, RemoteException { }


    public void verifyTimers() throws Exception {
        if (!timers.contains("Timer01"))
            throw new EJBException("Timer01 hadn't fired");
        if (!timers.contains("Timer00"))
            throw new EJBException("Timer00 hadn't fired");
    }

}
