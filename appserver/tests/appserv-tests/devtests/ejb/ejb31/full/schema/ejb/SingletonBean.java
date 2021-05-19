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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.concurrent.TimeUnit;

import jakarta.annotation.*;

@Startup
@Singleton
@LocalBean
public class SingletonBean implements RemoteSingleton {


    @EJB
    private StatefulBean sf;

    @Resource
    private SessionContext ctx;

    @EJB
    private SingletonBean me;

    private boolean gotTimeout;

    @PostConstruct
    private void init() {
        System.out.println("In SingletonBean::init()");

        // Call a method marked as async in .xml.  This would block
        // if it's not correctly identified as an async method since
        // we're not out of PostConstruct yet.
        me.foo();

        SingletonBean2 sb2 = (SingletonBean2)
            ctx.lookup("java:module/SingletonBean2");

        // Call read-lock async method and have it sleep for a little
        // while
        sb2.fooAsync(10);

        // Sleep for a short time to allow enough time for fooAsync() to
        // block
        try {
            Thread.sleep(1000);
        } catch(Exception e) {
            e.printStackTrace();
        }

        // Call a sychronous write-lock method with a .xml specified
        // timeout that's less than the fooAsync sleep time
        try {
            sb2.foo();
            throw new EJBException("Should have gotten timeout exception");
        } catch(ConcurrentAccessTimeoutException cate) {
            System.out.println("Got expected timeout exception");
        }

        RemoteSingleton lookup1 = (RemoteSingleton)
            ctx.lookup("java:comp/env/ejb/lookup1");
        System.out.println("lookup1 = " + lookup1);

        SingletonBean2 lookup2 = (SingletonBean2)
            ctx.lookup("java:comp/env/ejb/lookup2");
        System.out.println("lookup2 = " + lookup2);

        // Now call a synchronous read-lock method.  This should just
        // proceed.
        sb2.foo2();

        sf.foo();

        System.out.println("Leaving SingletonBean::init()");
    }

    @AccessTimeout(value=10, unit=TimeUnit.DAYS)
    public void foo() {
        System.out.println("In SingletonBean::foo()");
    }

    @PreDestroy
    private void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }

    @Schedule(second="1", minute="*", hour="*", persistent=false)
    private void mytimeout(Timer t) {
        System.out.println("In SingletonBean::mytimeout, info = " + t.getInfo() + "t = " +
                           t.getSchedule());

        try {
            t.getHandle();
            System.out.println("Successfully got handle for persistent timer");
        } catch(IllegalStateException e) {
            throw new EJBException(e);
        }

        if( t.getInfo().equals("info") && t.isPersistent() && t.isCalendarTimer() ) {
            gotTimeout = true;
        } else {
            throw new EJBException("Timer metadata doesn't match descriptor " + t);
        }
    }

    private void mytimeout2() {
        System.out.println("In SingletonBean::mytimeout");
    }

    private void mytimeout1(Timer t) {
        System.out.println("In SingletonBean::mytimeout1");
    }

    public boolean getTestResult() {
        System.out.println("In SingletonBean::testPassed");
        return gotTimeout;
    }
}
