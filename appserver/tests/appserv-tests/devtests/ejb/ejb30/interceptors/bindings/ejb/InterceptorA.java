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

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import jakarta.annotation.PostConstruct;
import javax.naming.InitialContext;
import jakarta.ejb.SessionContext;
import jakarta.annotation.Resource;

public class InterceptorA {

    @Resource SessionContext sessionCtx;

    @PostConstruct
    private void postConstruct(InvocationContext ctx) {
        try {
            ctx.proceed();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @AroundInvoke
    Object aroundInvoke(InvocationContext ctx)
                throws Exception {

        // access injected environment dependency
        System.out.println("invoked business interface = " +
                           sessionCtx.getInvokedBusinessInterface());

        // look up ejb-ref defined within default interceptor in ejb-jar.xml
        InitialContext ic = new InitialContext();
        Sless3 sless3 = (Sless3) ic.lookup("java:comp/env/ejb/Sless3");


        Common.aroundInvokeCalled(ctx, "A");
        return ctx.proceed();
    }

}
