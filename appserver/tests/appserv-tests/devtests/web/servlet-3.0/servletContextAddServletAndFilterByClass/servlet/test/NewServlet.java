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
import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.naming.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import javax.sql.DataSource;

@Resource(name="myDataSource4", type=DataSource.class)
@Resources({ @Resource(name="myDataSource5", type=DataSource.class),
             @Resource(name="jdbc/myDataSource6", type=DataSource.class) })

public class NewServlet extends HttpServlet {

    private @Resource DataSource ds1;
    private @Resource(name="myDataSource2") DataSource ds2;
    private DataSource ds3;

    @Resource(name="jdbc/myDataSource3")
    private void setDataSource(DataSource ds) {
        ds3 = ds;
    }

    private @Resource String welcomeMessage;

    public void init() throws ServletException {

        ServletContext sc = getServletContext();

        try {
            int loginTimeout = ds1.getLoginTimeout();
            sc.log("ds1-login-timeout=" + loginTimeout);
            loginTimeout = ds2.getLoginTimeout();
            sc.log(",ds2-login-timeout=" + loginTimeout);
            loginTimeout = ds3.getLoginTimeout();
            sc.log(",ds3-login-timeout=" + loginTimeout);

            InitialContext ic = new InitialContext();

            DataSource ds4 = (DataSource)
                ic.lookup("java:comp/env/myDataSource4");
            loginTimeout = ds4.getLoginTimeout();
            sc.log(",ds4-login-timeout=" + loginTimeout);

            DataSource ds5 = (DataSource)
                ic.lookup("java:comp/env/myDataSource5");
            loginTimeout = ds5.getLoginTimeout();
            sc.log(",ds5-login-timeout=" + loginTimeout);

            DataSource ds6 = (DataSource)
                ic.lookup("java:comp/env/jdbc/myDataSource6");
            loginTimeout = ds6.getLoginTimeout();
            sc.log(",ds6-login-timeout=" + loginTimeout);

            if (!"Hello World from env-entry!".equals(welcomeMessage)) {
                throw new Exception("welcomeMessage not injected!");
            }

            sc.setAttribute("success", new Object());

        } catch (Throwable t) {
            sc.log("Error during init", t);
            throw new ServletException(t);
        }
    }

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {
        if (!"servletInitParamValue".equals(
                getServletConfig().getInitParameter("servletInitParamName"))) {
            throw new ServletException("Missing servlet init param");
        }

        if (!"filterInitParamValue".equals(
                req.getAttribute("filterInitParamName"))) {
            throw new ServletException("Missing filter init param");
        }

        if (getServletContext().getAttribute("success") == null) {
            throw new ServletException("Missing ServletContext attribute");
        }

    }
}
