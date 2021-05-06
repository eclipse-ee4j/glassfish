/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import jakarta.interceptor.InvocationContext;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import jakarta.ejb.PrePassivate;
import jakarta.ejb.PostActivate;
import jakarta.ejb.EJB;
import jakarta.annotation.Resource;
import jakarta.ejb.SessionContext;

public class LifecycleCallbackInterceptor {

    private static int prePassivateCallbackCount = 0;
    private static int postActivateCallbackCount = 0;

    private int interceptorID;
    private int computedInterceptorID;

    @EJB Sless sless;
    @Resource SessionContext sessionCtx;

    @PrePassivate
    void prePassivate(InvocationContext ctx) {
        prePassivateCallbackCount++;
    }

    @PostActivate
    private void postActivate(InvocationContext ctx) {
        postActivateCallbackCount++;
    }

    @AroundInvoke
    Object aroundInvoke(InvocationContext ctx)
                throws Exception {

        System.out.println("In LifecycleCallbackInterceptor:aroundInvoke()");
        sless.sayHello();
        System.out.println("caller principal = " + sessionCtx.getCallerPrincipal());

        if (ctx.getMethod().getName().equals("setID")) {
            ctx.getContextData().put("LifecycleCallbackInterceptor", this);
            System.out.println("calling sless from interceptor. sless says " +
                               sless.sayHello());
            System.out.println("invoked business interface = " +
                               sessionCtx.getInvokedBusinessInterface());
        }
        return ctx.proceed();
    }

    public static void resetLifecycleCallbackCounters() {
        prePassivateCallbackCount = postActivateCallbackCount = 0;
    }

    public static int getPrePassivateCallbackCount() {
        return prePassivateCallbackCount;
    }

    public static int getPostActivateCallbackCount() {
        return postActivateCallbackCount;
    }


    void setInterceptorID(int val) {
        this.interceptorID = val;
        this.computedInterceptorID = 2 * val + 1;
    }

    int getInterceptorID() {
        return interceptorID;
    }

    boolean checkInterceptorID(int val) {
        return (val == interceptorID) &&
            (computedInterceptorID == 2 * val + 1);
    }

}
