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

import jakarta.ejb.*;
import jakarta.annotation.*;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class FooBMT extends TimerStuffImpl implements Foo {

    @Resource private SessionContext sc;

    @Timeout
    @Schedule(second="*/2", minute="*", hour="*", info="Automatic BMT", persistent=false)
    public void timeout(Timer t) {
        try {
            System.out.println("In FooBMT::Timeout --> " + t.getInfo());
            if (t.isPersistent())
                throw new RuntimeException("FooBMT::Timeout -> "
                       + t.getInfo() + " is PERSISTENT!!!");
        } catch(RuntimeException e) {
            System.out.println("got exception while calling getInfo");
            throw e;
        }

        try {
            handleTimeout(t);
        } catch(RuntimeException re) {
            throw re;
        } catch(Exception e) {
            System.out.println("handleTimeout threw exception");
            e.printStackTrace();
        }

    }

    @PostConstruct
    private void init() throws EJBException {
        System.out.println("In ejblite.timer.Foo::init !!");
        isBMT = true;
        setContext(sc);
        getTimerService("init", true);
        doTimerStuff("init", false);
    }

    @PreDestroy
    public void remove() throws EJBException {
        System.out.println("In FooBMT::remove");
        getTimerService("remove", true);
        doTimerStuff("remove", false);
    }

}
