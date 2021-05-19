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
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns="/test", asyncSupported=true)
public class TestServlet extends HttpServlet {
    private static final int MAX_TIME_MILLIS = 10 * 1000;
    private static final int LENGTH = 500000;

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        AsyncContext ac = req.startAsync();
        ServletOutputStream output = res.getOutputStream();
        WriteListenerImpl writeListener = new WriteListenerImpl(output, ac);
        output.setWriteListener(writeListener);
    }

    static class WriteListenerImpl implements WriteListener {
        private ServletOutputStream output = null;
        private AsyncContext ac = null;
        private int count = 0;

        WriteListenerImpl(ServletOutputStream sos,
                AsyncContext c) {
            output = sos;
            ac = c;
        }

        public void onWritePossible() throws IOException {
            if (count == 0) {
                long startTime = System.currentTimeMillis();
                while (output.isReady()) {
                    writeData(output);
                    count++;
                    if (System.currentTimeMillis() - startTime > MAX_TIME_MILLIS
                            || count > 10) {
                        throw new IOException("Cannot fill the write buffer");
                    }
                }
            } else if (count > 0) {
                String message = "onWritePossible";
                System.out.println("--> " + message);
                output.write(message.getBytes());
                ac.complete();
            }
        }

        public void onError(final Throwable t) {
            ac.complete();
            t.printStackTrace();
        }
    }

    static void writeData(ServletOutputStream output) throws IOException {
        System.out.println("--> calling writeData");
        byte[] b = new byte[LENGTH];
        Arrays.fill(b, 0, LENGTH, (byte)'a');
        output.write(b);
    }
}
