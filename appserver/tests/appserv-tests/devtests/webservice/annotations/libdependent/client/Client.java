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

import com.example.hello.HelloService;
import com.example.hello.Hello;
import com.example.hello.RetVal;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef(wsdlLocation="http://localhost:8080/libdependent/webservice/HelloService?WSDL")
        static HelloService service;

        public static void main(String[] args) {
            stat.addDescription("webservices-simple-annotation");
            Client client = new Client();
            client.doTest(args);
            stat.printSummary("webservices-annotation");
       }

       public void doTest(String[] args) {
            try {
                Hello port = service.getHelloPort();
                for (int i=0;i<10;i++) {
                    RetVal ret = port.sayHello("Appserver Tester !" + args[0]);
                    if(ret.getRetVal().indexOf("WebSvcTest-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus(args[0], stat.FAIL);
                        return;
                    }
                    if(ret.getRetVal().indexOf(args[0]) == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus(args[0], stat.FAIL);
                        return;
                    }
                    System.out.println(ret);
                    System.out.println(ret.getRetVal());
                }
                stat.addStatus(args[0], stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus(args[0], stat.FAIL);
            }
       }
}

