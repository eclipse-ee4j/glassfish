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

package org.glassfish.jaccApi.programmaticauthentication.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.security.auth.Subject;
import jakarta.security.jacc.PolicyContext;
import jakarta.security.jacc.PolicyContextException;
import java.security.Principal;
import java.util.stream.Collectors;
import java.util.Set;

@WebServlet(urlPatterns = "/public/authenticate")
public class AuthenticateServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.getWriter().write("This is a public servlet \n");
        request.setAttribute("doLogin",true);
        boolean authenticateOutcome = request.authenticate(response);
        String webName;
        if (request.getUserPrincipal() != null) {
            webName = request.getUserPrincipal().getName();
        }
        //get Subject via jacc api
        try {
            Subject subject = (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
            if (subject != null) {
                response.getWriter().write(subject.toString());
                Set<Principal> principalsSet = subject.getPrincipals();
//                String princiaplsInSubject = "";
                String princiaplsInSubject = principalsSet.stream()
                                                          .map(e -> e.getName())
                                                          .collect(Collectors.joining(", "));
                response.getWriter().write("Principals: " + princiaplsInSubject);
//            response.getWriter().write("Principals in subject are :" + subject.getPrincipals().stream().map(Principal::getName()).collect(Collectors.join(",")));
            }
        }catch (PolicyContextException e){
            response.getWriter().write("ERROR while getting Subject");
            e.printStackTrace(response.getWriter());
        }

    }

}
