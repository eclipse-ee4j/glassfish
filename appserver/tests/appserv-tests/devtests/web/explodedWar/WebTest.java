/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
    private static String EXPECTED_RESPONSE = "PASS PASS SCInit-OK";
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {

        stat.addDescription("IT 11802: exploded war support");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;

        try {
            goGet(host, port, contextRoot + "/ServletTest" );
        } catch (Throwable t) {
            t.printStackTrace();
        }

        stat.printSummary("exploded-war");
    }

    private static void goGet(String host, int port,
                              String contextPath) throws Exception {
        Socket s = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;
        try{
            long time = System.currentTimeMillis();
            s = new Socket(host, port);
            os = s.getOutputStream();

            String getString = "GET " + contextPath + " HTTP/1.0\n";
            System.out.println(getString);
            os.write(getString.getBytes());
            os.write("\n".getBytes());

            is = s.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            boolean expected = false;
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
                if (EXPECTED_RESPONSE.equals(line)) {
                    expected = true;
                }
            }
            stat.addStatus("exploded-war", expected ? stat.PASS : stat.FAIL);
        } catch( Exception ex){
            ex.printStackTrace();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
            if (s != null) {
                s.close();
            }
        }
   }

}
