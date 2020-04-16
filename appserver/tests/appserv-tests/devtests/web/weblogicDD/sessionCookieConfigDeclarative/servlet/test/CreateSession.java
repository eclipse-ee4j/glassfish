/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class CreateSession extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.getSession(true);

        String sessionCookie = res.getHeader("Set-Cookie");
        if (sessionCookie == null) {
            throw new ServletException("Missing Set-Cookie response header");
        }

        // name
        if (sessionCookie.indexOf("MYJSESSIONID=") == -1) {
            throw new ServletException("Missing session id");
        }

        // comment
        if (sessionCookie.indexOf("Comment=myComment") == -1) {
            throw new ServletException("Missing cookie comment");
        }      

        // domain
        if (sessionCookie.indexOf("Domain=mydomain") == -1) {
            throw new ServletException("Missing cookie domain");
        }      

        // path
        if (sessionCookie.indexOf("Path=/myPath") == -1) {
            throw new ServletException("Missing cookie path");
        }      

        // secure
        if (sessionCookie.indexOf("Secure") == -1) {
            throw new ServletException("Missing Secure attribute");
        }      

        // http-only
        if (sessionCookie.indexOf("HttpOnly") == -1) {
            throw new ServletException("Missing HttpOnly attribute");
        }      

        // max-age
        if (sessionCookie.indexOf("Max-Age=123") == -1) {
            throw new ServletException("Missing max-age");
        }      

    }
}
