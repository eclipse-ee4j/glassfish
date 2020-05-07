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
import java.util.Enumeration;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/output")
public class TestServlet extends HttpServlet {
    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        try {
            Thread.currentThread().sleep(1000);
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
        PrintWriter writer = res.getWriter();
        String attrA = (String)req.getAttribute("A");
        String attrB = (String)req.getAttribute("C");
        StringBuilder sb = new StringBuilder("Hello world: ");
        if (attrA != null) {
            sb.append(attrA);
        }
        if (attrB != null) {
            sb.append(", ");
            sb.append(attrB);
        }
        String output = sb.toString(); 
        writer.write(output);
        System.out.println(output);
    }
}
