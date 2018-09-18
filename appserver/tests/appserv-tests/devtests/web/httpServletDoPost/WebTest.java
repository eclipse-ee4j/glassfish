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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

import com.sun.appserv.test.util.results.SimpleReporterAdapter;

public class WebTest {
    private static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", "httpServletDoPost");

    public static void main(String args[]) {
        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
        stat.addDescription("Invoking HttpServlet.doPost");
        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];
        int port = new Integer(portS);
        try {
            goGet(host, port, contextRoot + "/ServletTest");
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }
        stat.printSummary();
    }

    private static void goGet(String host, int port, String contextPath)
        throws Exception {
        checkResponseCode(host, port, contextPath);
        test(host, port, contextPath, "HTTP/1.1");
        test(host, port, contextPath, "HTTP/1.0");
    }

    private static void checkResponseCode(final String host, final int port, final String contextPath)
        throws IOException {
        final URL url = new URL("http://" + host + ":" + port + contextPath);
        System.out.println("\n Invoking url: " + url.toString());
        final URLConnection conn = url.openConnection();
        DataOutputStream out = null;
        try {
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection urlConnection = (HttpURLConnection) conn;
                urlConnection.setDoOutput(true);
                out = new DataOutputStream(urlConnection.getOutputStream());
                out.writeByte(1);
                int responseCode = urlConnection.getResponseCode();
                stat.addStatus("httpServletDoPost",
                    urlConnection.getResponseCode() == 405 ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL);

                urlConnection.disconnect();
            }
        } finally {
            if(out != null) {
                out.close();
            }
        }
    }

    private static void test(final String host, final int port, final String contextPath, final String protocol)
        throws IOException {
        boolean mark = false;
        int i = 0;
        final String name = "httpServletDoPost-noCL-" + protocol;
        Socket s = new Socket(host, port);
        try {
            s.setSoTimeout(10000);
            OutputStream os = s.getOutputStream();
            System.out.println("POST " + contextPath + " " + protocol + "\n");
            os.write(("POST " + contextPath + " " + protocol + "\n").getBytes());
            os.write("Host: localhost\r\n".getBytes());
            os.write("content-length: 0\r\n".getBytes());
            os.write("\r\n".getBytes());
            InputStream is = s.getInputStream();
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));
            String line;
            int index;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf("httpServletDoPost");
                System.out.println(i++ + ": " + line);
                if (index != -1) {
                    index = line.indexOf("::");
                    String status = line.substring(index + 1);
                    if ("FAIL".equalsIgnoreCase(status)) {
                        stat.addStatus(name, SimpleReporterAdapter.FAIL);
                        mark = true;
                    }
                }
            }
        } catch (Exception ex) {
        } finally {
            s.close();
            if (!mark && i > 0) {
                stat.addStatus(name, SimpleReporterAdapter.PASS);
            }
        }
    }

}
