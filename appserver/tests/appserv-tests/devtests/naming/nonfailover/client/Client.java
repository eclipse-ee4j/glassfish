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

package com.sun.s1aspe.naming.nonfailover.client;

import jakarta.jms.*;
import javax.naming.*;
import java.sql.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
    new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {

        stat.addDescription("This is to test naming nonfailover scenario.\n" +
                            "Exisitng and New InitialContexts must be able to "+
                            "talk to the server after a restart assuming there "+
                            "were no requests during resart.");

        Context                 jndiContext = null;
        QueueConnectionFactory  queueConnectionFactory = null;
        Queue                   queue = null;
        boolean                 passed = true;

        try {
            jndiContext = new InitialContext();
            System.out.println("Context created!!!");
        } catch (NamingException e) {
            System.out.println("Could not create JNDI " +
                             "context: " + e.toString());
            stat.addStatus("naming nonfailover main", stat.FAIL);
            stat.printSummary("nonfailoverID");
            passed = false;
            System.exit(1);
        }

        try {
            queueConnectionFactory = (QueueConnectionFactory)
                jndiContext.lookup
                ("java:comp/env/jms/QCFactory");
            System.out.println("looked up QueueConnectionFactory...");
        } catch (NamingException e) {
            System.out.println("JNDI lookup failed: " +
                e.toString());
            stat.addStatus("naming nonfailover main", stat.FAIL);
            passed = false;
            stat.printSummary("nonfailoverID");
            System.exit(1);
        }

        try {
            Thread.sleep(240000);
        } catch (Throwable e) {
            System.out.println("Exception occurred: " + e.toString());
            passed = false;
            stat.addStatus("naming nonfailover main", stat.FAIL);
        }

        try {
            //see if the existing InitialContext is still alive
            if (jndiContext != null) {
                System.out.println("Existing InitialContext is alive after server restart!");
            }

            queue = (Queue) jndiContext.lookup("java:comp/env/jms/SampleQueue");
            System.out.println("looked up Queue...");

            //creating new InitialContext
            jndiContext = new InitialContext();
            System.out.println("Creating new InitialContext...");
            queueConnectionFactory = (QueueConnectionFactory)
              jndiContext.lookup
              ("java:comp/env/jms/QCFactory");
            System.out.println("looked up QueueConnectionFactory with new InitialContext..." +
                               queueConnectionFactory);

        } catch (NamingException e) {
            System.out.println("JNDI lookup failed: " +
                e.toString());
            stat.addStatus("naming nonfailover main", stat.FAIL);
            stat.printSummary("nonfailoverID");
            passed = false;
            System.exit(1);
        }
        if (passed) stat.addStatus("naming nonfailover main", stat.PASS);
        stat.printSummary("nonfailoverID");
        System.exit(0);
    } // main
} // class


