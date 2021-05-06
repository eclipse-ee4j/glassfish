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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import com.sun.ejte.ccl.reporter.*;
import com.sun.appserv.test.BaseDevTest;

/*
 * Unit test for @RunAs with init and destroy
 */
public class WebTest extends BaseDevTest {

    private static final String TEST_NAME = "servlet-3.1-run-as-init-destroy";
    private static final String EXPECTED_RESPONSE = "RunAsInitDestroy-SL: Hello javaee";
    private static final String EXPECTED_RESPONSE2 = "RunAsInitDestroy-SL: Hello destroy";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private int port;
    private String contextRoot;
    private String domain;
    private String serverLog;
    private String appName;

    public WebTest(String[] args) {
        host = args[0];
        port = Integer.parseInt(args[1]);
        contextRoot = args[2];
        domain = args[3];
        serverLog = args[4];
        appName = args[5];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for @RunAs with init and destroy");
        WebTest webTest = new WebTest(args);

        try {
            boolean ok = webTest.run();
            boolean ok2 = webTest.undeployAndCheckLog();
            stat.addStatus(TEST_NAME, ((ok && ok2)? stat.PASS : stat.FAIL));
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

            stat.printSummary();
    }

    @Override
    public String getTestName() {
        return TEST_NAME;
    }

    @Override
    public String getTestDescription() {
        return TEST_NAME;
    }

    private boolean run() throws Exception {
        String urlStr = "http://" + host + ":" + port + contextRoot + "/myurl";
        System.out.println("GET " + urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Authorization", "Basic amF2YWVlOmphdmFlZQ==");
        urlConnection.connect();

        int code = urlConnection.getResponseCode();
        boolean ok = (code == 200);
        InputStream is = null;
        BufferedReader bis = null;
        String line = null;

        try {
            is = urlConnection.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            int lineNum = 1;
            while ((line = bis.readLine()) != null) {
                System.out.println(lineNum + ":  " + line);
                lineNum++;
                ok = ok && EXPECTED_RESPONSE.equals(line);
            }
        } catch( Exception ex){
            ex.printStackTrace();
            throw new Exception("Test UNPREDICTED-FAILURE");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch(IOException ioe) {
                 // ignore
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
        }

        return ok;
    }

    private boolean undeployAndCheckLog() throws Exception {
        asadmin("undeploy", appName);
        boolean found = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(serverLog));) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.contains(EXPECTED_RESPONSE2)) {
                    found = true;
                    break;
                }
            }
        }

        return found;
    }
}
