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

import jakarta.ejb.EJB;
import ejb.Hello;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @EJB(mappedName="ejb.Hello")
        static Hello hello;

        public static void main(String[] args) {
            stat.addDescription("localremoteejbwebservice-issue");
            Client client = new Client();
            client.doTest(args);
            stat.printSummary("localremoteejbwebservice-issue");
       }

       public void doTest(String[] args) {
            try {
                for (int i=0;i<10;i++) {
                    String ret = hello.invoke("Hello Tester !");
                    if(ret.indexOf("Hello Tester") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus("Simple-Annotation", stat.FAIL);
                        return;
                    }
                    System.out.println(ret);
                }
                stat.addStatus("localremoteejbwebservice-issue", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("localremoteejbwebservice-issue", stat.FAIL);
            }
       }
}

