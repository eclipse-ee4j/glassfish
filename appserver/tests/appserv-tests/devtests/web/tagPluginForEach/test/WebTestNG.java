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

import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;
import org.testng.annotations.Parameters;

import java.io.*;
import java.net.*;

public class WebTestNG {

    private static final String TEST_NAME =
        "tag-plugin-for-each";

    @Parameters({ "host", "port", "contextroot" })
    @Test(groups ={ "even"} ) // test method
    public void webtest(String host, String port, String contextroot) throws Exception{

        boolean success = false;
        String hostPortRoot = host  + ":" + port + contextroot;
        success = doTest("http://" + hostPortRoot + "/jsp/iterator.jsp",
                         "OneTwoThree");

        if (success) {
            success = doTest("http://" + hostPortRoot + "/jsp/map.jsp",
                             "Three=ThreeTwo=TwoOne=One");
        }

        if (success) {
            success = doTest("http://" + hostPortRoot + "/jsp/enum.jsp",
                             "OneTwoThree");
        }

        assert success == true;

    }

    /*
     * Returns true in case of success, false otherwise.
     */
    private static boolean doTest(String urlString, String expected) {
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
                if (expected.equals(line)) {
                    found = true;
                }
            }

            if (!found) {
                System.out.println("Invalid response. Response did not " +
                                   "contain expected string: " + expected);
                return false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public static void echo(String msg) {
        System.out.println(msg);
    }

}
