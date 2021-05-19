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
import jakarta.interceptor.*;
import java.lang.reflect.*;


public class BaseBean {

    // InterceptorA
    boolean ac = false;
    boolean pc = false;
    boolean ai = false;

    // InterceptorB
    boolean ac1 = false;
    boolean pc1 = false;
    boolean ai1 = false;

    // InterceptorC
    boolean ac2 = false;
    boolean pc2 = false;
    boolean ai2 = false;

    Method method = null;

    void verifyMethod(String name) {
        if (method == null) {
            if (name != null) throw new RuntimeException("In " + getClass().getName() + " expected method name: " + name + " got null");
        } else {
            if (!method.getName().equals(name))
                throw new RuntimeException("In " + getClass().getName() + " expected method name: " + name + " got: " + method.getName());
        }
    }

    void verify(String name) {
        if (!ac) throw new RuntimeException("[" + name + "] InterceptorA.AroundConstruct was not called");
        if (!ac1) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was not called");
        if (ac2) throw new RuntimeException("[" + name + "] InterceptorC.AroundConstruct was called");

        if (!pc) throw new RuntimeException("[" + name + "] InterceptorA.PostConstruct was not called");
        if (!pc1) throw new RuntimeException("[" + name + "] InterceptorB.PostConstruct was not called");
        if (pc2) throw new RuntimeException("[" + name + "] InterceptorC.PostConstruct was called");
    }

    void verifyA(String name) {
        if (!ac) throw new RuntimeException("[" + name + "] InterceptorA.AroundConstruct was not called");
        if (ac1) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was called");
        if (ac2) throw new RuntimeException("[" + name + "] InterceptorC.AroundConstruct was called");

        if (!pc) throw new RuntimeException("[" + name + "] InterceptorA.PostConstruct was not called");
        if (pc1) throw new RuntimeException("[" + name + "] InterceptorB.PostConstruct was called");
        if (pc2) throw new RuntimeException("[" + name + "] InterceptorC.PostConstruct was called");
    }

    void verifyA_AC(String name) {
        if (!ac) throw new RuntimeException("[" + name + "] InterceptorA.AroundConstruct was not called");
        if (ac1) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was called");
        if (ac2) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was called");
    }

    void verifyB_AC(String name) {
        if (ac) throw new RuntimeException("[" + name + "] InterceptorA.AroundConstruct was called");
        if (!ac1) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was not called");
        if (ac2) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was called");
    }

    void verifyAB_AC(String name) {
        if (!ac) throw new RuntimeException("[" + name + "] InterceptorA.AroundConstruct was not called");
        if (!ac1) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was not called");
        if (ac2) throw new RuntimeException("[" + name + "] InterceptorC.AroundConstruct was called");
    }

    void verifyAC_AC(String name) {
        if (!ac) throw new RuntimeException("[" + name + "] InterceptorA.AroundConstruct was not called");
        if (ac1) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was called");
        if (!ac2) throw new RuntimeException("[" + name + "] InterceptorC.AroundConstruct was not called");
    }

    void verifyAB_PC(String name) {
        if (!pc) throw new RuntimeException("[" + name + "] InterceptorA.PostConstruct was not called");
        if (!pc1) throw new RuntimeException("[" + name + "] InterceptorB.PostConstruct was not called");
        if (pc2) throw new RuntimeException("[" + name + "] InterceptorC.PostConstruct was called");
    }

    void verifyAC_PC(String name) {
        if (!pc) throw new RuntimeException("[" + name + "] InterceptorA.PostConstruct was not called");
        if (pc1) throw new RuntimeException("[" + name + "] InterceptorB.PostConstruct was called");
        if (!pc2) throw new RuntimeException("[" + name + "] InterceptorC.PostConstruct was not called");
    }

    void verifyA_PC(String name) {
        if (!pc) throw new RuntimeException("[" + name + "] InterceptorA.PostConstruct was not called");
        if (pc1) throw new RuntimeException("[" + name + "] InterceptorB.PostConstruct was called");
        if (pc2) throw new RuntimeException("[" + name + "] InterceptorC.PostConstruct was called");
    }

    void verifyA_AI(String name) {
        if (!ai) throw new RuntimeException("[" + name + "] InterceptorA.AroundInvoke was not called");
        if (ai1) throw new RuntimeException("[" + name + "] InterceptorB.AroundInvoke was called");
        if (ai2) throw new RuntimeException("[" + name + "] InterceptorC.AroundInvoke was called");
    }

    void verifyB_PC(String name) {
        if (pc) throw new RuntimeException("[" + name + "] InterceptorA.PostConstruct was called");
        if (!pc1) throw new RuntimeException("[" + name + "] InterceptorB.PostConstruct was not called");
        if (pc2) throw new RuntimeException("[" + name + "] InterceptorC.PostConstruct was called");
    }
}
