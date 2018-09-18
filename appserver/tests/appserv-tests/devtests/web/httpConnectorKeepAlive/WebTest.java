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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import com.sun.appserv.test.util.results.SimpleReporterAdapter;

public class WebTest {
    private static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests",
        "httpConnectorKeepAlive");

    public static void main(String args[]) {
        stat.addDescription("Http Connector httpConnectorKeepAlive test");
        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];
        int port = new Integer(portS);
        try {
            goGet(host, port, contextRoot + "/test.jsp");
        } catch (Throwable t) {
        } finally {
            stat.printSummary();
        }
    }

    private static void goGet(String host, int port, String contextPath)
        throws Exception {
        boolean closed = true;
        try {
            Socket s = new Socket(host, port);
            OutputStream os = s.getOutputStream();
            os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
            os.write("Connection: keep-alive\n".getBytes());
            os.write("\n".getBytes());
            InputStream is = s.getInputStream();
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = bis.readLine()) != null) {
                int index = line.indexOf("Connection:");
                System.out.println("--" + line);
                if (index >= 0 && line.contains("closed")) {
                    closed = false;
                    stat.addStatus("httpConnectorKeepAlive", SimpleReporterAdapter.FAIL);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println(closed);
        if (closed) {
            stat.addStatus("httpConnectorKeepAlive", SimpleReporterAdapter.PASS);
        }
    }
}
