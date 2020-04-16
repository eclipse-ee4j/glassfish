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

package com.sun.security.devtests.weblogicdd;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;

public class TestServlet extends HttpServlet {

    private static final String[] roles = {"weblogic-xml", "weblogic-app-xml"};

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        out.println("<br>Basic Authentication tests from Servlet: Test1,Test2 ");
        out.println("<br>Authorization test from Servlet: Test3 -> HttpServletRequest.isUserInRole() authorization from Servlet.");

        test1(request, response, out);
        test2(request, response, out);
        test3(request, response, out);
    }

    //Tests begin
    public void test1(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        //Check the auth type - request.getAuthType()
        out.println("<br><br>Test1. Postive check for the correct authentication type");
        String authtype = request.getAuthType();
        if ("BASIC".equalsIgnoreCase(authtype)) {
            out.println("<br>request.getAuthType() test Passed.");
        } else {
            out.println("<br>request.getAuthType() test Failed!");
        }
        out.println("<br>Info:request.getAuthType() is= " + authtype);
    }
    //Test2

    public void test2(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        Principal ruser = request.getUserPrincipal();
        out.println("<br><br>Test2. Positive check for the correct principal name");
        if (ruser != null) {
            out.println("<br>request.getUserPrincipal() test Passed.");
        } else {
            out.println("<br>request.getUserPrincipal() test Failed!");
        }
        out.println("<br>Info:request.getUserPrincipal() is= " + ruser);

    }
    //Test3 - positive test for checking the user's proper role

    public void test3(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        out.println("<br><br>Test3. Positive check whether the user is in proper role");
        boolean isInProperRole = false;
        for (int i = 0; i < 2; i++) {
            if (request.isUserInRole(roles[i])) {
                isInProperRole = true;
                out.println("<br>Hello " + roles[i] + "!!!");
            }
        }
        if (isInProperRole) {
            out.println("<br>HttpServletRequest.isUserInRole() test Passed.");
        } else {
            out.println("<br>HttpServletRequest.isUserInRole() test Failed!");
        }
    }
}
