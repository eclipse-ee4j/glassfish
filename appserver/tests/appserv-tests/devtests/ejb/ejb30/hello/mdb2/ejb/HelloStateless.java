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

package com.sun.s1asdev.ejb.ejb30.hello.mdb2;

import jakarta.ejb.Stateless;
import jakarta.ejb.TimerService;
import jakarta.ejb.Local;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.EJBException;

@Local({Hello1.class})
@Stateless public class HelloStateless extends HelloStatelessSuper implements Hello1 {

    @Resource TimerService timerSvc;
    private boolean initialized = false;

    @PostConstruct public void create() {
        System.out.println("in HelloStateless:create");
        initialized = true;
    }

    public void hello(String s) {
        if( !initialized ) {
            throw new EJBException("not initialized");
        }

        System.out.println("HelloStateless: " + s);
        timerSvc.createTimer(1, "quick timer");
        System.out.println("Created quick timer");
    }

}
