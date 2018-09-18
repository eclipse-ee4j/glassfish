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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

import com.sun.appserv.test.util.results.SimpleReporterAdapter;

/*
* Unit test for 6273998
*/
public class WebTest {
    public static final String TEST_NAME = "keepAliveTimeout";
    private static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", TEST_NAME);
    private String host;
    private String port;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
    }

    public static void main(String[] args) {
        stat.addDescription(TEST_NAME);
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary();
    }

    public void doTest() {
        try {
            invoke();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, SimpleReporterAdapter.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {
        Socket sock = new Socket(host, new Integer(port));
        BufferedReader bis = null;
        try {
            sock.setSoTimeout(50000);
            OutputStream os = sock.getOutputStream();
            String get = "GET /index.html HTTP/1.1\n";
            os.write(get.getBytes());
            os.write("Host: localhost\n".getBytes());
            os.write("\n".getBytes());
            InputStream is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            long start = System.currentTimeMillis();
            while (bis.readLine() != null) {
            }
            long end = System.currentTimeMillis();
            System.out.println("WebTest.invoke: end - start = " + (end - start));
            stat.addStatus(TEST_NAME, end - start >= 10000 ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL);
        } finally {
            if (sock != null) {
                sock.close();
            }
            if (bis != null) {
                bis.close();
            }
        }
    }
}
