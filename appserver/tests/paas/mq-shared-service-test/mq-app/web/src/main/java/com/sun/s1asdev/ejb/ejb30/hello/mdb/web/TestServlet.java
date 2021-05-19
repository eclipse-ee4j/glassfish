/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb30.hello.mdb.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.*;
import jakarta.jms.*;
import jakarta.annotation.Resource;


//@WebServlet(name="testServlet", urlPatterns={"/mdbtest"}, initParams={ @WebInitParam(name="n1", value="v1"), @WebInitParam(name="n2", value="v2") })
public class TestServlet extends HttpServlet {

    private static long TIMEOUT = 90000;

    @Resource(mappedName="jms/ejb_ejb30_hello_mdb_QCF")
    private QueueConnectionFactory queueConFactory;

    //Target Queue
    @Resource(mappedName="jms/ejb_ejb30_hello_mdb_InQueue")
    private jakarta.jms.Queue msgBeanQueue;

    //Reply Queue
    @Resource(mappedName="jms/ejb_ejb30_hello_mdb_OutQueue")
    private jakarta.jms.Queue clientQueue;

    private QueueConnection queueCon;
    private QueueSession queueSession;
    private QueueSender queueSender;
    private QueueReceiver queueReceiver;
    private int numMessages = 2;


    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("filterMessage=" + req.getAttribute("filterMessage"));
        writer.write("testattribute=" + req.getAttribute("testattribute"));
        String msg = "";
        Enumeration en = getInitParameterNames();
        while (en.hasMoreElements()) {
            String name = (String)en.nextElement();
            String value = getInitParameter(name);
            msg += name + "=" + value + ", ";
        }
        writer.write(", initParams: " + msg + "\n");
        doTest(writer);
    }

 public void doTest(PrintWriter writer) {
        try {
            setup();
            doTest(numMessages);
            writer.write("EJB 3.0 MDB" + "PASS");
        } catch(Throwable t) {
            writer.write("EJB 3.0 MDB" + "FAIL");
            t.printStackTrace();
        } finally {
            cleanup();
        }
    }

    public void setup() throws Exception {
        queueCon = queueConFactory.createQueueConnection();
        queueSession = queueCon.createQueueSession
            (false, Session.AUTO_ACKNOWLEDGE);

        // Destination will be specified when actual msg is sent.
        queueSender = queueSession.createSender(null);
        queueReceiver = queueSession.createReceiver(clientQueue);
        queueCon.start();
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
  public void sendMsgs(jakarta.jms.Queue queue, int num)
        throws JMSException {
        for(int i = 0; i < num; i++) {
            Message message = queueSession.createTextMessage("foo #" + (i + 1));
            System.out.println("Sending message " + i + " to " + queue +
                               " at time " + System.currentTimeMillis());
            queueSender.send(queue, message);

            System.out.println("Sent message " + i + " to " + queue +
                               " at time " + System.currentTimeMillis());
        }
    }

    public void doTest(int num)
        throws Exception {
        sendMsgs((jakarta.jms.Queue) msgBeanQueue, num);

        //Now attempt to receive responses to our message
        System.out.println("Waiting for queue message");
        Message recvdmessage = queueReceiver.receive(TIMEOUT);
        if( recvdmessage != null ) {
            System.out.println("Received message : " +
                                   ((TextMessage)recvdmessage).getText());
        } else {
            System.out.println("timeout after " + TIMEOUT + " seconds");
            throw new JMSException("timeout" + TIMEOUT + " seconds");
        }
    }

}
