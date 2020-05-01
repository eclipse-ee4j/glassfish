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
import java.net.*;
import java.io.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    public static void main(String[] s) {
        appName = s[0];
        stat.addDescription(appName);
        Client t = new Client();
        if (s.length == 2 && s[1].equals("servlet")) {
            try {
                t.testServlet(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                t.test(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        stat.printSummary(appName + "ID");
    }

    private void test(String[] args) {

        boolean pass = true;
        EJBContainer c = null;
        if (args.length == 2 && args[1].equals("installed_instance")) {
            Map p = new HashMap();
            p.put("org.glassfish.ejb.embedded.glassfish.instance.reuse", "true");
            p.put("org.glassfish.ejb.embedded.keep-temporary-files", "true");
            c = EJBContainer.createEJBContainer(p);
        } else {
            c = EJBContainer.createEJBContainer();
        }
        // ok now let's look up the EJB...
        Context ic = c.getContext();
        try {
            System.out.println("Looking up EJB...");
            Simple ejb = (Simple) ic.lookup("java:global/sample/SimpleEjb");
            System.out.println("Invoking EJB...");
            System.out.println("EJB said: " + ejb.saySomething());
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

    private void testServlet(String[] args) {

        boolean pass = true;
        EJBContainer c = null;
        try {
            Map p = new HashMap();
            p.put("org.glassfish.ejb.embedded.glassfish.web.http.port", "8080");
            // p.put("org.glassfish.ejb.embedded.glassfish.instance.reuse", "true");
            System.setProperty("org.glassfish.ejb.embedded.keep-temporary-files", "true");
            c = EJBContainer.createEJBContainer(p);
            String url = "http://localhost:8080/" + "ejb-ejb31-embedded-ejbwinwar-web/mytest";

            System.out.println("invoking webclient servlet at " + url);

            URL u = new URL(url);

            HttpURLConnection c1 = (HttpURLConnection)u.openConnection();
            int code = c1.getResponseCode();
            InputStream is = c1.getInputStream();
            BufferedReader input = new BufferedReader (new InputStreamReader(is));
            String line = null;
            while((line = input.readLine()) != null)
                System.out.println(line);
            if(code != 200) {
                throw new RuntimeException("Incorrect return code: " + code);
            }

            System.out.println("Testing EJB via Servlet...");

        } catch (Exception e) {
            pass = false;
            System.out.println("ERROR calling EJB:");
            e.printStackTrace();
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                    pass = false;
                    System.out.println("ERROR Closing container:");
                    e.printStackTrace();
                }
            }
        }
        stat.addStatus("EJB embedded " + appName + " EJB", (pass)? stat.PASS : stat.FAIL);
        System.out.println("..........FINISHED test");
    }

}
