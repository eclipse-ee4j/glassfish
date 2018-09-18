/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.tests.ejb.embedded.SimpleEjb;

import javax.ejb.*;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;

import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;
import org.testng.annotations.*;
import org.testng.Assert;

public class Client {

    private static final String TEST_NAME = "ejb-embedded";
    static String result = "";

    private static String appName = "app";

    public static void main(String[] s) throws Exception {
        appName = s[0];
        System.out.println(".......... Testing module: " + appName);
        Client t = new Client();
        t.testEmbedded();
   }

   @Test
   public void testEmbedded() throws Exception {
        try {
            boolean result = test();
            Assert.assertEquals(result, true,"Unexpected Results");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    private boolean test() throws Exception {
        boolean rc = false;
        EJBContainer c = null;
        try {
            c = EJBContainer.createEJBContainer();
            Context ic = c.getContext();
            System.out.println("Looking up EJB...");
            SimpleEjb ejb = (SimpleEjb) ic.lookup("java:global/" + appName + "/SimpleEjb");
            System.out.println("Invoking EJB...");
            String result = ejb.saySomething();
            System.out.println("EJB said: " + result);
            System.out.println("Done calling EJB");
            rc = true;
            c.close();
            c = EJBContainer.createEJBContainer();
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                    rc = false;
                    e.printStackTrace();
                }
            }
        }

        return rc;
    }

}
