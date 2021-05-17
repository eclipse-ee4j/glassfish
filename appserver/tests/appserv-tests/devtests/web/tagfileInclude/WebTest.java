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

import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest {

    static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    static final String TEST_NAME = "tagfile-include";

    public static void main(String args[]) {

        stat.addDescription("Test that static include works with a taglib " +
            "directive for tag files");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];
        int port = new Integer(portS).intValue();

        try {
            goGet(host, port, contextRoot + "/test.jsp" );
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Throwable t) {
            t.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    private static void goGet(String host, int port, String uri)
         throws Exception
    {

        URL url = new URL("http://" + host  + ":" + port + uri);
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Unexpected response code. Expected: 200, " +
                "received: " + responseCode);
        }

        BufferedReader br = null;
        boolean pass = false;
        try {
            InputStream is = conn.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.indexOf("PASS") >= 0) {
                    pass = true;
                    break;
                }
            }
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        if (!pass) {
            throw new Exception("Response did not contain PASS string");
        }
    }
}
