/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.transaction.txglobal.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1peqe.transaction.txglobal.ejb.beanA.*;


public class Client {

    private TxRemoteHomeA home = null;
    private SimpleReporterAdapter status =
        new SimpleReporterAdapter("appserv-tests");

    public Client() {
    }

    public static void main(String[] args) {
        System.out.println("\nStarting Txglobal Test Suite");
        Client client = new Client();

        // initialize the context and home object
        client.setup();

        // run the tests
        client.runTestClient();
    }

    public void setup() {
        Class homeClass = TxRemoteHomeA.class;
        try {
            // Initialize the Context
            Context context = new InitialContext();
            System.out.println("Context Initialized...");

            // Create Home object
            java.lang.Object obj = context.lookup("java:comp/env/ejb/TxBeanA");
            home = (TxRemoteHomeA) PortableRemoteObject.narrow(obj, homeClass);
            System.out.println("Home Object Initialized...");
        } catch (Throwable ex) {
            System.out.println("Exception in setup: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void runTestClient() {
        try{
            status.addDescription("This is to test the global transaction!");
            testTxCommit();
            testTxRollback();
            status.printSummary("txglobalID");
        } catch (Exception ex) {
            System.out.println("Exception in runTestClient: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void testTxCommit() {
        try {
            System.out.println("Execute BeanA::testTxCommit");

            TxRemoteA beanA = home.create();
            boolean result = beanA.txCommit();

            if (result) {
                status.addStatus("txglobal testTxCommit: ", status.PASS);
            } else {
                status.addStatus("txglobal testTxCommit: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txglobal testTxCommit: ", status.FAIL);
            System.out.println("Exception in testTxCommit: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void testTxRollback() {
        try {
            System.out.println("Execute BeanA::testTxRollback");

            TxRemoteA beanA = home.create();
            boolean result = beanA.txRollback();

            if (result) {
                status.addStatus("txglobal testTxRollback: ", status.PASS);
            } else {
                status.addStatus("txglobal testTxRollback: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txglobal testTxRollback: ", status.FAIL);
            System.out.println("Exception in testTxRollback: " + ex.toString());
            ex.printStackTrace();
        }
    }
}
