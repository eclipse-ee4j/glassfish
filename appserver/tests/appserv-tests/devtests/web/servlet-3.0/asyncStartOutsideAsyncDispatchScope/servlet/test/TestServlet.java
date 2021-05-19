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
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        if (!req.isAsyncSupported()) {
            throw new ServletException("Async not supported when it should");
        }

        if (req.getDispatcherType() == DispatcherType.REQUEST) {
            // Initial dispatch (as opposed to ASYNC dispatch)
            final AsyncContext ac = req.startAsync();
            Timer asyncTimer = new Timer("AsyncTimer", true);
            asyncTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        // Need to getResponse before calling dispatch
                        PrintWriter writer = null;
                        try {
                            writer = ac.getResponse().getWriter();
                        } catch(IOException ioe) {
                            throw new RuntimeException(ioe);
                        }
                        ac.dispatch();
                        // Make sure IllegalStateException is thrown, since
                        // ServletRequest#startAsync is called outside the
                        // scope of the ASYNC dispatch
                        try {
                            ac.getRequest().startAsync();
                        } catch (IllegalStateException e) {
                            writer.println("Hello world");
                        }
                    }
                },
                5000);
        } else if (req.getDispatcherType() == DispatcherType.ASYNC) {
            /*
             * Sleep during the ASYNC dispatch, which is executed on a
             * separate thread, in order to delay the closing of the
             * response, so that the thread that called AsyncContext#dispatch
             * has a chance to write "Hello world" to the response
             */
            try {
                Thread.currentThread().sleep(5000);
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
