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

import java.lang.reflect.Method;

import jakarta.interceptor.InvocationContext;
import jakarta.interceptor.AroundInvoke;

import jakarta.ejb.*;
import jakarta.annotation.*;

public class ErrorRecoveryInterceptor {

    @Resource(name="sc") SessionContext sc;

    @EJB(name="foobar") Sless foobar;

    @AroundInvoke
    public Object interceptCall(InvocationContext ctx)
           throws MyBadException, AssertionFailedException, Exception
    {
        System.out.println("In ErrorREcoveryInterceptor");
        System.out.println("sless = " + foobar);
        System.out.println("sc = " + sc);

        Method method = ctx.getMethod();
        String methodName = method.getName();
        Object[] params = ctx.getParameters();
        boolean recoverFromError = false;
        Object retVal = ctx.proceed();

        if (methodName.equals("assertIfTrue")) {
            params[0] = new Boolean(false);

            try {
                ctx.setParameters(params);
            } catch (IllegalArgumentException illArgEx) {
                throw new MyBadException("Invalid type as argument", illArgEx);
            }

            retVal = ctx.proceed();
        }

        return retVal;
    }

}
