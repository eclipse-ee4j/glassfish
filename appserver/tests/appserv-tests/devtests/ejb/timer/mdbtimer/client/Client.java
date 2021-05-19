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

package com.sun.s1asdev.ejb.timer.mdbtimer.client;

import java.util.*;
import javax.naming.*;
import jakarta.jms.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        Client client = new Client(args);

        stat.addDescription("ejb-timer-mdbtimer");
        client.go();
        stat.printSummary("ejb-timer-mdbtimerID");
        System.exit(0);
    }

    private InitialContext context;
    private QueueConnection queueCon;
    private QueueSession queueSession;
    private QueueSender queueSender;
    private QueueReceiver queueReceiver;
    private jakarta.jms.Queue clientDest;
    private jakarta.jms.Queue targetDest;

    private int numMessages = 2;
    public Client(String[] args) {

        if( args.length == 1 ) {
            numMessages = new Integer(args[0]).intValue();
        }
    }

    public void go() {
        try {
            setup();
            doTest();
            stat.addStatus("mdbtimer main", stat.PASS);
        } catch(Throwable t) {
            stat.addStatus("mdbtimer main", stat.FAIL);
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

        targetDest = (jakarta.jms.Queue) context.lookup("java:comp/env/jms/MsgBeanQueue");

        // Producer will be specified when actual msg is sent.
        queueSender = queueSession.createSender(targetDest);

        clientDest = (jakarta.jms.Queue) context.lookup("java:comp/env/jms/ClientQueue");

        queueReceiver = queueSession.createReceiver(clientDest);

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

    public void sendMsg(Message msg)
        throws JMSException {
        System.out.println("Sending message to " +
                               " at time " + System.currentTimeMillis());
        queueSender.send(msg);
        System.out.println("Sent message " +
                           " at time " + System.currentTimeMillis());
    }

    public Message recvQueueMsg(long timeout) throws JMSException {
        System.out.println("Waiting for queue message ");
        Message recvdmessage = queueReceiver.receive(timeout);
        if( recvdmessage != null ) {
            System.out.println("Received message : " + recvdmessage +
                               " at " + new Date());
        } else {
            System.out.println("timeout after " + timeout + " seconds");
            throw new JMSException("timeout" + timeout + " seconds");
        }
        return recvdmessage;
    }

    public void doTest() throws Exception {

        TextMessage message = queueSession.createTextMessage();
        message.setText("ejb-timer-mdbtimer");
        sendMsg(message);

        ObjectMessage recvMsg1 = (ObjectMessage) recvQueueMsg(20000);
        Date cancellation = (Date) recvMsg1.getObject();

        Date now = new Date();

        // wait for message after timer is cancelled (plus some buffer time)
        long wait = now.before(cancellation) ?
            (cancellation.getTime() - now.getTime()) + 30000 : 0;
        System.out.println("Timer will be cancelled after " + cancellation);
        System.out.println("Waiting for cancellation notification until " +
                           new Date(now.getTime() + wait));

        ObjectMessage recvMsg2 = (ObjectMessage) recvQueueMsg(wait);
        System.out.println("got message after periodic timer was cancelled!!");
        System.out.println("Cancellation time = " + recvMsg2.getObject());

    }
}

