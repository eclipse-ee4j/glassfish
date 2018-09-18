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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import com.sun.appserv.test.util.results.SimpleReporterAdapter;

public class WebTest extends Thread {
    private static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", "dosBrokenPost");

    public static void main(String args[]) {
        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
        stat.addDescription("Double content-length header");
        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];
        int port = new Integer(portS);
        try {
            goGet(host, port, contextRoot + "/ServletTest");
        } catch (Throwable t) {
        }
        stat.printSummary();
    }

    private static void goGet(String host, int port,
        String contextPath)
        throws Exception {
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();
        os.write(("POST " + contextPath + " HTTP/1.1\n").getBytes());
        os.write("Host: localhost\r".getBytes());
        os.write("content-length: 0\r".getBytes());
        os.write("content-length: 10\r".getBytes());
        os.write("content-type: application/x-www-form-urlencoded\r".getBytes());
        os.write("a\r\n".getBytes());
        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line;
        try {
            int i = 0;
            while ((line = bis.readLine()) != null) {
                System.out.println(i + ":" + line);
                i++;
            }
            if (i == 0) {
                stat.addStatus("dosBrokenPost", SimpleReporterAdapter.PASS);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
