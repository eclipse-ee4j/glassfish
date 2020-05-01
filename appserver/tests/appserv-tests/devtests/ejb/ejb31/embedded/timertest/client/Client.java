/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.tests.ejb.timertest.SimpleEjb;

import java.util.Map;
import java.util.HashMap;
import jakarta.ejb.*;
import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    public static void main(String[] s) {
        appName = s[0];
        System.err.println(".......... Testing module: " + appName);
        stat.addDescription(appName);
        Client t = new Client();
        try {
            t.test(appName, 1);
        } catch (Exception e) {
            stat.addStatus("EJB embedded with timertest 1", stat.FAIL);
            e.printStackTrace();
        }
        System.err.println("------------------------");
        if (s.length == 2 && s[1].equals("false")) {
            System.err.println("-------This part of the test will fail if ran against Full Profile ------------");
            try {
                t.test(appName, 2);
            } catch (Exception e) {
                stat.addStatus("EJB embedded with timertest 2", stat.FAIL);
                e.printStackTrace();
            }
            System.err.println("------------------------");
        } else {
            System.err.println("-------Do not run 2nd time until timer app reload is fixed ------------");
        }
        stat.printSummary(appName + "ID");
        System.exit(0);
    }

    private void test(String module, int id) {

        EJBContainer c = EJBContainer.createEJBContainer();
        // ok now let's look up the EJB...
        Context ic = c.getContext();
        try {
            System.err.println("Looking up EJB...");
            SimpleEjb ejb = (SimpleEjb) ic.lookup("java:global/" + module + "/SimpleEjb");
            System.err.println("Invoking EJB...");
            ejb.createTimer();
            Thread.sleep(8000);
            boolean result = ejb.verifyTimer();
            System.err.println("EJB timer called: " + result);
            if (!result)
                throw new Exception ("EJB timer was NOT called for 1 or 2 timers");

            stat.addStatus("EJB embedded with timertest" + id, stat.PASS);
        } catch (Exception e) {
            stat.addStatus("EJB embedded with timertest" + id, stat.FAIL);
            System.err.println("ERROR calling EJB:");
            e.printStackTrace();
        } finally {
            c.close();
        }
        System.err.println("Done calling EJB");
    }

}
