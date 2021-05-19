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

import jakarta.interceptor.InvocationContext;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptors;


import jakarta.ejb.*;
import jakarta.annotation.*;


@Stateful
@Interceptors(InterceptorB.class)
public class SfulEJB extends BaseBean {

    @EJB private SlessEJB sless;

    @Interceptors(InterceptorA.class)
    public SfulEJB() {}

    @Interceptors(InterceptorA.class)
    public String hello() {
        System.out.println("In SfulEJB:hello()");
        verify("SfulEJB");
        return sless.sayHello();
    }

    @Interceptors(InterceptorA.class)
    @Remove
    public void remove() {
        System.out.println("In SfulEJB:remove()");
    }

    @Interceptors(InterceptorA.class)
    @PostConstruct
    private void init0() {
        System.out.println("**SfulEJB PostConstruct");
        verifyMethod("init0");
    }

}
