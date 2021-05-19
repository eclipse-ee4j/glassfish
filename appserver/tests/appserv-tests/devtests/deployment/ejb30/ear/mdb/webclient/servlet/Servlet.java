/*
 * Copyright (c) 2005, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.jms.*;
import jakarta.annotation.Resource;

public class Servlet extends HttpServlet {

    @Resource(name="FooCF")
    private QueueConnectionFactory queueConFactory;

    @Resource(name="MsgBeanQueue")
    private jakarta.jms.Queue msgBeanQueue;

    @Resource(name="ClientQueue")
    private jakarta.jms.Queue clientQueue;

    private QueueConnection queueCon;
    private QueueSession queueSession;
    private QueueSender queueSender;
    private QueueReceiver queueReceiver;

    private int numMessages = 1;
    // in milli-seconds
    private static long TIMEOUT = 90000;



    public void  init() throws ServletException {

        super.init();
        log("init()...");
    }

    public void sendMsgs(jakarta.jms.Queue queue, Message msg, int num) throws JMSException {
        for(int i = 0; i < num; i++) {
            System.out.println("Sending message " + i + " to " + queue +
                               " at time " + System.currentTimeMillis());
            queueSender.send(queue, msg);
            System.out.println("Sent message " + i + " to " + queue +
                               " at time " + System.currentTimeMillis());
        }
    }


    public void service ( HttpServletRequest req , HttpServletResponse resp ) throws ServletException, IOException {

        log("service()...");

        try {
            resp.setContentType("text/html");
            PrintWriter out = resp.getWriter();
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<p>");

            queueCon = queueConFactory.createQueueConnection();

            queueSession = queueCon.createQueueSession
                (false, Session.AUTO_ACKNOWLEDGE);

            // Producer will be specified when actual msg is sent.
            queueSender = queueSession.createSender(null);

            queueReceiver = queueSession.createReceiver(clientQueue);

            queueCon.start();


            Destination dest = msgBeanQueue;

            Message message = queueSession.createTextMessage("foo");

            message.setBooleanProperty("flag", true);
            message.setIntProperty("num", 1);
            sendMsgs((jakarta.jms.Queue) dest, message, numMessages);

            log("Waiting for queue message");
            Message recvdmessage = queueReceiver.receive(TIMEOUT);
            if( recvdmessage != null ) {
                log("Received message : " +
                                   ((TextMessage)recvdmessage).getText());
                out.println("Message is [" + recvdmessage + "]");
                out.println("</body>");
                out.println("</html>");
                out.flush();
                out.close();
            } else {
                log("timeout after " + TIMEOUT + " seconds");
                throw new JMSException("timeout" + TIMEOUT + " seconds");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("webclient servlet test failed");
            throw new ServletException(ex);
        } finally {
            cleanup();
        }
    }


    public void cleanup() {
        try {
            if( queueCon != null ) {
                queueCon.close();
            }
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

    public void  destroy() {
        log("destroy()...");
    }

    public void log (String message) {
       System.out.println("[webclient Servlet]:: " + message);
    }
}
