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

package com.sun.s1asdev.ejb.ejb30.ee.remote_client;

import java.io.*;
import java.util.*;
import jakarta.ejb.EJB;
import javax.naming.InitialContext;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args)
        throws Exception {

        stat.addDescription("ejb-ejb30-ee-remote_sfsb");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-ee-remote_sfsbID");
    }

    public Client (String[] args) {
    }

    @EJB
    private static SfulProxy proxy;

    public void doTest() {


        boolean initialized = proxy.initialize();
        stat.addStatus("remote initialize",
            initialized ? stat.PASS : stat.FAIL);
        if (initialized) {
        try {
            System.out.println("invoking stateless");
            String result = proxy.sayHello();
            stat.addStatus("remote hello",
                "Hello".equals(result) ? stat.PASS : stat.FAIL);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("remote hello" , stat.FAIL);
        }

        try {
            for (int i=0; i<5; i++) {
                String result = proxy.sayRemoteHello();
                proxy.doCheckpoint();
            }
            for (int i=0; i<5; i++) {
                String result = proxy.sayRemoteHello();
                proxy.sayHello();
            }
            for (int i=0; i<5; i++) {
                String result = proxy.sayRemoteHello();
                proxy.doCheckpoint();
            }
            stat.addStatus("remote remote_hello", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("remote remote_hello" , stat.FAIL);
        }
        }

        System.out.println("test complete");
    }


    private static void sleepFor(int seconds) {
        while (seconds-- > 0) {
            try { Thread.sleep(1000); } catch (Exception ex) {}
            System.out.println("Sleeping for 1 second. Still " + seconds + " seconds left...");
        }
    }

}
