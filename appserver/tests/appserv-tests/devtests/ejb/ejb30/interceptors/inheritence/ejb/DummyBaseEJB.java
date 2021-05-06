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

import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.InvocationContext;
import jakarta.interceptor.AroundInvoke;
import jakarta.ejb.EJBException;
import jakarta.annotation.PostConstruct;

public abstract class DummyBaseEJB
{
    protected List<String> postConstructList = new ArrayList<String>();
    protected List<String> aroundInvokeList = new ArrayList<String>();

    protected int dummyBaseAICount = 0;
    protected int dummyBaseEJBPostConstructCount = 0;

        @AroundInvoke
        private Object dummyBaseAroundInvoke(InvocationContext ctx)
                throws Exception {
                        aroundInvokeList = new ArrayList<String>();
                        aroundInvokeList.add("DummyBaseEJB");
                        dummyBaseAICount++;
                        return ctx.proceed();
        }


        @PostConstruct
    private void dummyBasePostConstruct() {
                postConstructList = new ArrayList<String>();
                postConstructList.add("DummyBaseEJB");
                dummyBaseEJBPostConstructCount++;
                System.out.println("GGGG: DummyLevel2EJB.postConstruct ");
        }

}
