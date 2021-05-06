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

import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for http://java.net/jira/browse/GLASSFISH-17093
 * ("Custom errorReportValve is not working even server start")
 */
public class WebTest {

    private static final String TEST_NAME = "virtual-server-with-custom-error-report-valve";

    private static final String EXPECTED_RESPONSE = "My error page";

    private static final SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private int port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = Integer.parseInt(args[1]);
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for IT 17093");

        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void doTest() throws Exception {

        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader input = null;
        try {
            sock = new Socket(host, port);
            os = sock.getOutputStream();
            String get = "GET " + contextRoot + "/test HTTP/1.0\n";
            System.out.print(get);
            os.write(get.getBytes());
            os.write("\r\n".getBytes());

            is = sock.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            String line = null;
            boolean found = false;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
                if (EXPECTED_RESPONSE.equals(line)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new Exception("Cannot find message: " + EXPECTED_RESPONSE);
            }
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }
}
