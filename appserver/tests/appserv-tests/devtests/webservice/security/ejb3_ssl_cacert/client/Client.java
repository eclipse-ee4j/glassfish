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

import java.util.Map;
import jakarta.xml.ws.WebServiceRef;
import jakarta.xml.ws.BindingProvider;

import endpoint.HelloImplService;
import endpoint.HelloImpl;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef
        static HelloImplService service;

        public static void main(String[] args) {
            boolean successExpected=true;
            String description;
            if (args.length>1) {
                if (args[0].compareToIgnoreCase("Failure")==0) {
                    successExpected=false;
                }
                description=args[1];
            } else {
                successExpected = false;
                description="webservices-ejb-rolesAllowed-annotation-negative";
            }
            System.out.println("Starting test " + description);
            stat.addDescription(description);
            Client client = new Client();
            client.doTest(description, successExpected);
            stat.printSummary(description);
       }

       public void doTest(String desc, boolean successExpected) {

           try {
               HelloImpl port = service.getHelloImplPort();

               String ret = port.sayHello("Appserver Tester !");
               if(ret.indexOf("WebSvcTest-Hello") == -1) {
                   System.out.println("Unexpected greeting " + ret);
                   stat.addStatus(desc, stat.FAIL);
                   return;
               }
               System.out.println("WebService said " + ret);
               if (successExpected) {
                   stat.addStatus(desc, stat.PASS);
               } else {
                   System.out.println("Was expected failure, go an answer...");
                   stat.addStatus(desc, stat.FAIL);
               }
           } catch(Throwable t) {
               if (successExpected) {
                   System.out.println("method invocation failed - TEST FAILED");
                   stat.addStatus(desc, stat.FAIL);
               } else {
                   System.out.println("method invocation failed - good...");
                   stat.addStatus(desc, stat.PASS);
               }
           }
       }
}

