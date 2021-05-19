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

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
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
        ac.addListener(new AsyncListener() {
            public void onComplete(AsyncEvent event) throws IOException {
                System.out.println("my asyncListener.onComplete");
            }
            public void onError(AsyncEvent event) {
                System.out.println("my asyncListener.onError: " + event.getThrowable());
            }
            public void onStartAsync(AsyncEvent event) {
                System.out.println("my asyncListener.onStartAsync");
            }
            public void onTimeout(AsyncEvent event) {
                System.out.println("my asyncListener.onTimeout");
            }
        });

        ServletInputStream input = req.getInputStream();
        // read all data first
        ReadListener readListener = new ReadListenerImpl(input, res, ac);
        input.setReadListener(readListener);
    }

    static class ReadListenerImpl implements ReadListener {
        private ServletInputStream input = null;
        private HttpServletResponse res = null;
        private AsyncContext ac = null;
        private Queue<String> queue = new LinkedBlockingQueue<String>();

        ReadListenerImpl(ServletInputStream in, HttpServletResponse r,
                AsyncContext c) {
            input = in;
            res = r;
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
                sb.append(data);
            }
            queue.add(sb.toString());
        }

        public void onAllDataRead() throws IOException {
            System.out.println("--> onAllDataRead");
            // now all data are read, write the result
            ServletOutputStream output = res.getOutputStream();
            WriteListener writeListener = new WriteListenerImpl(output, queue, ac);
            output.setWriteListener(writeListener);
        }

        public void onError(final Throwable t) {
            ac.complete();
            t.printStackTrace();
        }
    }

    static class WriteListenerImpl implements WriteListener {
        private ServletOutputStream output = null;
        private Queue<String> queue = null;
        private AsyncContext ac = null;

        WriteListenerImpl(ServletOutputStream sos, Queue<String> q,
                AsyncContext c) {
            output = sos;
            queue = q;
            ac = c;
        }

        public void onWritePossible() throws IOException {
            System.out.println("--> onWritePossible");
            System.out.println("--> queue: " + queue);
            while (queue.peek() != null && output.isReady()) {
                String data = queue.poll();
                System.out.println("--> data = " + data);
                output.print(data);
            }
            System.out.println("--> ac.complete");
            if (queue.peek() == null) {
                ac.complete();
            }
        }

        public void onError(final Throwable t) {
            ac.complete();
            t.printStackTrace();
        }
    }
}
