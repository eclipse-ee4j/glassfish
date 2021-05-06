/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jacc.test.mr8;

import java.security.Principal;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;

import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateful;

@DeclareRoles({"javaUsers"})

@Stateful
public class HelloStatefulEJB implements HelloStateful {
        @Resource
        private SessionContext ctx;

        @PostConstruct
        public void postConstruction() {
                System.out.println("In HelloStatefulEJB::postConstruction()");
        }

        public String hello(String name) {
                System.out.println("In HelloStatefulEJB::hello('"+name+"')");
                String principalName = "NONE";
        String principalType = "UNKNOWN";
                Principal p = ctx.getCallerPrincipal();
                if (p != null) {
                principalName = p.getName();
                principalType = p.getClass().getName();
                }
                String result = principalName + " is " + principalType;
                System.out.println("Caller Principal: " + result);
                return result;
        }

        public boolean inRole(String roleName) {
                System.out.println("In HelloStatefulEJB::inRole('"+roleName+"')");
                //try {
                        boolean result = ctx.isCallerInRole(roleName);
                        System.out.println("In HelloStatefulEJB::inRole('"+roleName+"') - " + result);
                        return result;
                //}
                //catch (Exception exc) {
                //        System.out.println("In HelloStatefulEJB - Exception: " + exc.toString());
                //        exc.printStackTrace();
                //        return false;
                //}
        }

        @RolesAllowed({"javaUsers"})
        public void methodAuthUser() {
            System.out.println("In HelloStatefulEJB::methodAuthUser()");
    }

        @RolesAllowed({"**"})
        public void methodAnyAuthUser() {
            System.out.println("In HelloStatefulEJB::methodAnyAuthUser()");
    }
}
