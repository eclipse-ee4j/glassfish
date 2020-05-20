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

package test.beans.interceptors;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import com.acme.StatelessBean;

@Interceptor
@Another
public class AnotherInterceptor {

    @AroundInvoke
    public Object process(InvocationContext ctx) throws Exception {
        System.err.println("====> AnotherInterceptor::AroundInvoke");
        if ((ctx.getTarget() instanceof StatelessBean) && ctx.getMethod().getName().equals("hello")) {
            StatelessBean sb = (StatelessBean)ctx.getTarget();
            sb.interceptorCalled(1);
        }
        return ctx.proceed();
    }

 }
