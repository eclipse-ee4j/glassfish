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

import jakarta.interceptor.InvocationContext;
import jakarta.interceptor.AroundInvoke;
import jakarta.annotation.PostConstruct;

public class BlockingInterceptor {
    //implements java.io.Serializable {

    private static int blockCount = 0;

    @PostConstruct
    private void afterCreation(InvocationContext ctx) {

        String value = (String) ctx.getContextData().get("PostConstruct");
        System.out.println("In BlockingInterceptor.afterCreation value = "
                           + value);
        if(value == null) {
            throw new IllegalStateException("BaseInterceptor.PostConstruct " +
                                            " should have executed first");
        }
        try {
            ctx.proceed();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @AroundInvoke
    private Object interceptCall(InvocationContext ctx)
           throws CallBlockedException
    {
        System.out.println("[test.BlockingInterceptor @AroundInvoke]: " + ctx.getMethod());
        blockCount++;
        throw new CallBlockedException("Call blocked");
    }


}
