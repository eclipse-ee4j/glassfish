/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ee.methodcheckpoint.multi.client;

import javax.naming.*;
import jakarta.jms.*;
import jakarta.ejb.*;
import javax.rmi.PortableRemoteObject;
import com.sun.s1asdev.ejb.ee.ejb.SFSBHome;
import com.sun.s1asdev.ejb.ee.ejb.SFSB;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static final int INCREMENT_VAL = 60000;

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private SFSBHome home;
    private SFSB sfsb;

    private String accountName = "Account[" + System.currentTimeMillis() + "]";

    public static void main (String[] args) {

        stat.addDescription("multi");
        Client client = new Client(args);
        System.out.println("[multiClient] doTest()...");
        client.doTest();
        System.out.println("[multiClient] DONE doTest()...");
        stat.printSummary("multi");
    }

    public Client (String[] args) {
    }

    public void doTest() {
        createSFSB();     //create SFSBs
        initialStateTest();       //access the SFBS
        nonTxNonCheckpointTest();
        nonTxCheckpointTest();
    }

    private void createSFSB() {
        String testCaseName = "ee.multiClient createSFSB ";
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/SFSBHome");
            home = (SFSBHome)PortableRemoteObject.narrow
                (objref, SFSBHome.class);
            sfsb = (SFSB) home.create(accountName, 4000);
            System.out.println("[multi] Initalization done");
            stat.addStatus(testCaseName, stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            //stat.addStatus("ejbclient localEntityGetEJBObject(-)" , stat.PASS);
            System.out.println("[multiClient] Exception in init....");
            e.printStackTrace();
            stat.addStatus(testCaseName, stat.FAIL);
        }
    }

    public void initialStateTest() {
        String testCaseName = "ee.multiClient initialStateTest ";
        try {
            boolean nameOK = true;
            nameOK = accountName.equals(sfsb.getAccountHolderName());
            int preBalance = sfsb.getBalance();
            int preCheckpointedBalance = sfsb.getCheckpointedBalance();
            boolean balanceOK = (preBalance != preCheckpointedBalance);
            if (!balanceOK) {
                System.out.println(testCaseName + " failing because: "
                        + preBalance + " == " + preCheckpointedBalance
                        + " failed");
            }
            stat.addStatus(testCaseName,
                    ((nameOK && balanceOK) ? stat.PASS : stat.FAIL));
        } catch (Exception ex) {
            stat.addStatus(testCaseName, stat.FAIL);
        }
    }

    public void nonTxNonCheckpointTest() {
        String testCaseName = "ee.multiClient nonTxNonCheckpointTest ";
        try {
            int preBalance = sfsb.getBalance();
            int preCheckpointedBalance = sfsb.getCheckpointedBalance();
            sfsb.incrementBalance(INCREMENT_VAL);
            sfsb.nonTxNonCheckpointedMethod();
            int postBalance = sfsb.getBalance();
            int postCheckpointedBalance = sfsb.getCheckpointedBalance();

            boolean ok = (preBalance+INCREMENT_VAL== postBalance)
                && (preCheckpointedBalance == postCheckpointedBalance);
            if (!ok) {
                System.out.println(testCaseName + " failing because: "
                    + "(" + preBalance + " + " + INCREMENT_VAL
                    + " == " + postBalance + ")"
                    + " && (" + preCheckpointedBalance + " == "
                    + postCheckpointedBalance + ")"
                    + " failed");
            }
            stat.addStatus(testCaseName, (ok ? stat.PASS : stat.FAIL));
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(testCaseName, stat.FAIL);
        }
    }

    public void nonTxCheckpointTest() {
        String testCaseName = "ee.multiClient nonTxCheckpointTest ";
        try {
            int preBalance = sfsb.getBalance();
            int preCheckpointedBalance = sfsb.getCheckpointedBalance();
            sfsb.incrementBalance(INCREMENT_VAL);
            sfsb.nonTxCheckpointedMethod();
            int postBalance = sfsb.getBalance();
            int postCheckpointedBalance = sfsb.getCheckpointedBalance();

            boolean ok = (preBalance+INCREMENT_VAL == postBalance)
                && (preCheckpointedBalance != postCheckpointedBalance)
                && (postBalance == postCheckpointedBalance);
            stat.addStatus(testCaseName, (ok ? stat.PASS : stat.FAIL));
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(testCaseName, stat.FAIL);
        }
    }

    private void sleepFor(int seconds) {
        System.out.println("Waiting for 10 seconds before accessing...");
        for (int i=0; i<seconds; i++) {
            System.out.println("" + (10 - i) + " seconds left...");
            try {
                Thread.currentThread().sleep(1*1000);
            } catch (Exception ex) {
            }
        }
    }

} //Client{}
