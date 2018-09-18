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
import java.util.StringTokenizer;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for upgrade with CDI
 */
public class WebTest {

    private static String TEST_NAME = "servlet-3.1-upgrade-with-cdi";
    private static final String CRLF = "\r\n";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String contextRoot = args[2];
        stat.addDescription("Unit test for upgrade with CDI");

        try {
            Socket s = null;

            BufferedReader input = null;
            BufferedWriter output = null;
            boolean valid = true;
            try {
                s = new Socket(host, port);
                output = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

                output.write("POST " + contextRoot + "/test HTTP/1.1" + CRLF);
                output.write("User-Agent: Java/1.6.0_33" + CRLF);
                output.write("Host: " + host + ":" + port + CRLF);
                output.write("Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2" + CRLF);
                output.write("Upgrade: isbn" + CRLF);
                output.write("Connection: Upgrade" + CRLF);
                output.write("Content-type: application/x-www-form-urlencoded" + CRLF);
                output.write(CRLF);
                output.flush();

                input = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String line = null;
                // consume http headers
                boolean containsUpgrade = false;
                while ((line = input.readLine()) != null) {
                    System.out.println(line);
                    containsUpgrade = containsUpgrade || (line.toLowerCase().startsWith("upgrade"));
                    if ("".equals(line)) {
                        break;
                    }
                }
                valid = valid && containsUpgrade;

                System.out.println("First req ...");
                output.write("0691081220\n");
                output.flush();

                line = input.readLine();
                System.out.println(line);
                valid = valid && "0691081220 true".equals(line);

                System.out.println("Second req ...");
                output.write("069108122X\n");
                output.flush();

                line = input.readLine();
                System.out.println(line);
                valid = valid && "069108122X false".equals(line);

                output.write("EXIT\n");
                Thread.sleep(2000);
                output.flush();
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
                try {
                    if (s != null) {
                        s.close();
                    }
                } catch(Exception ex) {
                }
            }
            stat.addStatus(TEST_NAME, ((valid) ? stat.PASS : stat.FAIL));
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }
}
