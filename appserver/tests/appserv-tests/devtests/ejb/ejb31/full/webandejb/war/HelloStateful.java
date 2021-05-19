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
import java.util.concurrent.TimeUnit;

@Stateful
@LocalBean
@Local(Hello.class)
@StatefulTimeout(value=15, unit=TimeUnit.SECONDS)
public class HelloStateful {

    @Resource
    private SessionContext sesCtx;

    private HelloStateful me;

    @EJB HelloStateful2 sf2;

    // invalid    @EJB java.util.Observable sf22;


    @PostConstruct
    private void init() {
        System.out.println("HelloStateful::init()");
               me = sesCtx.getBusinessObject(HelloStateful.class);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String hello() {

        System.out.println("get invoked interface = " +
                             sesCtx.getInvokedBusinessInterface() );

        sf2.hello();
        sf2.goodbye();

        return "hello, world!\n";
    }

    public void foo() {
        System.out.println("In HelloStateful::foo");
    }

    @Remove
    public void goodbye() {}

    @PreDestroy
    private void destroy() {
        System.out.println("HelloStateful::destroy()");
    }

    @AfterBegin
        private void afterBegin() {
        System.out.println("In HelloStateful::afterBegin()");
    }

    @BeforeCompletion
        protected void beforeCompletion() {
        System.out.println("In HelloStateful::beforeCompletion()");
    }

    @AfterCompletion
    void afterCompletion(boolean committed) {
        System.out.println("In HelloStateful::afterCompletion(). Committed = " +
                           committed);
    }


}
