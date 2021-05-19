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
 * Bug 6172839
 */
public class WebTest {

    private static int EXPECTED_COUNT = 1;

    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.

        stat.addDescription("Standalone Session Invalid war test");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;

        try {
            boolean ok = goGet(host, port, "", contextRoot + "/" );

            stat.addStatus("web-directoryListing",
                    ((ok)? stat.PASS : stat.FAIL));
        } catch (Throwable t) {
            t.printStackTrace();
            stat.addStatus("web-directoryListing", stat.FAIL);
        }

        stat.printSummary("web/directoryListing---> expect 1 PASS");
    }

    private static boolean goGet(String host, int port,
                              String result, String contextPath)
            throws Exception {
        int count = 0;
        try{
            Socket s = new Socket(host, port);
            OutputStream os = s.getOutputStream();

            System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
            os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
            os.write("Authorization: Basic ajJlZTpqMmVl\n".getBytes());
            os.write("\n".getBytes());

            InputStream is = s.getInputStream();
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            int index=0, lineNum=0;
            String cookies = "";
            while ((line = bis.readLine()) != null) {
                System.out.println(lineNum + ": " + line);
                if (line.indexOf("Directory Listing") != -1){
                    System.out.println("Getting a \"Directory Listing\"");
                    count++;
                    break;
                }
                lineNum++;
            }
        } catch( Exception ex){
            ex.printStackTrace();
            throw new Exception("Test UNPREDICTED-FAILURE");
        }

        return (count == EXPECTED_COUNT);
   }

}
