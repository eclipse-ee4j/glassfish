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
    private static int count = 0;
    private static final int EXPECTED_COUNT = 1;
    static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", "keep-alive");

    public static void main(String args[]) {
        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
        stat.addDescription("Standalone keepAlive war test");
        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];
        int port = new Integer(portS);
        String name;
        try {
            goGet(host, port, "KeepAlive", contextRoot + "/test.jsp");

        } catch (Throwable t) {
        } finally {
            if (count != EXPECTED_COUNT) {
                stat.addStatus("web-keepAlive", SimpleReporterAdapter.FAIL);
            }
        }
        stat.printSummary();
    }

    private static void goGet(String host, int port,
        String result, String contextPath)
        throws Exception {
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();
        System.out.println("GET " + contextPath + " HTTP/1.0");
        System.out.println("Connection: keep-alive");
        os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
        os.write("Connection: keep-alive\n".getBytes());
        os.write("\n".getBytes());
        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;
        int tripCount = 0;
        try {
            while ((line = bis.readLine()) != null) {
                System.out.println("from server: " + line);
                int index = line.indexOf("Connection:");
                if (index >= 0) {
                    index = line.indexOf(":");
                    String state = line.substring(index + 1).trim();
                    if ("keep-alive".equalsIgnoreCase(state)) {
                        System.out.println("found keep-alive");
                        stat.addStatus("web-keepalive ", SimpleReporterAdapter.PASS);
                        count++;
                    }
                }
                if (line.contains("KeepAlive:end")) {
                    if (++tripCount == 1) {
                        System.out.println("GET " + contextPath + " HTTP/1.0");
                        os.write(("GET " + contextPath + " HTTP/1.0\n\n").getBytes());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Test UNPREDICTED-FAILURE");
        }
    }
}
