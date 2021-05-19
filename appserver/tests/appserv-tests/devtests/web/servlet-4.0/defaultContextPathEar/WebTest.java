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
import org.glassfish.grizzly.test.http2.*;

/*
 * Unit test for default-context-path in web.xml in an ear file.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "servlet-4.0-default-context-path-ear";
    private static final String MY_CONTEXT_ROOT = "/mycontextroot";

    private String host;
    private int port;

    public WebTest(String[] args) {
        host = args[0];
        port = Integer.parseInt(args[1]);
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for default-context-path setting in web.xml in an ear file");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invokeJsp();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeJsp() throws Exception {
        String path = MY_CONTEXT_ROOT + "/jsp/test.jsp";

        try (HttpClient httpClient = HttpClient.builder().
                host(host).port(port).build()) {
            httpClient.request().path(path).build().send();
            HttpResponse httpResponse = httpClient.getHttpResponse();
            int code = httpResponse.getStatus();
            if (code != 200) {
                throw new Exception("Wrong response code. Expected: 200, received: " + code);
            }
        }
    }
}
