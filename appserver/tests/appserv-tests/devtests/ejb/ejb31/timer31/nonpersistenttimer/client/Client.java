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

package com.sun.s1asdev.timer31.nonpersistenttimer.client;

import jakarta.ejb.*;
import javax.naming.*;

import com.sun.s1asdev.ejb31.timer.nonpersistenttimer.StatefulWrapper;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    InitialContext context;

    @EJB private static StatefulWrapper wrapper;

    public static void main(String args[]) {
        boolean doJms = false; // TODO (args.length == 1) && (args[0].equalsIgnoreCase("jms"));

        stat.addDescription("ejb31-timer-nonpersistenttimer");


        System.out.println("Doing foo timer test for ejbs/Foo_CMT");
        boolean result = wrapper.doFooTest("ejbs/Foo_CMT", doJms);
        System.out.println("Foo: ejbs/Foo_CMT" + ((result)? " passed!!" : "failed!!"));
        stat.addStatus("Foo: ejbs/Foo_CMT", (result)? stat.PASS : stat.FAIL);

        System.out.println("Doing foo timer test for ejbs/Foo_UNSPECIFIED_TX");
        result = wrapper.doFooTest("ejbs/Foo_UNSPECIFIED_TX", doJms);
        System.out.println("Foo: ejbs/Foo_UNSPECIFIED_TX" + ((result)? " passed!!" : "failed!!"));
        stat.addStatus("Foo: ejbs/Foo_UNSPECIFIED_TX", (result)? stat.PASS : stat.FAIL);

        System.out.println("Doing foo timer test for ejbs/Foo_BMT");
        result = wrapper.doFooTest("ejbs/Foo_BMT", doJms);
        System.out.println("Foo: ejbs/Foo_BMT" + ((result)? " passed!!" : "failed!!"));
        stat.addStatus("Foo: ejbs/Foo_BMT", (result)? stat.PASS : stat.FAIL);


        /** TODO
        result = wrapper.doMessageDrivenTest("jms/TimerMDBQueue_CMT", doJms);
        System.out.println("Message-driven test jms/TimerMDBQueue_CMT"
                + ((result)? " passed!!" : "failed!!"));
        stat.addStatus("Message-driven test: jms/TimerMDBQueue_CMT",
                (result)? stat.PASS : stat.FAIL);

        result = wrapper.doMessageDrivenTest("jms/TimerMDBQueue_BMT", doJms);
        System.out.println("Message-driven test jms/TimerMDBQueue_BMT"
                + ((result)? " passed!!" : "failed!!"));
        stat.addStatus("Message-driven test: jms/TimerMDBQueue_BMT",
                (result)? stat.PASS : stat.FAIL);
        **/

        try {
             wrapper.removeFoo();
        } catch(Exception e) {
            e.printStackTrace();
        }

        stat.printSummary("ejb31-timer-nonpersistenttimer");
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
