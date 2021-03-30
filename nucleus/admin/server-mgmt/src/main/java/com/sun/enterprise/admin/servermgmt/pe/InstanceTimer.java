/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.pe;

public final class InstanceTimer implements Runnable {
    private final int timeOutSeconds;
    private final TimerCallback callBack;
    private final int startAfterSeconds;
    private boolean timeOutReached;
    private long startTime;

    public InstanceTimer(int timeOutSeconds, int startAfterSeconds, TimerCallback callBack) {
        this.timeOutSeconds = timeOutSeconds;
        this.startAfterSeconds = startAfterSeconds;
        this.callBack = callBack;
        this.timeOutReached = false;
    }

    public void run() {
        startTime = System.currentTimeMillis();
        try {
            Thread.sleep(startAfterSeconds * 1000L);
            while (!timeOutReached() && !callBack.check()) {
                try {
                    Thread.sleep(1000);
                    computeTimeOut();
                } catch (InterruptedException ie) {
                    //sLogger.warning(ie.toString());
                    timeOutReached = true;
                }
            }
        } catch (Exception e) {
            //sLogger.warning(e.toString());
            timeOutReached = true;
        }
    }

    private boolean timeOutReached() {
        return timeOutReached;
    }

    private void computeTimeOut() {
        long currentTime = System.currentTimeMillis();
        timeOutReached = ((currentTime - startTime) >= (timeOutSeconds * 1000L));
    }
}
