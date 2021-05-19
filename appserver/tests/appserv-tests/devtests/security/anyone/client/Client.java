/*
 * Copyright (c) 2005, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.security.anyone.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import jakarta.ejb.EJB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    private static String testSuite = "Sec::Anyone test ";
    private static @EJB com.sun.s1asdev.security.anyone.ejb.Hello hello;
    private String host;
    private int port;

    public static void main(String[] args) {
        Client client = new Client(args);
        client.doTest();
    }

    public Client(String[] args) {
        host = (args.length > 0) ? args[0] : "localhost";
        port = (args.length > 1) ? Integer.parseInt(args[1]) : 8080;
    }

    public void doTest() {
        stat.addDescription("security-anyone");

        String description = null;
        System.out.println("Invoking ejb");
        try {
            description = testSuite + " ejb: hello";
            hello.hello("Sun");
            stat.addStatus(description, stat.PASS);
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(description, stat.FAIL);
        }

        System.out.println("Invoking servlet");
        description = testSuite + " servlet";
        try {
            int count = goGet(host, port, "/security-anyone/servlet");
            if (count == 1) {
                stat.addStatus(description, stat.PASS);
            } else {
                System.out.println("Servlet does not return expected result.");
                stat.addStatus(description, stat.FAIL);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(description, stat.FAIL);
        }

        stat.printSummary("security-anyone");
    }

    private static int goGet(String host, int port, String contextPath)
            throws Exception {
        Socket s = new Socket(host, port);

        OutputStream os = s.getOutputStream();
        System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
        os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
        os.write("Authorization: Basic amF2YWVlOmphdmFlZQ==\n".getBytes());
        os.write("\n".getBytes());

        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        int count = 0;
        int lineNum = 0;
        while ((line = bis.readLine()) != null) {
            System.out.println(lineNum + ": " + line);
            if (line.equals("Hello World")) {
                count++;
            }
            lineNum++;
        }

        return count;
    }
}
