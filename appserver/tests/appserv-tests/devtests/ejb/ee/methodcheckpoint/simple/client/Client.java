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

package com.sun.s1asdev.ejb.ee.methodcheckpoint.simple.client;

import javax.naming.*;
import jakarta.jms.*;
import jakarta.ejb.*;
import javax.rmi.PortableRemoteObject;

import com.sun.s1asdev.ejb.ee.ejb.SFSBHome;
import com.sun.s1asdev.ejb.ee.ejb.SFSB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private SFSBHome home;
    private SFSB sfsb;

    private String _sfsbPrefix = "SFSB_" + System.currentTimeMillis() + "_";

    public static void main (String[] args) {

        stat.addDescription("simpletx");
        Client client = new Client(args);
        System.out.println("[simpletxClient] doTest()...");
        client.doTest();
        System.out.println("[simpletxClient] DONE doTest()...");
        stat.printSummary("simpletx");
    }

    public Client (String[] args) {
    }

    public void doTest() {
        initSFSBList();     //create SFSBs
        accessSFSB();       //access the SFBS

    }

    private void initSFSBList() {
        System.out.println("[simpletxClient] Inside init....");
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/SFSBHome");
            home = (SFSBHome)PortableRemoteObject.narrow
                (objref, SFSBHome.class);
            sfsb = (SFSB) home.create(_sfsbPrefix);
            System.out.println("[simpletx] Initalization done");
            stat.addStatus("ejbclient initSFSBList", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            //stat.addStatus("ejbclient localEntityGetEJBObject(-)" , stat.PASS);
            System.out.println("[simpletxClient] Exception in init....");
            e.printStackTrace();
            stat.addStatus("ejbclient initSFSBList", stat.FAIL);
        }
    }

    public void accessSFSB() {
        try {
            boolean actCountOK = true;
            System.out.println("ActivateCount[0]: " + sfsb.getActivateCount());
            actCountOK = sfsb.getActivateCount() == 0;

            String retrievedName = sfsb.getName();
            boolean nameOK = _sfsbPrefix.equalsIgnoreCase(retrievedName);
            System.out.println("ActivateCount[1]: " + sfsb.getActivateCount());
            actCountOK = (sfsb.getActivateCount() == 1);

            System.out.println("ActivateCount[2]: " + sfsb.getActivateCount());
            sfsb.getPassivateCount();
            actCountOK = (sfsb.getActivateCount() == 1);

            System.out.println("ActivateCount[3]: " + sfsb.getActivateCount());
            sfsb.getPassivateCount();
            actCountOK = (sfsb.getActivateCount() == 1);

            System.out.println("ActivateCount[4]: " + sfsb.getActivateCount());
            sfsb.getPassivateCount();
            actCountOK = (sfsb.getActivateCount() == 1);

            if (nameOK && actCountOK) {
                stat.addStatus("ejbclient accessSFSB ", stat.PASS);
            } else {
                stat.addStatus("ejbclient accessSFSB ", stat.FAIL);
            }

        } catch (Exception ex) {
            stat.addStatus("ejbclient accessSFSB", stat.FAIL);
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
