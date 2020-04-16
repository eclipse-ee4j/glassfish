/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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
import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;

@WebServlet(urlPatterns={"/test2"}, asyncSupported=true)
public class TestServlet2 extends HttpServlet implements AsyncListener {
    private static StringBuffer sb = new StringBuffer();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        addInfo("S2i");
        final AsyncContext asyncContext = req.startAsync();
        asyncContext.addListener(this);

        new Thread() {
            @Override
            public void run() {
                asyncContext.complete();
            }
        }.start();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            //
        }
        addInfo("S2o");
    }

    public void onComplete(AsyncEvent event) throws IOException {
        addInfo("AC");
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

    static void addInfo(String text) {
        sb.append(text);
    }

    static void clearInfo() {
        sb.delete(0, sb.length());
    }

    static String getInfo() {
        return sb.toString();
    }
}
