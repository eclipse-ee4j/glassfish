/*
 * Copyright (c) 2004, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.devtest.admin.notification.lookup.client;

import java.util.Properties;
import java.math.BigDecimal;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.devtest.admin.notification.lookup.ejb.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 * A simple java client. This uses the services provided by the
 * <code>LookupBean</code>.
 */
public class LookupClient {

    private SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

    LookupClient() {
    }

   /**
    * The main method of the client.
    */
    public static void main(String[] args) {
        LookupClient client = new LookupClient();
        client.run(args);
    }

    private void run(String[] args) {

        String url = null;
        String testId = null;
        String jndiName = null;
        Context context = null;
        String ctxFactory = null;
        java.lang.Object obj = null;

        try {
            stat.addDescription("Tests dynamic reconfig of resources");

            if (args.length == 3) {
                url = args[0];
                ctxFactory = args[1];
                jndiName = args[2];
            }
            testId = System.getProperty("testId", "0");

            if ( (url == null) || (ctxFactory == null) ) {
                // Initialize the Context with default properties
                context = new InitialContext();
                System.out.println("Default Context Initialized...");
                // Create Home object
                obj = context.lookup("java:comp/env/ejb/lookupBean");
            } else {
                Properties env = new Properties();
                env.put("java.naming.provider.url", url);
                env.put("java.naming.factory.initial", ctxFactory);
                // Initialize the Context with JNDI specific properties
                context = new InitialContext(env);
                System.out.println("Context Initialized with " +
                                   "URL: " + url + ", Factory: " + ctxFactory);
                // Create Home object
                obj = context.lookup(jndiName);
            }

            LookupRemoteHome home =
               (LookupRemoteHome) PortableRemoteObject.narrow(obj,
                                            LookupRemoteHome.class);
            LookupRemote bean = home.create();

            runDefaultTest(bean, "converter");
        } catch (Exception ex) {
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        }
    }

    private void runDefaultTest(LookupRemote bean, String testId) {
        try {
            BigDecimal param = new BigDecimal ("100.00");
            BigDecimal amount = bean.dollarToYen(param);
            System.out.println("\n\n\n===========Beginning Simple Test=====\n\n");
            System.out.println("$100 is : "+amount+"Yen");
            amount = bean.yenToEuro(param);
            System.out.println("Yen is :"+amount+"Euro");
            stat.addStatus(testId, stat.PASS);
        } catch (Exception e) {
            stat.addStatus(testId, stat.FAIL);
            e.printStackTrace();
        } finally {
            stat.printSummary("Test Result for #" + testId);
        }
    }

}
