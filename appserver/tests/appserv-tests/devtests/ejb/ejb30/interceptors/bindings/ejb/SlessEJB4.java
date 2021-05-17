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

package com.sun.s1asdev.ejb.ejb30.interceptors.bindings;


import jakarta.ejb.Stateless;
import jakarta.ejb.EJBException;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.ExcludeDefaultInterceptors;
import jakarta.interceptor.ExcludeClassInterceptors;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;


// Exclude default interceptors, but re-add one of them at class-level
@Stateless
@ExcludeDefaultInterceptors
@Interceptors({InterceptorC.class, InterceptorB.class, InterceptorD.class})
public class SlessEJB4 implements Sless4
{
    private boolean aroundInvokeCalled = false;

    public void cbd() {
        System.out.println("in SlessEJB4:cbd().  aroundInvokeCalled = " +
                           aroundInvokeCalled);

        // a little extra checking to make sure aroundInvoke is invoked...
        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke not called");
        }
        aroundInvokeCalled = false;
    }

    @ExcludeClassInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    public void ef() {}

    // explicitly add default interceptors that were disabled at
    // class-level.
    @ExcludeClassInterceptors
    @Interceptors({InterceptorA.class, InterceptorB.class,
                   InterceptorE.class, InterceptorF.class})
    public void abef(int i) {}

    // @ExcludeDefaultInterceptors is a no-op here since it
    // was already excluded at class-level
    @ExcludeDefaultInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    public void cbdef() {}

    @ExcludeClassInterceptors
    public void nothing() {}

    // declared in ejb-jar.xml
    private Object aroundInvoke(InvocationContext ctx)
    {
        System.out.println("In SlessEJB4:aroundInvoke");
        aroundInvokeCalled = true;
        Common.checkResults(ctx);
        try {
            return ctx.proceed();
        } catch(Exception e) {
            throw new EJBException(e);
        }
    }

}


