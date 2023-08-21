/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jacc.test.uncoveredmethods;

import java.net.*;
import java.io.*;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Hashtable;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");
    private static String testSuite = "Security::UncoveredHTTPMethods";
    private static String contextPathOpen = "/open";
    private static String contextPathDeny = "/deny";

    private String host;
    private String port;
    private String username;
    private String password;

    public static void main(String[] args) {
        stat.addDescription(testSuite);
        Client client = new Client(args);
        client.doTests();
        stat.printSummary();
    }

    public Client(String[] args) {
        host = args[0];
        port = args[1];
        username = args[2];
        password = args[3];
        System.out.println("      Host: " + host);
        System.out.println("      Port: " + port);
        System.out.println("  Username: " + username);
    }

    public void doTests() {
        testExample1();
        testExample1Put();
        testExample2();
        testExample2Delete();
        testExample3a();
        testExample3aPut();
        testExample3bPost();
        testExample3bDelete();
        testCovered1Post();
        testCovered1Put();
        testCovered2();
        testCovered2Put();
        testCovered3aPost();
        testCovered3aDelete();
        testCovered3b();
        testCovered3bPut();
    }

    public void testExample1() {
        String servlet = "/Example1";
        String descriptionOpen = contextPathOpen + servlet;
        String descriptionDeny = contextPathDeny + servlet;

        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, null, 302, username, contextPathOpen, output);
        int index = output.indexOf("https://");
        if (success && (index != -1)) {
            stat.addStatus(descriptionOpen, stat.PASS);
        } else
            stat.addStatus(descriptionOpen, stat.FAIL);

        output = new StringBuffer();
        success = doIndividualTest(servlet, null, 302, username, contextPathDeny, output);
        if (success)
            stat.addStatus(descriptionDeny, stat.PASS);
        else
            stat.addStatus(descriptionDeny, stat.FAIL);
    }

    public void testExample1Put() {
        String servlet = "/Example1";
        String method = "PUT";
        String descriptionOpen = contextPathOpen + servlet + "-" + method;
        String descriptionDeny = contextPathDeny + servlet + "-" + method;

        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, method, 200, username, contextPathOpen, output);
        if (success) {
            stat.addStatus(descriptionOpen, stat.PASS);
        } else
            stat.addStatus(descriptionOpen, stat.FAIL);

        output = new StringBuffer();
        success = doIndividualTest(servlet, method, 403, username, contextPathDeny, output);
        if (success)
            stat.addStatus(descriptionDeny, stat.PASS);
        else
            stat.addStatus(descriptionDeny, stat.FAIL);
    }

    public void testExample2() {
        String servlet = "/Example2";
        String descriptionOpen = contextPathOpen + servlet;
        String descriptionDeny = contextPathDeny + servlet;

        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, null, 200, username, contextPathOpen, output);
        if (success) {
            stat.addStatus(descriptionOpen, stat.PASS);
        } else
            stat.addStatus(descriptionOpen, stat.FAIL);

        output = new StringBuffer();
        success = doIndividualTest(servlet, null, 403, username, contextPathDeny, output);
        if (success)
            stat.addStatus(descriptionDeny, stat.PASS);
        else
            stat.addStatus(descriptionDeny, stat.FAIL);
    }

    public void testExample2Delete() {
        String servlet = "/Example2";
        String method = "DELETE";
        String descriptionOpen = contextPathOpen + servlet + "-" + method;
        String descriptionDeny = contextPathDeny + servlet + "-" + method;

        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, method, 403, username, contextPathOpen, output);
        if (success) {
            stat.addStatus(descriptionOpen, stat.PASS);
        } else
            stat.addStatus(descriptionOpen, stat.FAIL);

        output = new StringBuffer();
        success = doIndividualTest(servlet, method, 403, username, contextPathDeny, output);
        if (success)
            stat.addStatus(descriptionDeny, stat.PASS);
        else
            stat.addStatus(descriptionDeny, stat.FAIL);
    }

    public void testExample3a() {
        String servlet = "/Example3a";
        String descriptionOpen = contextPathOpen + servlet;
        String descriptionDeny = contextPathDeny + servlet;

        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, null, 200, username, contextPathOpen, output);
        if (success) {
            stat.addStatus(descriptionOpen, stat.PASS);
        } else
            stat.addStatus(descriptionOpen, stat.FAIL);

        output = new StringBuffer();
        success = doIndividualTest(servlet, null, 200, username, contextPathDeny, output);
        if (success)
            stat.addStatus(descriptionDeny, stat.PASS);
        else
            stat.addStatus(descriptionDeny, stat.FAIL);
    }

    public void testExample3aPut() {
        String servlet = "/Example3a";
        String method = "PUT";
        String descriptionOpen = contextPathOpen + servlet + "-" + method;
        String descriptionDeny = contextPathDeny + servlet + "-" + method;

        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, method, 200, username, contextPathOpen, output);
        if (success) {
            stat.addStatus(descriptionOpen, stat.PASS);
        } else
            stat.addStatus(descriptionOpen, stat.FAIL);

        output = new StringBuffer();
        success = doIndividualTest(servlet, method, 403, username, contextPathDeny, output);
        if (success)
            stat.addStatus(descriptionDeny, stat.PASS);
        else
            stat.addStatus(descriptionDeny, stat.FAIL);
    }

    public void testExample3bPost() {
        String servlet = "/Example3b";
        String method = "POST";
        String descriptionOpen = contextPathOpen + servlet + "-" + method;
        String descriptionDeny = contextPathDeny + servlet + "-" + method;

        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, method, 200, username, contextPathOpen, output);
        if (success) {
            stat.addStatus(descriptionOpen, stat.PASS);
        } else
            stat.addStatus(descriptionOpen, stat.FAIL);

        output = new StringBuffer();
        success = doIndividualTest(servlet, method, 200, username, contextPathDeny, output);
        if (success)
            stat.addStatus(descriptionDeny, stat.PASS);
        else
            stat.addStatus(descriptionDeny, stat.FAIL);
    }

    public void testExample3bDelete() {
        String servlet = "/Example3b";
        String method = "DELETE";
        String descriptionOpen = contextPathOpen + servlet + "-" + method;
        String descriptionDeny = contextPathDeny + servlet + "-" + method;

        StringBuffer output = new StringBuffer();
        boolean success = false;
        
        success = doIndividualTest(servlet, method, 200, username, contextPathOpen, output);
        if (success) {
            stat.addStatus(descriptionOpen, stat.PASS);
        } else {
            stat.addStatus(descriptionOpen, stat.FAIL);
        }

        output = new StringBuffer();
        success = doIndividualTest(servlet, method, 403, username, contextPathDeny, output);
        if (success) {
            stat.addStatus(descriptionDeny, stat.PASS);
        } else {
            stat.addStatus(descriptionDeny, stat.FAIL);
        }
    }

    public void testCovered1Post() {
        String servlet = "/Covered1";
        String method = "POST";
        String descriptionOpen = contextPathOpen + servlet + "-" + method;
        String descriptionDeny = contextPathDeny + servlet + "-" + method;

        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, method, 302, username, contextPathOpen, output);
        int index = output.indexOf("https://");
        if (success && (index != -1)) {
            stat.addStatus(descriptionOpen, stat.PASS);
        } else
            stat.addStatus(descriptionOpen, stat.FAIL);

        output = new StringBuffer();
        success = doIndividualTest(servlet, method, 302, username, contextPathDeny, output);
        if (success)
            stat.addStatus(descriptionDeny, stat.PASS);
        else
            stat.addStatus(descriptionDeny, stat.FAIL);
    }

    public void testCovered1Put() {
        String servlet = "/Covered1";
        String method = "PUT";
        String descriptionOpen = contextPathOpen + servlet + "-" + method;
        String descriptionDeny = contextPathDeny + servlet + "-" + method;

        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, method, 302, username, contextPathOpen, output);
        int index = output.indexOf("https://");
        if (success && (index != -1)) {
            stat.addStatus(descriptionOpen, stat.PASS);
        } else
            stat.addStatus(descriptionOpen, stat.FAIL);

        output = new StringBuffer();
        success = doIndividualTest(servlet, method, 302, username, contextPathDeny, output);
        if (success)
            stat.addStatus(descriptionDeny, stat.PASS);
        else
            stat.addStatus(descriptionDeny, stat.FAIL);
    }

    public void testCovered2() {
        String servlet = "/Covered2";
        String descriptionOpen = contextPathOpen + servlet;
        String descriptionDeny = contextPathDeny + servlet;

        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, null, 302, username, contextPathOpen, output);
        int index = output.indexOf("https://");
        if (success && (index != -1)) {
            stat.addStatus(descriptionOpen, stat.PASS);
        } else
            stat.addStatus(descriptionOpen, stat.FAIL);

        output = new StringBuffer();
        success = doIndividualTest(servlet, null, 302, username, contextPathDeny, output);
        if (success)
            stat.addStatus(descriptionDeny, stat.PASS);
        else
            stat.addStatus(descriptionDeny, stat.FAIL);
    }

    public void testCovered2Put() {
        String servlet = "/Covered2";
        String method = "PUT";
        String descriptionOpen = contextPathOpen + servlet + "-" + method;
        String descriptionDeny = contextPathDeny + servlet + "-" + method;

        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, method, 403, username, contextPathOpen, output);
        if (success) {
            stat.addStatus(descriptionOpen, stat.PASS);
        } else
            stat.addStatus(descriptionOpen, stat.FAIL);

        output = new StringBuffer();
        success = doIndividualTest(servlet, method, 403, username, contextPathDeny, output);
        if (success)
            stat.addStatus(descriptionDeny, stat.PASS);
        else
            stat.addStatus(descriptionDeny, stat.FAIL);
    }

    public void testCovered3aPost() {
        String servlet = "/Covered3a";
        String method = "POST";
        String descriptionOpen = contextPathOpen + servlet + "-" + method;
        String descriptionDeny = contextPathDeny + servlet + "-" + method;

        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, method, 200, username, contextPathOpen, output);
        if (success) {
            stat.addStatus(descriptionOpen, stat.PASS);
        } else
            stat.addStatus(descriptionOpen, stat.FAIL);

        output = new StringBuffer();
        success = doIndividualTest(servlet, method, 200, username, contextPathDeny, output);
        if (success)
            stat.addStatus(descriptionDeny, stat.PASS);
        else
            stat.addStatus(descriptionDeny, stat.FAIL);
    }

    public void testCovered3aDelete() {
        String servlet = "/Covered3a";
        String method = "DELETE";
        String descriptionOpen = contextPathOpen + servlet + "-" + method;
        String descriptionDeny = contextPathDeny + servlet + "-" + method;

        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, method, 200, username, contextPathOpen, output);
        if (success) {
            stat.addStatus(descriptionOpen, stat.PASS);
        } else {
            stat.addStatus(descriptionOpen, stat.FAIL);
        }

        output = new StringBuffer();
        success = doIndividualTest(servlet, method, 200, username, contextPathDeny, output);
        if (success) {
            stat.addStatus(descriptionDeny, stat.PASS);
        } else {
            stat.addStatus(descriptionDeny, stat.FAIL);
        }
    }

    public void testCovered3b() {
        String servlet = "/Covered3b";
        String descriptionOpen = contextPathOpen + servlet;
        String descriptionDeny = contextPathDeny + servlet;

        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, null, 302, username, contextPathOpen, output);
        int index = output.indexOf("https://");
        if (success && (index != -1)) {
            stat.addStatus(descriptionOpen, stat.PASS);
        } else
            stat.addStatus(descriptionOpen, stat.FAIL);

        output = new StringBuffer();
        success = doIndividualTest(servlet, null, 302, username, contextPathDeny, output);
        if (success)
            stat.addStatus(descriptionDeny, stat.PASS);
        else
            stat.addStatus(descriptionDeny, stat.FAIL);
    }

    public void testCovered3bPut() {
        String servlet = "/Covered3b";
        String method = "PUT";
        String descriptionOpen = contextPathOpen + servlet + "-" + method;
        String descriptionDeny = contextPathDeny + servlet + "-" + method;

        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, method, 302, username, contextPathOpen, output);
        int index = output.indexOf("https://");
        if (success && (index != -1)) {
            stat.addStatus(descriptionOpen, stat.PASS);
        } else
            stat.addStatus(descriptionOpen, stat.FAIL);

        output = new StringBuffer();
        success = doIndividualTest(servlet, method, 302, username, contextPathDeny, output);
        if (success)
            stat.addStatus(descriptionDeny, stat.PASS);
        else
            stat.addStatus(descriptionDeny, stat.FAIL);
    }

    private boolean doIndividualTest(String servlet, String method, int code, String user, String context, StringBuffer output) {
        boolean result = false;
        
        try {
            int rtncode;
            String url = "http://" + host + ":" + port + context + servlet;
            System.out.println("\nInvoking servlet at " + url);
            
            rtncode = invokeServlet(url, method, user, output);
            
            System.out.println("The servlet return code: " + rtncode);
            if (rtncode != code) {
                System.out.println("Incorrect return code, expecting: " + code);
            } else {
                result = true;
            }
            
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.toString());
        }
        
        return result;
    }

    private int invokeServlet(String url, String method, String user, StringBuffer output) throws Exception {
        String httpMethod = "GET";
        if ((method != null) && (method.length() > 0)) {
            httpMethod = method;
        }
        
        System.out.println("Invoking servlet with HTTP method: " + httpMethod);
        
        URL u = new URL(url);
        HttpURLConnection c1 = (HttpURLConnection) u.openConnection();
        c1.setRequestMethod(httpMethod);
        
        if ((user != null) && (user.length() > 0)) {
            // Add BASIC header for authentication
            String auth = user + ":" + password;
            String authEncoded = Base64.getEncoder().encodeToString(auth.getBytes());
            c1.setRequestProperty("Authorization", "Basic " + authEncoded);
        }
        c1.setUseCaches(false);

        // Connect and get the response code and/or output to verify
        c1.connect();
        int code = c1.getResponseCode();
        if (code == HttpURLConnection.HTTP_OK) {
            InputStream is = null;
            BufferedReader input = null;
            String line = null;
            try {
                is = c1.getInputStream();
                input = new BufferedReader(new InputStreamReader(is));
                while ((line = input.readLine()) != null) {
                    output.append(line);
                    System.out.println(line);
                }
            } finally {
                try {
                    if (is != null)
                        is.close();
                } catch (Exception exc) {
                }
                try {
                    if (input != null)
                        input.close();
                } catch (Exception exc) {
                }
            }
        } else if (code == HttpURLConnection.HTTP_MOVED_TEMP) {
            URL redir = new URL(c1.getHeaderField("Location"));
            String line = "Servlet redirected to: " + redir.toString();
            output.append(line);
            System.out.println(line);
        }
        
        return code;
    }
}
