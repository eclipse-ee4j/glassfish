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

import java.io.Serializable;
import java.util.Date;
import java.rmi.RemoteException;
import jakarta.ejb.*;

public interface TimerStuff {

    TimerHandle createTimer(long duration, String info) throws RemoteException, Exception;

    TimerHandle createTimer(long duration) throws RemoteException, Exception;
    TimerHandle createTimer(long duration, long interval) throws RemoteException, Exception;
    TimerHandle createTimer(long duration, long interval, String info) throws RemoteException, Exception;
    TimerHandle createTimer(Date expirationTime) throws RemoteException, Exception;
    TimerHandle createTimer(Date expirationTime, long interval) throws RemoteException, Exception;

    void createTimerAndRollback(long duration) throws RemoteException, Exception;

    void createTimerAndCancel(long duration) throws RemoteException, Exception;

    void createTimerAndCancelAndCancel(long duration) throws RemoteException, Exception;

    void createTimerAndCancelAndRollback(long duration) throws RemoteException, Exception;

    void cancelTimerNoError(TimerHandle timerHandle) throws RemoteException, Exception;
    void cancelTimer(TimerHandle timerHandle) throws RemoteException, Exception;

    void cancelTimerAndRollback(TimerHandle timerHandle) throws RemoteException, Exception;

    void cancelTimerAndCancel(TimerHandle timerHandle) throws RemoteException, Exception;

    void cancelTimerAndCancelAndRollback(TimerHandle timerHandle) throws RemoteException, Exception;

    void getTimersTest() throws RemoteException, Exception;

    TimerHandle getTimeRemainingTest1(int numIterations) throws RemoteException, Exception;

    void  getTimeRemainingTest2(int numIterations, TimerHandle th) throws RemoteException, Exception;

    TimerHandle getNextTimeoutTest1(int numIterations) throws RemoteException, Exception;

    void  getNextTimeoutTest2(int numIterations, TimerHandle th) throws RemoteException, Exception;

    void assertNoTimers() throws RemoteException, Exception;

    Serializable getInfo(TimerHandle handle) throws RemoteException, Exception;
    Serializable getInfoNoError(TimerHandle handle) throws RemoteException, Exception;

    void assertTimerNotActive(TimerHandle handle) throws RemoteException;

    void sendMessageAndCreateTimer() throws RemoteException, Exception;
    void recvMessageAndCreateTimer(boolean expectMessage)
        throws RemoteException, Exception;
    void sendMessageAndCreateTimerAndRollback()
        throws RemoteException, Exception;
    void recvMessageAndCreateTimerAndRollback(boolean expectMessage)
        throws RemoteException, Exception;
}
