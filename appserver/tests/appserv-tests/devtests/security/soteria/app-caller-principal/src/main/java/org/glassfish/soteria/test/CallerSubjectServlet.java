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
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Optional;
import java.util.Set;

/**
 * The Servlet which validates if for the authenticated user, both
 * container and caller principals are present in the subject
 * representing the caller.
 */
@WebServlet("/callerSubjectServlet")
@ServletSecurity(@HttpConstraint(rolesAllowed = "foo"))
public class CallerSubjectServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private SecurityContext securityContext;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String containerCallerPrincipalName = null;
        String appPrincipalName = null;
        String callerPrincipalFromSecurityContextName = null;
        boolean isUserInRole = securityContext.isCallerInRole("foo");
        int callerPrincipalCount = 0;

        Principal containerCallerPrincipal = securityContext.getCallerPrincipal();
        containerCallerPrincipalName = containerCallerPrincipal.getName();

        Set<Principal> principals = securityContext.getPrincipalsByType(java.security.Principal.class);

        Optional<Principal> appCallerPrincipalOptional = principals.stream().filter((p) -> p.getClass().getName() == AppPrincipal.class.getName())
                .findAny();
        Principal appPrincipal = null;
        if (appCallerPrincipalOptional.isPresent()) {
            callerPrincipalCount++;
            appPrincipal = appCallerPrincipalOptional.get();
            appPrincipalName = appPrincipal.getName();
        }

        Optional<Principal> containerCallerPrincipalOptional = principals.stream().filter((p) -> p.getClass().getName() == CallerPrincipal
                .class.getName())
                .findAny();
        Principal callerPrincipalFromSecurityContext = null;
        if (containerCallerPrincipalOptional.isPresent()) {
            callerPrincipalCount++;
            callerPrincipalFromSecurityContext = containerCallerPrincipalOptional.get();
            callerPrincipalFromSecurityContextName = callerPrincipalFromSecurityContext.getName();
        }

        if (!containerCallerPrincipalName.isEmpty() && !appPrincipalName.isEmpty() && containerCallerPrincipalName.equals
                (appPrincipalName) && isUserInRole & callerPrincipalCount == 1) {
            response.getWriter().write(String.format("Container caller principal and application caller principal both are " +
                    "represented by same principal for user %s and is in role %s", containerCallerPrincipal.getName(), "foo"));
        } else {
            response.getWriter().write(String.format("Both %s and %s principal types are available wherein only principal of " +
                    "type %s was expected for user %s and is in role %s",AppPrincipal.class.getName(), CallerPrincipal.class
                            .getName(), AppPrincipal.class.getName(), containerCallerPrincipal.getName(),
                    "foo"));
        }
    }
}
