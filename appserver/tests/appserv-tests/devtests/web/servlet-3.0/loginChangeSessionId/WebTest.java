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
import java.util.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;
/**
 * Test login change session id in Servlet 3.0
 */
public class WebTest {

    private static final String TEST_NAME = "servlet-3.0-login-change-session-id";
    private static final String JSESSIONID = "JSESSIONID";
    private static final String EXPECTED_RESPONSE = "one";

    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {

        stat.addDescription("Change session id");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();

        try {
            List<String> result = goGet(host, port, contextRoot + "/test?run=first",
                   null);
            List<String> result2 = null;
            if (result.size() > 0) {
                result2 = goGet(host, port, contextRoot + "/test?run=second",
                        result.get(0));
            }

            stat.addStatus(TEST_NAME,
                    (result.size() > 1 && result2.size() > 1
                    && !result.get(0).equals(result2.get(0))
                    && EXPECTED_RESPONSE.equals(result2.get(1))) ?
                    stat.PASS : stat.FAIL);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    private static List<String> goGet(String host, int port,
             String contextPath, String sessionId) throws Exception {
        List<String> result = new ArrayList<String>();
        Socket s = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;

        try{
            s = new Socket(host, port);
            os = s.getOutputStream();
            System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
            os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
            if (sessionId != null) {
                os.write(("Cookie: JSESSIONID=" + sessionId + "\n").getBytes());
            }
            os.write("\n".getBytes());

            is = s.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            int lineNum=0;
            while ((line = bis.readLine()) != null) {
                System.out.println(lineNum + ":  " + line);
                int index = line.indexOf(JSESSIONID);
                if (index != -1) {
                    result.add(getSessionIdFromCookie(line));
                } else if (line.startsWith("A=")) {
                    result.add(line.substring(2));

                }
                lineNum++;
            }
        } catch( Exception ex){
            ex.printStackTrace();
            throw new Exception("Test UNPREDICTED-FAILURE");
         } finally {
            try {
                if (os != null) os.close();
            } catch (IOException ex) {}
            try {
                if (s != null) s.close();
            } catch (IOException ex) {}
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}

            System.out.println("result= " + result);
            return result;
        }
    }

    private static String getSessionIdFromCookie(String cookie) {

        String ret = null;

        int index = cookie.indexOf(JSESSIONID + "=");
        if (index != -1) {
            int startIndex = index + JSESSIONID.length() + 1;
            int endIndex = cookie.indexOf(';', index);
            if (endIndex != -1) {
                ret = cookie.substring(startIndex, endIndex);
            } else {
                ret = cookie.substring(startIndex);
            }
            ret = ret.trim();
        }

        return ret;
    }

}
