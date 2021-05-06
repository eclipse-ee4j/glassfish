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

        @WebServiceRef
        static AddNumbersImplService service;

        public static void main(String[] args) {
            stat.addDescription("async-ejb-service");
            Client client = new Client();
            client.doSyncTest();
            client.doASyncPollTest();
            client.doASyncCallBackTest();
            stat.printSummary("async-ejb-service");
        }

        public void doSyncTest() {
            try {
                AddNumbersImpl port = service.getAddNumbersImplPort();
                int ret = port.addNumbers(2222, 1234);
                if(ret!=(2222+1234)) {
                    System.out.println("Unexpected add result " + ret);
                    stat.addStatus("async-ejb-service-sync-test", stat.FAIL);
                    return;
                }
                System.out.println(ret);
                stat.addStatus("async-ejb-service-sync-test", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("async-ejb-service-sync-test", stat.FAIL);
            }
        }

        private void doASyncPollTest () {
            System.out.println ("Invoking Asynchronous Polling addNumbers");
            try {
                AddNumbersImpl port = service.getAddNumbersImplPort();
                Response<AddNumbersResponse> resp = port.addNumbersAsync(1234, 5678);
                Thread.sleep (2000);
                AddNumbersResponse output = resp.get();
                int ret = output.getReturn();
                if(ret!=(1234+5678)) {
                    System.out.println("Unexpected add result " + ret);
                    stat.addStatus("async-ejb-service-poll-test", stat.FAIL);
                    return;
                }
                System.out.println(ret);
                stat.addStatus("async-ejb-service-poll-test", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("async-ejb-service-poll-test", stat.FAIL);
            }
        }

        public void doASyncCallBackTest () {
            System.out.println ("Invoking Asynchronous Callback addNumbers");
            try {
                AddNumbersImpl port = service.getAddNumbersImplPort();
                AddNumbersCallbackHandler cbh = new AddNumbersCallbackHandler();
                Future<?> response = port.addNumbersAsync(9876, 5432, cbh);
                Thread.sleep (2000);
                AddNumbersResponse output = cbh.getResponse ();
                int ret = output.getReturn();
                if(ret!=(9876+5432)) {
                    System.out.println("Unexpected add result " + ret);
                    stat.addStatus("async-ejb-service-callbackhandler-test", stat.FAIL);
                    return;
                }
                System.out.println(ret);
                stat.addStatus("async-ejb-service-callbackhandler-test", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("async-ejb-service-callbackhandler-test", stat.FAIL);
            }
        }

        // The actual call back handler
        private class AddNumbersCallbackHandler implements AsyncHandler<AddNumbersResponse> {
            private AddNumbersResponse output;
            public void handleResponse (Response<AddNumbersResponse> response) {
                try {
                    output = response.get ();
                } catch (ExecutionException e) {
                    e.printStackTrace ();
                } catch (InterruptedException e) {
                    e.printStackTrace ();
                }
            }

            AddNumbersResponse getResponse (){
                return output;
            }
        }
}

