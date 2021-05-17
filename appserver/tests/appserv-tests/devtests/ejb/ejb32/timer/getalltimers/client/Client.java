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

package com.acme.ejb32.timer.getalltimers;

import jakarta.ejb.*;
import javax.naming.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    InitialContext context;

    @EJB(lookup = "java:app/ejb-timer-getalltimers-ejb1/StlesTimeoutEJB")
    private static StlesTimeout stlesTimeout;
    @EJB(lookup = "java:app/ejb-timer-getalltimers-ejb2/StlesNonTimeoutEJB")
    private static StlesNonTimeout stlesNonTimeout;

    public static void main(String args[]) {
        stat.addDescription("ejb32-timer-getalltimers");


        try {
            System.out.println("Waiting timers to expire for getalltimers timer test");
            Thread.sleep(2000);
            System.out.println("Verifying getalltimers from non-timeout bean");
            stlesNonTimeout.verifyAllTimers();
            stat.addStatus("getalltimers nontimeout: ", stat.PASS );

            System.out.println("Verifying getalltimers from timeout bean");
            stlesTimeout.createProgrammaticTimers();
            stlesTimeout.verify();
            stat.addStatus("getalltimers timeout: ", stat.PASS );
        } catch(Exception e) {
            stat.addStatus("getalltimers: ", stat.FAIL);
            e.printStackTrace();
        }

        stat.printSummary("ejb32-timer-getalltimers");
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
