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

package com.sun.s1asdev.ejb.timer.sessiontimersecurity;

import jakarta.ejb.TimedObject;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerHandle;
import jakarta.ejb.TimerService;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
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
    public void dummyBusinessMethod() {
        try {
            System.out.println("dummyBusinessMethod(): getCallerPrincipal() = "
                + context.getCallerPrincipal());
            System.out.println("dummyBusinessMethod(): isCallerInRole(foo) = "
                + context.isCallerInRole("foo"));
        } catch(IllegalStateException ise) {
            System.out.println(
                "dummyBusinessMethod(): isCallerInRole should not throw "
                + "illegalstateexception in business method");
            throw ise;
        }
    }

        public TimerHandle createTimer(int ms) {
            System.out.println("Calling createTimer");
            System.out.println("createTimer(): getCallerPrincipal() = "
                + context.getCallerPrincipal());
            System.out.println("createTimer(): isCallerInRole(foo) = "
                + context.isCallerInRole("foo"));
            try {
                System.out.println("Calling isCallerInRole");
                boolean result = context.isCallerInRole("foo");
                if (!result) {
                    System.out.println("createTimer(): isCallerInRole failed");
                    throw new Exception("isCallerInRole() should not fail in a business method");
                }
            } catch(IllegalStateException ise) {
                System.out.println("isCallerInRole should not throw "
                    + "illegalstateexception in business method");
                throw ise;
            } catch(Exception ise) {
                // no security-role-ref defined so illegalargumentexception is thrown
            }

            try {
                System.out.println("Calling getMessageContext");
                context.getMessageContext();
            } catch(IllegalStateException ise) {
                System.out.println("getMessageContext() successfully threw illegalStateException");
            }


                TimerService timerService = context.getTimerService();
                Timer timer = timerService.createTimer(ms, "created timer");
                return timer.getHandle();
        }

        // timer callback method
        public void ejbTimeout(Timer timer) {
        try {
            System.out.println("Calling ejbTimeout");
            java.security.Principal principal = context.getCallerPrincipal();
            System.out.println("In ejbTimeout(): getCallerPrincipal(), principal - "
                + principal);
            System.out.println("In ejbTimeout(): isCallerInRole(foo), result - "
                + context.isCallerInRole("foo"));
        } catch(IllegalStateException ise) {
            System.out.println("ejbTimeout(): getCallerPrincipal() failed by "
                + "throwing IllegalStateException");
        }
        try {
            System.out.println("Calling isCallerInRole");
            boolean result = context.isCallerInRole("foo");
            System.out.println("In ejbTimeout(): isCallerInRole() = " + result);
            if (!result) {
                System.out.println("ejbTimeout(): isCallerInRole() failed" );
            }
        } catch(IllegalStateException ise) {
            System.out.println("ejbTimeout(): isCallerInRole() failed by "
                + "throwing IllegalStateException");
        }

        try {
            System.out.println("Calling getMessageContext");
            context.getMessageContext();
        } catch(IllegalStateException ise) {
            System.out.println("ejbTimeout(): getMessageContext() successfully "
                + "threw IllegalStateException");
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
                        message.setText("ejbTimeout() invoked");
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
