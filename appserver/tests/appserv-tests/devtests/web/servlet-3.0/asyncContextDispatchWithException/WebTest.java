/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * Unit test for GLASSFISH-19364.
 * Calling AsyncContext.complete if dispatched servlet throws Exception.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "servlet-3.0-async-context-dispatch-with-exception";
    private static final String EXPECTED_RESPONSE = "Error, onComplete";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for GLASSFISH-19364");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invoke();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {
        // just consume the first result
        String lastLine = lastLine("/myurl");

        lastLine = lastLine("/myurl?result=1");
        if (EXPECTED_RESPONSE.equals(lastLine)) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            System.out.println("Wrong response. Expected: " +
                    EXPECTED_RESPONSE + ", received: " + lastLine);
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }

    private String lastLine(String path) throws IOException {
        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        String lastLine = null;
        BufferedReader br = null;

        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + contextRoot + path + " HTTP/1.0\n";
            System.out.print(get);
            os.write(get.getBytes());
            os.write("\r\n".getBytes());

            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                lastLine = line;
            }
        } finally {
            close(os);
            close(is);
            close(br);
            close(sock);

            return lastLine;
        }
    }

    private void close(Socket sock) {
        try {
            if (sock != null) {
                sock.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(OutputStream os) {
        try {
            if (os != null) {
                os.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

}
