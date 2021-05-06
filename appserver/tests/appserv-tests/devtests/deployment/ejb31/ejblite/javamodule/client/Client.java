/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.acme;

import java.net.*;
import java.io.*;
import java.util.*;

import javax.naming.InitialContext;

public class Client {

    private static String appName;
    private String host;
    private String port;

    public static void main(String args[]) {
        System.out.println("ejb31-ejblite-javamodule");
        appName = args[0];
        Client client = new Client(args);
        client.doTest();
    }

    public Client(String[] args) {
        host = args[1];
        port = args[2];
    }

    public void doTest() {

        try {

            String url = "http://" + host + ":" + port +
                "/" + appName + "/HelloServlet";

            System.out.println("invoking webclient servlet at " + url);

            URL u = new URL(url);

            HttpURLConnection c1 = (HttpURLConnection)u.openConnection();
            int code = c1.getResponseCode();
            InputStream is = c1.getInputStream();
            BufferedReader input = new BufferedReader (new InputStreamReader(is));
            String line = null;
            while((line = input.readLine()) != null)
                System.out.println(line);
            if(code != 200) {
                throw new RuntimeException("Incorrect return code: " + code);
            }
            System.out.println("test complete");

        } catch(Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return;
    }
}
