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

package com.sun.s1asdev.ejb.mdb.msgbean;

import java.util.Vector;
import jakarta.ejb.EJBException;
import jakarta.ejb.MessageDrivenBean;
import jakarta.ejb.MessageDrivenContext;
import javax.naming.*;
import jakarta.jms.*;
import java.sql.*;
import javax.sql.*;

public class MsgBean implements MessageDrivenBean, MessageListener {

    // Keep track of all messages marked for redelivery due to
    // a rollback.  This collection is shared across all instances
    // of an mdb in a single application.  NOTE : This assumption is
    // is implementation-specific and breaks the EJB programming
    // contract.  However, it makes checking the rollback logic
    // much easier...
    private static Vector rollbackMessages = new Vector();

    static {
        rollbackMessages = new Vector();
        System.out.println("Instantiating static rollback msg vector " +
                           "in MsgBean");
    }

    private Context context;

    protected MessageDrivenContext mdc = null;

    private boolean beanManagedTx = false;

    private QueueConnectionFactory queueConFactory;
    private TopicConnectionFactory topicConFactory;
    private DataSource dataSource;

    public MsgBean() {
        System.out.println("In MsgBean::MsgBean()!");
    };

    public void ejbCreate() {
        System.out.println("In MsgBean::ejbCreate() !!");

        try {
            context = new InitialContext();
            beanManagedTx = ((Boolean) context.lookup
                             ("java:comp/env/beanManagedTx")).booleanValue();

            if( beanManagedTx ) {
                System.out.println("BEAN MANAGED TRANSACTIONS");
            } else {
                System.out.println("CONTAINER MANAGED TRANSACTIONS");
            }

            dataSource = (DataSource)
                context.lookup("java:comp/env/jdbc/AccountDB");

            // Create a Queue Session.
            queueConFactory = (QueueConnectionFactory)
                context.lookup("java:comp/env/jms/MyQueueConnectionFactory");

            topicConFactory = (TopicConnectionFactory)
                context.lookup("java:comp/env/jms/MyTopicConnectionFactory");
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException("ejbCreate error");
        }
    }

    public void onMessage(Message recvMsg) {

        try {

            String messageID = recvMsg.getJMSMessageID();

            boolean doJms = (recvMsg.getJMSReplyTo() != null);
            boolean doJdbc =
                recvMsg.getBooleanProperty("doJdbc");
            boolean rollbackEnabled =
                recvMsg.getBooleanProperty("rollbackEnabled");

            System.out.println("In MsgBean::onMessage() : " + messageID);
            System.out.println("jdbc enabled : " + doJdbc + " , " +
                               "jms reply enabled : " + doJms + " , " +
                               "rollback enabled : " + rollbackEnabled);

            if( beanManagedTx ) {
                mdc.getUserTransaction().begin();
            } else if( rollbackEnabled ) {
                if( rollbackMessages.contains(messageID) ) {
                    if( !recvMsg.getJMSRedelivered() ) {
                        throw new RuntimeException
                            ("Received msg multiple times " +
                             "but redelivered flag not set" +
                             " : " + recvMsg);
                    } else {
                        System.out.println("Got redelivered message " +
                                           messageID);
                    }
                }
            }

            doStuff(doJdbc, recvMsg);

            if( beanManagedTx ) {
                mdc.getUserTransaction().commit();
            } else if( rollbackEnabled ) {
                if( recvMsg.getJMSRedelivered() ) {
                    System.out.println("Got redelivered message " +
                                       messageID);
                    // no more rollbacks -- container will commit tx
                } else {
                    rollbackMessages.add(recvMsg);
                    System.out.println("Rolling back message " +
                                       messageID);
                    mdc.setRollbackOnly();
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    protected void doStuff(boolean doJdbc, Message recvMsg) throws Exception {

        Destination replyTo = recvMsg.getJMSReplyTo();

        if( replyTo != null ) {
            doJmsStuff(replyTo, recvMsg);
        }

        if( doJdbc) {
            doJdbcStuff();
        }
    }

    private void doJdbcStuff() {
        java.sql.Connection dbCon = null;
        try {
            dbCon = dataSource.getConnection();
            Statement stmt = dbCon.createStatement();
            String query = "SELECT balance from ejb_mdb_msgbean_accounts where accountId = 'richie rich'";
            ResultSet results = stmt.executeQuery(query);
            results.next();
            System.out.println("Richie rich has " + results.getInt("balance") + " dollars");
            results.close();
            stmt.close();
        } catch(Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if( dbCon != null ) {
                    dbCon.close();
                }
            } catch(Exception e) {}
        }
    }

    private void doJmsStuff(Destination replyTo, Message recvMsg) {

        QueueConnection queueCon = null;
        TopicConnection topicCon = null;

        try {

            if( replyTo instanceof Queue ) {
                queueCon = queueConFactory.createQueueConnection();

                // parameters to createQueueSession are ignored when there
                // is a tx context.  If there's a CMT unspecified tx context,
                // e.g. CMT NotSupported, jms activity must be coded
                // in a defensive way since the container has a lot of leeway
                // in how it performs the work.
                QueueSession queueSession = queueCon.
                    createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

                QueueSender sender  = queueSession.
                    createSender((Queue)replyTo);

                TextMessage sendMsg = queueSession.createTextMessage();
                sendMsg.setText("Reply for " + ((TextMessage)recvMsg).getText() + " " + recvMsg.getJMSMessageID());
                sender.send(sendMsg);
                System.out.println("Sent reply " + sendMsg +
                                   " to " + replyTo);
            } else {
                topicCon = topicConFactory.createTopicConnection();

                TopicSession topicSession = topicCon.
                    createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

                TopicPublisher publisher =
                    topicSession.createPublisher((Topic)replyTo);

                TextMessage sendMsg = topicSession.createTextMessage();
                sendMsg.setText("Reply for " + ((TextMessage)recvMsg).getText() + " " + recvMsg.getJMSMessageID());
                publisher.publish(sendMsg);
                System.out.println("Published reply " + sendMsg +
                                   " to " + replyTo);
            }
        } catch(JMSException jmse) {
            jmse.printStackTrace();
        } finally {
            if( queueCon != null ) {
                try { queueCon.close(); } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            if( topicCon != null ) {
                try { topicCon.close(); } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setMessageDrivenContext(MessageDrivenContext mdc) {
        System.out.println("In MsgBean::setMessageDrivenContext()!!");
        this.mdc = mdc;
    }

    public void ejbRemove() {
        System.out.println("In MsgBean::remove()!!");
    }
}
