/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest {

    private static final SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "servlet-3.0-webFragment-with-file-upload";
    private static final String EXPECTED_RESPONSE = "Uploaded content";

    public static void main(String args[]) {

        stat.addDescription("Unit test for fileupload in web fragment");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        String testdir = System.getenv("APS_HOME") +
            "/devtests/web/servlet-3.0/webFragmentWithFileUpload/";

        int port = new Integer(portS).intValue();
        try {
            goPost(host, port, contextRoot, "/single.xhtml", testdir);
        } catch (Throwable t) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            t.printStackTrace();
        }

        stat.printSummary(TEST_NAME);
    }

    private static void goPost(String host, int port, String contextRoot,
             String urlPattern, String dir) throws Exception
    {
        Socket sock =null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;

        String get = "GET " + contextRoot + urlPattern + " HTTP/1.0\r\n" + "Connection: keep-alive\r\n";
        System.out.println(get);

        try {
            sock = new Socket(host, port);
            os = sock.getOutputStream();
            os.write(get.getBytes());
            os.write("\r\n".getBytes());

            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            int i = 0;
            String line = null;
            String viewId = null;
            String sessionId = null;

            //get session id and JSF view id
            while ((line = br.readLine()) != null) {
                if(line.startsWith("Set-Cookie")) {
                    sessionId = line.split(";")[0].split("=")[1];
                    continue;
                }
                if(line.contains("jakarta.faces.ViewState:0")) {
                    String[] results = line.split("jakarta.faces.ViewState:0");
                    viewId = results[1].trim().split(" ")[1].trim().split("\"")[1];
                    break;
                }
            }

            // Write header for the uploaded text file
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            ba.write("--AaB03x\r\n".getBytes());
            ba.write("Content-Disposition: form-data; name=\"form\"\r\n\r\n".getBytes());
            ba.write("form\r\n".getBytes());
            ba.write("--AaB03x\r\n".getBytes());
            ba.write("Content-Disposition: form-data; name=\"form:file\"; filename=\"test.txt\"\r\n".getBytes());
            ba.write("Content-Type: application/octet-stream\r\n\r\n".getBytes());

            // Write content of the uploaded text file
            is = new FileInputStream (dir + "test.txt");
            int c;
            while ((c = is.read()) != -1) {
                ba.write(c);
            }

            ba.write("\r\n--AaB03x\r\n".getBytes());
            ba.write("Content-Disposition: form-data; name=\"form:j_idt4\"\r\n\r\n".getBytes());
            ba.write("upload\r\n".getBytes());
            ba.write("--AaB03x\r\n".getBytes());
            ba.write("Content-Disposition: form-data; name=\"form:j_idt5\"\r\n\r\n".getBytes());
            ba.write("\r\n--AaB03x\r\n".getBytes());
            ba.write("Content-Disposition: form-data; name=\"jakarta.faces.ViewState\"\r\n\r\n".getBytes());
            ba.write((viewId + "\r\n").getBytes());
            // Write boundary end
            ba.write("--AaB03x--\r\n".getBytes());
            byte[] data = ba.toByteArray();
            System.out.println(ba.toString());

            // Compose the post request header
            StringBuilder postHeader = new StringBuilder();
            postHeader.append("POST " + contextRoot + urlPattern + " HTTP/1.1\r\n");
            postHeader.append("Host: localhost:8080\r\n");
            postHeader.append("Connection: close\r\n");
            postHeader.append("Content-Type: multipart/form-data; boundary=AaB03x\r\n");
            postHeader.append("Content-Length: " + data.length + "\r\n");
            postHeader.append("Cookie: JSESSIONID=" + sessionId + "\r\n\r\n");
            System.out.println(postHeader);

            os.write(postHeader.toString().getBytes());
            os.write(data);

            while ((line = br.readLine()) != null) {
                if (line.contains(EXPECTED_RESPONSE)) {
                    stat.addStatus(TEST_NAME, stat.PASS);
                    return;
                }
            }

            System.out.println("Wrong response. Expected: " +
                               EXPECTED_RESPONSE + ", received: " + line);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch(IOException ex) {
                // ignore
            }
            try {
                if (br != null) {
                    br.close();
                }
            } catch(IOException ex) {
                // ignore
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch(IOException ex) {
                // ignore
            }
            try {
                if (sock != null) {
                    sock.close();
                }
            } catch(IOException ex) {
                // ignore
            }
        }

    }
}
