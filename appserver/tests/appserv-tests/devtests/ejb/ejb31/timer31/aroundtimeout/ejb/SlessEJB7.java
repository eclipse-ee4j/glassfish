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

package com.sun.s1asdev.ejb.ejb31.aroundtimeout;


import jakarta.ejb.Stateless;
import jakarta.ejb.Schedule;
import jakarta.ejb.EJBException;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.ExcludeDefaultInterceptors;
import jakarta.interceptor.ExcludeClassInterceptors;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

// No-interface view
@Stateless
@ExcludeDefaultInterceptors
@Interceptors({InterceptorD.class, InterceptorC.class})
public class SlessEJB7
{

    private final static int EXPECTED = 1;
    private static String calls = "";

    // called as a timeout and through interface
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB7-dc")
    public void dc() {}

    @AroundTimeout
    private Object aroundTimeout(InvocationContext ctx) throws Exception
    {
        String methodName = ctx.getMethod().getName();
        if (methodName.equals("verify")) {
            throw new EJBException("SlessEJB7:aroundTimeout is called for non-timeout method " + methodName);
        }
        Common.checkResults(ctx);
        Common.storeResult("SlessEJB7-dc");
        return null;
    }

    @AroundInvoke
    private Object aroundInvoke(InvocationContext ctx) throws Exception
    {
        String methodName = ctx.getMethod().getName();
        System.out.println("SlessEJB7:aroundInvoke called for " + methodName);

        return ctx.proceed();
    }

    public void verify() {
        Common.checkResults("SlessEJB7", EXPECTED);
    }
}
