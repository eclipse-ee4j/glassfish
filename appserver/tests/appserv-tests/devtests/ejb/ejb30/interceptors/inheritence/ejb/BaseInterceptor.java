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

import java.util.List;
import java.util.ArrayList;

import jakarta.interceptor.InvocationContext;
import jakarta.interceptor.AroundInvoke;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.PrePassivate;
import jakarta.ejb.PostActivate;

public class BaseInterceptor
        implements java.io.Serializable {

    protected static final String BASE_INTERCEPTOR_NAME = "BaseInterceptor";

    protected List<String> postConstructList = new ArrayList<String>();
    protected List<String> aroundInvokeList = new ArrayList<String>();

    protected int baseAICount = 0;
    protected int prePassivateCount = 0;
    private int postActivateCount = 0;
    protected int basePCCount = 0;


    @PostConstruct
    private void basePostConstruct(InvocationContext ctx)
            throws RuntimeException {
            postConstructList = new ArrayList<String>();
            postConstructList.add(BASE_INTERCEPTOR_NAME);
        ctx.getContextData().put(getName(), this);
        basePCCount++;
        System.out.println("GGGG: @PostConstruct for: " + this.getClass().getName());
        try {
            ctx.proceed();
        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }


    @AroundInvoke
    public Object baseAroundInvoke(InvocationContext ctx)
                   throws Exception
    {
            aroundInvokeList = new ArrayList<String>();
            aroundInvokeList.add(BASE_INTERCEPTOR_NAME);
                ctx.getContextData().put(getName(), this);
                baseAICount++;
            return ctx.proceed();
    }

    @PrePassivate
    public void prePassivate(InvocationContext ctx) {
        prePassivateCount++;
        }

    @PostActivate
    public void postActivate(InvocationContext ctx) {
        postActivateCount++;
        }


    String getName() {
       return BaseInterceptor.class.getName();
    }
}
