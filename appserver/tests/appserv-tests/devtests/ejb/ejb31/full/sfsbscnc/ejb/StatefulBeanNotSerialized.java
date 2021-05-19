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

@Stateful(mappedName="StatefulBeanNotSerialized")
@LocalBean
@AccessTimeout(0)
public class StatefulBeanNotSerialized extends StatefulBeanSuper implements StatefulCncRemote, StatefulCncLocal {

 @Resource
    private SessionContext sessionCtx;

    @PostConstruct
    public void init() {
        System.out.println("In StatefulBeanNotSerialized::init()");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In StatefulBeanNotSerialized::destroy()");
    }

    @Asynchronous
    public void sleep(int seconds) {
        System.out.println("In StatefulBeanNotSerialized::asyncSleep");
        try {
            System.out.println("Sleeping for " + seconds + " seconds...");
            Thread.sleep(seconds * 1000);
            System.out.println("Woke up from sleep");
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }
    }

     public String hello() {
        System.out.println("In StatefulBeanNotSerialized::hello");
        return "hello, world!\n";
    }

    public void attemptLoopback() {
        System.out.println("In StatefulBeanNotSerialized::attemptLoopback");
        StatefulCncSuperIntf me = sessionCtx.getBusinessObject(StatefulCncLocal.class);
        try {
            me.hello();
            throw new EJBException("Should have received concurrent access ex");
        } catch(ConcurrentAccessException cae) {
            System.out.println("Successfully received concurent access exception");
        }
    }



}
