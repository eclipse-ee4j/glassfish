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
 * Unit test for Bugzilla 27664 ("Welcome files not found in combination with
 * jsp-property-group").
 *
 * See http://nagoya.apache.org/bugzilla/show_bug.cgi?id=27664 for details.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "jsp-config-welcome-files";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugzilla 27664");
        WebTest webTest = new WebTest(args);
        boolean ok = webTest.doTest("http://" + webTest.host  + ":" + webTest.port
                       + webTest.contextRoot + "/subdir1/subdir2/");
        ok = ok && webTest.doTest("http://" + webTest.host  + ":" + webTest.port
                       + webTest.contextRoot + "/TestServlet");
        stat.addStatus(TEST_NAME, ((ok)? stat.PASS : stat.FAIL));
            stat.printSummary();
    }

    public boolean doTest(String urlString) {

        InputStream is = null;
        BufferedReader input = null;
        boolean status = false;
        try {
            URL url = new URL(urlString);
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                status = false;
            } else {
                is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(is));
                String line = input.readLine();
                if (!"Welcome".equals(line)) {
                    System.err.println("Wrong response. Expected: Welcome"
                                       + ", received: " + line);
                    status = false;
                } else {
                    status = true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            status = false;
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (input != null) input.close();
            } catch (IOException ex) {}
        }

        return status;
    }

}
