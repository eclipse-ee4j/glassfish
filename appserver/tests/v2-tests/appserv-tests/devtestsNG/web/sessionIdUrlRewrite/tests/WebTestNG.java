/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

    private static final String TEST_NAME = "session-id-url-rewrite";

    private static final String EXPECTED = "MY_SESSION_ATTRIBUTE";


    @Parameters({ "host", "port", "contextroot" })
    @Test(groups ={ "even"} ) // test method
    public void webtest(String host, String port, String contextroot) throws Exception{

        echo("contextroot="+contextroot);
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextroot + "/redirectFrom" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = bis.readLine()) != null) {
            System.out.println("line = " + line);
            if (line.startsWith("Location:")) {
                break;
            }
        }
        assert line != null : "Missing Location response header";

        int index = line.indexOf("http");
        assert index != -1 : "Missing http address in Location response header";

        String redirectTo = line.substring(index);
        System.out.println("Redirect to: " + redirectTo);
        URL url = new URL(redirectTo);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        assert responseCode == 200;

        bis = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        while ((line = bis.readLine()) != null) {
            if (line.equals(EXPECTED)) {
                break;
            }
        }
        assert line != null : "Did not receive expected response data: " + EXPECTED;
    }

    public static void echo(String msg) {
        System.out.println(msg);
    }

}
