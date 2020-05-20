/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns={"/myurl"}, asyncSupported=true)
public class TestServlet extends HttpServlet implements AsyncListener {
    static StringBuffer sb = new StringBuffer();

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        if ("1".equals(req.getParameter("result"))) {
            res.getWriter().println(sb.toString());
            sb.delete(0, sb.length());
            return;
        }

        AsyncContext ac = req.startAsync();
        ac.addListener(this);
        ac.dispatch("/exception");
    }

    public void onComplete(AsyncEvent event) throws IOException {
        sb.append(", onComplete");
    }

    public void onTimeout(AsyncEvent event) throws IOException {
        // do nothing
    }

    public void onError(AsyncEvent event) throws IOException {
        // do nothing
    }

    public void onStartAsync(AsyncEvent event) throws IOException {
        // do nothing
    }
}
