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

import java.net.*;
import java.io.*;
import com.example.hello.HelloService;
import com.example.hello.Hello;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");


        public static void main(String[] args) {
            stat.addDescription("ejbwebservicesinwar");
            Client client = new Client();
            client.doTest(args);
            stat.printSummary("ejbwebservicesinwar");
       }

       public void doTest(String[] args) {
            try {

                URL serviceInfo = new URL (args[0]);
                URLConnection con = serviceInfo.openConnection();
               BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String inputLine;
                int index=0;
                while ((inputLine = in.readLine()) != null) {
                   if ((index= inputLine.indexOf("message="))>0){

                      String url = inputLine.substring(index+7);
                      if (url.indexOf("sayHello")>0) {
                         stat.addStatus("ejbwebservicesinwar", stat.PASS);
                      }
                      System.out.println(inputLine);
                   }
                }
                  in.close();
                stat.addStatus("ejbwebservicesinwar", stat.FAIL);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus(args[0], stat.FAIL);
            }
       }
}

