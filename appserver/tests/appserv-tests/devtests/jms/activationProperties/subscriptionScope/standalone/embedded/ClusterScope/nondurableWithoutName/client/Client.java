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

package org.glassfish.test.jms.activationproperties.client;

import javax.naming.*;
import jakarta.jms.*;
import org.glassfish.test.jms.activationproperties.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private final static SimpleReporterAdapter STAT = new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        STAT.addDescription("subscriptionScope-standalone-embedded-clusterScopeNonDurableWithoutNameID");
        Client client = new Client(args);
        client.doTest();
        STAT.printSummary("subscriptionScope-standalone-embedded-clusterScopeNonDurableWithoutNameID");
    }

    public Client (String[] args) {
    }

    public void doTest() {
        String ejbName = "MySessionBean";
        String text = "Hello World!";
        try {
            Context ctx = new InitialContext();
            MySessionBeanRemote beanRemote = (MySessionBeanRemote) ctx.lookup(MySessionBeanRemote.RemoteJNDIName);
            beanRemote.sendMessage(text);

            String result = beanRemote.checkMessage(text);
            if (result.startsWith("Success"))
                STAT.addStatus("subscriptionScope-standalone-embedded-clusterScopeNonDurableWithoutNameID " + ejbName, STAT.PASS);
            else {
                System.out.println(result);
                STAT.addStatus("subscriptionScope-standalone-embedded-clusterScopeNonDurableWithoutNameID " + ejbName, STAT.FAIL);
            }
        } catch(Exception e) {
            e.printStackTrace();
            STAT.addStatus("subscriptionScope-standalone-embedded-clusterScopeNonDurableWithoutNameID " + ejbName, STAT.FAIL);
        }
    }
}
