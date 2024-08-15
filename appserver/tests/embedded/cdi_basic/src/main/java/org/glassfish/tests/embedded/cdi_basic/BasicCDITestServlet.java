/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.embedded.cdi_basic;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author bhavanishankar@dev.java.net
 */
@WebServlet(name = "BasicCDITestServlet",
        urlPatterns = "/BasicCDITestServlet")
public class BasicCDITestServlet extends HttpServlet {

    @Inject
    TestBean tb;

    @Inject
    TestRequestScopedBean trsb;

    @Override
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException, IOException {
        PrintWriter out = httpServletResponse.getWriter();
        out.println("Hi from BasicCDITestServlet");
    if(tb == null) {
      out.println("TestBean not injected.");
    } else if(trsb == null) {
      out.println("TestRequestScopeBean not injected.");
    } else {
      out.println("TestBean injected. [" + tb + "]");
      out.println("TestRequestScopeBean injected. [ " + trsb + "]");
      out.println("All CDI beans have been injected.");
    }
        out.flush();
        out.close();
    }
}

