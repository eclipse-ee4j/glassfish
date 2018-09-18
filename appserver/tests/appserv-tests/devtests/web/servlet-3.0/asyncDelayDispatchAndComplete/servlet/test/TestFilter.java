/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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
import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

@WebFilter(urlPatterns={"/*"}, asyncSupported=true)
public class TestFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println(">>> filter.init");
    }   

    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {

        boolean notResult = !"1".equals(req.getParameter("result"));
        if (notResult) {
            TestServlet2.addInfo("Fi");
        }
        chain.doFilter(req, res);
        if (notResult) {
            TestServlet2.addInfo("Fo");
        }
    }

    public void destroy() {
        System.out.println(">>> filter.destroy");
    }
}
