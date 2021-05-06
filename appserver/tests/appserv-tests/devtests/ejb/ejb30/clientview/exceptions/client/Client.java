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

package com.sun.s1asdev.ejb.ejb30.clientview.exceptions.client;

import java.io.*;
import java.util.*;
import jakarta.ejb.*;
import java.rmi.RemoteException;
import jakarta.transaction.TransactionRequiredException;
import com.sun.s1asdev.ejb.ejb30.clientview.exceptions.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import javax.naming.InitialContext;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    @EJB static private Hello hr;

    @EJB static protected SlessRemoteBusiness2 slessRemoteBusiness2;
    @EJB static public SfulRemoteBusiness2 sfulRemoteBusiness2;

    @EJB static public SlessRemoteBusiness slessRemoteBusiness;
    @EJB static public SfulRemoteBusiness sfulRemoteBusiness;

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-clientview-exceptions");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-clientview-exceptionsID");
    }

    public Client (String[] args) {
    }

    public void doTest() {

        try {

            if( hr == null ) {

                System.out.println("In stand-alone mode");
                InitialContext ic = new InitialContext();
                hr = (Hello) ic.lookup("ejb/ejb_ejb30_clientview_exceptions_Hello");
                sfulRemoteBusiness2 = (SfulRemoteBusiness2) ic.lookup("ejb/ejb_ejb30_clientview_exceptions_Sful#com.sun.s1asdev.ejb.ejb30.clientview.exceptions.SfulRemoteBusiness2");
                slessRemoteBusiness2 = (SlessRemoteBusiness2) ic.lookup("ejb/ejb_ejb30_clientview_exceptions_Sless#com.sun.s1asdev.ejb.ejb30.clientview.exceptions.SlessRemoteBusiness2");
                sfulRemoteBusiness = (SfulRemoteBusiness) ic.lookup("ejb/ejb_ejb30_clientview_exceptions_Sful#com.sun.s1asdev.ejb.ejb30.clientview.exceptions.SfulRemoteBusiness");
                slessRemoteBusiness = (SlessRemoteBusiness) ic.lookup("ejb/ejb_ejb30_clientview_exceptions_Sless#com.sun.s1asdev.ejb.ejb30.clientview.exceptions.SlessRemoteBusiness");

            }

            // TODO enable when             hr.runAccessDeniedExceptionTest();

            hr.runTxRequiredTest();

            hr.runTxRolledbackTest();

            hr.runNoSuchObjectTest();

            hr.runRollbackAppExceptionTest();

            hr.runAppExceptionTest();

            runTxRequiredTest();

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }

        runConcurrentAccessTest();

            return;
    }

    private void runTxRequiredTest() {

        int numEx = 0;

        try {
            sfulRemoteBusiness.forceTransactionRequiredException();
        } catch(EJBTransactionRequiredException txre) {
            numEx++;
        }

        try {
            slessRemoteBusiness.forceTransactionRequiredException();
        } catch(EJBTransactionRequiredException txre) {
            numEx++;
        }

        try {
            sfulRemoteBusiness2.forceTransactionRequiredException();
        } catch(TransactionRequiredException txre) {
            numEx++;
        } catch(RemoteException e) {}

        try {
            slessRemoteBusiness2.forceTransactionRequiredException();
        } catch(TransactionRequiredException txre) {
            numEx++;
        } catch(RemoteException e) {}

        if( numEx != 4 ) {
            throw new RuntimeException("Didn't receive all expected " +
                                       "exceptions : " + numEx);

        } else {
            System.out.println("PASS : runTxRequiredTest");
        }


    }

    public void runConcurrentAccessTest() {
            try {
                MyThread thread = new MyThread(sfulRemoteBusiness);
                thread.start();

                sleepFor(5);
                sfulRemoteBusiness.ping();
                stat.addStatus("local concurrentAccess[1.1]", stat.FAIL);
            } catch (jakarta.ejb.ConcurrentAccessException conEx) {
                stat.addStatus("local concurrentAccess[1.1]", stat.PASS);
            } catch (Throwable th) {
                System.out.println("Argggggg: Got: " + th);
                stat.addStatus("local concurrentAccess[1.2]", stat.FAIL);
            }

            try {
                MyThread2 thread = new MyThread2(sfulRemoteBusiness2);
                thread.start();

                sleepFor(5);
                sfulRemoteBusiness2.pingRemote();
                stat.addStatus("local concurrentAccess[2.1]", stat.FAIL);
            } catch (java.rmi.RemoteException remEx) {
                stat.addStatus("local concurrentAccess[2.1]", stat.PASS);
            } catch (Throwable th) {
                stat.addStatus("local concurrentAccess[2.2] Got: " + th, stat.FAIL);
            }
    }

    class MyThread extends Thread {
        SfulRemoteBusiness sfulBusiness;

        MyThread(SfulRemoteBusiness sfulBusiness) {
            this.sfulBusiness = sfulBusiness;
        }

        public void run() {
            try {
                sfulBusiness.sleepFor(20);
            } catch (Throwable th) {
                throw new RuntimeException("Could not invoke waitfor() method");
            }
        }
    }


    class MyThread2 extends Thread {
        SfulRemoteBusiness2 sfulBusiness2;

        MyThread2(SfulRemoteBusiness2 sfulBusiness2) {
            this.sfulBusiness2 = sfulBusiness2;
        }

        public void run() {
            try {
                sfulBusiness2.sleepFor(20);
            } catch (Throwable th) {
                throw new RuntimeException("Could not invoke waitfor() method");
            }
        }
    }

    private void sleepFor(int sec) {
        try {
            for (int i=0 ; i<sec; i++) {
                Thread.currentThread().sleep(1000);
                System.out.println("[" + i + "/" + sec + "]: Sleeping....");
            }
        } catch (Exception ex) {
        }
    }

}

