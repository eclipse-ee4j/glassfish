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
import jakarta.xml.ws.BindingProvider;

import endpoint.jaxws.HelloEJBService;
import endpoint.jaxws.Hello;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SOAPEjbConsumerNonHttpBC {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");
        private static String testId = "jbi-serviceengine/server/ejb/hello";

        @WebServiceRef
        static HelloEJBService service;

        public static void main(String[] args) {
            stat.addDescription(testId);
            SOAPEjbConsumerNonHttpBC client = new SOAPEjbConsumerNonHttpBC();
            client.doTest(args);
            stat.printSummary(testId);
       }

       public void doTest(String[] args) {
            try {
                Hello port = service.getHelloEJBPort();
                // Get Stub
                BindingProvider stub = (BindingProvider)port;
                String endpointURI ="http://localhost:8080/soap/ejb/noname/helloendpoint";
                stub.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                      endpointURI);
                System.out.println(" After setting endpoint address URI");
                String ret = port.sayHello("Appserver Tester !");
                if(ret.indexOf("WebSvcTest-Hello") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus(testId, stat.FAIL);
                    return;
                }
                System.out.println(ret);
                stat.addStatus(testId, stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus(testId, stat.FAIL);
            }
       }
}

