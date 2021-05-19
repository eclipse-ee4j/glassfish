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

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns="/test", asyncSupported=true)
public class TestServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        AsyncContext ac = req.startAsync();
        //ac.setTimeout(-1);
        ServletInputStream input = req.getInputStream();
        ReadListenerImpl readListener = new ReadListenerImpl(input, ac);
        input.setReadListener(readListener);
    }

    static class ReadListenerImpl implements ReadListener {
        private ServletInputStream input = null;
        private AsyncContext ac = null;
        private StringBuilder sb = new StringBuilder();

        ReadListenerImpl(ServletInputStream in, AsyncContext c) {
            input = in;
            ac = c;
        }

        public void onDataAvailable() throws IOException {
            System.out.println("--> onDataAvailable");
            int len = -1;
            byte b[] = new byte[1024];
            while (input.isReady()
                    && (len = input.read(b)) != -1) {
                String data = new String(b, 0, len);
                System.out.println("--> " + data);
                sb.append('/' + data);
            }
        }

        public void onAllDataRead() throws IOException {
            System.out.println("--> onAllDataRead");
            sb.append("-onAllDataRead");
            ac.getRequest().setAttribute("mysb", sb.toString());
            ac.dispatch("test2");
        }

        public void onError(final Throwable t) {
            ac.complete();
            t.printStackTrace();
        }
    }
}
