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

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import jakarta.xml.ws.*;
import service.web.example.calculator.*;
//import common.IncomeTaxDetails;
//import java.util.Hashtable;

public class Client extends HttpServlet {

       @WebServiceRef(name="sun-web.serviceref/calculator") CalculatorService service;

       public void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws jakarta.servlet.ServletException {
           doPost(req, resp);
       }

       public void doPost(HttpServletRequest req, HttpServletResponse resp)
              throws jakarta.servlet.ServletException {
            PrintWriter out=null;
            try {
                System.out.println(" Service is :" + service);
                resp.setContentType("text/html");
                    out = resp.getWriter();
                Calculator port = service.getCalculatorPort();
                                IncomeTaxDetails itDetails = new IncomeTaxDetails();
                                itDetails.setFirstName ( "bhavani");
                                itDetails.setLastName ("s");
                                itDetails.setAnnualIncome ( 400000);
                                itDetails.setStatus ("salaried");

                                long startTime = System.currentTimeMillis();
                                long ret = 0;
                                // Make 100 calls to see how much time it takes.
                                //for(int i=0; i<1000; i++) {
                                        ret = port.calculateIncomeTax(itDetails
                                                        , itDetails
                                                        , itDetails
                                                        , itDetails
                                                        , itDetails
                                                        , itDetails
                                                        , itDetails
                                                        , itDetails
                                                        , itDetails
                                                        , itDetails
                                                        );
                                //}
                                long timeTaken = System.currentTimeMillis() - startTime;

                //int ret = port.add(1, 2);
                printSuccess("Your income tax is : Rs ", out,ret, timeTaken);
                startTime = System.currentTimeMillis();
                int k = port.add(505, 50);
                                timeTaken = System.currentTimeMillis() - startTime;
                printSuccess("Sum of 505 and 50 is : ", out,k, timeTaken);

                startTime = System.currentTimeMillis();
                String hi = port.sayHi();
                                timeTaken = System.currentTimeMillis() - startTime;
                printSuccess("Output from webservice : ", out, hi, timeTaken);

                startTime = System.currentTimeMillis();
                port.printHi();
                                timeTaken = System.currentTimeMillis() - startTime;
                printSuccess("SUCCESS : ", out, "Webservice has successfully printed hi in server.log", timeTaken);

                startTime = System.currentTimeMillis();
                port.printHiToMe("JavaEEServiceEngine");
                                timeTaken = System.currentTimeMillis() - startTime;
                printSuccess("SUCCESS : ", out, "Webservice has successfully printed hi to me in server.log", timeTaken);

            } catch(java.lang.Exception e) {
                //e.printStackTrace();
                    printFailure(out, e.getMessage());
            } finally {
                if(out != null) {
                    out.flush();
                    out.close();
                }
            }
       }

       public void printFailure(PrintWriter out, String errMsg) {
                if(out == null) return;
                out.println("<html>");
                out.println("<head>");
                out.println("<title>TestServlet</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<p>");
                out.println("Test FAILED: Error message - " + errMsg);
                out.println("</p>");
                out.println("</body>");
                out.println("</html>");
       }

       public void printSuccess(String message, PrintWriter out, long result, long timeTaken) {
                if(out == null) return;
                out.println("\n\n");
                out.println(message + result);
                out.println("Time taken to invoke the endpoint operation is  :  " + timeTaken + " milliseconds.");
       }

       public void printSuccess(String message, PrintWriter out, String result, long timeTaken) {
                if(out == null) return;
                out.println("\n\n");
                out.println(message + result);
                out.println("Time taken to invoke the endpoint operation is  :  " + timeTaken + " milliseconds.");
       }
}

