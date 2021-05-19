/*
 * Copyright (c) 2005, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.security.wss.roles2.ejbws;

import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.SessionContext;
import jakarta.jws.WebService;

import com.sun.s1asdev.security.wss.roles2.ejb.SfulLocal;

@Stateless
@WebService(targetNamespace="http://ejbws.roles2.wss.security.s1asdev.sun.com", serviceName="WssRoles2EjbService")
@DeclareRoles({"javaee", "webuser", "ejbuser"})
@RunAs("ejbuser")
public class HelloEjb {
    @EJB private SfulLocal sful;
    @Resource private SessionContext sc;

    public String hello(String who) {
        if (!sc.isCallerInRole("javaee")) {
            throw new RuntimeException("not of role javaee");
        }
        if (sc.isCallerInRole("ejbuser")) {
            throw new RuntimeException("of role ejbuser");
        }
        return "Hello, " + who;
    }

    @RolesAllowed(value={"javaee"})
    public String rolesAllowed1(String who) {
        return "Hello, " + who;
    }

    @RolesAllowed(value={"webuser"})
    public String rolesAllowed2(String who) {
        return "Hello, " + who;
    }

    @DenyAll
    public String denyAll(String who) {
        return "Hello, " + who;
    }

    @PermitAll
    public String permitAll(String who) {
        return "Hello, " + who;
    }

    public String runAs1() {
        return sful.hello();
    }

    public String runAs2() {
        return sful.goodBye();
    }

    @RolesAllowed(value={"javaeegp"})
    public String runAsRunAs1() {
        return sful.slessHello();
    }

    public String runAsRunAs2() {
        return sful.slessGoodBye();
    }
}
