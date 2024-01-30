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

package com.tests;

//Simple TestBean to test CDI.
//This bean implements Serializable as it needs to be placed into a Stateful Bean
@jakarta.annotation.ManagedBean
public class TestManagedBean {
    boolean postConstructCalled = false;

    public static final String TOSTRING = "TestManagedBean";

    // A Managed Bean needs to have a no-arg constructor
    public TestManagedBean() {
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        System.out.println("In ManagedBean:: PostConstruct");
        postConstructCalled = true;
    }

    @Tester
    public void foo() {
        System.out.println("foo called");
    }

    public boolean testPostConstructCalled() {
        return this.postConstructCalled;
    }

    @Override
    public String toString() {
        return TestManagedBean.TOSTRING;
    }

}
