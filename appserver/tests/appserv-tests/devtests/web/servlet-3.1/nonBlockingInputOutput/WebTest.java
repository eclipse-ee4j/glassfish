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

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Non blocking Input and then non blocking Output
 */
public class WebTest {

    private static String TEST_NAME = "servlet-3.1-non-blocking-Input-Output";
    private static String EXPECTED_RESPONSE = "HelloWorld";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String contextRoot = args[2];
        stat.addDescription("Unit test for non blocking read and then non blocking write");

        try {
            URL url = new URL("http://" + host + ":" + port + "/" + contextRoot + "/test?testname=abc&data=1");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-type", "text/plain; charset=utf-8");
            conn.setChunkedStreamingMode(5);
            conn.setDoOutput(true);
            conn.connect();

            BufferedReader input = null;
            BufferedWriter output = null;
            boolean expected = false;
            try {
                output = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
                try {
                    String data = "Hello";
                    output.write(data);
                    output.flush();
                    int sleepInSeconds = 3;
                    System.out.format("Sleeping %d sec\n", sleepInSeconds);
                    Thread.sleep(sleepInSeconds * 1000);
                    data = "World";
                    output.write(data);
                    output.flush();
                    output.close();
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = null;
                while ((line = input.readLine()) != null) {
                    System.out.println(line);
                    int slashInd = line.indexOf("/", 1);
                    expected = line.replace("/", "").equals(EXPECTED_RESPONSE);
                    if (expected) {
                        break;
                    }
                }
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch(Exception ex) {
                }

                try {
                    if (output != null) {
                        output.close();
                    }
                } catch(Exception ex) {
                }
            }

            stat.addStatus(TEST_NAME, ((expected) ? stat.PASS : stat.FAIL));
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }
}
