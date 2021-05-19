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

@Singleton
@Lock(LockType.READ)
public class SingletonBean implements RemoteAsync {

    @Resource
    private SessionContext sessionCtx;

    private AtomicInteger fireAndForgetCount = new AtomicInteger();

    private AtomicInteger processAsyncCount = new AtomicInteger();

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");

    }

    public void startTest() {
        System.out.println("in SingletonBean::startTest()");
        // reset state
        fireAndForgetCount = new AtomicInteger();
        return;
    }

    @Asynchronous
    public void fireAndForget() {
        System.out.println("In SingletonBean::fireAndForget()");
        fireAndForgetCount.incrementAndGet();
    }

    public int getFireAndForgetCount() {
        return fireAndForgetCount.get();
    }

    @Asynchronous
    public Future<String> helloAsync() {
        return new AsyncResult<String>("hello, world\n");
    }

    @Asynchronous
    public Future<Integer> processAsync(int sleepInterval, int numIntervals)
       throws Exception {
        int me = processAsyncCount.incrementAndGet();
        boolean cancelled = false;
        int i = 0;
        while(i < numIntervals) {
            i++;
            if( sessionCtx.wasCancelCalled() ) {
                System.out.println("Cancelling processAsync " + me + " after " +
                                   i + " intervals");
                cancelled = true;
                break;
            }
            try {
                System.out.println("Sleeping for " + i + "th time in processAsync " +
                                   me);
                Thread.sleep(sleepInterval * 1000);
                System.out.println("Woke up for " + i + "th time in processAsync " +
                                   me);
            } catch(Exception e) {
                e.printStackTrace();
                throw new EJBException(e);
            }
        }

        if( cancelled ) {
            throw new Exception("Cancelled processAsync " + me);
        }

        return new AsyncResult<Integer>(numIntervals);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
