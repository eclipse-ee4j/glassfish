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
import org.glassfish.grizzly.test.http2.*;

/*
 * Unit test for Http2 Push cacheable
 */
public class WebTest {

    private static String TEST_NAME = "servlet-4.0-push-cacheable";
    private static String EXPECTED_PUSH_BODY = "Hello...";
    private static String EXPECTED_BODY = "test2";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private int port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = Integer.parseInt(args[1]);
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for http2 push cacheable");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invoke();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }


    private void invoke() throws Exception {
        String path = contextRoot + "/test";

        try (HttpClient httpClient = HttpClient.builder().
                host(host).port(port).build()) {
            httpClient.request().path(path).build().send();
            HttpResponse httpResponse = httpClient.getHttpResponse();
            HttpResponse httpResponse2 = httpClient.getHttpResponse();
            if (!verify(httpResponse) || !verify(httpResponse2)) {
                throw new Exception("Incorrect result");
            }
        }
    }

    private boolean verify(HttpResponse response) {
        if (response == null) {
            System.out.println("--> response is null");
            return false;
        }

        boolean push = response.isPush();
        if (push) {
            HttpPushPromise pushPromise = response.getHttpPushPromise();
            System.out.println(pushPromise);
            String testHeader = pushPromise.getHeader("test");
            if (!"gf".equals(testHeader)) {
                System.out.println("--> push promise header: gf = " + testHeader);
                return false;
            }
        }

        String body = response.getBody().trim();
        System.out.println("--> headers: " + response.getHeaders());
        System.out.println("--> body: " + body);
        return (push ? EXPECTED_PUSH_BODY.equals(body) : body.contains(EXPECTED_BODY));
    }
}
