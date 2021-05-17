/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.selfmanagement.ttimer.action;

import javax.management.*;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

public class TimerOccurrence implements NotificationListener,
    com.sun.s1peqe.selfmanagement.ttimer.action.TimerOccurrenceMBean {

    private final String JMX_TIMER = "timer";
    private static final int TIMER_NO_OF_OCCURRENCES = 3;
    protected static int incOccurrences = 0;

    public TimerOccurrence(){
        new NotifThread(this, TIMER_NO_OF_OCCURRENCES).start();
    }
    public int getNumberOfOccurrences() {
        return this.incOccurrences;
    }

    public synchronized void handleNotification(Notification notification,
        Object handback) {
        try {
            if(notification != null) {
                if(notification.getType().equals(JMX_TIMER)) {
                    incOccurrences++;
                }
            }
        } catch (Exception ex) { }
    }
}

class NotifThread extends Thread {
    private int expectedOccurrences;
    private TimerOccurrence timerMBean;
    NotifThread(TimerOccurrence t, int n) {
        this.expectedOccurrences = n;
        this.timerMBean = t;
    }

    public void run() {
        try {
            System.out.println("Now going to sleep for 40 secs...");
            sleep(60000);
            FileWriter out = new FileWriter(new File("/space/selfmanagementResult.txt"),true);
            if(timerMBean.getNumberOfOccurrences() != expectedOccurrences) {
                out.write("Timer Event - Test FAILED\n");
            } else {
                out.write("Timer Event - Test PASSED\n");
            }
            out.flush();
            out.close();
        } catch(InterruptedException ex) {
        } catch(Exception ex) {}

    }

}

