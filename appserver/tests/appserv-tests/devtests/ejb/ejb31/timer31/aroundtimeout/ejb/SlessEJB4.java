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

import java.util.Collection;
import jakarta.ejb.Stateless;
import jakarta.ejb.Schedule;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerService;
import jakarta.ejb.EJBException;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.ExcludeDefaultInterceptors;
import jakarta.interceptor.ExcludeClassInterceptors;
import jakarta.interceptor.InvocationContext;
import javax.naming.InitialContext;
import jakarta.annotation.Resource;

// Exclude default aroundtimeout, but re-add one of them at class-level
@Stateless
@ExcludeDefaultInterceptors
@Interceptors({InterceptorC.class, InterceptorB.class, InterceptorD.class})
public class SlessEJB4 implements Sless4
{
    @Resource TimerService timerSvc;

    private boolean aroundTimeoutCalled = false;

    private final static int EXPECTED = 5;

    // Called as a timeout and through interface. When called through interface
    // aroundTimeout should be still false, and exception is expected.
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB4-cbd")
    public void cbd() {
        System.out.println("in SlessEJB4:cbd().  aroundTimeoutCalled = " +
                           aroundTimeoutCalled);

        // a little extra checking to make sure aroundTimeout is invoked...
        if( !aroundTimeoutCalled ) {
            throw new EJBException("bean class aroundTimeout not called - may be correct - check the call stack");
        }
        aroundTimeoutCalled = false;
    }

    @ExcludeClassInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB4-ef")
    public void ef() {}

    // explicitly add default aroundtimeout that were disabled at
    // class-level.
    @ExcludeClassInterceptors
    @Interceptors({InterceptorA.class, InterceptorB.class,
                   InterceptorE.class, InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB4-abef")
    public void abef(Timer t) {}

    // @ExcludeDefaultInterceptors is a no-op here since it
    // was already excluded at class-level
    @ExcludeDefaultInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB4-cbdef")
    public void cbdef() {}

    @ExcludeClassInterceptors
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB4-nothing")
    public void nothing() {}

    // declared in ejb-jar.xml
    private Object aroundTimeout(InvocationContext ctx)
    {
        System.out.println("In SlessEJB4:aroundTimeout");
        aroundTimeoutCalled = true;
        Common.checkResults(ctx);
        try {
            return ctx.proceed();
        } catch(Exception e) {
            throw new EJBException(e);
        }
    }

    public void verify() {
        Common.checkResults("SlessEJB4", EXPECTED);
        Collection<Timer> timers = timerSvc.getTimers();
        for (Timer t : timers)
            t.cancel();
        aroundTimeoutCalled = false;
    }
}


