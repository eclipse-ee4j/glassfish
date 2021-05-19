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

import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest {

    private static final SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "servlet-3.0-multipart";

    public static void main(String args[]) {

        stat.addDescription("Unit test for multipart request");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        String testdir = System.getenv("APS_HOME") +
            "/devtests/web/servlet-3.0/multipart/";

        int port = new Integer(portS).intValue();
        try {
            goPost(host, port, contextRoot + "/ServletTest", testdir);
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Throwable t) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            t.printStackTrace();
        }

        stat.printSummary(TEST_NAME);
    }

    private static void goPost(String host, int port, String contextPath,
             String dir) throws Exception
    {
        // First compose the post request data
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        ba.write("--AaB03x\r\n".getBytes());
        // Write header for the first file
        ba.write("Content-Disposition: form-data; name=\"myFile\"; filename=\"test.txt\"\r\n".getBytes());
        ba.write("Content-Type: text/plain\r\n\r\n".getBytes());

        // Write content of first text file
        InputStream is = new FileInputStream (dir + "test.txt");
        int c;
        while ((c = is.read()) != -1) {
            ba.write(c);
        }
        ba.write("\r\n--AaB03x\r\n".getBytes());

        // Write header for the second file
        ba.write("Content-Disposition: form-data; name=\"myFile2\"; filename=\"Test.war\"\r\n".getBytes());
        ba.write("Content-Type: application/x-java-archive\r\n\r\n".getBytes());

        // Write content of second binary file
        is = new FileInputStream (dir + "Test.war");
        while ((c = is.read()) != -1) {
            ba.write(c);
        }
        ba.write("\r\n--AaB03x\r\n".getBytes());

        // Write header for the third part, this is has no file name
        ba.write("Content-Disposition: form-data; name=\"xyz\"\r\n".getBytes());
        ba.write("Content-Type: text/plain\r\n\r\n".getBytes());
        ba.write("1234567abcdefg".getBytes());

        // Write boundary end
        ba.write("\r\n--AaB03x--\r\n".getBytes());
        byte[] data = ba.toByteArray();

        // Compose the post request header
        StringBuilder header = new StringBuilder();
        header.append("POST " + contextPath + " HTTP/1.1\r\n");
        header.append("Host: localhost\r\n");
        header.append("Connection: close\r\n");
        header.append("Content-Type: multipart/form-data; boundary=AaB03x\r\n");
        header.append("Content-Length: " + data.length + "\r\n\r\n");

        // Now the actual request
        Socket sock = null;
        OutputStream os = null;
        BufferedReader bis = null;

        try {
            sock = new Socket(host, port);
            os = sock.getOutputStream();
            System.out.println(header);
            os.write(header.toString().getBytes());
            os.write(data);

            int i = 0;
            int partCount = -1;
            int failCount = 0;
            int expectedCount = 0;

            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = bis.readLine()) != null) {
                System.out.println(i++ + ": " + line);
                if (line.startsWith("getParameter")) {
                    partCount++;
                    expectedCount++;
                    failCount += check(partCount, 0, line);
                } else if (line.startsWith("Part name:")) {
                    partCount++;
                    expectedCount++;
                    failCount += check(partCount, 0, line);
                } else if (line.startsWith("Size:")) {
                    expectedCount++;
                    failCount += check(partCount, 1, line);
                } else if (line.startsWith("Content Type:")){
                    expectedCount++;
                    failCount += check(partCount, 2, line);
                } else if (line.startsWith("Header Names:")) {
                    expectedCount++;
                    failCount += check(partCount, 3, line);
                }
            }
            if (expectedCount != 13 || failCount > 0) {
                throw new Exception("Wrong expected count or Contains invalid values");
            }
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch(IOException ex) {
                // ignore
            }
            try {
                if (is != null) {
                    is.close();
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

   static String[][] expected = {
       {"1234567abcdefg"},
       {"myFile", "36", "text/plain", "content-disposition content-type" },
       {"myFile2", "13812", "application/x-java-archive",
            "content-disposition content-type" },
       {"xyz", "14", "text/plain", "content-disposition content-type"}
   };

   private static int check(int x, int y, String line) {
       if (line.contains(expected[x][y]))
           return 0;
       return 1;
   }

}
