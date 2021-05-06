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
import jakarta.xml.ws.Service;
import entapp.ejb.*;
import entapp.web.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");
        private static String testId = "jbi-serviceengine/service_unit/enterprise_app";

    public static void main (String[] args) {
        stat.addDescription(testId);
                Client client = new Client();
                client.invoke();
        stat.printSummary(testId );
    }

    private void invoke() {
                try {

                        HelloEJBService helloEJBService = new HelloEJBService();
                        HelloEJB port = helloEJBService.getHelloEJBPort();
                        String hello = port.sayHello("Bhavani");
                        System.out.println("Output :: " + hello);

                        HelloWebService helloWebService = new HelloWebService();
                        HelloWeb webPort = helloWebService.getHelloWebPort();
                        hello = webPort.sayHello("Bhavani");
                        System.out.println("Output :: " + hello);

                        stat.addStatus(testId, stat.PASS);

                } catch(Exception ex) {
                        ex.printStackTrace();
                stat.addStatus(testId, stat.FAIL);
            }
        }
}
