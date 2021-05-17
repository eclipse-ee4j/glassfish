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

package com.sun.s1peqe.connector.mq.simplestress.client;

import jakarta.jms.*;
import javax.naming.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleMessageClient implements Runnable{

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    int id =0;

    public SimpleMessageClient(int i) {
        this.id = i;
    }

    public static void main(String[] args) {
        int NUM_CLIENTS = 50;
        int TIME_OUT = 30000;
        try {
            for (int i =0; i < NUM_CLIENTS; i++) {
                Thread client = new Thread(new SimpleMessageClient(i));
                client.start();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            stat.addStatus("simple mdb main", stat.FAIL);
        }

        Context                 jndiContext = null;
        ConnectionFactory       connectionFactory = null;
        Connection              connection = null;
        Session                 session = null;
        Queue                   queue = null;
        MessageConsumer         msgConsumer = null;
        TextMessage             message = null;

        try {
            jndiContext = new InitialContext();
            connectionFactory = (ConnectionFactory)
                jndiContext.lookup
                ("java:comp/env/jms/CFactory");
            queue = (Queue) jndiContext.lookup("java:comp/env/jms/clientQueue");

            connection =
                connectionFactory.createConnection();
            session =
                connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            connection.start();
            msgConsumer = session.createConsumer(queue);

            for (int i =0; i < NUM_CLIENTS; i++) {
                TextMessage msg = (TextMessage) msgConsumer.receive(TIME_OUT);
                if(msg==null) {
                    System.out.println("Received null so waiting.");
                    Thread.sleep(TIME_OUT);
                    msg = (TextMessage) msgConsumer.receive(TIME_OUT);
                }
                System.out.println("Received :::::: " + msg.getText());
            }
            stat.addStatus("Simple Stress test", stat.PASS);
        }catch (Throwable t) {
            t.printStackTrace();
            stat.addStatus("simple stress test", stat.FAIL);
        }finally {
            stat.printSummary("simple stress program");
            System.exit(0);
        }


    }

    public void run() {

        Context                 jndiContext = null;
        ConnectionFactory       connectionFactory = null;
        Connection              connection = null;
        Session                 session = null;
        Queue                   queue = null;
        MessageProducer         msgProducer = null;
        TextMessage             message = null;

        try {
            jndiContext = new InitialContext();
            connectionFactory = (ConnectionFactory)
                jndiContext.lookup
                ("java:comp/env/jms/CFactory");
            queue = (Queue) jndiContext.lookup("java:comp/env/jms/SampleQueue");

            connection =
                connectionFactory.createConnection();
            session =
                connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            msgProducer = session.createProducer(queue);
            message = session.createTextMessage();

            message.setText("This is message " + id);
            System.out.println("Sending message: " +
                message.getText());
            msgProducer.send(message);
        } catch (Throwable e) {
            System.out.println("Exception occurred: " + e.toString());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {}
            } // if
        } // finally
    } // main
} // class

