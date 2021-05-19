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

package com.sun.s1asdev.timer31.schedule_ann.client;

import jakarta.ejb.*;
import javax.naming.*;
import com.sun.s1asdev.ejb31.timer.schedule_ann.Stles;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    InitialContext context;

    @EJB private static Stles bean;

    public static void main(String args[]) {
        boolean doJms = false; // TODO (args.length == 1) && (args[0].equalsIgnoreCase("jms"));

        stat.addDescription("ejb31-timer-schedule_ann");


        try {
            System.out.println("Waiting timers to expire for schedule_ann timer test");
            Thread.sleep(12000);
            System.out.println("Verifying timers for schedule_ann timer test");
            bean.verifyTimers();
            stat.addStatus("schedule_ann: ", stat.PASS );

        } catch(Exception e) {
            stat.addStatus("schedule_ann: ", stat.FAIL);
            e.printStackTrace();
        }

        stat.printSummary("ejb31-timer-schedule_ann");
    }

    // when running this class through the appclient infrastructure
    public Client() {
        try {
            context = new InitialContext();
        } catch(Exception e) {
            System.out.println("Client : new InitialContext() failed");
            e.printStackTrace();
            stat.addStatus("Client() ", stat.FAIL);
        }
    }

}
