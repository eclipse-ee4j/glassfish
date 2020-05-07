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

import org.glassfish.tests.ejb.remote.SimpleRemote;

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
    private static int run = 0;

    public static void main(String[] s) {
        appName = s[0];
        System.out.println(".......... Testing module: " + appName);
        stat.addDescription(appName);
        Client t = new Client();
        try {
            t.test(appName);
            System.err.println("---------------------");
            //t.test(appName);
            System.err.println("---------------------");
        } catch (Exception e) {
            e.printStackTrace();
            stat.addStatus("EJB embedded with remote " + run, stat.FAIL);
        }
        stat.printSummary(appName + "ID");
        System.exit(0);

    }

    private void test(String module) {

        run++;
        EJBContainer c = EJBContainer.createEJBContainer();
        // ok now let's look up the EJB...
        Context ic = c.getContext();
        try {
            System.out.println("Looking up EJB...");
            SimpleRemote ejb = (SimpleRemote) ic.lookup("java:global/" + module + "/SimpleEjb");
            System.out.println("Invoking EJB...");
            String result = ejb.saySomething();
            System.out.println("EJB said: " + result);

            c.close();
            stat.addStatus("EJB embedded with remote " + run, stat.PASS);
        } catch (Exception e) {
            stat.addStatus("EJB embedded with remote " + run, stat.FAIL);
            System.out.println("ERROR calling EJB:");
            e.printStackTrace();
        }
        System.out.println("Done calling EJB");
    }

}
