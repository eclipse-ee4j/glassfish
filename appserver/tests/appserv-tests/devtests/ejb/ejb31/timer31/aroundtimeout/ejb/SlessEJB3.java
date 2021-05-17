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

@Stateless
// total ordering DCBA expressed in .xml
@Interceptors({InterceptorC.class, InterceptorD.class})
public class SlessEJB3 implements Sless3
{

    private final static int EXPECTED = 10;

    @ExcludeDefaultInterceptors
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-dc")
    private void dc() {}

    @ExcludeClassInterceptors
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-ba")
    protected void ba() {}

    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-dcba")
    void dcba() {}

    @ExcludeClassInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-baef")
    private void baef() {}

    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-ef")
    private void ef() {}

    @ExcludeDefaultInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-dcef")
    private void dcef() {}

    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-nothing")
    private void nothing() {}

    @Interceptors({InterceptorE.class, InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-dcbaef")
    private void dcbaef() {}

    // total ordering overridden in deployment descriptor
    @Interceptors({InterceptorE.class, InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-abcdef")
    private void abcdef() {}

    // binding described in deployment descriptor
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-dcf")
    private void dcf() {}

    // called only through interface
    @Interceptors({InterceptorA.class, InterceptorG.class})
    public void noaroundtimeout() {}

    @AroundTimeout
    private Object aroundTimeout(InvocationContext ctx) throws Exception
    {
        // Common will verify that noaroundtimeout() is not called.
        Common.checkResults(ctx);
        return null;
    }

    @AroundInvoke
    private Object aroundInvoke(InvocationContext ctx) throws Exception
    {
        String methodName = ctx.getMethod().getName();
        if (!methodName.equals("noaroundtimeout") && !methodName.equals("verify")) {
            throw new EJBException("SlessEJB3:aroundInvoke is called for timeout method " + methodName);
        }
        return ctx.proceed();
    }

    public void verify() {
        Common.checkResults("SlessEJB3", EXPECTED);
    }
}
