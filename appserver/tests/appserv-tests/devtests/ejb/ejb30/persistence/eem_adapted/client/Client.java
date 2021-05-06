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

package com.sun.s1asdev.ejb.ejb30.persistence.eem_adapted.client;

import java.io.*;
import java.util.*;
import jakarta.ejb.EJB;
import com.sun.s1asdev.ejb.ejb30.persistence.eem_adapted.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String personName;

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-persistence-eem_adapted");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-persistence-eem_adaptedID");
    }

    public Client (String[] args) {

        personName = "duke";

        if( args.length > 0 ) {
            personName = args[0];
        }

    }

    private static @EJB Sful sful;

    public void doTest() {

        try {

            System.out.println("I am in client");
            System.out.println("calling setName(" + personName + ")");
            sful.setName(personName);

            Map<String, Boolean> map = sful.doTests();

            Iterator<String> iter = map.keySet().iterator();
            while (iter.hasNext()) {
                String testName = iter.next();
                boolean result = map.get(testName);
                stat.addStatus("local " + testName,
                        (result) ? stat.PASS : stat.FAIL);
            }
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }

            return;
    }


}

