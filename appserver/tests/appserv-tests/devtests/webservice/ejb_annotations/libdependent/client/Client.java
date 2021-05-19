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

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import jakarta.xml.ws.WebServiceRef;
import jakarta.xml.ws.AsyncHandler;
import jakarta.xml.ws.Response;

import endpoint.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        //@WebServiceRef
       // static HelloImplService service;

        public static void main(String[] args) {
            stat.addDescription("ejb-libdependent-service");
            Client client = new Client();
            client.doSyncTest();
            stat.printSummary("ejb-libdependent-service");
        }

        public void doSyncTest() {
            try {
                HelloImplService service = new HelloImplService();
                HelloImpl port = service.getHelloImplPort();
                RetVal ret = port.sayHello("Hi LibDependent");
                if(ret.getRetVal().indexOf("LibDep") == -1) {
                    System.out.println("WRONG GREETING " + ret.getRetVal());
                    stat.addStatus("ejb-libdependent-service-test", stat.FAIL);
                    return;
                }
                System.out.println(ret);
                System.out.println(ret.getRetVal());
                stat.addStatus("ejb-libdependent-service-test", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("ejb-libdependent-service-test", stat.FAIL);
            }
        }
}

