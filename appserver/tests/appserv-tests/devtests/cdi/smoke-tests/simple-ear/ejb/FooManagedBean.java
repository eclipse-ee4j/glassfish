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

import jakarta.annotation.*;

import jakarta.ejb.EJB;
import jakarta.annotation.Resource;
import org.omg.CORBA.ORB;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;

@ManagedBean("somemanagedbean")
public class FooManagedBean extends ManagedSuper implements Foo {

    @EJB HelloRemote s;
    @Resource ORB orb;
    @Resource BarManagedBean bmb;
    @PersistenceContext EntityManager em;

    @PostConstruct
    private void init() {
        System.out.println("In FooManagedBean::init() " + this);
    }


    public String getName() {
        return "somemanagedbean";
    }

    public void foo() {
        System.out.println("In FooManagedBean::foo() ");
        bmb.bar();
    }

    public Object getThis() {
        return this;
    }

    @PreDestroy
    private void destroy() {
        System.out.println("In FooManagedBean::destroy() ");
    }


    public String toString() {
        return "FooManagedBean this = " + super.toString() +
                           " s = " + s + " , orb = " + orb +
            " , bmb = " + bmb + " , em = " + em;
    }

}
