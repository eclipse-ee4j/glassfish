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

package com.sun.s1asdev.ejb32.ejblite.timer;

import java.io.Serializable;
import java.util.Date;
import jakarta.ejb.*;

public interface TimerStuff {

    Timer createTimer(long duration, String info) throws Exception;

    Timer createTimer(long duration) throws  Exception;
    Timer createTimer(long duration, long interval) throws  Exception;
    Timer createTimer(long duration, long interval, String info) throws  Exception;
    Timer createTimer(Date expirationTime) throws  Exception;
    Timer createTimer(Date expirationTime, long interval) throws  Exception;

    void createTimerAndRollback(long duration) throws  Exception;

    void createTimerAndCancel(long duration) throws  Exception;

    void createTimerAndCancelAndCancel(long duration) throws  Exception;

    void createTimerAndCancelAndRollback(long duration) throws  Exception;

    void cancelTimerNoError(Timer timer) throws  Exception;
    void cancelTimer(Timer timer) throws  Exception;

    void cancelTimerAndRollback(Timer timer) throws  Exception;

    void cancelTimerAndCancel(Timer timer) throws  Exception;

    void cancelTimerAndCancelAndRollback(Timer timer) throws  Exception;

    void getTimersTest() throws  Exception;

    Timer getTimeRemainingTest1(int numIterations) throws  Exception;

    void  getTimeRemainingTest2(int numIterations, Timer th) throws  Exception;

    Timer getNextTimeoutTest1(int numIterations) throws  Exception;

    void  getNextTimeoutTest2(int numIterations, Timer th) throws  Exception;

    void assertNoTimers() throws  Exception;

    Serializable getInfo(Timer timer) throws  Exception;
    Serializable getInfoNoError(Timer timer) throws  Exception;

    void assertTimerNotActive(Timer timer) throws Exception;

}
