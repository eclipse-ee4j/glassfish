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

import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugzilla 28840 ("NPE when using an Iterator for items in a
 * JSTL forEach tag")
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "tag-plugin-for-each";

    public static void main(String[] args) {

        stat.addDescription("Unit test for Bugzilla 28840");

        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];
        String hostPortRoot = host  + ":" + port + contextRoot;

        boolean success = false;

        success = doTest("http://" + hostPortRoot + "/jsp/iterator.jsp",
                         "One","Two", "Three");

        if (success) {
            success = doTest("http://" + hostPortRoot + "/jsp/map.jsp",
                             "Three=Three", "One=One", "Two=Two");
        }

        if (success) {
            success = doTest("http://" + hostPortRoot + "/jsp/enum.jsp",
                             "One", "Two", "Three");
        }

        if (success) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary(TEST_NAME);
    }

    /*
     * Returns true in case of success, false otherwise.
     */
    private static boolean doTest(String urlString,
                                  String expected1,
                                  String expected2,
                                  String expected3) {
        try {
            URL url = new URL(urlString);
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                return false;
            }

            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = null;
            boolean found = false;
            while ((line = input.readLine()) != null) {
                if (line.contains(expected1) &&
                    line.contains(expected2) &&
                    line.contains(expected3)) {
                    found = true;
                }
            }

            if (!found) {
                System.out.println("Invalid response. Response did not " +
                                   "contain one of the expected strings: " +
                                   expected1 + "," +
                                   expected2 + "," +
                                   expected3);
                return false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

}
