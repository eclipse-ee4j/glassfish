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

package com.sun.s1asdev.ejb.timer.timertests;

import java.rmi.RemoteException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.*;


public class StatefulEJB extends TimerStuffImpl implements SessionBean, SessionSynchronization {
    private SessionContext sc;
    private TimerHandle timerHandle;
    public StatefulEJB(){
    }

    public void setSessionContext(SessionContext sc) {
        this.sc = sc;
        setContext(sc);

        checkCallerSecurityAccess("setSessionContext", false);
        getTimerService("setSessionContext", false);
    }

    public void ejbCreate(TimerHandle th) throws RemoteException {
        System.out.println("In ejbtimer.Stateful::ejbCreate !!");

        timerHandle = th;

        checkGetSetRollbackOnly("ejbCreate", false);
        checkCallerSecurityAccess("ejbCreate", false);

        getTimerService("ejbCreate", false);

        try {
            Timer t = th.getTimer();
            throw new EJBException("shouldn't allow stateful ejbCreate to " +
                                   "access timer methods");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully caught exception when trying " +
                               "access timer methods in stateful ejbCreate");
        }
    }

    public void ejbRemove() throws RemoteException {
        checkCallerSecurityAccess("ejbRemove", false);
        checkGetSetRollbackOnly("ejbRemove", false);
        getTimerService("ejbRemove", false);
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void afterBegin() {
        System.out.println("in afterBegin");
        try {
            Timer t = timerHandle.getTimer();
            t.getInfo();
            TimerHandle aHandle = t.getHandle();
            java.util.Date date = t.getNextTimeout();
            System.out.println("Successfully got timer in afterBegin");
        } catch(Exception e) {
            System.out.println("Error : got exception in afterBegin");
        }
    }

    public void beforeCompletion() {
        System.out.println("in beforeCompletion");
        try {
            Timer t = timerHandle.getTimer();
        } catch(NoSuchObjectLocalException nsole) {
            System.out.println("Successfull got NoSuchObjectLocalException " +
                               " in beforeCompletion");
        } catch(Exception e) {
            System.out.println("Error : got exception in beforeCompletion");
            e.printStackTrace();
        }
    }

    public void afterCompletion(boolean committed) {
        System.out.println("in afterCompletion. committed = " + committed);
        try {
            Timer t = timerHandle.getTimer();
            System.out.println("Error : should have gotten exception in " +
                               "afterCompletion");
            Thread.currentThread().dumpStack();
        } catch(IllegalStateException ise) {
            System.out.println("got expected illegal state exception in " +
                               "afterCompletion");
        } catch(Exception e) {
            System.out.println("Error : got unexpected exception in " +
                               "beforeCompletion");
            e.printStackTrace();
        }

    }

}
