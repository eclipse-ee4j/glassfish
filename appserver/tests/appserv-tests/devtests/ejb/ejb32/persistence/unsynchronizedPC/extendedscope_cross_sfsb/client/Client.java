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

package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.extendedscope_cross_sfsb.client;

import jakarta.ejb.EJB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.extendedscope_cross_sfsb.ejb.Tester;

public class Client {

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter(
            "appserv-tests");
    @EJB(beanName = "TC1SfsbWithUnsynchPC")
    private static Tester tC1SfsbWithUnsynchPC;

    @EJB(beanName = "TC2SfsbWithSynchPC")
    private static Tester tC2SfsbWithSynchPC;

    public static void main(String[] args) {
        stat.addDescription("ejb32-persistence-unsynchronizedPC-extendedscope_cross_sfsb");
        Client client = new Client();
        client.doTest();
        stat.printSummary("ejb32-persistence-unsynchronizedPC-extendedscope_cross_sfsb");
    }

    public void doTest() {
        try {
            System.out.println("I am in client");

            System.out.println("Calling SFSBWithUnsynchPC.doTest");
            stat.addStatus("TestCase1_extendedScopeSfsbWithUnsyncPCInvokeSfsbWithSynchPC", tC1SfsbWithUnsynchPC.doTest() ? stat.PASS : stat.FAIL);

            System.out.println("Calling TC2_SFSBWithSynchPC.doTest");
            stat.addStatus("TestCase2_extendedScopeSfsbWithSyncPCInvokeSfsbWithUnsynchPC", tC2SfsbWithSynchPC.doTest() ? stat.PASS : stat.FAIL);

            System.out.println("DoTest method ends");
        } catch (Exception e) {
            e.printStackTrace();
            stat.addStatus("local main", stat.FAIL);
        }
    }

}
