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
 * Unit test to ensure that memory-managed sessions are correctly
 * persisted (serialized) to file during domain shutdown, and correctly
 * restored (deserialized) from file during domain restart.
 *
 * This test also makes sure that an instance of javax.naming.Context,
 * which is stored as an attribute in a memory-managed session, may be
 * persisted to and restored from file, to cover:
 *
 *   https://glassfish.dev.java.net/issues/show_bug.cgi?id=2003
 *   ("Fail to set javax.naming.InitialContext as session attribute in
 *   distributable webapp")
 */
public class WebTest {

    private static String TEST_NAME;
    private static final String TEST_ROOT_NAME = "session-memory-persist";

    private static final String EXPECTED_RESPONSE = "Test passed!";
    private static final String JSESSIONID = "JSESSIONID";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private String run;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        run = args[3];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for (de)serialization of "
                            + "memory-managed sessions");
        WebTest webTest = new WebTest(args);

        try {
            if ("first".equals(webTest.run)) {
                TEST_NAME = TEST_ROOT_NAME + "-first";
                webTest.firstRun();
            } else {
                TEST_NAME = TEST_ROOT_NAME + "-second";
                webTest.secondRun();
            }
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void firstRun() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/CreateSession" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\r\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        // Get the JSESSIONID from the response
        String line = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if (line.startsWith("Set-Cookie:")
                    || line.startsWith("Set-cookie:")) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing Set-Cookie response header");
        }

        String jsessionId = getSessionIdFromCookie(line, JSESSIONID);

        // Store the JSESSIONID in a file
        FileOutputStream fos = new FileOutputStream(JSESSIONID);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        osw.write(jsessionId);
        osw.close();

        stat.addStatus(TEST_NAME, stat.PASS);
    }

    public void secondRun() throws Exception {
        // Read the JSESSIONID from the previous run
        FileInputStream fis = new FileInputStream(JSESSIONID);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String jsessionId = br.readLine();
        new File(JSESSIONID).delete();

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/ResumeSession" + " HTTP/1.0\n";
        System.out.print(get);
        os.write(get.getBytes());
        String cookie = "Cookie: " + jsessionId + "\n";
        System.out.println(cookie);
        os.write(cookie.getBytes());
        os.write("\r\n".getBytes());

        InputStream is = sock.getInputStream();
        br = new BufferedReader(new InputStreamReader(is));

        String line = null;
        boolean found = false;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if (line.contains(EXPECTED_RESPONSE)) {
                found = true;
                break;
            }
        }

        if (found) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            throw new Exception("Wrong response. Expected response: "
                                + EXPECTED_RESPONSE + " not found");
        }
    }

    private String getSessionIdFromCookie(String cookie, String field) {

        String ret = null;

        int index = cookie.indexOf(field);
        if (index != -1) {
            int endIndex = cookie.indexOf(';', index);
            if (endIndex != -1) {
                ret = cookie.substring(index, endIndex);
            } else {
                ret = cookie.substring(index);
            }
            ret = ret.trim();
        }

        return ret;
    }
}
