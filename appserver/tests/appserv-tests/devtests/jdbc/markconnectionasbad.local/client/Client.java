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

package com.sun.s1asdev.jdbc.markconnectionasbad.local.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.markconnectionasbad.local.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.markconnectionasbad.local.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    SimpleReporterAdapter stat = new SimpleReporterAdapter();

    public static void main(String[] args)
            throws Exception {

        Client client = new Client();
        client.runTest();
    }

    public void runTest() throws Exception {
        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create();
        stat.addDescription("Mark-Connection-As-Bad test");
        String con1 = simpleBMP.test1();
        String con2 = simpleBMP.test1();
        //System.out.println("Client : con-1 -> " + con1);
        //System.out.println("Client : con-2 -> " + con2);
        if (con1 != null && con2 != null && !con1.equals(con2)) {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - Shareable - ReadOnly] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - Shareable - ReadOnly] : ", stat.FAIL);
        }

        con1 = simpleBMP.test2();
        con2 = simpleBMP.test2();
        //System.out.println("Client : con-1 -> " + con1);
        //System.out.println("Client : con-2 -> " + con2);

        if (con1 != null && con2 != null && !con1.equals(con2)) {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - Shareable Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - Shareable Write] : ", stat.FAIL);
        }

        if (simpleBMP.test3()) {
            stat.addStatus(" Mark-Connection-As-Bad [local - NoTx - Shareable ReadOnly] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [local - NoTx - Shareable ReadOnly] : ", stat.FAIL);
        }

        if (simpleBMP.test4()) {
            stat.addStatus(" Mark-Connection-As-Bad [local - NoTx - Shareable Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [local - NoTx - Shareable Write] : ", stat.FAIL);
        }

        if (simpleBMP.test5(1, true)) {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - UnShareable Write (1) ] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - UnShareable Write (1) ] : ", stat.FAIL);
        }

        if (simpleBMP.test5(2, false)) {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - UnShareable Write (2) ] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - UnShareable Write (2) ] : ", stat.FAIL);
        }


        if (simpleBMP.test5(5, false)) {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - UnShareable Write (3) ] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - UnShareable Write (3) ] : ", stat.FAIL);
        }

        System.out.println("Mark-Connection-As-Bad Status: ");
        stat.printSummary();
    }
}
