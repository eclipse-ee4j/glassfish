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
 * Unit test for Bugtraq 5027440 ("Impossible for webapp to override global
 * JspServlet settings").
 *
 * Note that for test "jsp-servlet-override-ieClassId" to work, JSP
 * precompilation must be turned off (see build.properties in this directory),
 * so that the value of the 'ieClassId' property is gotten from the JspServlet
 * (instead of from the JspC command line).
 */
public class WebTest {

    private static final String OBJECT_CLASSID = "ABCD";
    private static final String INCLUDED_RESPONSE = "This is included page";
    private static final String TEST_NAME = "jsp-servlet-override";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugtraq 5027440");
        WebTest webTest = new WebTest(args);
        try {
            webTest.overrideIeClassId();
            webTest.jspInclude();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    private void overrideIeClassId() throws Exception {
        URL url = new URL("http://" + host  + ":" + port +
            contextRoot + "/jsp/overrideIeClassId.jsp");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Wrong response code. Expected: 200" +
                ", received: " + responseCode);
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("<OBJECT")) {
                    break;
                }
            }

            if (line != null) {
                // Check <OBJECT> classid comment
                System.out.println(line);
                String classid = getAttributeValue(line, "classid");
                if (classid != null) {
                    if (!classid.equals(OBJECT_CLASSID)) {
                        throw new Exception("Wrong classid: " + classid +
                            ", expected: " + OBJECT_CLASSID);
                    }
                } else {
                    throw new Exception("Missing classid");
                }

            } else {
                throw new Exception("Missing OBJECT element in response body");
            }
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException ex) {}
        }
    }

    private void jspInclude() throws Exception {
        URL url = new URL("http://" + host  + ":" + port +
            contextRoot + "/jsp/include.jsp");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Wrong response code. Expected: 200" +
                ", received: " + responseCode);
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
            String line = br.readLine();
            if (!INCLUDED_RESPONSE.equals(line)) {
                throw new Exception("Wrong response. Expected: " +
                    INCLUDED_RESPONSE + ", received: " + filter(line));
            }
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException ex) {}
        }
    }

    private String getAttributeValue(String element, String attribute) {

        String ret = null;

        int index = element.indexOf(attribute);
        if (index != -1) {
            int beginIndex = index + attribute.length() + 2;
            int endIndex = element.indexOf('"', beginIndex);
            if (endIndex != -1) {
                ret = element.substring(beginIndex, endIndex);
            }
        }

        return ret;
    }

    private String filter(String message) {

        if (message == null)
            return (null);

        char content[] = new char[message.length()];
        message.getChars(0, message.length(), content, 0);
        StringBuffer result = new StringBuffer(content.length + 50);
        for (int i = 0; i < content.length; i++) {
            switch (content[i]) {
            case '<':
                result.append("&lt;");
                break;
            case '>':
                result.append("&gt;");
                break;
            case '&':
                result.append("&amp;");
                break;
            case '"':
                result.append("&quot;");
                break;
            default:
                result.append(content[i]);
            }
        }
        return (result.toString());

    }

}
