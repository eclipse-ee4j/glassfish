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
import jakarta.interceptor.ExcludeClassInterceptors;
import jakarta.interceptor.Interceptors;
import org.omg.CORBA.ORB;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;

@Interceptors(InterceptorC.class)
@ManagedBean("somemanagedbean3")
public class SomeManagedBean3 extends BaseBean {

    @ExcludeClassInterceptors
    @Interceptors(InterceptorA.class)
    public SomeManagedBean3() {}

    @PostConstruct
    private void init3() {
        System.out.println("In SomeManagedBean3::init3() " + this);
    }


    @Interceptors(InterceptorA.class)
    public void foo3() {
        System.out.println("In SomeManagedBean3::foo3() ");
        verifyA("SomeManagedBean3");
    }

    @PreDestroy
    private void destroy() {
        System.out.println("In SomeManagedBean3::destroy() ");
    }
}
