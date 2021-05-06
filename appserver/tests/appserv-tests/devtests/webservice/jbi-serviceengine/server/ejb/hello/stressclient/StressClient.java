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

import com.example.subtractor.SubtractorService;
import com.example.subtractor.Subtractor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class StressClient extends Thread {

    int clientId;
    Hello port;
    Hi port1;
    Subtractor port2;

        private static String testId = "jbi-serviceengine/server/ejb/hello/stressclient";

     static long startTime  = 0;
     static int minutesToRun = 0;
     static long endTime = 0;


       private SimpleReporterAdapter stat ;

       HelloEJBService service;
       HiEJBService service1;
       SubtractorService service2;

       StressClient(int i,SimpleReporterAdapter stat) {
           System.out.println("Instantiating a stress client");
           //stat.addDescription("jsr108-serverside-webservices-ejb-noname-annotation");
           clientId = i;
           this.stat = stat;
       }

       void setServiceHandle( HelloEJBService ser, HiEJBService ser1, SubtractorService ser2){
           service = ser;
           service1 = ser1;
           service2 = ser2;
       }

       static void setTimeToRun( int minutes ) {
           minutesToRun = minutes;
           startTime = System.currentTimeMillis();
       }

       public void run() {
           port = service.getHelloEJBPort();
           // Get Stub
           BindingProvider stub = (BindingProvider)port;
           String endpointURI ="http://localhost:12015/HelloEJBPort";
           stub.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                endpointURI);
           System.out.println(clientId + ":" + " After setting endpoint address URI");

            port1 = service1.getHiEJBPort();
            BindingProvider stub1 = (BindingProvider)port1;
            String endpointURI1 ="http://localhost:12017/HiEJBPort";
            stub1.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                          endpointURI1);

             port2 = service2.getSubtractorPort();
             BindingProvider stub2 = (BindingProvider)port2;
             String endpointURI2 ="http://localhost:12018/subtractorendpoint";
             stub2.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                          endpointURI2);


           while((endTime - startTime) < minutesToRun*60*1000) {
               // Iterative tester for stress, per Thread
               for ( int i=0 ; i < 10; i++)
                   doTest("Stress Tester",i);
               endTime = System.currentTimeMillis();
           }
       }

       public void doTest(String toSend, int id) {
            try {
                Integer i = (Integer)id;

                String ret = port.sayHello(toSend+i);
                String ret1 = port1.sayHi(toSend+i);
                int res = port2.add(5,2);

                if(ret.indexOf("WebSvcTest-Hello") == -1 || ret.indexOf(i.toString()) == -1) {
                    System.out.println(clientId + ":" + "Unexpected greeting " + ret);
                    stat.addStatus(testId, stat.FAIL);
                    stat.printSummary(testId);
                    return;
                }
                //pass();
                System.out.println(clientId + ":" + ret);

                 if(ret1.indexOf("WebSvcTest-Hi") == -1 || ret1.indexOf(i.toString()) == -1) {
                            System.out.println(clientId + ":" + "Unexpected greeting " + ret1);
                     stat.addStatus(testId, stat.FAIL);
                     return;
                 }
                 //pass();
                 System.out.println(clientId + ":" + ret1);

                 if( res != 3) {
                      System.out.println(clientId + ":" + "Unexpected result " + res);
                      stat.addStatus(testId, stat.FAIL);
                      return;
                 }
                 //pass();
                 System.out.println(clientId + "Result is :" + res);

             }  catch(Exception e) {
                stat.addStatus(testId, stat.FAIL);
                stat.printSummary(testId);
                e.printStackTrace();
            }
       }

    private void pass() {
        stat.addStatus(testId, stat.PASS);
    }

    private void fail() {
        stat.addStatus(testId, stat.FAIL);
    }


}

