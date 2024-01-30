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

package com.tests;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name="mytest",
        urlPatterns={"/myurl"},
        initParams={ @WebInitParam(name="n1", value="v1"), @WebInitParam(name="n2", value="v2") } )
public class TestServlet extends HttpServlet {
    @jakarta.annotation.Resource TestManagedBean testResource;

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0. ");
        String msg = "n1=" + getInitParameter("n1") +
            ", n2=" + getInitParameter("n2");

        msg += testManagedBean(testResource, " | TestManagedBean injected via @Resource");
        msg += testOverriddenMethods();

        writer.write("initParams: " + msg + "\n");
    }

    private String testOverriddenMethods() {
        String s = "";
        System.out.println("toString returned:" +testResource.toString());
        if (!testResource.toString().equals(TestManagedBean.TOSTRING))
            s += "TestManagedBean obtained via @Resource toString method invocation failed";
        return s;
    }

    private String testManagedBean(TestManagedBean tb, String info) {
        String msg = "";
        if (tb == null) msg += info + " is null!";
        if (tb != null && !tb.testPostConstructCalled()) msg += info + " postConstruct not called";
        return msg;
    }

}
