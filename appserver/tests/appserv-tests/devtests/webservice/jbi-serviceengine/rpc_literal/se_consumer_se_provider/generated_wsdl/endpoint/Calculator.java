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

package endpoint;

import jakarta.jws.WebService;
import jakarta.jws.WebMethod;
import jakarta.jws.Oneway;
import common.IncomeTaxDetails;
import java.util.Hashtable;
import jakarta.jws.soap.SOAPBinding;

@WebService(
    name="Calculator",
    serviceName="CalculatorService",
    targetNamespace="http://example.web.service/Calculator"
)
@SOAPBinding(style=SOAPBinding.Style.RPC, use=SOAPBinding.Use.LITERAL)
public class Calculator {

        public static final String testName = "\nTest :: rpc-literal-generated-wsdl : ";
        public Calculator() {}

        @WebMethod(operationName="add", action="urn:Add")
        public int add(int i, int j) throws Exception {
                int k = i +j ;
                System.out.println(testName + i + "+" + j +" = " + k);
                //throw new java.lang.Exception("This is my exception");
                return k;
        }

        @WebMethod(operationName="calculateIncomeTax", action="urn:CalculateIncomeTax")
        public long calculateIncomeTax(IncomeTaxDetails details
                         , IncomeTaxDetails details2
                         , IncomeTaxDetails details3
                         , IncomeTaxDetails details4
                         , IncomeTaxDetails details5
                         , IncomeTaxDetails details6
                         , IncomeTaxDetails details7
                         , IncomeTaxDetails details8
                         , IncomeTaxDetails details9
                         , IncomeTaxDetails details10
                        ) {
                long income = details.annualIncome;
                System.out.println(testName + "Annual income = " + income);
                long taxRate = 30; // 30%
                long taxToBePaid = income / taxRate;
                System.out.println(testName +"Tax to be paid = " + taxToBePaid);
                return taxToBePaid;
        }

        @WebMethod(operationName="sayHi", action="urn:SayHi")
        public String sayHi() {
                return testName + "Hi from sayHi()";
        }

        @WebMethod(operationName="printHi", action="urn:PrintHi")
        public void printHi() {
                System.out.println(testName +"Hi from printHi()");
        }

        @WebMethod(operationName="printHiToMe", action="urn:PrintHiToMe")
        public void printHiToMe(String name) {
                System.out.println(testName +"Hi to " + name + " from printHiToMe()");
        }
}
