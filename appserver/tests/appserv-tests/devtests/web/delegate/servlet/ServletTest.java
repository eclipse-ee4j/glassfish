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
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class ServletTest extends HttpServlet{

    private String status = "DelegateTest::FAIL";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("[Servlet.init]");

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("[Servlet.doGet]");
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("[Servlet.doPost]");

        response.setContentType("text/html");

        DelegateTest delegate = null;
        try{
            Class clazz = Class.forName("test.DelegateTest");
            delegate = (DelegateTest)clazz.newInstance();
            clazz.newInstance();
        } catch (Exception ex){
            status = "DelegateTest::FAIL";
            ex.printStackTrace();
        }

        if (delegate != null){
            try{
                System.out.println("Delegate: " + delegate.getChildName());
                status = "DelegateTest::PASS";
            } catch (Exception ex){
                status = "DelegateTest::FAIL";
                ex.printStackTrace();
            }
        }
        PrintWriter out = response.getWriter();
        out.println(status);

        try{
            Class clazz = Class.forName("javax.sql.rowset.BaseRowSet");

            if (clazz == null){
                status = "OverridableJavax::FAIL";
            } else {
                status = "OverridableJavax::PASS";
            }

        } catch (Exception ex){
            status = "OverridableJavax::FAIL";
            ex.printStackTrace();
        }
        out.println(status);

    }

}



