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

/**
 * Unit test for locale-encoding-mapping.
 */
public class WebTest {
    private static String TEST_NAME = "locale-encoding-mapping";
    private static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", "locale-encoding-mapping");

    public static void main(String args[]) {
        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
        stat.addDescription("Invoking localeEncodingMapping");
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
        throws IOException {
        boolean setEncoding = false;
        int i = 0;
        Socket s = new Socket(host, port);

        OutputStream os = null;
        BufferedReader bis = null;
        try {
            //s.setSoTimeout(10000);
            os = s.getOutputStream();
            System.out.println("GET " + contextPath + " HTTP/1.0\n");
            os.write(("GET " + contextPath + " HTTP/1.0\r\n").getBytes());
            os.write("\r\n".getBytes());

            bis = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String line;
            while ((line = bis.readLine()) != null) {
                System.out.println(i++ + ": " + line);
                if (line.contains("charset=euc-jp")) {
                    setEncoding = true;
                }
            }
        } catch (Exception ex) {
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch(Exception e) {
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch(Exception e) {
                }
            }
            try {
                s.close();
            } catch(Exception e) {
            }

            stat.addStatus(TEST_NAME, ((setEncoding) ? stat.PASS : stat.FAIL));
        }
    }

}
