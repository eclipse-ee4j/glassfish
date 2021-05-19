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

package com.sun.s1asdev.ejb.perf.timer1;

import jakarta.jms.*;
import jakarta.ejb.*;
import jakarta.annotation.Resource;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    // consts
    public static String kTestNotRun    = "TEST NOT RUN";
    public static String kTestPassed    = "TEST PASSED";
    public static String kTestFailed    = "TEST FAILED";


    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static @EJB TimerSession timerSession;

    private static @Resource(mappedName="jms/ejb_perf_timer1_TQCF")
        QueueConnectionFactory qcFactory;

    private static @Resource(mappedName="jms/ejb_perf_timer1_TQueue")
        Queue queue;

    public static void main(String args[]) {

        stat.addDescription("ejb-timer-sessiontimer");
        Client client = new Client(args);
        int interval = Integer.parseInt(args[0]);
        int maxTimeouts = Integer.parseInt(args[1]);
        client.doTest(interval, maxTimeouts);
        stat.printSummary("ejb-timer-sessiontimer");
    }

    public Client(String args[]) {

    }

    public String doTest(int interval, int maxTimeouts) {
        String result = kTestPassed;
        QueueConnection connection = null;

        timerSession.createTimer(interval, maxTimeouts);

        System.out.println("Creating periodic timer with interval of " +
                           interval + " milliseconds.");
        System.out.println("Max timeouts = " + maxTimeouts);

        try {
            connection = qcFactory.createQueueConnection();
            QueueSession session =
                connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueReceiver receiver = session.createReceiver(queue);
            connection.start();
            System.out.println("Waiting for message");
            Message message = receiver.receive();
            TextMessage textMsg = (TextMessage)message;

            if ( (message == null) ||
                 (! textMsg.getText().equals("ejbTimeout() invoked")))
                throw new Exception("Received a null message ... TimeOut failed!!");
            System.out.println("Message : " + message);


            stat.addStatus("timer1 ", stat.PASS);
        } catch(Exception e) {

            stat.addStatus("timer1 ", stat.FAIL);

        } finally {
            if( connection != null ) {
                try {
                    connection.close();
                } catch(Exception e) {}
            }
        }

        return result;
    }
}
