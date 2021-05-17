/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

    private static final String TEST_NAME = "servlet-3.1-servlet-context-get-virtual-server-name";

    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {

        stat.addDescription("Test ServletContext.getVirtualServerName");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];
        String virtualServerName = args[3];

        int port = new Integer(portS).intValue();
        try {
            boolean result = goGet(host, port, contextRoot + "/test", virtualServerName);
            stat.addStatus(TEST_NAME, (result)? stat.PASS : stat.FAIL);
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    private static boolean goGet(String host, int port, String contextPath,
            String expectedResult) throws Exception {

        boolean result = false;

        try (Socket sock = new Socket(host, port);
             OutputStream os = sock.getOutputStream();
             InputStream is = sock.getInputStream();
             BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        ) {
            String getPath = "GET " + contextPath + " HTTP/1.0\n";
            System.out.println(getPath);
            os.write(getPath.getBytes());
            os.write("\n".getBytes());

            String line = null;

            int lineNum=0;
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
                if (line.equals(expectedResult)) {
                    result = true;
                    break;
                }
            }

        }

        return result;
    }
}
