/*
 * Copyright (c) 2002, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb30.interceptors.mdb.client;

import java.io.*;
import java.util.*;
import jakarta.ejb.EJBHome;
import javax.naming.*;
import jakarta.jms.*;


import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    // in milli-seconds
    private static long TIMEOUT = 90000;

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        Client client = new Client(args);

        stat.addDescription("ejb-ejb30-interceptors-mdb");
        client.doTest();
        stat.printSummary("ejb-ejb30-interceptors-mdbID");
        System.exit(0);
    }

    private InitialContext context;
    private QueueConnection queueCon;
    private QueueSession queueSession;
    private QueueSender queueSender;
    private QueueReceiver queueReceiver;
    private jakarta.jms.Queue clientQueue;


    private int numMessages = 2;
    public Client(String[] args) {

        if( args.length == 1 ) {
            numMessages = new Integer(args[0]).intValue();
        }
    }

    public void doTest() {
        try {
            setup();
            if (doTest("java:comp/env/jms/MsgBeanQueue", numMessages)) {
                stat.addStatus("cmt main", stat.PASS);
            } else {
                stat.addStatus("cmt main", stat.FAIL);
            }
        } catch(Throwable t) {
            stat.addStatus("cmt main", stat.FAIL);
            t.printStackTrace();
        } finally {
            cleanup();
        }
    }

    public void setup() throws Exception {
        context = new InitialContext();

        QueueConnectionFactory queueConFactory =
            (QueueConnectionFactory) context.lookup
            ("java:comp/env/FooCF");

        queueCon = queueConFactory.createQueueConnection();

        queueSession = queueCon.createQueueSession
            (false, Session.AUTO_ACKNOWLEDGE);

        // Producer will be specified when actual msg is sent.
        queueSender = queueSession.createSender(null);

        clientQueue = (jakarta.jms.Queue)
            context.lookup("java:comp/env/jms/MsgBeanClientQueue");

        queueReceiver = queueSession.createReceiver(clientQueue);

        queueCon.start();

    }

    public void cleanup() {
        try {
            if( queueCon != null ) {
                queueCon.close();
            }
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

    public void sendMsgs(jakarta.jms.Queue queue, Message msg, int num)
        throws JMSException {
        for(int i = 0; i < num; i++) {
            System.out.println("Sending message " + i + " to " + queue +
                               " at time " + System.currentTimeMillis());
            queueSender.send(queue, msg);
            System.out.println("Sent message " + i + " to " + queue +
                               " at time " + System.currentTimeMillis());
        }
    }

    public boolean doTest(String destName, int num)
        throws Exception {

        Destination dest = (Destination) context.lookup(destName);

        Message message = queueSession.createTextMessage(destName);

        message.setBooleanProperty("flag", true);
        message.setIntProperty("num", 2);
        sendMsgs((jakarta.jms.Queue) dest, message, num);

        System.out.println("Waiting for queue message");
        Message recvdmessage = queueReceiver.receive(TIMEOUT);
        if( recvdmessage != null ) {
            String receivedMsg = ((TextMessage)recvdmessage).getText();
            System.out.println("Received message : " + receivedMsg);
            return receivedMsg.equals("mdb() invoked. Interceptor count: 1");
        } else {
            System.out.println("timeout after " + TIMEOUT + " seconds");
            throw new JMSException("timeout" + TIMEOUT + " seconds");
        }
    }
}

