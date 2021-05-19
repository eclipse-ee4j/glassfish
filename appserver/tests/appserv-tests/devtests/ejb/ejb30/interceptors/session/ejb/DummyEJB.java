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

import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.InvocationContext;
import jakarta.interceptor.AroundInvoke;
import jakarta.ejb.EJBException;
import jakarta.annotation.PostConstruct;

@Interceptors({
        com.sun.s1asdev.ejb.ejb30.interceptors.session.BaseInterceptor.class,
        com.sun.s1asdev.ejb.ejb30.interceptors.session.BlockingInterceptor.class
})

@Stateless
public class DummyEJB
        implements Dummy
{
    int interceptorId;
    private boolean createCalled = false;

    @PostConstruct
    private void afterCreate()
    {
        System.out.println("In DummyEJB::afterCreate");
        createCalled = true;
    }

    public String dummy()
        throws CallBlockedException
    {
            return "Dummy!!";
    }

    public void setInterceptorId(int val) {
        if( !createCalled ) {
            throw new EJBException("create was not called");
        }

        this.interceptorId = val;
    }

    @AroundInvoke
    private Object interceptCall(InvocationContext ctx)
        throws Exception {
        return ctx.proceed();
    }
}

