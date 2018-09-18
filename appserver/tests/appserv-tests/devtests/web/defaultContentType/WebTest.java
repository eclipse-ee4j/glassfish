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

import java.net.HttpURLConnection;
import java.net.URL;

import com.sun.appserv.test.BaseDevTest;

/*
 * Unit test for 6328909
 */
public class WebTest extends BaseDevTest {
    private static final String EXPECTED_CONTENT_TYPE = "text/plain;charset=iso-8859-1";
    private String host;
    private int port;

    public WebTest() {
        host = antProp("http.host");
        port = Integer.valueOf(antProp("http.port"));
    }

    public static void main(String[] args) {
        new WebTest().doTest();
    }

    public void doTest() {
        try {
            invoke(null);
            report("no-content-type", invoke(null));
            report("set-content-type-value", asadmin("set",
                "configs.config.server-config.network-config.protocols.protocol.http-listener-1.http."
                    + "default-response-type=" + EXPECTED_CONTENT_TYPE));
            report("default-content-type", invoke(EXPECTED_CONTENT_TYPE));
            report("set-content-type-value", asadmin("set",
                "configs.config.server-config.network-config.protocols.protocol.http-listener-1.http."
                    + "default-response-type="));
            report("no-content-type-again", invoke(null));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            stat.printSummary();
        }
    }

    private boolean invoke(final String expected) throws Exception {
        URL url = new URL("http://" + host + ":" + port + "/test.xyz");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Wrong response code. Expected: 200, received: " + responseCode);
        }
        String contentType = conn.getHeaderField("Content-Type");
        return expected == null ? contentType == null : contentType.equals(expected);
    }

    @Override
    protected String getTestName() {
        return "default-content-type";
    }

    @Override
    protected String getTestDescription() {
        return "Tests that the default content type can be set correctly";
    }
}
