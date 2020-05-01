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

package com.acme;

import jakarta.interceptor.InvocationContext;
import jakarta.interceptor.AroundInvoke;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

public class InterceptorA extends BaseInterceptor {

    @PostConstruct
    private void afterCreation0(InvocationContext ctx) {
        System.out.println("In InterceptorA.PostConstruct");
        if (!pc) throw new RuntimeException("BaseInterceptor was not called");
        pc = false; //reset

        try {
            ctx.proceed();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    private void preDestroy0(InvocationContext ctx) {
        System.out.println("In InterceptorA.PreDestroy");
        try {
            ctx.proceed();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AroundInvoke
    public Object interceptCall0(InvocationContext ctx) throws Exception {
        System.out.println("In InterceptorA.AroundInvoke");
        if (!ai) throw new RuntimeException("BaseInterceptor was not called");
        ai = false; //reset
        return ctx.proceed();
    }

}
