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
        com.sun.s1asdev.ejb.ejb30.interceptors.session.OverridingMethodsInterceptor.class,
        com.sun.s1asdev.ejb.ejb30.interceptors.session.DistinctMethodsInterceptor.class
})

@Stateless
public class DummyEJB
        extends DummyLevel2EJB
        implements Dummy
{
    int interceptorId;
    private boolean createCalled = false;
    private int beanAICount = 0;
    private int dummyEJBPostConstructCount = 0;

    DistinctMethodsInterceptor distinctInterceptor = null;
    OverridingMethodsInterceptor overridingInterceptor = null;

    private static final String distinctAIName = "com.sun.s1asdev.ejb.ejb30.interceptors.session.DistinctMethodsInterceptor";
    private static final String overridingAIName = "com.sun.s1asdev.ejb.ejb30.interceptors.session.OverridingMethodsInterceptor";

    @PostConstruct
    protected void overridablePostConstruct()
    {
        postConstructList.add("DummyEJB");
        createCalled = true;
        dummyEJBPostConstructCount++;
    }

    public String dummy() {
            return "Dummy!!";
    }

    public void setInterceptorId(int val) {
        if( !createCalled ) {
            throw new EJBException("create was not called");
        }

        this.interceptorId = val;
    }

    public String isInterceptorCallCounOK() {

                boolean beanAIResult = (beanAICount == dummyBaseAICount)
                        && (beanAICount == dummyLevel2AICount)
                        && checkAroundInvokeSequence();

                return "" + distinctInterceptor.isAICountOK()
                        + " " + overridingInterceptor.isAICountOK()
                        + " " + beanAIResult
                        + " " +  checkAroundInvokeSequence();
        }


    public String isPostConstructCallCounOK() {

                boolean beanPCCountResult = (dummyEJBPostConstructCount == dummyBaseEJBPostConstructCount)
                        && (dummyLevel2EJBPostConstructCount == 0)
                        && checkPostConstructSequence();

                return "" + distinctInterceptor.isPostConstructCallCounOK()
                        + " " + overridingInterceptor.isPostConstructCallCounOK()
                        + " " + beanPCCountResult
                        + " " +  checkPostConstructSequence();
        }

    @AroundInvoke
    Object myOwnAroundInvoke(InvocationContext ctx)
        throws Exception {
                aroundInvokeList.add("DummyEJB");
                if (distinctInterceptor == null) {
                         distinctInterceptor = (DistinctMethodsInterceptor)
                                 ctx.getContextData().get(distinctAIName);
                         overridingInterceptor = (OverridingMethodsInterceptor)
                                 ctx.getContextData().get(overridingAIName);
                }

                beanAICount++;
        return ctx.proceed();
    }

    private boolean checkPostConstructSequence() {
        boolean result = postConstructList.size() == 2;
        if (result) {
            "DummyBaseEJB".equals(postConstructList.get(0));
            "DummyEJB".equals(postConstructList.get(1));
        }

        return result;
    }

    private boolean checkAroundInvokeSequence() {
        boolean result = aroundInvokeList.size() == 3;
        if (result) {
            "DummyBaseEJB".equals(aroundInvokeList.get(0));
            "DummyLevel2EJB".equals(aroundInvokeList.get(1));
            "DummyEJB".equals(aroundInvokeList.get(2));
        }

        return result;
    }

}

