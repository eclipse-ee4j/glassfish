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

package com.sun.s1asdev.ejb.ee.client;

import javax.naming.*;
import jakarta.jms.*;
import jakarta.ejb.*;
import javax.rmi.PortableRemoteObject;

import com.sun.s1asdev.ejb.ee.ejb.BMTSessionHome;
import com.sun.s1asdev.ejb.ee.ejb.BMTSession;
import com.sun.s1asdev.ejb.ee.ejb.CMTSessionHome;
import com.sun.s1asdev.ejb.ee.ejb.CMTSession;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private BMTSessionHome home;
    private BMTSession sfsb;
    private CMTSessionHome cmtHome;
    private CMTSession cmtSfsb;

    private String _sfsbPrefix = "SFSB_" + System.currentTimeMillis() + "_";

    public static void main (String[] args) {

        stat.addDescription("bmttx");
        Client client = new Client(args);
        System.out.println("[bmttx] doTest()...");
        client.doTest();
        System.out.println("[bmttx] DONE doTest()...");
        stat.printSummary("bmttx");
    }

    public Client (String[] args) {
    }

    public void doTest() {
        initSFSB();     //create SFSBs
        nonTxAccessCheck();       //access the SFBS
        txAccessCheck();       //access the SFBS
        txBMTCMTAccess();
    }

    private void initSFSB() {
        System.out.println("[bmttx] Inside init....");
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/BMTSessionHome");
            home = (BMTSessionHome)PortableRemoteObject.narrow
                (objref, BMTSessionHome.class);
            sfsb = (BMTSession) home.create(_sfsbPrefix);

            objref = ic.lookup("java:comp/env/ejb/CMTSessionHome");
            cmtHome = (CMTSessionHome)PortableRemoteObject.narrow
                (objref, CMTSessionHome.class);
            cmtSfsb = (CMTSession) cmtHome.create(_sfsbPrefix);
            System.out.println("[bmtcmttx] Initalization done");
            stat.addStatus("bmtcmttx initSFSBList", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            //stat.addStatus("bmtcmttx localEntityGetEJBObject(-)" , stat.PASS);
            System.out.println("[bmtcmttx] Exception in init....");
            e.printStackTrace();
            stat.addStatus("bmtcmttx initSFSBList", stat.FAIL);
        }
    }

    public void nonTxAccessCheck() {
        try {
            String retrievedName = sfsb.getName();
            boolean nameOK = _sfsbPrefix.equalsIgnoreCase(retrievedName);
            boolean actCountOK = (sfsb.getActivateCount() == 0);

            if (nameOK && actCountOK) {
                retrievedName = cmtSfsb.getName();
                nameOK = _sfsbPrefix.equalsIgnoreCase(retrievedName);
                actCountOK = (cmtSfsb.getActivateCount() == 0);
            }

            if (nameOK && actCountOK) {
                stat.addStatus("bmtcmttx nonTxAccessCheck", stat.PASS);
            } else {
                stat.addStatus("bmtcmttx nonTxAccessCheck", stat.FAIL);
            }

        } catch (Exception ex) {
            stat.addStatus("bmtcmttx nonTxAccessCheck", stat.FAIL);
        }
    }

    public void txAccessCheck() {
        try {
            String retrievedName = sfsb.getName();
            sfsb.startTx();
            sfsb.incrementCount();
            sfsb.commitTx();

            boolean actCountOK = (sfsb.getActivateCount() == 1);
            if (actCountOK) {
                stat.addStatus("bmtcmttx BMTtxAccessCheck", stat.PASS);
            } else {
                stat.addStatus("bmtcmttx BMTtxAccessCheck", stat.FAIL);
            }

            cmtSfsb.incrementCount();
            int val =  cmtSfsb.getActivateCount();
            actCountOK = (cmtSfsb.getActivateCount() == 1);
            if (actCountOK) {
                stat.addStatus("bmtcmttx CMTtxAccessCheck", stat.PASS);
            } else {
                stat.addStatus("bmtcmttx CMTtxAccessCheck: " + val, stat.FAIL);
            }

        } catch (Exception ex) {
            stat.addStatus("bmtcmttx txAccessCheck", stat.FAIL);
        }
    }

    private void txBMTCMTAccess() {
        try {
            boolean passed = true;

            CMTSession cmt = sfsb.getCMTSession();

            int prevCount = sfsb.getActivateCount();
            int prevCMTCount = cmt.getActivateCount();

            sfsb.startTx();

                sfsb.incrementCount();
                passed = passed && (sfsb.getActivateCount() == prevCount);

                sfsb.accessCMTBean();
                passed = passed && (sfsb.getActivateCount() == prevCount);

                cmt = sfsb.getCMTSession();
                passed = passed && (sfsb.getActivateCount() == prevCount);

            sfsb.commitTx();

            passed = passed && (sfsb.getActivateCount() == (prevCount+1));
            passed = passed && (cmt.getActivateCount() == (prevCMTCount+1));

            if (passed) {
                stat.addStatus("bmtcmttx txBMTCMTAccess", stat.PASS);
            } else {
                stat.addStatus("bmtcmttx txBMTCMTAccess", stat.FAIL);
            }

        } catch (Exception ex) {
            stat.addStatus("bmtcmttx txBMTCMTAccess", stat.FAIL);
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
