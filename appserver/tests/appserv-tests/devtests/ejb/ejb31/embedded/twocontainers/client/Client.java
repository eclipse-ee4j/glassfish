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

import org.glassfish.tests.ejb.sample.SimpleEjb;

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
        stat.addDescription(appName);
        Client t = new Client();
        try {
            t.test();
            t.test();
        } catch (Exception e) {
            e.printStackTrace();
        }
        stat.printSummary(appName + "ID");
        System.exit(0);

    }

    private void test() {

        Map<String, Object> p = new HashMap<String, Object>();
        p.put(EJBContainer.MODULES, "sample");

        EJBContainer c = EJBContainer.createEJBContainer(p);
        // ok now let's look up the EJB...
        Context ic = c.getContext();
        try {
            System.err.println("Looking up EJB...");
            SimpleEjb ejb = (SimpleEjb) ic.lookup("java:global/sample/SimpleEjb");
            System.err.println("Invoking EJB...");
            int count = ejb.testJPA();
            if (count == 1) {
                System.err.println("1st call returned 1");
            } else {
                throw new Exception("ERROR: 1st call returned " + count);
            }
            count = ejb.testJPA();
            if (count == 2) {
                System.err.println("2nd call returned 2");
            } else {
                throw new Exception("ERROR: 2nd call returned " + count);
            }

            stat.addStatus("EJB 2container embedded with JPA", stat.PASS);
        } catch (Exception e) {
            stat.addStatus("EJB 2container embedded with JPA", stat.FAIL);
            System.err.println("ERROR running test:");
            e.printStackTrace();
        } finally {
            try {
                c.close();
            } catch (Exception e) {
                stat.addStatus("EJB 2container embedded close container", stat.FAIL);
                System.err.println("ERROR Closing container:");
                e.printStackTrace();
            }
        }
        System.err.println("..........FINISHED test");
    }
}
