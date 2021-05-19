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

package com.acme;

import jakarta.ejb.*;
import jakarta.annotation.*;

import javax.naming.InitialContext;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;


public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    public static void main(String args[]) {

        appName = args[0];
        stat.addDescription(appName);
        Client client = new Client(args);
        client.doTest();
        stat.printSummary(appName + "ID");
    }

    public Client(String[] args) {
    }

    public void doTest() {

        try {

            RemoteSingleton singleton = (RemoteSingleton) new InitialContext().lookup("java:global/" + appName + "/SingletonBean!com.acme.RemoteSingleton");

            // Also make sure we can look it up via the product-specific
            // global JNDI name specified via ejb-jar.xml
            RemoteSingleton singleton2 = (RemoteSingleton) new InitialContext().lookup("ejb_ejb31_full_schema_SingletonRemote");

            int sleepSeconds = 10;
            System.out.println("Sleeping for " + sleepSeconds + " seconds before checking " +
                               "results...");
            // wait a few seconds for timeout to happen
            Thread.sleep(sleepSeconds * 1000);

            System.out.println("Woke up");

            boolean testResult = singleton.getTestResult();

            if( !testResult ) {
                throw new EJBException("testResult = false");
            }

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            stat.addStatus("local main", stat.FAIL);
            e.printStackTrace();
        }

    }

}
