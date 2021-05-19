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
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class ServletTest extends HttpServlet implements HttpSessionListener {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException {
        request.getSession().setAttribute("response", response);
        request.getSession().invalidate();
    }

    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        // Do nothing
    }

    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        // Make sure IllegalStateException is not thrown
        try {
            HttpServletResponse response = (HttpServletResponse)
                httpSessionEvent.getSession().getAttribute("response");
            response.getWriter().println("SUCCESS");
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}



