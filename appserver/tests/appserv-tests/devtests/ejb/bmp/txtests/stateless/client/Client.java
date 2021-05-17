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

package com.sun.s1asdev.ejb.bmp.txtests.stateless.client;

import javax.naming.*;
import jakarta.jms.*;
import jakarta.ejb.*;
import javax.rmi.PortableRemoteObject;

import com.sun.s1asdev.ejb.bmp.txtests.stateless.ejb.SLSBHome;
import com.sun.s1asdev.ejb.bmp.txtests.stateless.ejb.SLSB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    // consts
    public static String kTestNotRun    = "TEST NOT RUN";
    public static String kTestPassed    = "TEST PASSED";
    public static String kTestFailed    = "TEST FAILED";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private SLSBHome home;

    public static void main (String[] args) {

        stat.addDescription("txtests");
        Client client = new Client(args);
        System.out.println("[txtests] doTest()...");
        client.doTest();
        System.out.println("[txtests] DONE doTest()...");
        stat.printSummary("txtests");
    }

    public Client (String[] args) {
    }

    public void doTest() {
        initSLSB();       //create MAX_TIMERS

        doRollbackTest();
        doReturnParamTest();
    }

    private void initSLSB() {
        System.out.println("[txtests] Inside init....");
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/SLSBHome");
            this.home = (SLSBHome)PortableRemoteObject.narrow
                (objref, SLSBHome.class);
            System.out.println("[txtests] Initalization done");
        } catch(Exception e) {
            System.out.println("[txtests] Exception in init....");
            e.printStackTrace();
        }
    }

    public void doRollbackTest() {
        try {
            int intVal = (int) System.currentTimeMillis();
            SLSB slsb = (SLSB) home.create();
            boolean retVal = slsb.doRollbackTest(intVal);
            if (retVal) {
                stat.addStatus("txtests doRollbackTest", stat.PASS);
            } else {
                stat.addStatus("txtests doRollbackTest", stat.FAIL);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus("txtests doRollbackTest", stat.FAIL);
        }
    }

    public void doReturnParamTest() {
        try {
            int intVal = (int) System.currentTimeMillis();
            intVal++;
            SLSB slsb = (SLSB) home.create();
            boolean retVal = slsb.doReturnParamTest(intVal);
            if (retVal) {
                stat.addStatus("txtests doReturnParamTest", stat.PASS);
            } else {
                stat.addStatus("txtests doReturnParamTest", stat.FAIL);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus("txtests doReturnParamTest", stat.FAIL);
        }
    }

}

