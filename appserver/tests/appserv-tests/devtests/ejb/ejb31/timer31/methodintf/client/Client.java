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

package client.timer31.methodintf;

import jakarta.ejb.*;
import javax.naming.*;

import ejb31.timer.methodintf.Stles;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    InitialContext context;

    public static void main(String args[]) {
        stat.addDescription("ejb31-timer-methodintf");

        try {
            Stles bean = (Stles) new InitialContext().lookup("java:global/ejb-timer-methodintf-ejb/StlesEJB");
            bean.createTimer();
            System.out.println("Waiting timers to expire for schedule_ann timer test");
            Thread.sleep(8000);
            System.out.println("Verifying timers transaction status");
            boolean pass = bean.verifyTimers();
            stat.addStatus("methodintf: ", ((pass)? stat.PASS : stat.FAIL) );

        } catch(Exception e) {
            stat.addStatus("methodintf: ", stat.FAIL);
            e.printStackTrace();
        }

        stat.printSummary("ejb31-timer-methodintf");
    }

}
