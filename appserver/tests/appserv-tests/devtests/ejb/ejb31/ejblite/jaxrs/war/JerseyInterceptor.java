/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

package com.sun.jersey;

import jakarta.interceptor.InvocationContext;
import jakarta.annotation.PostConstruct;

import com.acme.SingletonBean;
import com.acme.StatelessBean;
import com.acme.FooManagedBean;

public class JerseyInterceptor {

    @PostConstruct
    private void init(InvocationContext context) throws Exception {

        Object beanInstance = context.getTarget();

        System.out.println("In JerseyInterceptor::init() : " + beanInstance);

        if (beanInstance instanceof SingletonBean) {
            ((SingletonBean) beanInstance).interceptorWasHere = true;
        } else if (beanInstance instanceof StatelessBean) {
            ((StatelessBean) beanInstance).interceptorWasHere = true;
        } else if (beanInstance instanceof FooManagedBean) {
            ((FooManagedBean) beanInstance).interceptorWasHere = true;
        }

        // ...

        // Invoke next interceptor in chain
        context.proceed();
    }

}
