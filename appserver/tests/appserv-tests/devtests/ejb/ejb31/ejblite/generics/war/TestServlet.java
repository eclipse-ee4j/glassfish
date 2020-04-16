/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1as.devtests.ejb.generics;

import java.io.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import javax.annotation.*;
import javax.ejb.*;

@jakarta.servlet.annotation.WebServlet(urlPatterns = "/TestServlet")
public class TestServlet extends HttpServlet {
    @EJB
    private TestBean testBean;

    @EJB
    private TypeVariableBean<Integer> typeVariableBean;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        testBean.doSomething(null);
        testBean.doSomething2(null);
        testBean.doSomething3();
        testBean.doSomething4(new Object());
        testBean.doSomething4("");
        testBean.doSomething4(Integer.valueOf(1));
        testBean.doSomething5(null);
        testBean.doSomething6(null);
        out.println(testBean.hello());
        out.println("Successfully called methods on " + testBean); 

        out.println(typeVariableBean.hello("some text"));
        out.println(typeVariableBean.hello(10));
        out.println("Injected TypeVariableBean: " + typeVariableBean.toString());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
}
        
