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

package com.sun.s1asdev.ejb.stress.sfsbcache.client;

import java.util.ArrayList;

import javax.naming.*;
import jakarta.jms.*;
import jakarta.ejb.*;
import javax.rmi.PortableRemoteObject;

import com.sun.s1asdev.ejb.stress.sfsbcache.ejb.SFSBHome;
import com.sun.s1asdev.ejb.stress.sfsbcache.ejb.SFSB;

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

        stat.addDescription("sfsbcache");
        Client client = new Client(args);
        System.out.println("[sfsbcacheClient] doTest()...");
        client.doTest();
        System.out.println("[sfsbcacheClient] DONE doTest()...");
        stat.printSummary("sfsbcache");
    }

    public Client (String[] args) {
    }

    public void doTest() {

        System.out.println("[sfsbcacheClient] Inside init....");
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/SFSB");
            SFSBHome home = (SFSBHome)PortableRemoteObject.narrow
                (objref, SFSBHome.class);

            //Creating these many SFSBs will cause passivation
            SFSB sfsb = (SFSB) home.create("SFSB_");

            sfsb.getName();

            sfsb.remove();

            try {
                System.out.println("Calling getName() after removing bean");
                sfsb.getName();
                stat.addStatus("ejbclient accessSFSB", stat.FAIL);
            } catch(Exception e) {
                System.out.println("Successfully got exception after " +
                                   " calling business method on removed bean");
            }

            try {
                System.out.println("Calling getName() AGAIN after removing bean");
                sfsb.getName();
                stat.addStatus("ejbclient accessSFSB", stat.FAIL);
            } catch(Exception e) {
                System.out.println("Successfully got exception after " +
                                   " calling business method on removed bean");
                stat.addStatus("ejbclient accessSFSB", stat.PASS);
            }

        } catch(Exception e) {
            e.printStackTrace();
            //stat.addStatus("ejbclient localEntityGetEJBObject(-)" , stat.PASS);
            System.out.println("[sfsbcacheClient] Exception in init....");
            e.printStackTrace();
            stat.addStatus("ejbclient accessSFSB", stat.FAIL);
        }
    }

} //Client{}
