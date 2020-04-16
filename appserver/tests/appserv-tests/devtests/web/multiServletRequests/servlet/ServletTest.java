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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class ServletTest extends HttpServlet {

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String host = request.getParameter("host");
        String port = request.getParameter("port");
        String contextRoot = request.getParameter("contextRoot");

        URL url = new URL("http://" + host  + ":" + port + "/web-multiServletRequests/ServletTest2?host="+ host + "&port=" + port + "&contextRoot=" + contextRoot);
        System.out.println("\n Servlet1 Invoking url: " + url.toString());
        URLConnection conn = url.openConnection();
        if (conn instanceof HttpURLConnection) {
            HttpURLConnection urlConnection = (HttpURLConnection)conn;
            urlConnection.setDoOutput(true);

            DataOutputStream dout = 
               new DataOutputStream(urlConnection.getOutputStream());
                                    dout.writeByte(1);

            int responseCode=  urlConnection.getResponseCode();
            System.out.println("responseCode: " + responseCode);
            if (responseCode == 200){
               out.println("multiServletRequests::PASS");
            } else {
               out.println("multiServletRequests::FAIL");
            }
        }
    }
}



