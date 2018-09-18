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

package test;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns="/test", asyncSupported=true)
public class TestServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        String test = req.getParameter("testname");
        AsyncContext ac = req.startAsync();
        ac.addListener(new AsyncListener() {
            public void onComplete(AsyncEvent event) {
                System.out.println("my asyncListener.onComplete");
            }
            public void onError(AsyncEvent event) {
                System.out.println("my asyncListener.onError");
            }
            public void onStartAsync(AsyncEvent event) {
                System.out.println("my asyncListener.onStartAsync");
            }
            public void onTimeout(AsyncEvent event) {
                System.out.println("my asyncListener.onTimeout");
            }
        });
        ServletOutputStream output = res.getOutputStream();
        ServletInputStream input = req.getInputStream();
        ReadListenerImpl readListener = new ReadListenerImpl(input, output, ac);
        input.setReadListener(readListener);
    }

    static class ReadListenerImpl implements ReadListener {
        private ServletInputStream input = null;
        private ServletOutputStream output = null;
        private AsyncContext ac = null;

        ReadListenerImpl(ServletInputStream in, ServletOutputStream out,
                AsyncContext c) {
            input = in;
            output = out;
            ac = c;
        }

        public void onDataAvailable() throws IOException {
            StringBuilder sb = new StringBuilder();
            System.out.println("--> onDataAvailable");
            int len = -1;
            byte b[] = new byte[1024];
            while (input.isReady() 
                    && (len = input.read(b)) != -1) {
                String data = new String(b, 0, len);
                System.out.println("--> " + data);
                sb.append('/' + data);
                //output.print('/' + data);
            }
            output.print(sb.toString());
        }

        public void onAllDataRead() throws IOException {
            try {
                System.out.println("--> onAllDataRead");
                output.println("-onAllDataRead");
            } finally {
                ac.complete();
            }
        }

        public void onError(final Throwable t) {
            ac.complete();
            t.printStackTrace();
        }
    }
}
