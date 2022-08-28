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

public class DistinctMethodsInterceptor extends BaseLevel2Interceptor {

    protected static final String DISTINCT_INTERCEPTOR_NAME = "DistinctInterceptor";

    protected int distinctAICount = 0;
    protected int distinctPCCount = 0;

    @PostConstruct
    private void distinctPostConstruct(InvocationContext ctx) throws RuntimeException {
        postConstructList.add(DISTINCT_INTERCEPTOR_NAME);
        distinctPCCount++;
        try {
            ctx.proceed();
        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AroundInvoke
    private Object distinctMethodInterceptor(InvocationContext ctx)
            throws Throwable {
        aroundInvokeList.add(DISTINCT_INTERCEPTOR_NAME);
        distinctAICount++;
        return ctx.proceed();
    }

    protected boolean isAICountOK() {
        System.out.println("isAICountOK\n  distinctAICount=baseAICount?[" + distinctAICount + "=" + baseAICount + "],"
            + " distinctAICount=baseLevel2AICount?[" + distinctAICount + "=" + baseLevel2AICount + "],"
            + " aroundInvokeList=base,level2,distinct?[" + aroundInvokeList + "]");
        return (distinctAICount == baseAICount)
                && (distinctAICount == baseLevel2AICount)
                        && checkForCorrectSequence(aroundInvokeList);
        }

    protected boolean isPostConstructCallCounOK() {
        System.out.println("isPostConstructCallCounOK\n  distinctPCCount=basePCCount?[" + distinctPCCount + "=" + basePCCount + "],"
            + " distinctPCCount=baseLevel2PCCount?[" + distinctPCCount + "=" + baseLevel2PCCount + "],"
            + " postConstructList=base,level2,distinct?[" + postConstructList + "]");
        return (distinctPCCount == basePCCount)
                && (distinctPCCount == baseLevel2PCCount)
                        && checkForCorrectSequence(postConstructList);
    }

    private boolean checkForCorrectSequence(List<String> list) {
        boolean result = list.size() == 3;
        if (result) {
            return BASE_INTERCEPTOR_NAME.equals(list.get(0)) && LEVEL2_INTERCEPTOR_NAME.equals(list.get(1))
                && DISTINCT_INTERCEPTOR_NAME.equals(list.get(2));
        }
        return false;
    }

    String getName() {
       return DistinctMethodsInterceptor.class.getName();
    }
}
