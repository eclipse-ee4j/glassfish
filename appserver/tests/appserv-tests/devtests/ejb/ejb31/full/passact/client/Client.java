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

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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

    public Client(String[] args) {}

    public void doTest() {

        try {
            int numHellos = 8; // 8 is the batch size, so 8 should be guaranteed to be pasivated

            Hello[] hellos = new Hello[numHellos];
            int passivationCount = 0;

            for(int i = 0; i < numHellos; i++) {

                hellos[i] = (Hello) new InitialContext().lookup("java:global/"+appName+"/HelloBean");
                hellos[i].hello();
            }

            try {
                System.out.println("Waiting for passivation...");
                Thread.sleep(20000);
            } catch(Exception e) {
                e.printStackTrace();
            }

            for(int i = 0; i < numHellos; i++) {

                hellos[i].hello();
                if( hellos[i].passivatedAndActivated() ) {
                    passivationCount++;
                }
            }

            if( passivationCount != numHellos ) {
                System.out.println("Passivation failed -- count = " + passivationCount + " instead of expected " + numHellos);
                throw new EJBException("passivation failed");
            }

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            stat.addStatus("local main", stat.FAIL);
            e.printStackTrace();
        }

    }


}
