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
 * WeblogicDD
 *     container-descriptor:index-directory-enabled
 *     container-descriptor:index-directory-sort-by
 */
public class WebTest {

    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.

        stat.addDescription("WeblogicDD: index-directory-enabled, index-directory-sort-by");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;

        try {
            boolean ok = goGet(host, port, "", contextRoot + "/" );

            stat.addStatus("wl-web-directoryListing",
                    ((ok)? stat.PASS : stat.FAIL));
        } catch (Throwable t) {
            t.printStackTrace();
            stat.addStatus("wl-web-directoryListing", stat.FAIL);
        }

        stat.printSummary("wl-web/directoryListing---> expect a < c < b PASS");
    }

    private static boolean goGet(String host, int port,
                              String result, String contextPath)
            throws Exception {

        boolean listDir = false;
        int a = 0;
        int b = 0;
        int c = 0;
        try{
            Socket s = new Socket(host, port);
            OutputStream os = s.getOutputStream();

            System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
            os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
            os.write("\n".getBytes());

            InputStream is = s.getInputStream();
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            int lineNum=0;
            while ((line = bis.readLine()) != null) {
                System.out.println(lineNum + ": " + line);
                if (line.indexOf("Directory Listing") != -1){
                    listDir = true;
                }
                if (line.contains("testA.html")) {
                    a = lineNum;
                } else if (line.contains("testB.html")) {
                    b = lineNum;
                } else if (line.contains("testC.html")) {
                    c = lineNum;
                }
                lineNum++;
            }
            System.out.println("a, b, c: " + a + ", " + b + ", " + c);
        } catch( Exception ex){
            ex.printStackTrace();
            throw new Exception("Test UNPREDICTED-FAILURE");
        }

        return (listDir && (b < c) && (c < a));
   }

}
