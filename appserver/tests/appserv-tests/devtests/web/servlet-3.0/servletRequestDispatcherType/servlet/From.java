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
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class From extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        if (req.getDispatcherType() != DispatcherType.REQUEST) {
            throw new ServletException("Wrong dispatcher type: " +
                                       req.getDispatcherType() +
                                       ", should be REQUEST");
        }

        MyRequestWrapper wreq = new MyRequestWrapper(req);

        String mode = req.getParameter("mode");
        if ("forward".equals(mode)) {
            getServletContext().getRequestDispatcher("/ForwardTarget").forward(wreq, res);
        } else if ("include".equals(mode)) {
            getServletContext().getRequestDispatcher("/IncludeTarget").include(wreq, res);
        } else if ("error".equals(mode)) {
            res.sendError(555);
        } else {
            throw new ServletException("Invalid dispatching mode");
        }
    }

    static class MyRequestWrapper extends HttpServletRequestWrapper {
        public MyRequestWrapper(HttpServletRequest req) {
            super(req);
        }
    }
}
