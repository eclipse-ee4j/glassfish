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

package stressclient;

import jakarta.xml.ws.WebServiceRef;
import jakarta.xml.ws.BindingProvider;

import endpoint.jaxws.HelloEJBService;
import endpoint.jaxws.Hello;
import endpoint.jaxws.HiEJBService;
import endpoint.jaxws.Hi;
import com.example.subtractor.Subtractor;
import com.example.subtractor.SubtractorService;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class StressSOAPEjbConsumer {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");
        private static String testId = "jbi-serviceengine/server/ejb/hello/stressclient";

        @WebServiceRef
        static HelloEJBService service;

         @WebServiceRef
         static HiEJBService service1;

         @WebServiceRef(wsdlLocation="http://localhost:8080/subtractorservice/webservice/SubtractorService?WSDL")
         static SubtractorService service2;


        static long startTime  = 0;
        static int minutesToRun = 0;
        static long endTime = 0;

        StressSOAPEjbConsumer() {
            //create multiple instances of iterative test clients.
            StressClient clients[] = new StressClient[100];
            for(int i = 0 ; i < 100 ; i++) {
                clients[i] = new StressClient(i,stat);
                clients[i].setServiceHandle(service,service1,service2);
                clients[i].start();
            }
        }
        public static void main(String[] args) throws Exception {
           stat.addDescription(testId);

           if( args != null && args.length > 0 && args[0] != null)
               try {
                   minutesToRun = Integer.parseInt(args[0]);
               } catch(NumberFormatException numEx) {
                   minutesToRun = 3;
               }
           System.out.println("Time to run is: "+minutesToRun);
           Thread.currentThread().sleep(2000);
           StressClient.setTimeToRun(minutesToRun);
           StressSOAPEjbConsumer stressClient = new StressSOAPEjbConsumer();
           //stat.addStatus("jsr108-serverside-webservices-ejb-noname-annotation", stat.PASS);
           //stat.printSummary("jsr108-serverside-webservices-ejb-noname-annotation");
       }
}
