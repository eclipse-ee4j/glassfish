/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class DispatchTo extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        Enumeration<String> attrNames = req.getAttributeNames();
        if (attrNames == null) {
            throw new ServletException("Missing ASYNC dispatch related " +
                                       "request attributes");
        }

        int asyncRequestAttributeFound = 0;
        while (attrNames.hasMoreElements()){
            String attrName = attrNames.nextElement();
            if (AsyncContext.ASYNC_REQUEST_URI.equals(attrName)) {
                if (!"/fromContext/dispatchFrom".equals(
                        req.getAttribute(attrName))) {
                    throw new ServletException("Wrong value for " +
                        AsyncContext.ASYNC_REQUEST_URI +
                        " request attribute");
                }
                asyncRequestAttributeFound++;
            } else if (AsyncContext.ASYNC_CONTEXT_PATH.equals(attrName)) {
                if (!"/fromContext".equals(req.getAttribute(attrName))) {
                    throw new ServletException("Wrong value for " +
                        AsyncContext.ASYNC_CONTEXT_PATH +
                        " request attribute");
                }
                asyncRequestAttributeFound++;
            } else if (AsyncContext.ASYNC_PATH_INFO.equals(attrName)) {
                if (req.getAttribute(attrName) != null) {
                    throw new ServletException("Wrong value for " +
                        AsyncContext.ASYNC_PATH_INFO +
                        " request attribute");
                }
                asyncRequestAttributeFound++;
            } else if (AsyncContext.ASYNC_SERVLET_PATH.equals(attrName)) {
                if (!"/dispatchFrom".equals(
                        req.getAttribute(attrName))) {
                    throw new ServletException("Wrong value for " +
                        AsyncContext.ASYNC_SERVLET_PATH +
                        " request attribute");
                }
                asyncRequestAttributeFound++;
            } else if (AsyncContext.ASYNC_QUERY_STRING.equals(attrName)) {
                if (!"myname=myvalue".equals(req.getAttribute(attrName))) {
                    throw new ServletException("Wrong value for " +
                        AsyncContext.ASYNC_QUERY_STRING +
                        " request attribute");
                }
                asyncRequestAttributeFound++;
            }
        }

        if (asyncRequestAttributeFound != 5) {
            throw new ServletException("Wrong number of ASYNC dispatch " +
                                       "related request attributes");
        }

        res.getWriter().println("Hello world");
    }
}
