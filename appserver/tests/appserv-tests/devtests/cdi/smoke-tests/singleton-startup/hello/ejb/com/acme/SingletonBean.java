/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.EJBException;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

import org.omg.CORBA.ORB;

@Singleton
@Startup
public class SingletonBean {

    @Resource
    private ORB orb;

    @Inject
    Foo foo;
    @Inject
    TestBean tb;

    Bar b;

    @Inject
    public SingletonBean(Bar b){
        this.b = b;
    }

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");
        System.out.println("orb = " + orb);
        if (orb == null) {
            throw new EJBException("null ORB");
        }
    }

    public String hello() {
        System.out.println("In SingletonBean::hello()");
        return "hello, world!\n";
    }

    public void testError() {
        throw new Error("test java.lang.Error");
    }

    public String testInjection() {
        if (foo == null)
            return "foo is null";
        if (tb == null)
            return "tb is null";
        if (!foo.testInjection())
            return "testInjection in Foo failed";
        if (b == null)
            return "Constructor Injection of bean failed";
        return "";
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }

}
