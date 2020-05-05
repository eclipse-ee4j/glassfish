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
import java.text.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class HelloJapan extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("text/html");
        res.setCharacterEncoding("Shift_JIS");
        res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                      "BEGIN_JAPANESE"
                      + "\u4eca\u65e5\u306f\u4e16\u754c"  // Hello World
                      + "END_JAPANESE");
    }
}
