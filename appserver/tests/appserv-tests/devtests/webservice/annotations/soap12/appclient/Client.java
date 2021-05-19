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

import jakarta.xml.ws.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef(name="service/MyService") static AddNumbersService service;

        public static void main(String[] args) {
            stat.addDescription("webservices-simple-annotation");
            Client client = new Client();
            client.doTest(args);
            stat.printSummary("webservices-annotation");
       }

       public void doTest(String[] args) {
            try {
                AddNumbersPortType port = service.getAddNumbersPort();
                ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,"http://HTTP_HOST:HTTP_PORT/soap12/webservice/AddNumbersService?WSDL");
                int ret = port.addNumbers(100, 200);
                if(ret != 300) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus("soap12", stat.FAIL);
                    return;
                }
                System.out.println("Add result = " + ret);
                stat.addStatus("soap12", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("soap12", stat.FAIL);
            }
       }
}

