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
 * Unit test for Bugzilla 16181 ("JspWriter not restored properly when
 * exception thrown in a tag's body content"), see
 * http://issues.apache.org/bugzilla/show_bug.cgi?id=16181
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "jsp-exception-in-tag-body-restore-jsp-writer";
    private static final String EXPECTED = "java.lang.Exception: exception thrown by throwEx";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugzilla 16181");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invokeJsp();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    public void invokeJsp() throws Exception {

        BufferedReader bis = null;
        try {
            String url = "http://" + host + ":" + port + "/" + contextRoot
                + "/jsp/test.jsp";
            HttpURLConnection conn = (HttpURLConnection)
                (new URL(url)).openConnection();

            int code = conn.getResponseCode();
            if (code != 200) {
                System.err.println("Unexpected return code: " + code);
                stat.addStatus(TEST_NAME, stat.FAIL);
                return;
            }

            bis = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
            String line = null;
            while ((line = bis.readLine()) != null) {
                if (EXPECTED.equals(line)) {
                    stat.addStatus(TEST_NAME, stat.PASS);
                    break;
                }
            }

            if (line == null) {
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } finally {
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
    }
}
