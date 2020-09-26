/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package test.beans;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;

import jakarta.validation.constraints.NotNull;

import jakarta.inject.Inject;


@RequestScoped
@CDITest
public class TestBean {

    private Bar myBar;

    @Inject
    private Foo myFoo;

    private String ctorLog;

    public TestBean() {
        ctorLog = "no-args ctor";
    }


    @Inject
    public TestBean(@NotNull Bar bar) {
        ctorLog = "annotated ctor";
        this.myBar = bar;
    }

    @PostConstruct
    private void log() {
        System.out.println(toString() + ": ctor=" + getConstructorLog() + " ; myFoo=" + myFoo.toString() + " ; myBar=" + myBar.toString());
    }

    public String echo(@NotNull String message) {
        System.out.println("TestBean::echo called with String argument: " + message);
        return message + " : " + myFoo.value() + " : " + myBar.toString();
    }


    public void hello(String name) {
        System.out.println("TestBean::hello called with String argument: " + name);
    }


    public String getConstructorLog() {
        return ctorLog;
    }

}
