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

package com.sun.s1asdev.deployment.noappxml.client;

import java.net.*;
import java.io.*;
import jakarta.ejb.EJB;
import com.sun.s1asdev.deployment.noappxml.ejb.Sful;
import com.sun.s1asdev.deployment.noappxml.ejb.Sless;

public class Client {

    public static void main (String[] args) {
        Client client = new Client(args);
        client.doTest(args);
    }

    public Client (String[] args) {}

    @EJB
    private static Sful sful;

    @EJB
    private static Sless sless;

    public void doTest(String args[]) {
        try {

            System.err.println("invoking stateful");
            sful.hello();

            System.err.println("invoking stateless");
            sless.hello();

            System.err.println("argument = " + args[0]);
            String url = args[0];
            int code = invokeServlet(url);
            if (code != 200) {
                System.err.println("Incorrect return code = " + code);
                fail();
                return;
            }

            pass();
        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }

            return;
    }

    private void pass() {
        System.err.println("PASSED: descriptor_free_zone/ear/no_appxml_2");
        System.exit(0);
    }

    private void fail() {
        System.err.println("FAILED: descriptor_free_zone/ear/no_appxml_2");
        System.exit(-1);
    }

    private int invokeServlet(String url) throws Exception {
        System.err.println("Invoking URL = " + url);
        URL u = new URL(url);
        HttpURLConnection c1 = (HttpURLConnection)u.openConnection();
        int code = c1.getResponseCode();
        InputStream is = c1.getInputStream();
        BufferedReader input = new BufferedReader (new InputStreamReader(is));
        String line = null;
        while ((line = input.readLine()) != null) {
            System.err.println(line);
        }
        return code;
    }
}
