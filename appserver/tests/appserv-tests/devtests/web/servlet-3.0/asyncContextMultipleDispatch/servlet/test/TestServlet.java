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

public class TestServlet extends HttpServlet implements AsyncListener {
    private static StringBuffer sb = new StringBuffer();

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        if ("1".equals(req.getParameter("result"))) {
            res.getWriter().println(sb.toString());
            sb.delete(0, sb.length());
            return;
        }

        if (!req.isAsyncSupported()) {
            throw new ServletException("Async not supported when it should");
        }
   
        if (req.getDispatcherType() == DispatcherType.REQUEST) {
            // Container-initiated dispatch
            req.setAttribute("ABC", "DEF");
            final AsyncContext ac = req.startAsync();
            ac.addListener(this);
            Timer asyncTimer = new Timer("AsyncTimer", true);
            asyncTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        ac.dispatch();
                    }
                },
                5000);
        } else if (req.getDispatcherType() == DispatcherType.ASYNC) {
            if ("DEF".equals(req.getAttribute("ABC"))) {
                // First async dispatch
                req.removeAttribute("ABC");
                req.startAsync().dispatch();
            } else {
                // Second async dispatch
                req.startAsync().complete();
            }
        }
    }

    public void onComplete(AsyncEvent event) throws IOException {
        sb.append("onComplete");
    }

    public void onTimeout(AsyncEvent event) throws IOException {
        // do nothing
    }

    public void onError(AsyncEvent event) throws IOException {
        // do nothing
    }

    public void onStartAsync(AsyncEvent event) throws IOException {
        sb.append("onStartAsync,");
        /*
         * ServletRequest#startAsync clears the list of AsyncListener
         * instances registered with the AsyncContext - after calling
         * each AsyncListener at its onStartAsync method, which is the 
         * method we're in.
         * Register ourselves again, so we continue to get notified
         */
        event.getAsyncContext().addListener(this);
    }

}
