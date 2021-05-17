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

package com.sun.s1asdev.ejb.timer.restore;

import java.util.Iterator;
import java.util.Date;
import jakarta.ejb.TimedObject;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerHandle;
import jakarta.ejb.TimerService;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.EJBException;
import jakarta.jms.Session;
import java.rmi.RemoteException;
import jakarta.jms.QueueConnectionFactory;
import jakarta.jms.QueueConnection;
import jakarta.jms.Queue;
import jakarta.jms.QueueSender;
import jakarta.jms.QueueSession;
import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import javax.naming.*;

import com.sun.ejb.containers.TimerBean;

public class TimerSessionEJB implements TimedObject, SessionBean
{
    private SessionContext context;
    private QueueConnection connection;
    private QueueSession session;
    private Queue queue;
    private QueueSender sender;

    public void ejbCreate() throws RemoteException {}

    public void ejbRemove() throws RemoteException {}

    public void setSessionContext(SessionContext sc) {
        context = sc;
    }


    // business method to create a timer
    public TimerHandle createTimer(long initialDelay, long interval,
                                   String msg) {

        try {
            System.out.println("Calling getMessageContext");
            context.getMessageContext();
        } catch(IllegalStateException ise) {
            System.out.println("getMessageContext() successfully threw illegalStateException");
        }


        TimerService timerService = context.getTimerService();
        Timer timer;
        if( interval == 0 ) {
            timer = timerService.createTimer(initialDelay, msg);
        } else {
            timer = timerService.createTimer(initialDelay, interval,
                                             msg);
        }
        return timer.getHandle();
    }

    public void createTimerInOtherServer(String owner, String timerId,
                                         long initialExpiration,
                                         long intervalDuration, String info) {

        try {
            Date now = new Date();
            TimerBean.testCreate(timerId, context, owner,
                             new Date(now.getTime() + initialExpiration),
                                 intervalDuration,
                             info);
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }

    }

    public void migrateTimersFrom(String owner) {

        TimerBean.testMigrate(owner);

    }

    public void deleteTimers() {

        java.util.Collection timers = context.getTimerService().getTimers();
        for(Iterator iter = timers.iterator(); iter.hasNext();) {
            Timer t = (Timer) iter.next();
            System.out.println("Cancelling timer " + t);
            t.cancel();
        }

    }

    // timer callback method
    public void ejbTimeout(Timer timer) {

        String info = (String) timer.getInfo();
        System.out.println("Got ejbTimeout for timer " + info);

        try {
            System.out.println("Calling getMessageContext");
            context.getMessageContext();
        } catch(IllegalStateException ise) {
            System.out.println("getMessageContext() successfully threw illegalStateException");
        }


        // add message to queue
        try {


            InitialContext ic = new InitialContext();
            QueueConnectionFactory qcFactory = (QueueConnectionFactory)
                ic.lookup("java:comp/env/jms/MyQueueConnectionFactory");
            Queue queue = (Queue) ic.lookup("java:comp/env/jms/MyQueue");
            connection = qcFactory.createQueueConnection();

            QueueSession session = connection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
            sender  = session.createSender(queue);

            TextMessage message = session.createTextMessage();
            message.setText(info);
            System.out.println("Sending time out message");
            sender.send(message);
            System.out.println("Time out message sent");
        } catch(NamingException e) {
            e.printStackTrace();
        } catch(JMSException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(connection != null) {
                    connection.close();
                    connection = null;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void ejbActivate() {}
    public void ejbPassivate() {}

}
