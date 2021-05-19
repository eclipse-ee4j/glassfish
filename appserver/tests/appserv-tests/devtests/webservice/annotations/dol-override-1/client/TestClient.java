/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class TestClient {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public boolean found1 = false;
    public boolean found2 = false;

    public static void main (String[] args) {
        stat.addDescription("webservices-descoverride-annotation");
        TestClient client = new TestClient();
        client.doTest(args);
        stat.printSummary("webservices-annotation");
    }

    public void doTest(String[] args) {

        String url = args[0];
        try {
            int code = invokeServlet(url);
            report(code);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private int invokeServlet(String url) throws Exception {
        log("Invoking url = " + url);
        URL u = new URL(url);
        HttpURLConnection c1 = (HttpURLConnection)u.openConnection();
        int code = c1.getResponseCode();
        InputStream is = c1.getInputStream();
        BufferedReader input = new BufferedReader (new InputStreamReader(is));
        String line = null;
        while ((line = input.readLine()) != null) {
            log(line);
            if(line.indexOf("So the RESULT OF SUBTRACT SERVICE IS") != -1)
                found1 = true;
            if(line.indexOf("[1111]") != -1)
                found2 = true;
        }
        return code;
    }

    private void report(int code) {
        if(code != 200) {
            log("Incorrect return code: " + code);
            fail();
        }
        if(!found1) {
            fail();
        }
        if(!found2) {
            fail();
        }
        pass();
    }

    private void log(String message) {
        System.out.println("[client.TestClient]:: " + message);
    }

    private void pass() {
        stat.addStatus("DOL-overiding-of-annotation", stat.PASS);
    }

    private void fail() {
        stat.addStatus("DOL-overiding-of-annotation", stat.FAIL);
    }
}
