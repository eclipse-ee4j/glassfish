/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.security.jmac.https;

import java.io.*;
import java.util.regex.Pattern;
import java.net.*;
import javax.net.ssl.*;
import com.sun.ejte.ccl.reporter.*;

public class WebSSLClient {

    private static final String TEST_NAME = "security-jmac-https";
    private static final String EXPECTED_RESPONSE_PATTERN = "Hello, CN=.* from com.sun.s1asdev.security.jmac.https.HttpsTestAuthModule";
    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) throws Exception {

        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];

        System.out.println("host/port=" + host + "/" + port);

        try {
            stat.addDescription(TEST_NAME);
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            HttpsURLConnection connection = connect("https://" + host + ":"
                    + port + "/" + contextRoot
                    + "/index.jsp",
                    ssf);

            parseResponse(connection);

        } catch (Throwable t) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            t.printStackTrace();
        }
        stat.printSummary(TEST_NAME);
    }

    private static void parseResponse(HttpsURLConnection connection)
            throws Exception {

        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            String line = null;
            String matched = null;
            try {
                Pattern p = Pattern.compile(EXPECTED_RESPONSE_PATTERN);
                while ((line = in.readLine()) != null) {
                    if (p.matcher(line).matches()) {
                        stat.addStatus(TEST_NAME, stat.PASS);
                        matched = line;
                    }
                    System.out.println(line);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (matched == null) {
                System.err.println("Wrong response. Expected Pattern: "
                        + EXPECTED_RESPONSE_PATTERN
                        + ", received: " + matched);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private static HttpsURLConnection connect(String urlAddress,
            SSLSocketFactory ssf)
            throws Exception {

        URL url = new URL(urlAddress);
        HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setHostnameVerifier(
                new HostnameVerifier() {

                    public boolean verify(String rserver, SSLSession sses) {
                        return true;
                    }
                });

        connection.setDoOutput(true);

        return connection;
    }
}
