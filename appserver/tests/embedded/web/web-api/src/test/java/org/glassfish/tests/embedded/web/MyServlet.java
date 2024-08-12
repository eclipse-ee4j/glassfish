/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.embedded.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;

import java.io.IOException;
import java.io.PrintWriter;

public class MyServlet extends HttpServlet {

    public MyServlet() {
        System.out.println("Servlet MyServlet initialized");
    }

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {
        if (!"def".equals(req.getAttribute("abc"))) {
            throw new ServletException("Missing ServletRequest parameter");
        } else {
            PrintWriter pw = res.getWriter();
            try {
                pw.println("Hello World!");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
