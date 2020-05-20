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

public class BaseInterceptor {

    boolean ai = false;
    boolean pc = false;
    boolean pd = false;

    @PostConstruct
    private void afterCreation(InvocationContext ctx) {
        System.out.println("In BaseInterceptor.PostConstruct");
        pc = true;

        try {
            ctx.proceed();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    private void preDestroy(InvocationContext ctx) {
        System.out.println("In BaseInterceptor.PreDestroy");
        pd = true;

        try {
            ctx.proceed();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AroundInvoke
    public Object interceptCall(InvocationContext ctx) throws Exception {
        System.out.println("In BaseInterceptor.AroundInvoke");
        ai = true;
        return ctx.proceed();
    }

}
