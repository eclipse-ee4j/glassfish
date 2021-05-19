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
    @WebServiceRef(wsdlLocation="http://localhost:12011/calculatorendpoint/CalculatorService?WSDL")
    static CalculatorService service;
    private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");
        private static String testId = "jbi-serviceengine/soapfault/se_provider";

    public static void main (String[] args) {
        stat.addDescription(testId);
        SOAPWebConsumer client = new SOAPWebConsumer();
        client.addUsingSOAPConsumer();
        stat.printSummary(testId );
    }

    private void addUsingSOAPConsumer() {
        com.example.calculator.Calculator port= null;

                port = service.getCalculatorPort();

                // Get Stub
                BindingProvider stub = (BindingProvider)port;
                String endpointURI ="http://localhost:12011/calculatorendpoint";
                stub.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                      endpointURI);

                String failedMsg = null;

                try {
                System.out.println("\nInvoking throwRuntimeException");
                           port.throwRuntimeException("bhavani");
                } catch(Exception ex) {
                        System.out.println(ex);
                        if(!(ex instanceof RuntimeException) ||
                                        !(ex.getMessage().equals("Calculator :: Threw Runtime Exception"))) {
                                failedMsg = "port.throwRuntimeException() did not receive RuntimeException 'Calculator :: Threw Runtime Exception'";
                        }
                }

                try {
                System.out.println("\nInvoking throwApplicationException");
                           port.throwApplicationException("bhavani");
                } catch(Exception ex) {
                        System.out.println(ex);
                        if(!(ex instanceof com.example.calculator.Exception_Exception)) {
                                failedMsg = "port.throwApplicationException() did not throw ApplicationException";
                        }
                }

                if(failedMsg != null) {
                stat.addStatus(testId, stat.FAIL);
                } else {
                stat.addStatus(testId, stat.PASS);
                }
    }
}
