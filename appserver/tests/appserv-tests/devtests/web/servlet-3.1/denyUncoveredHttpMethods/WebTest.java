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

import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for deny-uncovered-http-methods.
 */
public class WebTest {

    private static final String TEST_NAME = "servlet-3.1-deny-uncovered-http-methods";
    private static final String EXPECTED_RESPONSE = "Hello javaee";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private int port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = Integer.parseInt(args[1]);
        contextRoot = args[2];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for deny-uncovered-http-methods");
        WebTest webTest = new WebTest(args);

        try {
            boolean ok = webTest.run("GET", true, 200, false);
            boolean ok2 = webTest.run("POST", false, 403, true);
            stat.addStatus(TEST_NAME, ((ok && ok2)? stat.PASS : stat.FAIL));
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

            stat.printSummary();
    }

    private boolean run(String method, boolean auth,
            int status, boolean checkOKStatusOnly) throws Exception {

        String urlStr = "http://" + host + ":" + port + contextRoot + "/myurl";
        System.out.println(method + " " + urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
        urlConnection.setRequestMethod(method);
        if (auth) {
            urlConnection.setRequestProperty("Authorization", "Basic amF2YWVlOmphdmFlZQ==");
        }
        urlConnection.connect();

        int code = urlConnection.getResponseCode();
        boolean ok = (code == status);
        if (checkOKStatusOnly) {
            return ok;
        }
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
}
