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

package com.sun.s1asdev.cdi.hello.mdb;

import jakarta.ejb.Stateful;
import jakarta.ejb.Remove;
import jakarta.annotation.PreDestroy;

// Hello1 interface is not annotated with @Local. If the
// bean only implements one interface it is assumed to be
// a local business interface.
@Stateful(description="my stateful bean description")
public class HelloStateful implements Hello2 {

    private String msg;

    public void hello(String s) {
        msg = s;
        System.out.println("HelloStateful: " + s);
    }

    @Remove public void removeMethod() {
        System.out.println("Business method marked with @Remove called in " +
                           msg);
    }
    @PreDestroy public void myPreDestroyMethod() {
        System.out.println("PRE-DESTROY callback received in " + msg);
    }

}
