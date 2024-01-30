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

import jakarta.annotation.*;

import jakarta.ejb.EJB;
import jakarta.annotation.Resource;
import jakarta.transaction.UserTransaction;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;

import jakarta.interceptor.InvocationContext;
import jakarta.interceptor.AroundInvoke;


public class InterceptorB {


    @PostConstruct
    private void init(InvocationContext c) throws Exception {
        System.out.println("In InterceptorB::init() ");
        ((ManagedBeanSuper)c.getTarget()).newInterceptorInstance();
        c.proceed();
    }


    @AroundInvoke
    private Object roundInvoke(InvocationContext c) throws Exception {
        System.out.println("In InterceptorB::aroundInvoke() ");
        if( c.getMethod().getName().equals("getAroundInvokeSequence") ) {
            String result = (String) c.proceed();
            return "B" + result;
        } else {
            return c.proceed();
        }
    }

    @PreDestroy
    private void destroy(InvocationContext c) throws Exception {
        System.out.println("In InterceptorB::destroy() ");
        c.proceed();
    }

}
