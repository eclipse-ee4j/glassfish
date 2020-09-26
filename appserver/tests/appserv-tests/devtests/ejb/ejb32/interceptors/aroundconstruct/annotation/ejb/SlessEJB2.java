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

import jakarta.ejb.Stateless;
import jakarta.interceptor.*;
import jakarta.annotation.*;

@Stateless
public class SlessEJB2 extends BaseBean {

    @Resource private SomeManagedBean mb;
    @Resource private SomeManagedBean2 mb2;
    @Resource private SomeManagedBean3 mb3;

    public String sayHello() {
        try {
            verify("SlessEJB2");
            throw new RuntimeException("SlessEJB2 was intercepted");
        } catch (Exception e) {
            // ok
        }
        mb.foo();
        mb2.foo();
        mb3.foo();
        return "Hello";
    }

}
