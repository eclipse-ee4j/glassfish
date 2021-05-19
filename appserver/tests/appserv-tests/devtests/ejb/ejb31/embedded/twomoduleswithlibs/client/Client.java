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
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            t.testError();
        } catch (Exception e) {
            e.printStackTrace();
        }
        stat.printSummary(appName + "ID");

        System.exit(0);
    }

    private void testError() {

        Map<String, Object> p = new HashMap<String, Object>();
        p.put(EJBContainer.APP_NAME, "foo");
        p.put(EJBContainer.MODULES, new String[] {"sample", "foo", "bar"});

        try {
            System.out.println("Requesting wrong set of modules....");
            EJBContainer c = EJBContainer.createEJBContainer(p);
            stat.addStatus("EJB embedded create new container with errors in MODULES", stat.FAIL);
        } catch (EJBException e) {
            String msg = e.getMessage();
            System.out.println("Caught expected: " + msg);
            stat.addStatus("EJB embedded create new container with errors in MODULES", stat.PASS);
        }
    }


    private void test() {

        EJBContainer c = null;
        try {
            Map<String, Object> p = new HashMap<String, Object>();
            p.put(EJBContainer.APP_NAME, "foo");
            c = EJBContainer.createEJBContainer(p);
            if (c == null) {
                stat.addStatus("EJB embedded module 1", stat.FAIL);
                return;
            }
            // ok now let's look up the EJB...
            Context ic = c.getContext();
            System.out.println("Looking up EJB...");
            SimpleEjb ejb = (SimpleEjb) ic.lookup("java:global/foo/sample/SimpleEjb");
            System.out.println("Invoking EJB from module 1...");
            System.out.println("EJB said: " + ejb.saySomething());
            System.out.println("JPA call returned: " + ejb.testJPA());

            System.out.println("Done calling EJB from module 1");

        } catch (Exception e) {
            System.out.println("ERROR calling EJB:");
            e.printStackTrace();
            stat.addStatus("EJB embedded module 1", stat.FAIL);
        } finally {
            if (c != null)
                c.close();
        }
        stat.addStatus("EJB embedded module 1", stat.PASS);
        System.out.println("..........FINISHED 2 modules Embedded test");
    }
}
