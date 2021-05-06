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

package com.sun.s1asdev.security.mdb;

import jakarta.ejb.AccessLocalException;
import jakarta.ejb.MessageDriven;
import jakarta.ejb.EJBException;
import jakarta.ejb.NoSuchEJBException;
import jakarta.ejb.EJB;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.ejb.ActivationConfigProperty;

import jakarta.jms.MessageListener;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.QueueConnectionFactory;
import jakarta.jms.QueueConnection;
import jakarta.jms.QueueSession;
import jakarta.jms.QueueSender;
import jakarta.jms.TextMessage;
import jakarta.jms.Session;

import jakarta.annotation.Resource;
import jakarta.annotation.security.RunAs;

@TransactionManagement(TransactionManagementType.BEAN)
@MessageDriven(mappedName="jms/security_mdb_InQueue", description="mymessagedriven bean description")
@RunAs("javaee")

 public class MessageBean implements MessageListener {

    @EJB private Hello1 hello1;
    @EJB private Hello2 hello2;

    @Resource(name="jms/MyQueueConnectionFactory",
              mappedName="jms/security_mdb_QCF")
    QueueConnectionFactory qcFactory;

    @Resource(mappedName="jms/security_mdb_OutQueue") Queue clientQueue;

    public void onMessage(Message message) {
        System.out.println("Got message!!!");

        QueueConnection connection = null;
        try {

            System.out.println("Calling hello1 stateless bean");
            hello1.hello("local ejb3.0 stateless");

            try {
                System.out.println("Calling hello2 stateful bean");
                hello2.hello("local ejb3.0 stateful");
                throw new IllegalStateException("Illegal Access of hello2");
            } catch(AccessLocalException ex) {
                System.out.println("Expected Exception: " + ex);
            }

            hello2.removeMethod();

            connection = qcFactory.createQueueConnection();
            QueueSession session = connection.createQueueSession(false,
                                   Session.AUTO_ACKNOWLEDGE);
            QueueSender sender = session.createSender(clientQueue);
                connection.start();

            TextMessage tmessage = session.createTextMessage();
            tmessage.setText("mdb() invoked");
            System.out.println("Sending message");
            sender.send(tmessage);
            System.out.println("message sent");
                connection.close();

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(connection != null) {
                    connection.close();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

    }

}
