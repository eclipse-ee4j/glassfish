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
import java.util.concurrent.Future;

@Stateless(mappedName="HH")
@RemoteHome(HelloHome.class)
@Remote(Hello.class)
@TransactionManagement(TransactionManagementType.BEAN)
@EJB(name="java:global/HBGlobal", beanName="HelloBean", beanInterface=Hello.class)
public class HelloBean {

    @Resource(name="java:module/env/sesCtx")
    private SessionContext sessionCtx;

    @EJB(name="java:module/env/M1")
    private Hello m1;

    @EJB(name="java:module/M2")
    private HelloHome m2;

    @EJB(name="java:app/env/A1")
    private Hello a1;

    @EJB(name="java:app/A2")
    private HelloHome a2;

    @EJB(name="java:comp/env/C1")
    private Hello c1;

    @EJB(name="java:comp/C2")
    private HelloHome c2;


    @PostConstruct
    public void init() {
        System.out.println("In HelloBean::init()");
    }

    public String hello() {
        System.out.println("In HelloBean::hello()");
        return "hello, world\n";
    }

    @Asynchronous
    public Future<String> helloAsync() {
        System.out.println("In HelloBean::helloAsync()");
        return new AsyncResult<String>("helo, async world!\n");
    }

    @Asynchronous
    public Future<String> asyncBlock(int seconds) {
        System.out.println("In HelloBean::asyncBlock");
        sleep(seconds);
        return new AsyncResult<String>("blocked successfully");
    }

    @Asynchronous
    public void fireAndForget() {
        System.out.println("In HelloBean::fireAndForget()");
        sleep(5);
    }

    @Asynchronous
    public Future<String> asyncThrowException(String exceptionType) {
        System.out.println("In HelloBean::asyncThrowException");
        throwException(exceptionType);
        return new AsyncResult<String>("should have thrown exception");
    }

    @Asynchronous
    public Future<String> asyncCancel(int seconds) throws Exception
    {
        System.out.println("In HelloBean::asyncCancel");
        sleep(seconds);
        if( sessionCtx.wasCancelCalled() ) {
            throw new Exception("Canceled after " + seconds + " seconds");
        }
        return new AsyncResult<String>("asyncCancel() should have been cancelled");
    }

    public void throwException(String exceptionType) {
        if( exceptionType.equals("jakarta.ejb.EJBException") ) {
            throw new EJBException(exceptionType);
        } else if( exceptionType.equals("jakarta.ejb.ConcurrentAccessException") ) {
            throw new ConcurrentAccessException(exceptionType);
        } else if( exceptionType.equals("jakarta.ejb.ConcurrentAccessTimeoutException") ) {
            throw new ConcurrentAccessTimeoutException(exceptionType);
        } else if( exceptionType.equals("jakarta.ejb.IllegalLoopbackException") ) {
            throw new IllegalLoopbackException(exceptionType);
        }

        throw new IllegalArgumentException(exceptionType);
    }

    private void sleep(int seconds) {

        System.out.println("In HelloBean::sleeping for " + seconds +
                           "seconds");
        try {
            Thread.currentThread().sleep(seconds * 1000);
            System.out.println("In HelloBean::woke up from " + seconds +
                               "second sleep");
        } catch(Exception e) {
            e.printStackTrace();
        }

    }


    @PreDestroy
    public void destroy() {
        System.out.println("In HelloBean::destroy()");
    }


}
