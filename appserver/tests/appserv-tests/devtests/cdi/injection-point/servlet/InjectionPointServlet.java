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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import test.artifacts.HttpParam;
import test.artifacts.HttpParams;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class InjectionPointServlet extends HttpServlet {

    @Inject
    Logger log;

    // Another workaround. The servletrequest is not available in HttpParams
    // unless the request comes in and so we use programmatic lookup of the
    // http parameters
    @Inject
    @HttpParam()
    Instance<String> username;
    @Inject
    @HttpParam()
    Instance<String> password;

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";

        System.out.println("Injected logger into servlet:" + log);
        boolean loggerAvailable = (log != null);
        if (!loggerAvailable)
            msg += " Logger unavailable:" + "injection into Servlet Failed ";

        System.out.println("Injected username:" + username.get()
                + " password:" + password.get());
        boolean httpParamInjectionSuccess = username.get().equals("scott")
                && password.get().equals("tiger");
        if (!httpParamInjectionSuccess)
            msg += " HTTP Parameter injection through InjectionPoint capabilities failed ";

        writer.write(msg + "\n");
    }

}
