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

package com.sun.s1asdev.security.multiRoleMapping.web;

import java.io.*;
import java.net.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class WebTest extends HttpServlet {

    protected void doGet(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // check roles 1 through 7
        boolean found = false;
        for (int i=0; i<8; i++) {
            if (request.isUserInRole("role" + i)) {
                found = true;
                out.println("Hello role" + i);
            }
        }
        if (!found) {
            out.println("User '" + request.getRemoteUser() +
                "' is not in expected role. Something's messed up.");
        }
        out.close();
    }

}
