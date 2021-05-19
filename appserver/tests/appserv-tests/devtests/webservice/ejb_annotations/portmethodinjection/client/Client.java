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

import endpoint.HelloImplService;
import endpoint.HelloImpl;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        private final static String desc = "ws-ejb-port-method-injection";

        @WebServiceRef(HelloImplService.class)
        static void setPort(HelloImpl s) {
                port = s;
        }

        static HelloImpl port;

        public static void main(String[] args) {
            stat.addDescription(desc);
            Client client = new Client();
            client.doTest(args);
            stat.printSummary(desc);
       }

       public void doTest(String[] args) {
            try {
                for (int i=0;i<10;i++) {
                    String ret = port.sayHello("Appserver Tester !");
                    if(ret.indexOf("WebSvcTest-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus(desc, stat.FAIL);
                        return;
                    }
                    System.out.println(ret);
                }
                stat.addStatus(desc, stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus(desc, stat.FAIL);
            }
       }
}

