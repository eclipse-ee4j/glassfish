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

package samples.ejb.serializabletest.servlet;

import com.sun.s1asdev.connector.serializabletest.ejb.SimpleSessionHome;
import com.sun.s1asdev.connector.serializabletest.ejb.SimpleSession;

import java.io.*;
import jakarta.servlet.*;
import javax.naming.*;
import jakarta.servlet.http.*;


public class SimpleServlet extends HttpServlet {


    InitialContext initContext = null;


    public void init() {
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * handles the HTTP POST operation *
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        try {
            String testId = "serializable connector test";

            response.setContentType("text/html");

            InitialContext ic = new InitialContext();
            SimpleSessionHome simpleSessionHome = (SimpleSessionHome) ic.lookup("java:comp/env/ejb/simpleSession");

            out.println("Running serializable connector test ");
            SimpleSession bean = simpleSessionHome.create();

            boolean passed = false;

            try {
                if (!bean.test1()) {
                    out.println(testId + " test1 :  " + " FAIL");
                    out.println("TEST:FAIL");
                } else {
                    out.println(testId + " test1 :  " + " PASS");
                    out.println("TEST:PASS");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch(Exception e){

        } finally {
            out.println("END_OF_TEST");
        }
    }
}
