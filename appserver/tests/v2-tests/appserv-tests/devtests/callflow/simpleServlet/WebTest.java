/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

    private static SimpleReporterAdapter stat
            = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "callflow-simple-servlet";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        WebTest webTest = new WebTest(args);
        try {
            if (args.length == 5){
                if (args[3].equalsIgnoreCase("report")){
                    stat.addDescription("Callflow Simple Servlet Test");
                    webTest.analyseResult(args[4]);
                    stat.printSummary(TEST_NAME);

                }
            }else if (args.length == 4){
                if (args[3].equalsIgnoreCase ("clean-db")){
            webTest.cleandb ();
        }
            }else
                webTest.doTest();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    public void doTest() throws Exception {

        URL url = new URL("http://" + host  + ":" + port
                + contextRoot + "/SimpleServlet");
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {
            System.err.println("Unexpected return code: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
        }
    conn.disconnect ();
    }

    public void analyseResult(String result) throws Exception {

        URL url = new URL("http://" + host  + ":" + port
                +"/dbReader/dbReader?servletName=callflow-simple-servlet");
        System.out.println("Analysing Result .... Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {
            System.err.println("Unexpected return code: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
        System.out.println ("Expected Result :" + result);
            System.out.println ("Actual Result   :" + line);
        if(result.equals (line))
                stat.addStatus(TEST_NAME, stat.PASS);
        else
        stat.addStatus(TEST_NAME, stat.FAIL);

        }
    conn.disconnect ();
    }

    public void cleandb() throws Exception {

        URL url = new URL("http://" + host  + ":" + port
                +"/dbReader/dbReader?cleandb=true");
        System.out.println("Cleaning DB .... Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {
            System.err.println("Unexpected return code: " + responseCode);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
        System.out.println (line);
    }

    conn.disconnect ();
    }
}
