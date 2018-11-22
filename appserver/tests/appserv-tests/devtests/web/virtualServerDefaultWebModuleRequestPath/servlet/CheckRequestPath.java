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

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class CheckRequestPath extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        int port = req.getLocalPort();
        String servletPath = req.getServletPath();
        String host = req.getLocalName();

        String expectedCtxtRoot = null;
        String expectedRequestURL = null;
        String expectedRequestURI = null;
        
        String run = req.getParameter("run");
        if ("first".equals(run)) {
            expectedCtxtRoot = "";
            expectedRequestURI = servletPath;
            expectedRequestURL = "http://" + host + ":" + port +
                    expectedRequestURI;
        } else if ("second".equals(run)) {
            expectedCtxtRoot =
                "/web-virtual-server-default-web-module-request-path";
            expectedRequestURI = expectedCtxtRoot + servletPath;
            expectedRequestURL = "http://" + host + ":" + port + 
                    expectedRequestURI;
        } else {
            throw new ServletException();
        }

        System.out.println("CheckRequestPath: "
            + String.format("host=%s, port=%d, expectedRequestURI=%s, expectedRequestURL=%s, expectedCtxtRoot=%s, req.getContextPath=%s, req.getRequestURL=%s, req.getRequestURI=%s",
                host, port, expectedRequestURI, expectedRequestURL, expectedCtxtRoot, req.getContextPath(), req.getRequestURL().toString(), req.getRequestURI()));

        if (!expectedCtxtRoot.equals(req.getContextPath()) ||
                !expectedRequestURL.equals(req.getRequestURL().toString()) ||
                !expectedRequestURI.equals(req.getRequestURI())) {
            throw new ServletException();
        }
    }
}



