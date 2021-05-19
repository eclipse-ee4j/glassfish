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

import com.example.calculator.CalculatorService;
import com.example.calculator.Calculator;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class JAXWSClient {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");
        private static String testId = "jbi-serviceengine/server/web/client";

            @WebServiceRef(wsdlLocation="http://localhost:8080/calculatorservice/webservice/CalculatorService?WSDL")
            static CalculatorService service;

        public static void main(String[] args) {
            try {
            stat.addDescription(testId);
            //CalculatorService service = new CalculatorService();
            JAXWSClient client = new JAXWSClient();
            client.doTest(args, service);
            stat.printSummary(testId);
            } catch(Exception e) {
               e.printStackTrace();
            }
       }

       public void doTest(String[] args, CalculatorService service) {
            try {
                System.out.println(" Before getting port service is : " + service);
                Calculator port = service.getCalculatorPort();
                for (int i=0;i<10;i++) {
                    int ret = port.add(i, 10);
                    if(ret != (i + 10)) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus(testId, stat.FAIL);
                        return;
                    }
                    System.out.println(" Adding : " + i + " + 10 = "  + ret);
                }
                stat.addStatus(testId, stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus(testId, stat.FAIL);
            }
       }
}

