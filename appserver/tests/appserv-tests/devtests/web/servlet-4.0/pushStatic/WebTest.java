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
 * Unit test for Http2 Push static file
 */
public class WebTest {

    private static String TEST_NAME = "servlet-4.0-push-static";
    private static String EXPECTED_PUSH_BODY = "body { color: yellow; }";
    private static String EXPECTED_BODY = "my.css";

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
        stat.addDescription("Unit test for http2 push static file");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try (HttpClient httpClient = HttpClient.builder().
                host(host).port(port).build()) {
            httpClient.request().path(contextRoot + "/test").build().send();
            HttpResponse httpResponse = httpClient.getHttpResponse();
            HttpResponse httpResponse2 = httpClient.getHttpResponse();
            if (!verify(httpResponse) || !verify(httpResponse2)) {
                throw new Exception("Incorrect result");
            }
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch(Throwable t) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            t.printStackTrace();
        }
    }

    private boolean verify(HttpResponse response) {
        if (response == null) {
            System.out.println("--> response is null");
            return false;
        }

        String body = response.getBody().trim();
        System.out.println("--> body: " + body);
        return (response.isPush() ? EXPECTED_PUSH_BODY.equals(body) : body.contains(EXPECTED_BODY));
    }
}
