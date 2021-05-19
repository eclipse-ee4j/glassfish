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

public class BaseLevel2Interceptor
   extends BaseInterceptor {

    protected static final String LEVEL2_INTERCEPTOR_NAME = "Level2Interceptor";

    protected int baseLevel2AICount = 0;
    protected int baseLevel2PCCount = 0;

    @PostConstruct
    protected void overridablePostConstructMethod(InvocationContext ctx)
            throws RuntimeException {
        postConstructList.add(LEVEL2_INTERCEPTOR_NAME);
        baseLevel2PCCount++;
        try {
            ctx.proceed();
        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AroundInvoke
    protected Object overridableAroundInvokeMethod(InvocationContext ctx)
            throws Throwable {
        aroundInvokeList.add(LEVEL2_INTERCEPTOR_NAME);
        baseLevel2AICount++;
        return ctx.proceed();
    }

    String getName() {
       return BaseLevel2Interceptor.class.getName();
    }

}
