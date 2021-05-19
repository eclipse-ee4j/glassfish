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
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.ExcludeDefaultInterceptors;
import jakarta.interceptor.ExcludeClassInterceptors;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

@Stateless
// total ordering DCBA expressed in .xml
@Interceptors({InterceptorC.class, InterceptorD.class})
public class SlessEJB3 implements Sless3
{

    @ExcludeDefaultInterceptors
    public void dc() {}

    @ExcludeClassInterceptors
    public void ba() {}

    public void dcba() {}

    @ExcludeClassInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    public void baef() {}

    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    public void ef() {}

    @ExcludeDefaultInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    public void dcef() {}

    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    public void nothing() {}

    @Interceptors({InterceptorE.class, InterceptorF.class})
    public void dcbaef() {}

    // total ordering overridden in deployment descriptor
    @Interceptors({InterceptorE.class, InterceptorF.class})
    public void abcdef() {}

    // binding described in deployment descriptor
    public void dcf() {}

    @AroundInvoke
    private Object aroundInvoke(InvocationContext ctx)
    {
        Common.checkResults(ctx);
        return null;
    }

}
