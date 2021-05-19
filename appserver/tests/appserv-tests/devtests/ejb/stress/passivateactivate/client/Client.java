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

package com.sun.s1asdev.ejb.stress.passivateactivate.client;

import java.util.ArrayList;

import javax.naming.*;
import jakarta.jms.*;
import jakarta.ejb.*;
import javax.rmi.PortableRemoteObject;

import com.sun.s1asdev.ejb.stress.passivateactivate.ejb.SFSBHome;
import com.sun.s1asdev.ejb.stress.passivateactivate.ejb.SFSB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    // consts
    public static String kTestNotRun    = "TEST NOT RUN";
    public static String kTestPassed    = "TEST PASSED";
    public static String kTestFailed    = "TEST FAILED";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final int    MAX_SFSBS = 600;

    private ArrayList sfsbList = new ArrayList();

    public static void main (String[] args) {

        stat.addDescription("passivateactivate");
        Client client = new Client(args);
        System.out.println("[passivateactivateClient] doTest()...");
        client.doTest();
        System.out.println("[passivateactivateClient] DONE doTest()...");
        stat.printSummary("passivateactivate");
    }

    public Client (String[] args) {
    }

    public void doTest() {
        initSFSBList();     //create SFSBs
        accessSFSB();       //access the SFBS

    }

    private void initSFSBList() {
        System.out.println("[passivateactivateClient] Inside init....");
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/SFSB");
            SFSBHome home = (SFSBHome)PortableRemoteObject.narrow
                (objref, SFSBHome.class);
            for (int i=0; i < MAX_SFSBS; i++) {

                //Creating these many SFSBs will cause passivation
                SFSB sfsb = (SFSB) home.create("SFSB_"+i);

                sfsbList.add(sfsb);
            }
            System.out.println("[passivateactivate] Initalization done");
            stat.addStatus("ejbclient initSFSBList", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            //stat.addStatus("ejbclient localEntityGetEJBObject(-)" , stat.PASS);
            System.out.println("[passivateactivateClient] Exception in init....");
            e.printStackTrace();
            stat.addStatus("ejbclient initSFSBList", stat.FAIL);
        }
    }

    public void accessSFSB() {
        try {
            for (int i=0; i < MAX_SFSBS; i++) {
                SFSB sfsb = (SFSB) sfsbList.get(i);
                String sfsbName = sfsb.getName();
                System.out.println("Successfully accessed SFSB bean for: " + sfsbName);

                stat.addStatus("ejbclient accessSFSB", stat.PASS);
            }
        } catch (Exception ex) {
            stat.addStatus("ejbclient accessSFSB", stat.FAIL);

        }
    }

} //Client{}
