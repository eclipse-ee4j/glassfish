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

package test;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class DispatchFrom extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        if (!req.isAsyncSupported()) {
            throw new ServletException("Async not supported when it should");
        }

        final ServletContext toContext = getServletContext().getContext("/toContext");
        if (toContext == null) {
            throw new ServletException("Unable to acquire target ServletContext");
        }
        final AsyncContext ac = req.startAsync();

        Timer asyncTimer = new Timer("TestTimer", true);
        asyncTimer.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    ac.dispatch(toContext, "/dispatchTo");
                }
            },
	    5000);
    }
}
