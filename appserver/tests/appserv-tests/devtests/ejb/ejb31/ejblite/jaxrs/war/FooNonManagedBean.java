/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

import jakarta.transaction.UserTransaction;

public class FooNonManagedBean {

    @Resource
    private UserTransaction ut;

    @PostConstruct
    private void init() {
        if (ut == null) {
            throw new IllegalStateException("ut is null");
        }

        System.out.println("In FooNonManagedBean:init()");
    }

    public void hello() {
        System.out.println("In FooNonManagedBean::hello()");
    }

    @PreDestroy
    private void destroy() {
        System.out.println("In FooNonManagedBean:destroy()");
    }

}
