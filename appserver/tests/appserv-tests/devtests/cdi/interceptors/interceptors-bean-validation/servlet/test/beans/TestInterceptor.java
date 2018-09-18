/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.beans;

import javax.inject.Inject;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@CDITest
@Interceptor
public class TestInterceptor {

    public static boolean aroundConstructCalled = false;
    public static int     aroundConstructInvocationCount = 0;

    public static boolean aroundInvokeCalled = false;
    public static int     aroundInvokeInvocationCount = 0;

    public static String  errorMessage = "";

    @AroundConstruct
    public void interceptConstruction(InvocationContext ctx) throws Exception {

        System.out.println(getClass().getName() + "::AroundConstruct");

        aroundConstructCalled = true;
        aroundConstructInvocationCount++;

        ctx.proceed();
    }

    @AroundInvoke
    public Object interceptInvocation(InvocationContext ctx) throws Exception {

        System.out.println(getClass().getName() + "::AroundInvoke");

        aroundInvokeCalled = true;
        aroundInvokeInvocationCount++;

        return ctx.proceed();
    }

    public static void clear() {
        aroundConstructCalled = false;
        aroundConstructInvocationCount = 0;
        errorMessage = "";
    }

 }
