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

package com.sun.s1asdev.ejb.ejb30.hello.session3;

import jakarta.ejb.Stateful;
import jakarta.ejb.AccessTimeout;
import jakarta.annotation.PostConstruct;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.EntityManager;

@AccessTimeout(0)
@Stateful public class HelloStatefulEJB implements HelloStateful {


    @PersistenceContext(name="myem",
                        unitName="foo", type=PersistenceContextType.EXTENDED)
        private EntityManager em;

    @PostConstruct public void postConstruction() {
        System.out.println("In HelloStatefulEJB::postConstruction()");
    }

    public void hello() {
        System.out.println("In HelloStatefulEJB::hello()");
    }

    public void sleepFor(int sec) {
        System.out.println("In HelloStatefulEJB::sleepFor()");
        try {
            for (int i=0 ; i<sec; i++) {
                Thread.currentThread().sleep(1000);
            }
        } catch (Exception ex) {
        }
        System.out.println("Finished HelloStatefulEJB::sleepFor()");
    }

    public void ping() {
        System.out.println("In HelloStatefulEJB::ping()");
    }

}
