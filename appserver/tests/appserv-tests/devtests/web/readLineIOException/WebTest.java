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

    private static int count = 0;
    private static int EXPECTED_COUNT = 1;

    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    /**
     * Fix for Bugzilla Bug 28959 IOException using CoyoteReader.readLine()
     * but not using .read()
     */
    public static void main(String args[]) {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.

        stat.addDescription("Standalone test for Bugzilla 28959");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;

        try {
            goGet(host, port, "readline", contextRoot + "/ServletTest2" );

            if (count != EXPECTED_COUNT){
                stat.addStatus("readLineIOException UNPREDICTED-FAILURE", stat.FAIL);
            }
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            stat.addStatus("readLineIOException UNPREDICTED-FAILURE", stat.FAIL);
        }

        stat.printSummary("web/readLineIOException---> expect 1 PASS");
    }

    private static void goGet(String host, int port,
                              String result, String contextPath)
         throws Exception {
        Socket s = new Socket(host, port);
        s.setSoTimeout(0);
        OutputStream os = s.getOutputStream();

        System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
        os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
        os.write("\n".getBytes());

        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        try{
            int index, lineNum=0;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf("::");
                System.out.println(lineNum + ":  " + line);
                if (index != -1) {
                    String status = line.substring(index+2);

                    if (status.equalsIgnoreCase("PASSED")){
                        count++;
                    }
                }
                lineNum++;
             }
             if (count == 10){
                stat.addStatus("web-readLineIOException: readLine"
                                    , stat.PASS);
             } else {
                stat.addStatus("web-readLineIOException: readLine "
                                    , stat.FAIL);
             }
             count = 1;
        } catch( Exception ex){
            ex.printStackTrace();
         }
   }

}
