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

package com.acme;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import jakarta.annotation.Resource;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static final String TEST_NAME = "normal-lookup-in-ear";

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter(
            "appserv-tests");

    private static String appName;
    private String host;
    private String port;

    @Resource(lookup = "java:app/env/value1")
    private static Integer appLevelViaLookup;

    public static void main(String args[]) {
        appName = args[0];
        stat.addDescription(appName);
        Client client = new Client(args);
        client.doTest();
        stat.printSummary(appName + "ID");
        System.out.println("appLevelViaLookup = '" + appLevelViaLookup + "'");
    }

    public Client(String[] args) {
        host = args[1];
        port = args[2];
    }

    public void doTest() {

        try {

            String url = "http://" + host + ":" + port + "/" + appName
                    + "/HelloServlet";

            System.out.println("invoking webclient servlet at " + url);

            URL u = new URL(url);

            HttpURLConnection c1 = (HttpURLConnection) u.openConnection();
            int code = c1.getResponseCode();
            InputStream is = c1.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    is));
            String line = null;
            while ((line = input.readLine()) != null) {
                System.out.println("<response>:" + line);
                if (line.trim().length() > 0) {
                    stat.addStatus(TEST_NAME, stat.FAIL);
                    return;
                }
            }
            if (code != 200) {
                stat.addStatus(TEST_NAME, stat.FAIL);
                return;
            }
            stat.addStatus(TEST_NAME, stat.PASS);

        } catch (Exception e) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            e.printStackTrace();
        }
    }

}
