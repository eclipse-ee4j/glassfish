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
/**
 * Fix for CR 6947296
 */
public class WebTest {

    private static final String TEST_NAME = "change-session-id-on-authentication";
    private static int EXPECTED_COUNT = 1;

    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {

        stat.addDescription("Change session id on authentication");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();

        try {
            String result = goGet(host, port, contextRoot + "/welcome.jsp",
                   null, false);
            String result2 = null;
            if (result != null && result.length() > 0) {
                result2 = goGet(host, port, contextRoot + "/secure.jsp",
                        result, true);
            }

            stat.addStatus(TEST_NAME,
                    (result != null && result.length() > 0 &&
                    result2 != null && result2.length() > 0 &&
                    !result.equals(result2)) ?
                    stat.PASS : stat.FAIL);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    private static String goGet(String host, int port,
             String contextPath, String sessionId, boolean requireAuthentication) throws Exception {
        String result = null;
        Socket s = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;

        try{
            s = new Socket(host, port);
            os = s.getOutputStream();
            System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
            os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
            if (requireAuthentication) {
                os.write("Authorization: Basic amF2YWVlOmphdmFlZQ==\n".getBytes());
            }
            if (sessionId != null) {
                os.write(("Cookie: JSESSIONID=" + sessionId + "\n").getBytes());
            }
            os.write("\n".getBytes());

            is = s.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            int lineNum=0;
            while ((line = bis.readLine()) != null) {
                int index = line.indexOf("sessionId=");
                System.out.println(lineNum + ":  " + line);
                if (index != -1) {
                     result = line.substring(index + 10);
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

            System.out.println("session id = " + result);
            return result;
        }

   }

}
