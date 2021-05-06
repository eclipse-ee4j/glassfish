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

import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6273998
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "maxKeepAliveRequests";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription(TEST_NAME);
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invoke();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        sock.setSoTimeout(5000);
        OutputStream os = sock.getOutputStream();
        String get = "GET / HTTP/1.1\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Host: localhost\n".getBytes());
        os.write("\n".getBytes());
        os.flush();

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        boolean found = false;
        String line = null;
        int i =0;
        try{
            while ((line = bis.readLine()) != null) {
            }
        }catch (SocketTimeoutException t){
            ;
        }

        System.out.println(get);
        os.write(get.getBytes());
        os.write("Host: localhost\n".getBytes());
        os.write("\n".getBytes());
        os.flush();
        try{
            while ((line = bis.readLine()) != null) {
            }
        }catch (SocketTimeoutException t){
            ;
        }

        try{
            found = true;

            System.out.println(get);
            os.write(get.getBytes());
            os.write("Host: localhost\n".getBytes());
            os.write("\n".getBytes());
            os.flush();

            try{
                while ((line = bis.readLine()) != null) {
                    found = false;
                    System.out.println("line: " + line);
                }
            }catch (SocketTimeoutException t){
                ;
            }
        }catch(SocketException ex){
        }

        if (found) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
