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
import jakarta.annotation.PostConstruct;
import jakarta.ejb.PrePassivate;
import jakarta.ejb.PostActivate;

public class BaseInterceptor {

    private static int baseCount = 0;
    private static int prePassivateCount = 0;
    private static int postActivateCount = 0;

    private int interceptorID;
    private int computedInterceptorID;

    @PostConstruct
    private void afterCreation(InvocationContext ctx) {
        System.out.println("In BaseInterceptor.afterCreation");
        ctx.getContextData().put("PostConstruct", "BaseInterceptor");
        try {
            ctx.proceed();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @AroundInvoke
    public Object interceptCall(InvocationContext ctx)
           throws Exception
    {
        Object result = null;
        boolean setId = false;
        baseCount++;
    if (ctx.getMethod().getName().equals("setInterceptorId")) {
        java.util.Map map = ctx.getContextData();
        map.put("BaseInterceptor", this);
        setId = true;
    }
    try {
        result = ctx.proceed();
    } finally {
        if (setId == true) {
            computedInterceptorID = interceptorID * 2 + 1;
        }
    }
    return result;
    }

    public static int getInvocationCount() {
            return baseCount;
    }

    @PrePassivate
    public void prePassivate(InvocationContext ctx) {
        prePassivateCount++;
        System.out.println("prePassivateCount: " + prePassivateCount);
    }

    @PostActivate
    public void postActivate(InvocationContext ctx) {
        postActivateCount++;
        System.out.println("postActivateCount: " + postActivateCount);
    }

    //Some package private methods to check state
    void setInterceptorID(int val) {
        this.interceptorID = val;
    }

    int getInterceptorID() {
        boolean valid = (computedInterceptorID == interceptorID * 2 + 1);
        if (! valid) {
            throw new IllegalStateException("" + interceptorID + " != " + computedInterceptorID);
        }
        return this.interceptorID;
    }

}
