/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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
 * Unit test for Bugzilla 31659 ("Page context not fully populated for
 * Exception if using app-wide error page"), see
 * http://issues.apache.org/bugzilla/show_bug.cgi?id=31659
 */
public class WebTest {

    private static final String TEST_NAME = "jsp-error-page-servlet-error-exception";

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugzilla 31659");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary();
    }

    public void doTest() {
        try {
            Socket s = new Socket(host, Integer.valueOf(port));
            OutputStream os = s.getOutputStream();
            String requestUri = contextRoot + "/causeError.jsp";

            System.out.println("GET " + requestUri + " HTTP/1.0");
            os.write(("GET " + requestUri + " HTTP/1.0\n").getBytes());
            os.write("\n".getBytes());

            try (InputStream is = s.getInputStream(); BufferedReader bis = new BufferedReader(new InputStreamReader(is))) {

                String line = null;
                int count = 0;
                while ((line = bis.readLine()) != null) {
                    System.out.println(line);
                    if (line.contains("java.lang.NullPointerException")) {
                        count++;
                    }
                }

                if (count == 2) {
                    stat.addStatus(TEST_NAME, stat.PASS);
                } else {
                    stat.addStatus(TEST_NAME, stat.FAIL);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }

}
