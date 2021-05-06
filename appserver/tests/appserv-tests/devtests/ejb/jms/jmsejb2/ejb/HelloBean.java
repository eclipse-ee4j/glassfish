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

package com.sun.s1asdev.ejb.jms.jmsejb2;

import java.rmi.RemoteException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.EJBException;
import javax.naming.*;
import jakarta.jms.*;

public class HelloBean implements SessionBean {
    private String str;
    private SessionContext sc;
    private Queue queue;
    private QueueConnectionFactory qcFactory;
    private QueueConnection savedConnection;
    private QueueSession savedSession;

    public HelloBean() {}

    public void ejbCreate(String str) throws RemoteException {
        System.out.println("In ejbCreate !!");
        this.str = str;
        try {
            Context context = new InitialContext();

            System.out.println("HelloEJB has BEAN MANAGED TRANSACTIONS");

            queue = (Queue) context.lookup("java:comp/env/jms/QueueName");

            qcFactory = (QueueConnectionFactory)
                context.lookup("java:comp/env/jms/MyQueueConnectionFactory");

        } catch(Exception e) {
            e.printStackTrace();
            throw new RemoteException();
        }
    }

    /**
     * Send message is a user-demarcated transaction without committing
     * the transaction.   A subsequent business method will commit the
     * transaction and receive the message.
     */
    public String sendMessageNoCommitPart1(String msg) throws EJBException {
        try {
            if( savedConnection == null ) {
                savedConnection = qcFactory.createQueueConnection();
                savedSession = savedConnection.createQueueSession(true, 0);
                savedConnection.start();
            }

            sc.getUserTransaction().begin();

            sendMessageInternal(savedSession, msg);

            System.out.println("Sent message " + msg);
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }

        // NOTE : leave connection and session open.  they will be
        // re-enlisted when the next business method is called since
        // the transaction is still active.

        return msg;
    }

    /**
     * Commit tx started in part1 and receive sent message in a new tx.
     */
    public void sendMessageNoCommitPart2() throws EJBException {

        try {

            // commit transaction started in Part1
            sc.getUserTransaction().commit();

            // start a new transaction
            sc.getUserTransaction().begin();

            Message message = recvMessageInternal(savedSession);

            if( message != null ) {
                System.out.println("Received message " + message);
                sc.getUserTransaction().commit();
            } else {
                throw new EJBException("no message received");
            }

        } catch(Exception e) {
            e.printStackTrace();
            try {
                sc.getUserTransaction().rollback();
            } catch(Exception re) {}

            throw new EJBException(e);
        } finally {
            try {
                if( savedConnection != null ) {
                    savedConnection.close();
                    savedConnection = null;
                    savedSession = null;
                }
            } catch(Exception e) { e.printStackTrace(); }
        }
    }

    private void sendMessageInternal(QueueSession session, String msg)
        throws JMSException {
        // Create a message producer.
        QueueSender sender = session.createSender(queue);

        // Send a message.
        TextMessage message = session.createTextMessage();
        message.setText(msg);
        sender.send(message);
    }

    private Message recvMessageInternal(QueueSession session)
        throws JMSException {
        // Create a message consumer
        QueueReceiver receiver = session.createReceiver(queue);
        System.out.println("Waiting for message on " + queue);
        Message message = receiver.receive(30000);
        return message;
    }

    public void setSessionContext(SessionContext sc) {
        this.sc = sc;
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
