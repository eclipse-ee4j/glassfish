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

   /*     private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef(name="ignoredName", mappedName="MyMappedName", wsdlLocation="http://HTTP_HOST:HTTP_PORT/Hello/HelloService?WSDL")
        static HelloService service;

        public static void main(String[] args) {
            stat.addDescription("webservices-mapped-name");
            Client client = new Client();
            client.doTest(args);
            stat.printSummary("webservices-mapped-name");
       }

       public void doTest(String[] args) {
            boolean gotEx = false;
            try {
                javax.naming.InitialContext ic = new javax.naming.InitialContext();
                Object res = ic.lookup("java:comp/env/ignoredName");
            } catch(Exception ex) {
                System.out.println("Caught Expected exception - " + ex.getMessage());
                gotEx = true;
            }
            if(!gotEx) {
                System.out.println("Mapped name not mapped as expected");
                stat.addStatus("Simple-mapped-name", stat.FAIL);
                return;
            }
            try {
                javax.naming.InitialContext ic = new javax.naming.InitialContext();
                Object res = ic.lookup("java:comp/env/MyMappedName");
                Hello port = service.getHelloPort();
                for (int i=0;i<10;i++) {
                    String ret = port.sayHello("Appserver Tester !");
                    if(ret.indexOf("WebSvcTest-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus("Simple-Annotation", stat.FAIL);
                        return;
                    }
                    System.out.println(ret);
                }
                stat.addStatus("Simple-mapped-name", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("Simple-mapped-name", stat.FAIL);
            }
       }
*/
}

