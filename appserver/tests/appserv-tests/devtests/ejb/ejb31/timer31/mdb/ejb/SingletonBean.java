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

import java.util.Set;
import java.util.HashSet;

import jakarta.ejb.*;
import jakarta.annotation.*;
import org.omg.CORBA.ORB;

@Singleton
@Remote(SingletonRemote.class)
@LocalBean
public class SingletonBean {

    boolean passed1 = false;
    boolean passed2 = false;
    Set<String> around_timeouts = new HashSet<String>();

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");
    }

    public void test1Passed() {
        passed1 = true;
    }

    public void test2Passed() {
        passed2 = true;
    }

    public boolean getTestPassed() {
        return passed1 && passed2;
    }

    public boolean getAroundTimeoutCalled(String s) {
        if (s == null) {
            s = "no-arg";
        }
        return around_timeouts.contains(s);
    }

    public void setAroundTimeoutCalled(String s) {
        if (s == null) {
            s = "no-arg";
        }
        around_timeouts.add(s);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
