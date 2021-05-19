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

package com.sun.s1asdev.ejb.ejb30.hello.mdb;

import jakarta.annotation.Resource;
import jakarta.ejb.*;
import jakarta.jms.*;

//Messages received from InQueue
@MessageDriven(mappedName="jms/ejb_ejb30_hello_mdb_InQueue")
 public class MessageBean implements MessageListener {

    @Resource(mappedName="jms/ejb_ejb30_hello_mdb_QCF")
    QueueConnectionFactory qcFactory;

    //Destination Queue
    @Resource(mappedName="jms/ejb_ejb30_hello_mdb_OutQueue")
    Queue replyQueue;

    public void onMessage(Message message) {
        System.out.println("MessageBean::  onMessage :: Got message!!!" + message);

        QueueConnection connection = null;
    QueueSession session = null;
        try {
            connection = qcFactory.createQueueConnection();
            session = connection.createQueueSession(false,
                                   Session.AUTO_ACKNOWLEDGE);
            QueueSender sender = session.createSender(replyQueue);
            TextMessage tmessage = session.createTextMessage();
        String msgText =  "Reply for " + ((TextMessage) message).getText();
            tmessage.setText(msgText);
            System.out.println("Sending " + msgText);
            sender.send(tmessage);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
        if (session != null) {
            session.close();
        }
                if(connection != null) {
                    connection.close();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
