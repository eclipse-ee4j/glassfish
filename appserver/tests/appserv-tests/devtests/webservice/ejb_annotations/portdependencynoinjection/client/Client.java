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
import javax.naming.Context;
import javax.naming.InitialContext;

import endpoint.HelloImplService;
import endpoint.HelloImpl;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

@WebServiceRef(name="service/helloport", wsdlLocation="http://HTTP_HOST:HTTP_PORT/HelloImplService/HelloImpl?WSDL", type=endpoint.HelloImpl.class, value=HelloImplService.class)
public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        static HelloImpl port;

        public static void main(String[] args) {
            stat.addDescription("ws-portdependency-no-injection");
            try {
                    Context ic = new InitialContext();

                    port = (HelloImpl) ic.lookup("java:comp/env/service/helloport");
            } catch(Throwable t) {
                t.printStackTrace();
                System.out.println("Dependency lookup failed : " + t.getMessage());
                stat.addStatus("ws-dependency-no-injection", stat.FAIL);
            }
            System.out.println("Port is " + port);
            if (port!=null) {
                    Client client = new Client();
                    client.doTest(args);
            }
            stat.printSummary("ws-portdependency-no-injection");
       }

       public void doTest(String[] args) {
            try {
                for (int i=0;i<10;i++) {
                    String ret = port.sayHello("Appserver Tester !");
                    if(ret.indexOf("WebSvcTest-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus("ws-portdependency-no-injection", stat.FAIL);
                        return;
                    }
                    System.out.println(ret);
                }
                stat.addStatus("ws-portdependency-no-injection", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("ws-portdependency-no-injection", stat.FAIL);
            }
       }
}

