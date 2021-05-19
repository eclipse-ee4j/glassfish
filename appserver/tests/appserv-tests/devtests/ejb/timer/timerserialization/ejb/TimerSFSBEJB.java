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
import jakarta.ejb.NoSuchObjectLocalException;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerHandle;
import jakarta.ejb.TimerService;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import java.rmi.RemoteException;

public class TimerSFSBEJB
    implements SessionBean
{
        private SessionContext context;
    private String timerName;
    private Context initialCtx;
    private Timer timer;

        public void ejbCreate(String timerName) {
        this.timerName = timerName;
    }

    public String getName() {
        return this.timerName;
    }

        public void setSessionContext(SessionContext sc) {
                this.context = sc;
        try {
            this.initialCtx = new InitialContext();
        } catch (Throwable th) {
            th.printStackTrace();
        }
        }

        // business method to create a timer
        public void createTimer(int ms)
        throws RemoteException
    {
        try {
            InitialContext initialCtx = new InitialContext();
            TimerSLSBHome home = (TimerSLSBHome) initialCtx.lookup("java:comp/env/ejb/TimerSLSB");
                    TimerSLSB slsb = (TimerSLSB) home.create();
                    timer = slsb.createTimer(ms);
            System.out.println ("PG-> after createTimer()");
        } catch (Exception ex) {
            throw new RemoteException("Exception during TimerSFSBEJB::createTimer", ex);
        }
        }

        public long getTimeRemaining() {
            long timeRemaining = -1;
            try {
                timeRemaining = timer.getTimeRemaining();
            } catch(NoSuchObjectLocalException nsole) {
                System.out.println("Timer was cancelled, but that's ... OK!");
            }
            return timeRemaining;
        }

        public TimerHandle getTimerHandle() {
            TimerHandle handle = null;
            try {
                handle = timer.getHandle();
            } catch(NoSuchObjectLocalException nsole) {
                System.out.println("Timer was cancelled, but that's ... OK!");
            }
            return handle;
        }

        public void cancelTimer() {
            try {
                timer.cancel();
            } catch(NoSuchObjectLocalException nsole) {
                System.out.println("Timer was cancelled, but that's ... OK!");
            }
    }


        public void ejbRemove() {}

        public void ejbActivate() {
        System.out.println ("In TimerSFSB.activate()");
    }

        public void ejbPassivate() {
        System.out.println ("In TimerSFSB.passivate()");
    }
}
