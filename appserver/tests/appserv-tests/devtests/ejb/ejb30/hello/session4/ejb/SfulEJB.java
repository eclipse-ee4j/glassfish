/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb30.hello.session4;

import jakarta.ejb.Stateful;
import jakarta.ejb.SessionContext;
import jakarta.annotation.Resource;

@Stateful(mappedName="ejb_ejb30_hello_session4_Sful")
public class SfulEJB implements Sful, Sful2
{

    private String id_;

    @Resource private SessionContext ctx;

    public void setId(String id) {
        id_ = id;
    }

    public String getId() {
        return id_;
    }

    public Sful2 getSful2() {
        return (Sful2) ctx.getBusinessObject(Sful2.class);
    }

    public String hello() {
        System.out.println("In SfulEJB:hello()");
        return "hello";
    }

    public String hello2() {
        System.out.println("In SfulEJB:hello2()");
        return "hello2";
    }

    public void sameMethod() {
        System.out.println("In SfulEJB:sameMethod()");
    }


}
