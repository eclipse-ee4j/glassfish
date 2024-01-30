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
import jakarta.interceptor.Interceptors;
import org.omg.CORBA.ORB;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;

@ManagedBean("someothermanagedbean")
public class SomeManagedBean2 extends BaseBean {

    @Resource ORB orb;

    @PostConstruct
    private void init() {
        System.out.println("In SomeManagedBean2::init() " + this);
    }


    public void foo() {
        System.out.println("In SomeManagedBean2::foo() ");
        if (orb == null) throw new RuntimeException("SomeManagedBean2: ORB is null");
    }

    @PreDestroy
    private void destroy() {
        System.out.println("In SomeManagedBean2::destroy() ");
    }
}
