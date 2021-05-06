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

package webapp1;

import java.io.IOException;
import java.util.Enumeration;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/mytest1")
public class Test1Servlet extends HttpServlet {
    ServletContext context;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        context = config.getServletContext();
    }

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        StringBuffer ret = new StringBuffer();
        String attr = (String) context.getAttribute("SHAREDLIB-1");
        ret.append(attr +";");
        attr = (String) context.getAttribute("SHAREDLIB-2");
        ret.append(attr +";");
        attr = (String) context.getAttribute("SHAREDLIB-3");
        ret.append(attr +";");
        attr = (String) context.getAttribute("SHAREDLIB-4");
        ret.append(attr +";");
        attr = (String) context.getAttribute("APPLIB-1");
        ret.append(attr +";");
        attr = (String) context.getAttribute("APPLIB-2");
        ret.append(attr);
        res.getWriter().write(ret.toString());
    }
}
