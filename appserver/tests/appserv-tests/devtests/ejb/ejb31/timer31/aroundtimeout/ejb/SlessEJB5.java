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
import jakarta.ejb.Timer;
import jakarta.ejb.EJBException;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.ExcludeDefaultInterceptors;
import jakarta.interceptor.ExcludeClassInterceptors;
import jakarta.interceptor.InvocationContext;


// Define some aroundtimeout via annotations and some via
// ejb-jar.xml.  Ejb-jar.xml bindings are additive and
// ordered after aroundtimeout at same level declared via
// annotations.
@Stateless
@Interceptors({InterceptorD.class})
public class SlessEJB5 implements Sless5
{
    private volatile static boolean aroundTimeoutCalled = false;
    boolean aroundAllCalled = false;

    private final static int EXPECTED = 3;

    // Called as a timeout and through interface. InderceptorD will
    // set aroundAllCalled to true either way.
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB5-abdc")
    public void abdc() {
        System.out.println("in SlessEJB5:abdc().  aroundTimeoutCalled = " +
                           aroundTimeoutCalled);

        // a little extra checking to make sure AroundTimeout is invoked...
        if( !aroundTimeoutCalled ) {
            throw new EJBException("bean class aroundTimeout not called");
        }
        // Enough if it is set once to true. Otherwise a timer can be executed
        // between verify() and direct call to this method through the interface
        // aroundTimeoutCalled = false;

        if( !aroundAllCalled ) {
            throw new EJBException("InderceptorD aroundAll not called");
        }
        aroundAllCalled = false;
    }

    // Interceptor E added at method level within ejb-jar.xml
    @ExcludeDefaultInterceptors
    @Interceptors({InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB5-dcfe")
    public void dcfe() {}

    @ExcludeClassInterceptors
    @ExcludeDefaultInterceptors
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB5-nothing")
    public void nothing() {}

    // declared in ejb-jar.xml
    private Object aroundTimeout(InvocationContext ctx)
    {
        System.out.println("In SlessEJB5:aroundTimeout");
        aroundTimeoutCalled = true;
        Common.checkResults(ctx);
        try {
            return ctx.proceed();
        } catch(Exception e) {
            throw new EJBException(e);
        }
    }

    public void verify() {
        Common.checkResults("SlessEJB5", EXPECTED);
        aroundTimeoutCalled = true;
    }
}


