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
import java.net.Socket;
import com.sun.appserv.test.util.results.SimpleReporterAdapter;

/*
* Unit test for
*
*  https://glassfish.dev.java.net/issues/show_bug.cgi?id=6447
*  ("Avoid serializing and saving sessions to file during un- or
*  redeployment (unless requested by user)")
*/
public class WebTest {
    private static final String TEST_ROOT_NAME = "session-serialize-on-shutdown-only";
    private static final String EXPECTED_RESPONSE = "Found map";
    private static final String JSESSIONID = "JSESSIONID";
    private static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests",
        "session-serialize-on-shutdown-only");
    private String testName;
    private String host;
    private String port;
    private String contextRoot;
    private String run;
    private boolean shouldFindSession;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        run = args[3];
        shouldFindSession = args.length >= 5 && Boolean.parseBoolean(args[4]);
    }

    public static void main(String[] args) {
        new WebTest(args).run();
    }

    private void run() {
        stat.addDescription("Unit test for GlassFish Issue 6447");
        try {
            testName = TEST_ROOT_NAME + "-" + run;
            if ("first".equals(run)) {
                createSession();
            } else {
                checkForSession();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(testName, SimpleReporterAdapter.FAIL);
        }
        stat.printSummary();
    }

    public void createSession() throws Exception {
        Socket sock = new Socket(host, new Integer(port));
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/CreateSession" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\r\n".getBytes());
        saveSessionID(sock);
        stat.addStatus(testName, SimpleReporterAdapter.PASS);
    }

    public void checkForSession() throws Exception {
        // Read the JSESSIONID from the previous run
        String jsessionId = readSessionID();
        Socket sock = new Socket(host, new Integer(port));
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/ResumeSession" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        String cookie = "Cookie: " + jsessionId + "\n";
        os.write(cookie.getBytes());
        os.write("\r\n".getBytes());
        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        boolean found = false;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            found |= line.contains(EXPECTED_RESPONSE);
        }
        stat.addStatus(testName, found == shouldFindSession ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL);
    }

    private String readSessionID() throws IOException {
        FileInputStream fis = new FileInputStream(JSESSIONID);
        return new BufferedReader(new InputStreamReader(fis)).readLine();
    }

    private void saveSessionID(Socket sock) throws Exception {
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
    }

    private String getSessionIdFromCookie(String cookie, String field) {
        String ret = null;
        int index = cookie.indexOf(field);
        if (index != -1) {
            int endIndex = cookie.indexOf(';', index);
            if (endIndex == -1) {
                ret = cookie.substring(index);
            } else {
                ret = cookie.substring(index, endIndex);
            }
            ret = ret.trim();
        }
        return ret;
    }
}
