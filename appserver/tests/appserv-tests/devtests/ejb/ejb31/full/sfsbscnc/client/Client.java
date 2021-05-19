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

package com.acme;

import jakarta.ejb.*;
import jakarta.annotation.*;

import javax.naming.InitialContext;

import java.util.concurrent.*;


import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    private RemoteAsync remoteAsync;

    private StatefulCncSuperIntf serializedBean;

    private StatefulCncSuperIntf nonSerializedBean;

    private int num;

    public static void main(String args[]) {

        appName = args[0];
        stat.addDescription(appName);
        Client client = new Client(args);
        client.doTest();
        stat.printSummary(appName + "ID");
    }

    public Client(String[] args) {
        num = Integer.valueOf(args[1]);
    }

    public void doTest() {

        try {

            serializedBean = (StatefulCncSuperIntf) new InitialContext().lookup("java:global/" + appName + "/StatefulBeanSerialized!com.acme.StatefulCncRemote");

            nonSerializedBean = (StatefulCncSuperIntf) new InitialContext().lookup("java:global/" + appName + "/StatefulBeanNotSerialized!com.acme.StatefulCncRemote");


            nonSerializedBean.sleep(3);


            try {
                System.out.println("Sleep a small amount to make sure the async method" +
                                   "is dispatched before we make the next call...");
                Thread.sleep(500);
                nonSerializedBean.hello();
                throw new EJBException("Did not receive concurrent access exception");
            } catch(ConcurrentAccessException cae) {
                System.out.println("Successfully received concurrent access exception for " +
                                   " concurrent access attempt on non-serialized sfsb");
            }

            try {
                serializedBean.sleep(3);
                try {
                    System.out.println("Sleep a small amount to make sure the async method" +
                                   "is dispatched before we make the next call...");
                    Thread.sleep(100);
                    serializedBean.helloWait();
                    throw new EJBException("Should have received timeout exception");
                } catch(ConcurrentAccessTimeoutException cate) {
                    System.out.println("Successfully received timeout exception");
                }

                System.out.println("Attempt loopback call");
                serializedBean.attemptLoopback();

                // Invoking the same bean must still work since the concurrent access
                // exceptions do not result in the bean being destroyed
                String result = serializedBean.hello();
                System.out.println("Serialized bean says " + result);

            } catch(ConcurrentAccessException cae) {
                 throw new EJBException("Shouldn't have received concurrent access exception");
            }

            nonSerializedBean.attemptLoopback();

            remoteAsync = (RemoteAsync) new InitialContext().lookup("java:global/" + appName + "/SingletonBean");
            remoteAsync.startTest();

            ExecutorService executor = Executors.newCachedThreadPool();

            IncrementCount ics[] = new IncrementCount[num];
            for(int i = 0; i < ics.length; i++) {

                ics[i] = new IncrementCount();

                executor.execute( ics[i] );
            }

            executor.shutdown();

            executor.awaitTermination(15, TimeUnit.SECONDS);

            int count = serializedBean.getCount();

            if( count == num ) {
                System.out.println("Total count of " + count + " invocations is correct");
            } else {
                throw new EJBException("Wrong count total = " + count);
            }

            // Now put the bean to sleep and make a blocking 2nd call.  The first method
            // is a @Remove method so when the blocking call awakes we should get the
            // appropriate exception.
            serializedBean.sleepAndRemove(3);

            try {
                System.out.println("Sleep a small amount to make sure the async method" +
                                   "is dispatched before we make the next call...");
                Thread.sleep(100);
                serializedBean.hello();
                throw new EJBException("Did not receive NoSuchEJBException");
            } catch(NoSuchEJBException nse) {
                System.out.println("Successfully received NoSuchEJBException for " +
                                   " concurrent access attempt on removed sfsb");
            }


            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            stat.addStatus("local main", stat.FAIL);
            e.printStackTrace();
        }
    }


     class IncrementCount implements Runnable {
        public void run() {
            serializedBean.incrementCount(1);
        }
    }

}
