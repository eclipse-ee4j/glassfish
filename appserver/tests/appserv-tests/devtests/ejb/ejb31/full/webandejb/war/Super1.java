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
import java.util.concurrent.TimeUnit;

@AccessTimeout(value=9876544, unit=TimeUnit.HOURS)
@Lock(LockType.READ)
public   class Super1 /*extends Super2*/ {

        public void super1() {}

        @Lock(LockType.WRITE)
        public void super11() {}

        @AccessTimeout(value=44544, unit=TimeUnit.HOURS)
        private void foobar() {}

        @AccessTimeout(value=44544, unit=TimeUnit.HOURS)
            public String hello() { return ""; }

    }
