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

import jakarta.ejb.*;
import jakarta.annotation.*;
import jakarta.interceptor.ExcludeClassInterceptors;
import jakarta.interceptor.ExcludeDefaultInterceptors;
import jakarta.interceptor.Interceptors;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.event.Observes;

@Stateless
@test.beans.interceptors.Another
@Interceptors(InterceptorA.class)
@LocalBean
public class StatelessBean implements StatelessLocal {

    private List<Integer> interceptorIds = new ArrayList<Integer>();

    @Inject Foo foo;

    @PostConstruct
        public void init() {
        System.out.println("In StatelessBean::init()");
    }

    public void processSomeEvent(@Observes SomeEvent event) {
        System.out.println("In StatelessBean::processSomeEvent " +
                           event);
    }

    public void hello() {
        System.out.println("In StatelessBean::hello() " +
                           foo);
        if (interceptorIds.size() != 2) {
            throw new IllegalStateException("Wrong number of interceptors were called: expected 2, got " + interceptorIds.size());
        } else if (interceptorIds.get(0) != 0 || interceptorIds.get(1) != 1) {
            throw new IllegalStateException("Interceptors were called in a wrong order");
        }

        interceptorIds.clear();
    }

    @ExcludeClassInterceptors
    @ExcludeDefaultInterceptors
    public void interceptorCalled(int id) {
        System.out.println("In StatelessBean::interceptorCalled() " + id);
        interceptorIds.add(id);
    }

    @PreDestroy
        public void destroy() {
        System.out.println("In StatelessBean::destroy()");
    }



}
