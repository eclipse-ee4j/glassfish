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
import jakarta.interceptor.*;
import jakarta.annotation.*;

@Stateless
public class SlessEJB extends BaseBean {

    @EJB SlessEJB2 s2;
    @EJB SlessEJB3 s3;

    public SlessEJB() {}

    public String sayHello() {
        verifyA_AC("SlessEJB");
        //verifyB_AC("SlessEJB");
        verifyA_PC("SlessEJB");
        return (s2.sayHello() + s3.sayHello());
    }

    @PostConstruct
    private void init() {
        System.out.println("**SlessEJB PostConstruct");
        verifyMethod("init");
    }

}
