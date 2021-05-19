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
 * Issue: GLASSFISH-17413
 */
public class WebTest {

    private static final String TEST_NAME = "web-default-servlet-with-subdirectory-mapping";

    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.

        stat.addDescription("DefaultServlet with sub-directory mapping.");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;

        try {
            boolean ok = goGet(host, port, "From index.jsp", contextRoot + "/index.jsp" );
            ok = ok && goGet(host, port, "From test.txt", contextRoot + "/static/test.txt");

            stat.addStatus(TEST_NAME,
                    ((ok)? stat.PASS : stat.FAIL));
        } catch (Throwable t) {
            t.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary(TEST_NAME + "---> expect 1 PASS");
    }

    private static boolean goGet(String host, int port,
                              String expectedResult, String contextPath)
            throws Exception {
        boolean ok = false;
        try{
            Socket s = new Socket(host, port);
            OutputStream os = s.getOutputStream();

            System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
            os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
            os.write("\n".getBytes());

            InputStream is = s.getInputStream();
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            int index=0, lineNum=0;
            String cookies = "";
            while ((line = bis.readLine()) != null) {
                System.out.println(lineNum + ": " + line);
                lineNum++;
                if (expectedResult.equals(line)) {
                    ok = true;
                    break;
                }
            }
        } catch( Exception ex){
            ex.printStackTrace();
            throw new Exception("Test UNPREDICTED-FAILURE");
        }

        return ok;
   }

}
