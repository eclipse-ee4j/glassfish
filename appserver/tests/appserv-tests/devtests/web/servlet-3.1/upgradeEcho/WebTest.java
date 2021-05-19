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

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for upgrade echo
 */
public class WebTest {

    private static String TEST_NAME = "servlet-3.1-upgrade-echo";
    private static String EXPECTED_RESPONSE = "HelloWorld";
    private static final String CRLF = "\r\n";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String contextRoot = args[2];
        stat.addDescription("Unit test for upgrade");

        try {
            Socket s = null;

            InputStream input = null;
            OutputStream output = null;
            boolean expected = false;
            try {
                s = new Socket(host, port);
                output = s.getOutputStream();

                String reqStr = "POST " + contextRoot + "/test HTTP/1.1" + CRLF;
                reqStr += "User-Agent: Java/1.6.0_33" + CRLF;
                reqStr += "Host: " + host + ":" + port + CRLF;
                reqStr += "Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2" + CRLF;
                reqStr += "Upgrade: echo" + CRLF;
                reqStr += "Connection: Upgrade" + CRLF;
                reqStr += "Content-type: application/x-www-form-urlencoded" + CRLF;
                reqStr += CRLF;
                output.write(reqStr.getBytes());

                input = s.getInputStream();
                int len = -1;
                byte b[] = new byte[1024];
                StringBuilder sb = new StringBuilder();
                //consume headers
                System.out.println("Consuming headers");
                boolean containsUpgrade = false;
                while ((len = input.read(b)) != -1) {
                    String line = new String(b, 0, len);
                    System.out.println(line);
                    sb.append(line);
                    String temp = sb.toString();
                    if (!containsUpgrade &&  temp.toLowerCase().contains("upgrade")) {
                        containsUpgrade = true;
                    }
                    if (temp.contains("\r\n\r\n") || temp.contains("\n\n")) {
                        break;
                    }
                }

                writeChunk(output, "Hello");
                int sleepInSeconds = 1;
                System.out.format("Sleeping %d sec\n", sleepInSeconds);
                Thread.sleep(sleepInSeconds * 1000);
                writeChunk(output, "World");

                // read data without using readLine
                long startTime = System.currentTimeMillis();
                System.out.println("Consuming results");
                while ((len = input.read(b)) != -1) {
                    String line = new String(b, 0, len);
                    sb.append(line);
                    boolean hasInfo = sb.toString().replace("/", "").contains(EXPECTED_RESPONSE);
                    boolean hasError = sb.toString().contains("WrongClassLoader");
                    if (hasInfo || hasError || System.currentTimeMillis() - startTime > 20 * 1000) {
                        break;
                    }
                }

                System.out.println(sb.toString());
                StringTokenizer tokens = new StringTokenizer(sb.toString(), CRLF);
                String line = null;
                while (tokens.hasMoreTokens()) {
                    line = tokens.nextToken();
                }

                expected = containsUpgrade && line.contains("/")
                        && (line.indexOf("/") < line.indexOf("d"))
                        && line.replace("/", "").equals(EXPECTED_RESPONSE);
            } finally {
                try {
                    if (input != null) {
                        System.out.println("# Closing input...");
                        input.close();
                        System.out.println("# Input closed.");
                    }
                } catch(Exception ex) {
                }

                try {
                    if (output != null) {
                        System.out.println("# Closing output...");
                        output.close();
                        System.out.println("# Output closed .");
                    }
                } catch(Exception ex) {
                }
                try {
                    if (s != null) {
                        System.out.println("# Closing socket...");
                        s.close();
                        System.out.println("# Socked closed.");
                    }
                } catch(Exception ex) {
                }
            }

            // server.log should contain "##### OnError" and
            // stacktrace of produced exception (EOF).

            // sleep is here only for verifying that onError was
            // called before this process ended.
            Thread.sleep(10000);

            stat.addStatus(TEST_NAME, ((expected) ? stat.PASS : stat.FAIL));
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    private static void writeChunk(OutputStream out, String data) throws IOException {
        if (data != null) {
            out.write(data.getBytes());
        }
        out.flush();
    }
}
