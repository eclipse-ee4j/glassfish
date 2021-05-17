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
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Non blocking Write
 */
public class WebTest {

    private static String TEST_NAME = "servlet-3.1-non-blocking-output";
    private static String EXPECTED_RESPONSE = "onWritePossible";
    private static final String CRLF = "\r\n";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String contextRoot = args[2];
        stat.addDescription("Unit test for non blocking output");

        try (Socket s = new Socket(host, port);
                     OutputStream output = s.getOutputStream()) {
            String reqStr = "GET /" + contextRoot + "/test HTTP/1.1" + CRLF +
                 "Host: localhost" + CRLF + CRLF;
            output.write(reqStr.getBytes());

            int count = 0;
            int sleepInSec = 5;
            System.out.format("Sleeping %s sec\n", sleepInSec);
            Thread.sleep(sleepInSec * 1000);

            boolean expected = false;
            try (InputStream is = s.getInputStream();
                    BufferedReader input = new BufferedReader(new InputStreamReader(is))) {
                boolean isHeader = true;
                String line = null;
                while (!expected && (line = input.readLine()) != null) {
                    if (isHeader) {
                        System.out.println(line);
                        isHeader = line.length() != 0;
                        continue;
                    }
                    expected = expected || line.endsWith(EXPECTED_RESPONSE);
                    System.out.println("\n " + (count++) + ": " + line.length());
                    int length = line.length();
                    int lengthToPrint = 20;
                    int end = ((length > lengthToPrint) ? lengthToPrint : length);
                    System.out.print(line.substring(0, end) + "...");
                    if (length > 20) {
                        System.out.println(line.substring(length - 20));
                    }
                    System.out.println();
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
