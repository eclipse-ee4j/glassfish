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

import org.glassfish.tests.ejb.sample.Simple;

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
        } catch (Exception e) {
            e.printStackTrace();
        }
        stat.printSummary(appName + "ID");
    }

    private void test() {

        boolean pass = true;
        Map<String, Object> p = new HashMap<String, Object>();
        p.put(EJBContainer.APP_NAME, "foo");

        EJBContainer c = EJBContainer.createEJBContainer(p);
        // ok now let's look up the EJB...
        Context ic = c.getContext();
        try {
            System.out.println("Looking up EJB...");
            Simple ejb = (Simple) ic.lookup("java:global/foo/sample/SimpleEjb");
            System.out.println("Invoking EJB...");
            System.out.println("EJB said: " + ejb.saySomething());
            System.out.println("Other EJB said: " + ejb.askOther());
            System.out.println("JPA call returned: " + ejb.testJPA());

        } catch (Exception e) {
            pass = false;
            System.out.println("ERROR calling EJB:");
            e.printStackTrace();
        }
        System.out.println("Done calling EJB");

        System.out.println("Closing container ...");
        try {
            c.close();
        } catch (Exception e) {
            pass = false;
            System.out.println("ERROR Closing container:");
            e.printStackTrace();
        }
        stat.addStatus("EJB embedded with JPA", (pass)? stat.PASS : stat.FAIL);
        System.out.println("..........FINISHED Embedded test");
    }
}
