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
import jakarta.interceptor.Interceptors;
import jakarta.annotation.*;
import java.util.concurrent.Future;

@Stateful
@Interceptors(NonSerializableInterceptor.class)
public class HelloBean implements Hello {

    @Resource
    private SessionContext sessionCtx;

    private boolean passivated = false;
    private boolean activated = false;

    @EJB private SingletonNoIntf singletonNoIntf;

    @EJB private SingletonMultiIntf singletonMultiIntf;

    @EJB private Local1 local1;

    @EJB private Local1 local2;

    @EJB private Remote1 remote1;

    @EJB private Remote2 remote2;

    @EJB private StatelessNoIntf statelessNoIntf;

    @EJB private StatefulNoIntf statefulNoIntf;


    @PostConstruct
    public void init() {
        System.out.println("In HelloBean::init()");
    }

    public String hello() {
        System.out.println("In HelloBean::hello()");
System.out.println("+++ sessionCtx type: " + sessionCtx.getClass());

        StatefulExpiration se = (StatefulExpiration) sessionCtx.lookup("java:module/StatefulExpiration");
        se.hello();

        singletonNoIntf.hello();
        singletonMultiIntf.hello();
        local1.hello();
        local2.hello();
        remote1.hello();
        remote2.hello();
        statelessNoIntf.hello();
        statefulNoIntf.hello();

        return "hello, world\n";
    }

    public boolean passivatedAndActivated() {
        return passivated && activated;
    }

    @PrePassivate
    public void prePass() {
        System.out.println("In HelloBean::prePass()");
        passivated = true;
    }

    @PostActivate
    public void postAct() {
        System.out.println("In HelloBean::postAct()");
        hello();
        activated = true;
    }



    @PreDestroy
    public void destroy() {
        System.out.println("In HelloBean::destroy()");
    }


}
