/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.test.jms.annotation.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.glassfish.test.jms.annotation.ejb.MySessionBeanRemote;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private final static SimpleReporterAdapter STAT = new SimpleReporterAdapter("appserv-tests");
    private final static String name = "annotation-stateless-ejb";

    public static void main (String[] args) {
        STAT.addDescription(name);
        Client client = new Client(args);
        client.doTest();
        STAT.printSummary(name + "ID");
    }

    public Client (String[] args) {
    }

    public void doTest() {
        String text = "Hello JMS 2.0!";
        try {
            Context ctx = new InitialContext();
            MySessionBeanRemote beanRemote = (MySessionBeanRemote) ctx.lookup(MySessionBeanRemote.RemoteJNDIName);
            beanRemote.sendMessage(text);
            boolean received = beanRemote.checkMessage(text);
            if (received)
                STAT.addStatus(name, STAT.PASS);
            else
                STAT.addStatus(name, STAT.FAIL);
        } catch(NamingException e) {
            e.printStackTrace();
            STAT.addStatus(name, STAT.FAIL);
        }
    }
}
