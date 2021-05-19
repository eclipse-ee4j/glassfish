/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.security.wss.permethod.servlet.client;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.rpc.Stub;

public class Client {

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");
    private static String testSuite = "Sec:Servlet Per method WSS test ";

    public static void main (String[] args) {
        String helloEndpoint = null;
        if (args[0] == null){
            System.out.println("WSS Permethod client: Argument missing. Please provide target endpoint address as argument");
            System.exit(1);
        } else {
            helloEndpoint = args[0];
        }

        stat.addDescription(testSuite);

        HelloIF helloIFPort = null;
        try {
            Context ic = new InitialContext();
            HelloServletService helloService = (HelloServletService)
                ic.lookup("java:comp/env/service/HelloServletService");
            helloIFPort = helloService.getHelloIFPort();
            ((Stub)helloIFPort)._setProperty(
                    Stub.ENDPOINT_ADDRESS_PROPERTY, helloEndpoint);
            System.out.println("Calling sayHello");
            String reply = helloIFPort.sayHello("Hello World");
            System.out.println("Reply sayHello: " + reply);
            stat.addStatus(testSuite + " sayHello", stat.PASS);
        } catch(Exception e){
            stat.addStatus(testSuite + " sayHello", stat.FAIL);
            e.printStackTrace();
        }

        try {
            System.out.println("Calling sendSecret");
            int code = helloIFPort.sendSecret("It is a secret");
            System.out.println("Reply sendSecret: " + code);
            stat.addStatus(testSuite + " sendSecret", stat.PASS);
        } catch(Exception e){
            stat.addStatus(testSuite + "sendSecret", stat.FAIL);
            e.printStackTrace();
        }

        try {
            System.out.println("Calling getSecret");
            String secret = helloIFPort.getSecret(100.0);
            System.out.println("Reply getSecret: " + secret);
            stat.addStatus(testSuite + " getSecret", stat.PASS);
        } catch(Exception e){
            stat.addStatus(testSuite + " getSecret", stat.FAIL);
            e.printStackTrace();
        }

        stat.printSummary(testSuite);
    }
}
