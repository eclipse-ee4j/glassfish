/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.net.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.MultipartConfig;

@MultipartConfig(maxFileSize=20480)
public class ServletTest extends HttpServlet {

    private ServletContext context;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        response.setContentType("text/html");

        // Do getParameter first, to test if it works if getParts not called.
        out.write("getParameter(\"xyz\"): " + request.getParameter("xyz"));
        out.write("\n\n");
        for (Part p: request.getParts()) {

            out.write("Part name: " + p.getName()+ "\n");
            out.write("Size: " + p.getSize() + "\n");
            out.write("Content Type: " + p.getContentType() + "\n");
            out.write("Header Names:");
            for (String name: p.getHeaderNames()) {
                out.write(" " + name);
            }
            out.write("\n");
        }
        out.flush();
    }

}



