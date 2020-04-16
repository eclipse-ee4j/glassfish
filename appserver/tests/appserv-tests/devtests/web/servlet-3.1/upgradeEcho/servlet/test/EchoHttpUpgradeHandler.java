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

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.WebConnection;

public class EchoHttpUpgradeHandler implements HttpUpgradeHandler {
    private String delimiter = "\\";

    public EchoHttpUpgradeHandler() {
    }

    public void init(WebConnection wc) {
        System.out.println("EchoProtocolHandler.init");
        try {
            ServletInputStream input = wc.getInputStream();
            ReadListenerImpl readListener = new ReadListenerImpl(delimiter, input, wc);
            input.setReadListener(readListener);
            wc.getOutputStream().flush();
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void destroy() {
        System.out.println("--> destroy");
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getDelimiter() {
        return delimiter;
    }

    static class ReadListenerImpl implements ReadListener {
        ServletInputStream input = null;
        ServletOutputStream output = null;
        WebConnection wc = null;
        String delimiter = null;

        ReadListenerImpl(String d, ServletInputStream in, WebConnection c)
                throws IOException {
            delimiter = d;
            input = in;
            wc = c;
            output = wc.getOutputStream();
        }

        public void onDataAvailable() throws IOException {
            StringBuilder sb = new StringBuilder();
            System.out.println("--> onDataAvailable");
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            System.out.println("#### Thread.currentThread.getContextClassloader(): " + cl);
            if (cl instanceof org.glassfish.web.loader.WebappClassLoader) {
                System.out.println("Correct ClassLoader");
            } else {
                System.out.println("ERROR Wrong ClassLoader!!!");
                sb.append("WrongClassLoader"); 
            }

            int len = -1;
            byte b[] = new byte[1024];
            while (input.isReady()
                    && (len = input.read(b)) != -1) {
                String data = new String(b, 0, len);
                System.out.println("--> " + data);
                sb.append(data);
            }
            output.print(delimiter + sb.toString());
            output.flush();
        }

        public void onAllDataRead() throws IOException {
            System.out.println("--> onAllDataRead");
            try {
                wc.close();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        public void onError(final Throwable t) {
            System.out.println("--> onError");
            t.printStackTrace();
            try {
                wc.close();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
