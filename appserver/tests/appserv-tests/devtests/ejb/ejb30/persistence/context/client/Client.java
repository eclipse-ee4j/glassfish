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

package com.sun.s1asdev.ejb.ejb30.persistence.context.client;

import java.io.*;
import java.util.*;
import jakarta.ejb.EJB;
import com.sun.s1asdev.ejb.ejb30.persistence.context.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String personName;

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-persistence-context");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-persistence-contextID");
    }

    public Client (String[] args) {

        personName = "duke";

        if( args.length > 0 ) {
            personName = args[0];
        }

    }

    private static @EJB Sless sless;

    public void doTest() {

        try {

            System.out.println("I am in client");
            System.out.println("calling createPerson(" + personName + ")");
            sless.createPerson(personName);
            System.out.println("created ");
            System.out.println("calling findPerson(" + personName + ")");
            Person p = sless.findPerson(personName);
            if( p == null ) {
                throw new Exception("findPerson returned null");
            }
            System.out.println("found " + p);

            System.out.println("calling nonTxFindPerson(" + personName + ")");
            System.out.println("found " + sless.nonTxFindPerson(personName));

            System.out.println("calling nonTxFindPerson(" + personName + ")");
            System.out.println("found " + sless.nonTxFindPerson(personName));

            System.out.println("calling nonTxBlah(" + personName + ")");
            sless.createPerson("Bob");
            sless.nonTxTest2("Bob");

            System.out.println("removing Person(" + personName + ")");
            sless.removePerson(personName);
            System.out.println("removed Person(" + personName + ")");

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }

            return;
    }

}

