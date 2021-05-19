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

import jakarta.xml.ws.WebServiceRefs;
import jakarta.xml.ws.WebServiceRef;

import servlet_endpoint.ServletHelloService;
import servlet_endpoint.ServletHello;

import ejb_endpoint.WSHelloEJBService;
import ejb_endpoint.WSHello;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

@WebServiceRefs({
        @WebServiceRef(name="service/MyServletService", type=servlet_endpoint.ServletHelloService.class, wsdlLocation="http://HTTP_HOST:HTTP_PORT/webservicerefs/webservice/ServletHelloService?WSDL"),
        @WebServiceRef(name="service/MyEjbService", type=ejb_endpoint.WSHelloEJBService.class, wsdlLocation="http://HTTP_HOST:HTTP_PORT/WSHelloEJBService/WSHelloEJB?WSDL") })
public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        public static void main(String[] args) {
            stat.addDescription("webservicerefs-test");
            Client client = new Client();
            client.doServletTest();
            client.doEjbTest();
            stat.printSummary("webservicerefs-test");
       }

       public void doServletTest() {
            try {
                javax.naming.InitialContext ic = new javax.naming.InitialContext();
                ServletHelloService svc = (ServletHelloService)ic.lookup("java:comp/env/service/MyServletService");
                ServletHello port = svc.getServletHelloPort();
                for (int i=0;i<10;i++) {
                    String ret = port.sayServletHello("Appserver Tester !");
                    if(ret.indexOf("WebSvcTest-Servlet-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus("WebServiceRefs-Servlet-Endpoint", stat.FAIL);
                        return;
                    }
                    System.out.println(ret);
                }
                stat.addStatus("WebServiceRefs-Servlet-Endpoint", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("WebServiceRefs-Servlet-Endpoint", stat.FAIL);
            }
       }

       public void doEjbTest() {
            try {
                javax.naming.InitialContext ic = new javax.naming.InitialContext();
                WSHelloEJBService svc = (WSHelloEJBService)ic.lookup("java:comp/env/service/MyEjbService");
                WSHello port = svc.getWSHelloEJBPort();
                for (int i=0;i<10;i++) {
                    String ret = port.sayEjbHello("Appserver Tester !");
                    if(ret.indexOf("WebSvcTest-EJB-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus("WebServiceRefs-EJB-Endpoint", stat.FAIL);
                        return;
                    }
                    System.out.println(ret);
                }
                stat.addStatus("WebServiceRefs-EJB-Endpoint", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("WebServiceRefs-EJB-Endpoint", stat.FAIL);
            }
       }
}

