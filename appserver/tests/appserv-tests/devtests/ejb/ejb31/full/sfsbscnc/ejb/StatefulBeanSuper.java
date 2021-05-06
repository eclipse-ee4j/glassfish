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

package com.acme;

import jakarta.ejb.*;
import jakarta.annotation.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public abstract class StatefulBeanSuper implements StatefulCncSuperIntf {

    private int count = 0;

    @AccessTimeout(-1)
     public String hello() {
        System.out.println("In StatefulBeanSuper::hello");
        return "hello, world!\n";
    }

    @AccessTimeout(value=100, unit=TimeUnit.NANOSECONDS)
    public String helloWait() {
        System.out.println("In StatefulBeanSuper::helloWait");
        return "hello, world!\n";
    }

    @Asynchronous
    public Future<String> helloAsync() {
        System.out.println("In StatefulBeanSuper::helloAsync");
        return new AsyncResult<String>("hello");
    }

    @Asynchronous
    public void fireAndForget() {
        System.out.println("In StatefulBeanSuper::fireAndForget");
    }

    @Asynchronous
    public void sleep(int seconds) {
        System.out.println("In StatefulBeanSuper::asyncSleep");
        try {
            System.out.println("Sleeping for " + seconds + " seconds...");
            Thread.sleep(seconds * 1000);
            System.out.println("Woke up from sleep");
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }
    }

    @Asynchronous
    @Remove
    public void sleepAndRemove(int seconds) {
        System.out.println("In StatefulBeanSuper::sleepAndRemove");
        try {
            System.out.println("Sleeping for " + seconds + " seconds...");
            Thread.sleep(seconds * 1000);
            System.out.println("Woke up from sleep. I will now be removed...");
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }
    }

    public void incrementCount(int seconds) {
        System.out.println("In StatefulBeanSuper::incrementCount(" + seconds + ")");
        // increment count but wait a bit to try force serialization
        sleep(seconds);
        count++;
        System.out.println("Count = " + count);
    }

    public int getCount() {
        return count;
    }
}
