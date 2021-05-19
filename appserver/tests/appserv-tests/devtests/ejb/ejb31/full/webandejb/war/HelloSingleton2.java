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
import jakarta.transaction.UserTransaction;

@Singleton
//@Startup
@TransactionManagement(TransactionManagementType.BEAN)
public class HelloSingleton2 {

    //@Resource
    //    private UserTransaction ut;

    @Resource
    private SessionContext sesCtx;

    @PostConstruct
    public void init() {
        System.out.println("In HelloSingleton2::init()");
        UserTransaction ut = sesCtx.getUserTransaction();
        try {
           ut.begin();
           TimerService ts = sesCtx.getTimerService();
           ts.createTimer(2000, "");
           ut.commit();
        } catch(Exception e) {
            try {
                ut.rollback();
            } catch(Exception e1) { e1.printStackTrace(); }
            e.printStackTrace();
        }
        //throw new EJBException("HelloSingleton2 :: force init failure");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In HelloSingleton2::destroy");
    }

    @Timeout
    public void timeout(Timer t) {
        System.out.println("In HelloSingleton2::timeout");
    }

    //@Schedule(second="15", minute="*", persistent=false)
    public void refresh() {
        System.out.println("In HelloSingleton2:refresh()");

    }

    //@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void hello1() {
        throw new EJBException("HelloSingleton2::hello1 -- force business method runtime exception");
        //System.out.println("In HelloSingleton2:hello1()");
    }

    public void hello2() {
        System.out.println("In HelloSingleton2:hello2()");
    }

}
