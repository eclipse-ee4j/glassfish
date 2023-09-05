package deletion;

/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

@WebServlet(urlPatterns={"/hello"})
public class HelloWorld extends HttpServlet {


    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter pw = res.getWriter();
        try {
            pw.println("Hello World !<br>");
          } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

