/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.client;

import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * @author Jitendra Kotamraju
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME =
                "portable-extensions-process-injection-target";
    private static final String EXPECTED_RESPONSE = "Hello from Servlet 3.0.";

    private final String host;
    private final String port;
    private final String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for process injection target extension");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invoke();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {
        String url = "http://" + host + ":" + port + contextRoot + "/myurl";
        System.out.println("opening connection to " + url);

        InputStream in = new URL(url).openStream();
        BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
        String line = rdr.readLine();
        if (EXPECTED_RESPONSE.equals(line)) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            System.out.println("Wrong response. Expected: " +
                        EXPECTED_RESPONSE + ", received: " + line);
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
        rdr.close();
    }
}
