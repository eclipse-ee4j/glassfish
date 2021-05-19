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
import java.net.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        URL[] urls = new URL[2];
        Enumeration<URL> e = getClass().getClassLoader().getResources("test.txt");
        int i = 0;
        while (e.hasMoreElements()) {
            if (i == 2) {
                throw new ServletException(
                    "Wrong number of resource URLs, expected 2");
            }
            urls[i++] = e.nextElement();
        }

        if (i != 2) {
            throw new ServletException(
                "Wrong number of resource URLs, expected 2");
        }

        getServletContext().log("urls[0]=" + urls[0]);
        getServletContext().log("urls[1]=" + urls[1]);

        if (!urls[0].toString().endsWith("test.txt") ||
                !urls[1].toString().endsWith("test.txt")) {
            throw new ServletException("Wrong resource URL(s)");
        }

        /*
         * Since delegate is set to false, we expect the first URL in the
         * returned enum:
         *
         * jar:file:/space/luehe/ws/v3/distributions/web/target/glassfish\
         *     /domains/domain1/applications\
         *     /web-classloader-get-resources-delegate-false-web/WEB-INF/lib\
         *     /mytest.jar!/test.txt
         *
         * to be longer than the second:
         *
         * jar:file:/space/luehe/ws/v3/distributions/web/target/glassfish\
         *     /lib/test.jar!/test.txt|#]
         */
        if (urls[0].toString().length() < urls[1].toString().length()) {
            throw new ServletException("Delegate flag not honored");
        }
    }
}



