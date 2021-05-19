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

import javax.naming.*;

import jakarta.enterprise.inject.spi.BeanManager;

@Singleton
@Startup
@LocalBean
    public class SingletonBean /* implements HelloRemote */ {

    @EJB SingletonBean me;

    @EJB StatefulBean sf;

    @Resource SessionContext sesCtx;

    private boolean gotAsyncCall = false;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");
        System.out.println("Thread = " + Thread.currentThread());
        me.fooAsync();

        try {
                BeanManager beanMgr = (BeanManager)
                    new InitialContext().lookup("java:comp/BeanManager");
        System.out.println("Successfully retrieved bean manager " +
                           beanMgr + " for JCDI enabled app");
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }


    }

    public String hello() {
        System.out.println("In SingletonBean::hello()");
        return "hello, world!\n";
    }

    @Asynchronous
    public void fooAsync() {
        System.out.println("In SingletonBean::fooAsync()");
        System.out.println("Thread = " + Thread.currentThread());
        gotAsyncCall = true;
    }

    public boolean getPassed() {
        return gotAsyncCall;
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
