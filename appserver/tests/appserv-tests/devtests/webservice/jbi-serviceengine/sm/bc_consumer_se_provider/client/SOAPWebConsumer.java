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
import jakarta.xml.ws.BindingProvider;
import com.example.calculator.CalculatorService;
import com.example.calculator.Calculator;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SOAPWebConsumer {
    @WebServiceRef(wsdlLocation="http://localhost:8080/provider/webservice/CalculatorService?WSDL")
    static CalculatorService service;
    private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");
        private static String testId = "jbi-serviceengine/sm/bc_consumer_se_provider";
    public static void main (String[] args) {
        stat.addDescription(testId);
        SOAPWebConsumer client = new SOAPWebConsumer();
        client.addUsingSOAPConsumer();
        stat.printSummary(testId );
    }

    private void addUsingSOAPConsumer() {
        com.example.calculator.Calculator port= null;
        try {

                System.out.println(" After creating CalculatorService");

                port = service.getCalculatorPort();
                System.out.println(" After getting port");

                // Get Stub
                BindingProvider stub = (BindingProvider)port;
                String endpointURI ="http://localhost:8192/calculatorendpoint/";
                stub.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                      endpointURI);
                System.out.println(" After setting endpoint address URI");
                System.out.println(" Using SOAP binding's consumer to add 1 + 2 = " + port.add(1,2));
                stat.addStatus(testId, stat.PASS);
                //System.out.println(" No Exception thrown");

        } catch(Exception e) {
                e.printStackTrace();
                if(e instanceof jakarta.xml.ws.soap.SOAPFaultException)
                        stat.addStatus(testId, stat.PASS);
                else {
                        stat.addStatus(testId,  stat.FAIL);
                        System.out.println(" SOAPFaultException Not thrown");
                }
        }
    }
}
