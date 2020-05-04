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

package org.glassfish.soteria.test;

import java.io.IOException;

import jakarta.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.CallerPrincipal;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Optional;
import java.util.Set;

/**
 * The Servlet which validates if for the authenticated user, both
 * container and caller principals are present in the subject
 * representing the caller.
 */
@WebServlet("/valildateAvailablePrincipalServlet")
@ServletSecurity(@HttpConstraint(rolesAllowed = "foo"))
public class ValidateAvailablePrincipalServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    @Inject
    private SecurityContext securityContext;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        boolean isUserInRole = securityContext.isCallerInRole("foo");
        boolean hasContainerCallerPrincipal = false;
        boolean hasApplicationCallerPrincipal = false;

        Principal containerCallerPrincipal = securityContext.getCallerPrincipal();

        if (containerCallerPrincipal instanceof javax.security.enterprise.CallerPrincipal) {
            hasContainerCallerPrincipal = true;
        }

        Set<Principal> principals = securityContext.getPrincipalsByType(java.security.Principal.class);

        Optional<Principal> principalOptional = principals.stream().filter((p) -> p.getClass().getName() == CallerPrincipal.class
                .getName())
                .findAny();
        if (principalOptional.isPresent()) {
            Principal applicationPrincipal = principalOptional.get();
            if(applicationPrincipal.equals(containerCallerPrincipal)) {
                response.getWriter().write("containerPrincipal:" + containerCallerPrincipal + "\n");
                response.getWriter().write("appPrincipal:" + applicationPrincipal + "\n");
                hasApplicationCallerPrincipal = true;
                response.getWriter().write("hasApplicationCallerPrincipal:" + hasApplicationCallerPrincipal + "\n");
            }
        }
        if (!hasApplicationCallerPrincipal && hasContainerCallerPrincipal && isUserInRole) {
            response.getWriter().write(String.format("Container caller principal and application caller principal must have " +
                            "been one and the same but are not for user %s in role " +
                            "%s",
                    containerCallerPrincipal.getName(), "foo"));
        } else {
            response.getWriter().write(String.format("Both container caller principal and application caller principals are one" +
                            " and the same for user %s in role %s",
                    containerCallerPrincipal.getName(), "foo"));
        }
    }
}
