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
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;

public class ResumeSession extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        HashMap map = (HashMap) session.getAttribute("map");
        InitialContext ic = (InitialContext)
            session.getAttribute("JNDIInitialContext");

        boolean passed = false; 
        if ("value1".equals(map.get("name1"))
                && "value2".equals(map.get("name2"))
                && (ic != null)) {
            passed = true;
        }

        if (passed) {
            res.getWriter().print("Test passed!");
        } else {
            res.getWriter().print("Test failed!");
        }
    }
}
