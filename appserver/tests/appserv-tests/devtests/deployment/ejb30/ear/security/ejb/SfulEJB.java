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

package com.sun.s1asdev.deployment.ejb30.ear.security;

import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateful;

@RunAs(value="sunuser")
@DeclareRoles({"j2ee", "sunuser"})
@Stateful
public class SfulEJB implements Sful
{
    @EJB private Sless sless;
    @EJB private SlessLocal slessLocal;
    @Resource private SessionContext sc;

    public String hello() {
        System.out.println("In SfulEJB:hello()");

        try {
            slessLocal.goodMorning();
            throw new RuntimeException("Unexpected success from slessLocal.goodMorning()");
        } catch(Exception ex) {
            System.out.println("Expected failure from slessLocal.goodMorning()");
        }

        try {
            slessLocal.goodBye();
            throw new RuntimeException("Unexpected success from slessLocal.goodBye()");
        } catch(EJBException ex) {
            System.out.println("Expected failure from slessLocal.goodBye()");
        }

        System.out.println(slessLocal.hello());
        return sless.hello();
    }

    @RolesAllowed({"j2ee"})
    public String goodAfternoon() {
        if (!sc.isCallerInRole("j2ee") || sc.isCallerInRole("sunuser")) {
            throw new RuntimeException("not of role j2ee or of role sunuser");
        }
        return "Sful: good afternoon";
    }

    @DenyAll
    public String goodNight() {
        System.out.println("In SfulEJB:goodNight()");
        return "goodNight";
    }
}
