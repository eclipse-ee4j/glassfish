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

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Secure @Interceptor
public class SecureInterceptor {
    public static boolean aroundInvokeCalled = false;
    public static int aroundInvokeInvocationCount = 0;
    public static String errorMessage = "";
    
    @Inject
    TestRequestScopedBean tb;
    
    @AroundInvoke
    public Object manageTransaction(InvocationContext ctx) throws Exception {
        
        System.out.println("SecureInterceptor::AroundInvoke");
        if (tb == null) errorMessage="Dependency Injection " +
                "into TransactionInterceptor failed";
        aroundInvokeCalled = true;
        aroundInvokeInvocationCount ++;
        tb.interceptorInvocationOrder.add(this.getClass().getCanonicalName());
        return ctx.proceed();
    }

    public static void clear() {
        aroundInvokeCalled = false;
        aroundInvokeInvocationCount = 0;
        errorMessage = "";
    }

}
