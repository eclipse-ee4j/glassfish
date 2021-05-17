/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * Test HttpServletResponse#reset and #setCharacterEncoding.
 */
public class WebTest {

    private static final String TEST_NAME = "servlet-3.1-reset-character-encoding";
    private static final String EXPECTED_CONTENT_TYPE = "Content-Type: text/plain;charset=Big5";
    private static final String EXPECTED_RESPONSE="Done";

    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {

        stat.addDescription("Test setCharacterEncoding after reset");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        try {
            boolean result = goGet(host, port, contextRoot + "/test", EXPECTED_CONTENT_TYPE);
            boolean result2 = goGet(host, port, contextRoot + "/test2", null);
            stat.addStatus(TEST_NAME, ((result && result2)? stat.PASS : stat.FAIL));
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    private static boolean goGet(String host, int port, String contextPath,
            String expectedContentType) throws Exception {

        boolean result = false;
        boolean hasExpectedContentType = (expectedContentType == null);
        boolean hasDone = false;
        Socket s = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;

        try{
            s = new Socket(host, port);
            os = s.getOutputStream();
            String getPath = "GET " + contextPath + " HTTP/1.0\n";
            System.out.println(getPath);
            os.write(getPath.getBytes());
            os.write("\n".getBytes());

            is = s.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            int lineNum=0;
            while ((line = bis.readLine()) != null) {
                System.out.println(lineNum + ": " + line);
                if (expectedContentType == null && line.startsWith("Content-Type:")) {
                    hasExpectedContentType = false;
                } else if (expectedContentType != null && line.equals(expectedContentType)) {
                    hasExpectedContentType = true;
                } else if (EXPECTED_RESPONSE.equals(line)) {
                    hasDone = true;
                }
                lineNum++;
            }

            result = hasExpectedContentType && hasDone;

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
        }

        return result;
    }
}
