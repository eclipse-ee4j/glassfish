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

package com.sun.s1peqe.ejb.mdb.simple.ejb;

import jakarta.ejb.MessageDrivenBean;
import jakarta.ejb.MessageDrivenContext;
import javax.naming.*;
import jakarta.jms.*;
import java.util.*;

public class SimpleMessageBean implements MessageDrivenBean,
    MessageListener {

    private transient MessageDrivenContext mdc = null;
    private Context context;
    private TextMessage msg = null;
    private ArrayList messageList=new ArrayList();
    public javax.naming.Context jndiContext;
    private static int beancount=0;
    public static final String  TOPICCONFAC = "jms/TCFactory";
    public SimpleMessageBean() {
        beancount++;
        System.out.println("MESSAGE BEAN:["+beancount+"].SimpleMessageBean()");
    }

    public void setMessageDrivenContext(MessageDrivenContext mdc) {
        System.out.println("In "
            + "MESSAGE BEAN:["+beancount+"].setMessageDrivenContext()");
    this.mdc = mdc;
         try {
            jndiContext=new javax.naming.InitialContext();
             }catch(Throwable e) {

          System.out.println(e.toString());
    }
    }

    public void ejbCreate() {
    System.out.println("MESSAGE BEAN: SimpleMessageBean.ejbCreate()");
    }

    public void onMessage(Message inMessage) {
        try {
            //inMessage.acknowledge();
            if (inMessage instanceof TextMessage) {
                msg = (TextMessage) inMessage;
                System.out.println("MESSAGE BEAN: Message received: "
                + msg.getText());
            } else {
                System.out.println("Message of wrong type: "
                + inMessage.getClass().getName());
            }
            messageList.add(msg);
            sendMessage(msg);
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (Throwable te) {
            te.printStackTrace();
        }
    }  // onMessage

    public void sendMessage(Message message) {
        System.out.println("MESSAGE BEAN: sendMessage back to appclient");
        try{

            TopicConnectionFactory topicfactory=(TopicConnectionFactory)jndiContext.lookup(TOPICCONFAC);
            Topic topic=(Topic)jndiContext.lookup("java:comp/env/jms/SampleTopic");

            TopicConnection

            connect = topicfactory.createTopicConnection();

            TopicSession session = connect.createTopicSession(false,0);

            TopicPublisher publisher=session.createPublisher(topic);
            Thread.sleep(3000);
            publisher.publish(message);
            System.out.println("<<Sent Message back to appclient >>");

        }catch(Throwable e) {
            System.out.println("!!!!MESSAGE BEAN: sendMessage Exception");
            e.printStackTrace();
        }
    }



    public void ejbRemove() {
        System.out.println("In SimpleMessageBean.remove()");
    }
} // class
