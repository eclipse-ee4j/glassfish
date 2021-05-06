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

import java.io.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class TestServlet extends HttpServlet {

    private static final int BUFFER_SIZE = 16*1024;

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {

        PrintWriter out = res.getWriter();

        /*
         * Check to see if we are in SE/EE, by trying to load a class that's
         * available only in SE/EE.
         * If running on SE/EE, return a message that does fit in the
         * response buffer, as to guarantee a Content-Length response header.
         */
        Class cl = null;
        try {
            Class.forName(
                    "com.sun.enterprise.ee.web.authenticator.HASingleSignOn");
            // EE
            out.println("This is EE");
        } catch (ClassNotFoundException e) {
            // PE
            for (int i=0; i<BUFFER_SIZE; i++) {
                out.print("X");
            }
        }
    }
}
