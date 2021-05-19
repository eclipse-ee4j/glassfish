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

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "jsp-config";

    public static void main(String args[]) {

        stat.addDescription("Standalone jsp-config war test");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];
        int port = new Integer(portS).intValue();

        try {
            goGet(host, port, "JSP-CONFIG", contextRoot + "/test.jsp" );
            goGet2(host, port, "ELIgnored", contextRoot + "/foo/test.jsp" );
            goGet2(host, port, "ELIgnored", contextRoot + "/foo/bar/test.jsp" );
            goGet2(host, port, "ELIgnored", contextRoot + "/foo/bar/baz/test.jsp" );
            goGet2(host, port, "ELIgnored", contextRoot + "/foo/bar/baz/test2.jsp" );
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Throwable t) {
            t.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    private static void goGet(String host, int port,
                              String result, String contextPath)
         throws Exception
    {
        Socket s = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;
        try {
            s = new Socket(host, port);
            os = s.getOutputStream();

            System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
            os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
            os.write("\n".getBytes());

            is = s.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            int index;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf(result);
                System.out.println(line);
                if (index != -1) {
                    index = line.indexOf(":");
                    String status = line.substring(index+1);
                    if (!status.equalsIgnoreCase("PASS")){
                        throw new Exception("Wrong response");
                    }
                }
            }
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
   }

    private static void goGet2(String host, int port,
                              String result, String contextPath)
         throws Exception
    {
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();

        System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
        os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
        os.write("\n".getBytes());

        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = bis.readLine()) != null) {
            int index = line.indexOf(result);
            System.out.println(line);
            if (index != -1) {
                index = line.indexOf(":");
                boolean ELAllowed = line.charAt(index+1) == 't';
                boolean ELSeen = line.indexOf("${") >= 0;
                if ((ELSeen && !ELAllowed) || (!ELSeen && ELAllowed)) {
                    throw new Exception("Wrong response");
                }
            }
        }
    }
}
