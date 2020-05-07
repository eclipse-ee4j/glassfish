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
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        boolean passed = false;

        HttpSession httpSession = request.getSession();

        // Exception expected
        Object attr1 = new MyNonSerializable();
        try {
            httpSession.setAttribute("attr1", attr1);
        } catch (IllegalArgumentException iae) {
            passed = true;
        }

        // No exception expected
        Object attr2 = new MySerializable();
        httpSession.setAttribute("attr2", attr2);

        response.getWriter().print(passed);
    }

    private static class MyNonSerializable {

        public MyNonSerializable() {
        }
    }

    private static class MySerializable implements Serializable {

        public MySerializable() {
        }
    }

}

