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

package client;

import jakarta.xml.ws.WebServiceRef;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef static HttpTestService service;

        public static void main(String[] args) {
            stat.addDescription("webservices-simple-annotation");
            Client client = new Client();
            client.doTest(args);
            stat.printSummary("webservices-annotation");
       }

       public void doTest(String[] args) {
            try {
                Hello port = service.getHelloPort();
                HelloRequest req = new HelloRequest();
                req.setString("From Vijay ");
                HelloResponse resp = port.hello(req);
                if(resp.getString().indexOf("From Vijay") == -1) {
                    System.out.println("Unexpected greeting " + resp.getString());
                    stat.addStatus(args[0], stat.FAIL);
                }
                if(resp.getString().indexOf("Hello1") != -1) {
                    System.out.println("Unexpected greeting " + resp.getString());
                    stat.addStatus(args[0], stat.FAIL);
                }
                System.out.println(resp.getString());
                Hello1 port1 = service.getHello1Port();
                Hello1Request req1 = new Hello1Request();
                req1.setString("From Vijay ");
                Hello1Response resp1 = port1.hello1(req1);
                if(resp1.getString().indexOf("From Vijay") == -1) {
                    System.out.println("Unexpected greeting " + resp1.getString());
                    stat.addStatus(args[0], stat.FAIL);
                }
                if(resp1.getString().indexOf("Hello1") == -1) {
                    System.out.println("Unexpected greeting " + resp1.getString());
                    stat.addStatus(args[0], stat.FAIL);
                }
                System.out.println(resp1.getString());
                stat.addStatus(args[0], stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                    stat.addStatus(args[0], stat.FAIL);
            }
       }
}

