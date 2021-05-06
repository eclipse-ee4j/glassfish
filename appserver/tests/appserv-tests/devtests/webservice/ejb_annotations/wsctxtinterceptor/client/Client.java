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

import endpoint.HelloEJBService;
import endpoint.Hello;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef
        static HelloEJBService service;

        public static void main(String[] args) {
            stat.addDescription("wsctxt-invocationhandlerctxt");
            Client client = new Client();
            client.doTest(args);
            stat.printSummary("wsctxt-invocationhandlerctxt");
       }

       public void doTest(String[] args) {
            try {
                Hello port = service.getHelloEJBPort();
                String ret = port.sayHello("Appserver Tester !");
                if(ret.indexOf("1234") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus("wsctxt-invocationhandlerctxt", stat.FAIL);
                    return;
                }
                if(ret.indexOf("45") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus("wsctxt-invocationhandlerctxt", stat.FAIL);
                    return;
                }
                if(ret.indexOf("WebSvcTest-Hello") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus("wsctxt-invocationhandlerctxt", stat.FAIL);
                    return;
                }
                System.out.println(ret);
                stat.addStatus("wsctxt-invocationhandlerctxt", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("wsctxt-invocationhandlerctxt", stat.FAIL);
            }
       }
}

