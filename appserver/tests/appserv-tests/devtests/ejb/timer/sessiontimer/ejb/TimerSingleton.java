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

import jakarta.ejb.*;
import java.util.concurrent.*;

@Singleton
@LocalBean
@Remote(TimerSingletonRemote.class)
@Lock(LockType.READ)
public class TimerSingleton
{

    boolean timeoutReceived = false;

    public void startTest() {
        timeoutReceived = false;
    }

    public void setTimeoutReceived() {
        System.out.println("TimerSingleton::setTimeoutReceived()");
        timeoutReceived = true;
    }

     public boolean waitForTimeout(int seconds) {
         int i = 0;
         while(i < seconds) {
             i++;
             try {
                 Thread.sleep(1000);
                 if( timeoutReceived ) {
                     System.out.println("Got timeout after " +
                                        i + " seconds");
                     break;
                 }
             } catch(Exception e) {
                 e.printStackTrace();
                 throw new EJBException(e);
             }
         }
        return timeoutReceived;
    }

}
