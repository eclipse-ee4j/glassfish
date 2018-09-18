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

public class WebTest {
    static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", "default-keep-alive");

    public static void main(String args[]) {
        stat.addDescription("Default keep-alive test");
        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];
        int port = new Integer(portS);
        String name;
        try {
            goGet(host, port, "/");
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }
        stat.printSummary();
    }

    private static void goGet(String host, int port,
        String contextPath) throws Exception {
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();
        System.out.println("GET " + contextPath + " HTTP/1.1\n");
        os.write(("GET " + contextPath + " HTTP/1.1\n").getBytes());
        os.write("Host: localhost\n".getBytes());
        os.write("\n".getBytes());
        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;
        final long start = System.currentTimeMillis();
        try {
            int index;
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            final long end = System.currentTimeMillis();
            final long duration = end - start;
            stat.addStatus("defaultKeepAlive", duration >= 30000 ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL);
            s.close();
        }
    }
}
