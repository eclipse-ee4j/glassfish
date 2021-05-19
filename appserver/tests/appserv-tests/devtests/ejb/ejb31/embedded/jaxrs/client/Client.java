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
import embedded.util.ZipUtil;

import jakarta.ejb.*;
import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;
import java.io.*;
import java.net.*;
import java.util.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;
    private static String type = "xxx";
    private static final String LOCALHOST = "http://localhost:8080/";

    public static void main(String[] s) {
        appName = s[0];
        type = s[2];
        stat.addDescription(appName);
        Client t = new Client();
        if (s[1].equals("ejb")) {
            try {
                System.out.println("Running test via EJB....");
                t.testEJB(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (s[1].equals("rest")) {
            /** This doesn't work as there is a problem to access the servlet **/
            try {
                System.out.println("Running test via REST....");
                t.testREST(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("WRONG TEST TYPE: " + s[1]);
        }

        stat.printSummary(appName + "-" + type);
    }

    private void testEJB(String[] args) {

        boolean pass = true;
        EJBContainer c = null;
        try {
           c = EJBContainer.createEJBContainer();
            Context ic = c.getContext();
            System.out.println("Looking up EJB...");
            Simple ejb = (Simple) ic.lookup("java:global/sample/SimpleEjb!org.glassfish.tests.ejb.sample.Simple");
            System.out.println("Invoking EJB...");
            System.out.println("EJB said: " + ejb.saySomething());
            System.out.println("JPA call returned: " + ejb.testJPA());

        } catch (Exception e) {
            pass = false;
            System.out.println("ERROR calling EJB:");
            e.printStackTrace();
            System.out.println("Saving temp instance dir...");
            ZipUtil.zipInstanceDirectory(appName + "-" + type);

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

    private void testREST(String[] args) {

        boolean pass = true;
        EJBContainer c = null;
        try {
            Map p = new HashMap();
            p.put("org.glassfish.ejb.embedded.glassfish.web.http.port", "8080");
            System.setProperty("org.glassfish.ejb.embedded.keep-temporary-files", "true");
            c = EJBContainer.createEJBContainer(p);
        // ok now let's look up the EJB...
            System.out.println("Testing EJB via REST...");
            System.out.println("EJB said: " + testResourceAtUrl(new URL(LOCALHOST + appName + "-web/test/simple")));
            System.out.println("JPA call returned: " + testResourceAtUrl(new URL(LOCALHOST + appName + "-web/test/jpa")));

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

    private static String testResourceAtUrl(URL url) throws Exception {

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.connect();

            InputStream inputStream = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String firstLineOfText = reader.readLine();//1 line is enough
            System.out.println("Read: " + firstLineOfText);

            connection.disconnect();
            return firstLineOfText;

        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new Exception("could not establish connection to " + url.toExternalForm());
    }

}
