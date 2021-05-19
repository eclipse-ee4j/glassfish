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
 * Unit test for 6646921 ("@PreDestroy method not called in jsp tag
 * handler")
 */
public class WebTest {

    private static final String TEST_NAME =
        "jsp-tag-handler-pre-destroy-annotation";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private String numRun;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        numRun = args[3];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for @PreDestroy on JSP tag handler");

        WebTest test = new WebTest(args);

        try {
            if ("first".equals(test.numRun)) {
                test.runFirst();
            } else {
                test.runSecond();
            }
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void runFirst() throws Exception {
        URL url = new URL("http://" + host  + ":" + port
                          + contextRoot + "/jsp/test.jsp");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Wrong response code. Expected: 200" +
                                ", received: " + responseCode);
        }
    }

    public void runSecond() throws Exception {
        File inFile = new File("/tmp/mytest");
        FileInputStream fis = new FileInputStream(inFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line = br.readLine();
        br.close();
        inFile.delete();
        if (!"SUCCESS".equals(line)) {
            throw new Exception("File contents that were supposed to have " +
                                "been written by @PreDestroy method " +
                                "not found");
        }
    }
}
