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

public class TestServlet extends HttpServlet implements AsyncListener {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        if (!req.isAsyncSupported()) {
            throw new ServletException("Async not supported when it should");
        }
 
        if (!req.getDispatcherType().equals(DispatcherType.ASYNC)) {
            // Container-initiated dispatch
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
        } else {
            // Async dispatch
            throw new ServletException("Error during dispatch");
        }
    }

    public void onComplete(AsyncEvent event) throws IOException {
        // do nothing
    }

    public void onTimeout(AsyncEvent event) throws IOException {
        // do nothing
    }

    public void onError(AsyncEvent event) throws IOException {
        if (event.getThrowable() != null) {
            AsyncContext ac = event.getAsyncContext();
            ServletResponse sr = ac.getResponse();
            ((HttpServletResponse)sr).setStatus(HttpServletResponse.SC_OK);
            sr.getWriter().println("Hello world");
            ac.complete();
        }
    }

    public void onStartAsync(AsyncEvent event) throws IOException {
        // do nothing
    }
}
