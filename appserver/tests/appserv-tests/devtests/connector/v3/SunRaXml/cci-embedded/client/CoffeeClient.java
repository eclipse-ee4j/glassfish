/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.connector.cci;

import java.util.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class CoffeeClient {
    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        String testId = "J2EE Connectors : Embedded Adapter Tests";
        try {

            if (args.length == 1) {
                testId = args[0];
            }

            System.err.println(testId + " : CoffeeClient started in main...");
            stat.addDescription("J2EE Connectors 1.5: Embedded CCI Adapter tests");
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SimpleCoffee");

            CoffeeRemoteHome home =
                    (CoffeeRemoteHome) PortableRemoteObject.narrow(objref,
                            CoffeeRemoteHome.class);

            CoffeeRemote coffee = home.create();

            int count = coffee.getCoffeeCount();
            System.err.println("Coffee count = " + count);

            System.err.println("Inserting 3 coffee entries...");
            coffee.insertCoffee("Mocha", 10);
            coffee.insertCoffee("Espresso", 20);
            coffee.insertCoffee("Kona", 30);

            int newCount = coffee.getCoffeeCount();
            System.err.println("Coffee count = " + newCount);
            if (count == (newCount - 3)) {
                stat.addStatus("Connector:cci Connector " + testId + " rar Test status:", stat.PASS);
            } else {
                stat.addStatus("Connector:cci Connector " + testId + " rar Test status:", stat.FAIL);
            }


        } catch (Exception ex) {
            System.err.println("Caught an unexpected exception!");
            stat.addStatus("Connector:CCI Connector " + testId + " rar Test status:", stat.FAIL);
            ex.printStackTrace();
        } finally {
            //print test summary
            stat.printSummary(testId);
        }
    }


}
