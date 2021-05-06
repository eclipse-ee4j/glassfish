/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.ejb.mdb.simple.client;

import jakarta.jms.*;
import javax.naming.*;

import org.testng.Assert;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class SimpleMessageClient {

    QueueConnectionFactory  qcf = null;
    TopicConnectionFactory  tcf = null;
    Queue queue = null;
    Topic topic = null;

    final String  MSG_TEXT = "Test Message - ";
    final int NUM_MSGS = 3;

    public static void main(String[] args) {
        org.testng.TestNG testng = new org.testng.TestNG();
        testng.setTestClasses(
            new Class[] { com.sun.s1peqe.ejb.mdb.simple.client.SimpleMessageClient.class } );
        testng.run();
    }

    @Configuration(beforeTestClass = true)
    public void setup() throws Exception {
        InitialContext ic = new InitialContext();
        qcf = (QueueConnectionFactory) ic.lookup("jms/QCFactory");
        queue = (Queue) ic.lookup("jms/SampleQueue");

        tcf = (TopicConnectionFactory) ic.lookup("jms/TCFactory");
        topic = (Topic) ic.lookup("jms/SampleTopic");
    }

    @Test
    public void sendMessageToMDB() throws Exception {
        QueueConnection  queueConnection = null;
        QueueSession queueSession = null;
        QueueSender queueSender = null;
        TextMessage message = null;

        try {

            queueConnection = qcf.createQueueConnection();
            queueSession = queueConnection.createQueueSession(
                false, Session.CLIENT_ACKNOWLEDGE);
            queueSender = queueSession.createSender(queue);
            message = queueSession.createTextMessage();
            message.setJMSDeliveryMode(DeliveryMode.PERSISTENT);

            System.out.println("Created durable queue subscriber, " +
                               "persistent delivery mode");

            for (int i = 1; i <= NUM_MSGS; i++) {
                message.setText(MSG_TEXT + i);
                if(i == NUM_MSGS)
                    message.setStringProperty("MESSAGE_NUM","LAST");

                System.out.println("Sending message: " + message.getText());
                queueSender.send(message);
                Thread.sleep(100);
            }
        } finally {
            if (queueSession !=null)
                try {
                    queueSession.close();
                } catch(JMSException ex) { }

            if (queueConnection !=null)
                try {
                    queueConnection.close();
                } catch(JMSException ex) { }
        }
    }

    @Test(dependsOnMethods = {"sendMessageToMDB"} )
    public void recvMessage() throws Exception {
        TopicConnection connect = null;
        TopicSubscriber subscriber = null;
        TopicSession session = null;

        TextMessage message = null;

        System.out.println("inside recvMessage of mdb appclient");

        try {
            connect = tcf.createTopicConnection();
            session = connect.createTopicSession(false,0);
            subscriber = session.createSubscriber(topic);
            System.out.println("Started subscriber");
            connect.start();

            int msgcount = 0;
            while (msgcount < NUM_MSGS) {
                Message msg = subscriber.receive(10000);
                if (msg != null) {
                    msgcount++;
                    if (msg instanceof TextMessage) {
                        message = (TextMessage) msg;
                        System.out.println("Received message:" +
                                           message.getText());
                    }
                }
            }
        } finally {
            if (session != null)
                try {
                    session.close();
                } catch (JMSException e) { }
            if (connect != null)
                try {
                    connect.close();
                } catch (JMSException e) { }
        }
    }
}




