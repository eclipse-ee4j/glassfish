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

package com.sun.s1asdev.ejb.mdb.cmt;

import java.rmi.RemoteException;
import jakarta.jms.*;
import jakarta.ejb.*;
import javax.naming.*;

public class MessageBean
    implements MessageDrivenBean, MessageListener {
    private MessageDrivenContext mdc;

    public MessageBean(){
    }

    public void onMessage(Message message) {
        System.out.println("Got message!!!");

        QueueConnection connection = null;
        try {
            InitialContext ic = new InitialContext();
            Queue queue = (Queue) ic.lookup("java:comp/env/jms/MyQueue");
            QueueConnectionFactory qcFactory = (QueueConnectionFactory)
                ic.lookup("java:comp/env/jms/MyQueueConnectionFactory");
            connection = qcFactory.createQueueConnection();
            QueueSession session = connection.createQueueSession(false,
                                   Session.AUTO_ACKNOWLEDGE);
            connection.start();
            QueueSender sender = session.createSender(queue);
            TextMessage tmessage = session.createTextMessage();
            tmessage.setText("mdb() invoked");
            System.out.println("Sending message");
            sender.send(tmessage);
            System.out.println("message sent");
        } catch(NamingException e) {
            e.printStackTrace();
        }
        catch(JMSException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(connection != null) {
                    connection.close();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void setMessageDrivenContext(MessageDrivenContext mdc) {
        this.mdc = mdc;
        System.out.println("In MessageDrivenEJB::setMessageDrivenContext !!");
    }

    public void ejbCreate() throws RemoteException {
        System.out.println("In MessageDrivenEJB::ejbCreate !!");
    }

    public void ejbRemove() {
        System.out.println("In MessageDrivenEJB::ejbRemove !!");
    }

}
