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
@Interceptors(InterceptorA.class)
public class SfulEJB extends BaseBean implements Sful {

    @EJB private Sless sless;

    public String hello() {
        System.out.println("In SfulEJB:hello()");
        return sless.sayHello();
    }

    @Remove
    public void remove() {
        System.out.println("In SfulEJB:remove()");
    }

    @AroundInvoke
    private Object interceptCall0(InvocationContext ctx) throws Exception {
        System.out.println("**SfulEJB AROUND-INVOKE++ [@AroundInvoke]: " + ctx.getMethod());
        if (!ai) throw new RuntimeException("BaseBean was not called");
        ai = false; //reset
        return ctx.proceed();
    }

    @PostConstruct
    private void init0() {
        System.out.println("**SfulEJB PostConstruct");
        if (!pc) throw new RuntimeException("BaseBean was not called");
        pc = false; //reset
    }

    @PreDestroy
    private void destroy0() {
        System.out.println("**SfulEJB PreDestroy");
    }

}
