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

package com.sun.s1peqe.connector.mq.simplestress.ejb;

import jakarta.ejb.MessageDrivenBean;
import jakarta.ejb.MessageDrivenContext;
import javax.naming.*;
import jakarta.jms.*;

public class SimpleMessageBean implements MessageDrivenBean,
    MessageListener {

    Context                 jndiContext = null;
    QueueConnectionFactory  queueConnectionFactory = null;
    QueueConnection         queueConnection = null;
    QueueSession            queueSession = null;
    Queue                   queue = null;
    QueueSender             queueSender = null;
    final int               NUM_MSGS = 100;

    private transient MessageDrivenContext mdc = null;
    private Context context;

    public SimpleMessageBean() {
        System.out.println("In SimpleMessageBean.SimpleMessageBean()");
    }

    public void setMessageDrivenContext(MessageDrivenContext mdc) {
        System.out.println("In "
            + "SimpleMessageBean.setMessageDrivenContext()");
        this.mdc = mdc;
    }

    public void ejbCreate() {
        System.out.println("In SimpleMessageBean.ejbCreate()");
        try {
            jndiContext = new InitialContext();
            queueConnectionFactory = (QueueConnectionFactory)
                jndiContext.lookup
                ("java:comp/env/jms/QCFactory");
            queue = (Queue) jndiContext.lookup("java:comp/env/jms/clientQueue");
        } catch (NamingException e) {
            System.out.println("JNDI lookup failed: " +
                e.toString());
        }
    }

    public void onMessage(Message inMessage) {
        TextMessage msg = null;

        try {
            if (inMessage instanceof TextMessage) {
                msg = (TextMessage) inMessage;
                System.out.println("MESSAGE BEAN: Message received: "
                    + msg.getText());
                long sleepTime = msg.getLongProperty("sleeptime");
                System.out.println("Sleeping for : " + sleepTime + " milli seconds ");
                Thread.sleep(sleepTime);
                queueConnection =
                    queueConnectionFactory.createQueueConnection();
                queueSession =
                    queueConnection.createQueueSession(false,
                    Session.AUTO_ACKNOWLEDGE);
                queueSender = queueSession.createSender(queue);
                TextMessage message = queueSession.createTextMessage();

                message.setText("REPLIED:" + msg.getText());
                message.setIntProperty("replyid", msg.getIntProperty("id") );
                System.out.println("Sending message: " +
                message.getText());
                queueSender.send(message);
            } else {
                System.out.println("Message of wrong type: "
                    + inMessage.getClass().getName());
            }
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (Throwable te) {
            te.printStackTrace();
        } finally {
            try {
                queueSession.close();
                queueConnection.close();
            } catch (Exception e) {
            }
        }
    }  // onMessage

    public void ejbRemove() {
        System.out.println("In SimpleMessageBean.remove()");
    }
} // class

