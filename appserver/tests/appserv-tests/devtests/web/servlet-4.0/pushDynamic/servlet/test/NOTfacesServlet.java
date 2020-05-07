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

package test;

import java.io.IOException;
import java.util.Arrays;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.HttpMethodConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.PushBuilder;

@WebServlet(urlPatterns="/NOTfaces/*")
public class NOTfacesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
            String pathInfo = req.getPathInfo();
            req.getServletContext().log("Servicing push request. pathInfo: "
                                            + pathInfo);
            if (pathInfo.contains("jsf.js")) {
                    if (pathInfo.contains("?")) {
                            req.getServletContext().log("pathInfo contains query string and should not!");
                            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            return;
                    }
            }
            if (pathInfo.contains("parameterMapFailure.js")) {
                    Object result;
                    if (null != (result = req.getParameter("ln"))) {
                            if (!"resources".equals(result)) {
                                    req.getServletContext().log("Parameter value incorrect.  Should be resources.  Is: " + result);
                                    res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                    return;
                            }
                    } else {
                            req.getServletContext().log("Should have ln parameter.  Does not.");
                            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            return;
                            
                    }
            }
            res.getWriter().println("foo: bar");
            res.setStatus(HttpServletResponse.SC_OK);
            return;
    }
}
