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

package wftest;

import java.io.*;
import java.util.Enumeration;
import jakarta.annotation.Resource;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(name="wftestServlet2", urlPatterns={"/wftest2"})
public class WFTestServlet2 extends HttpServlet {
    @Resource(name="wfmin") private Integer min;
    @Resource(name="wfmid") private Integer mid;
    @Resource(name="wfmax") private Integer max;

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        String message = "min=" + min + ", mid=" + mid + ", max=" + max;;
        res.getWriter().println(message);
    }
}
