/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.hk2.devtest.cdi.ear.runner;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.tests.utils.NucleusStartStopTest;
import org.glassfish.tests.utils.NucleusTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.oracle.hk2.devtest.cdi.ear.ejb1.Ejb1Remote;
import com.oracle.hk2.devtest.cdi.ear.ejb2.Ejb2Remote;

/**
 *
 * @author jwells
 *
 */
public class CDIEarTest extends NucleusStartStopTest {
    private final static String APP_JAR = "cdi/ear/app/target/app.ear";
    private final static String APP_NAME = "app";

    private final static String SOURCE_HOME = System.getProperty("source.home", "$");
    private final static String SOURCE_HOME_APP = "/appserver/tests/hk2/" + APP_JAR;

    private final static String EJB1_JNDI_NAME = "java:global/app/ejb1/Ejb1";
    private final static String EJB2_JNDI_NAME = "java:global/app/ejb2/Ejb2";

    private final static String WAR1_URL = "http://localhost:8080/war1/war1";
    private final static String WAR2_URL = "http://localhost:8080/war2/war2";

    private boolean deployed1;
    private Context context;

    @BeforeTest
    public void beforeTest() throws NamingException {
        context = new InitialContext();

        String appJar = APP_JAR;
        if (!SOURCE_HOME.startsWith("$")) {
            appJar = SOURCE_HOME + SOURCE_HOME_APP;
        }

        deployed1 = NucleusTestUtils.nadmin("deploy", appJar);
        Assert.assertTrue(deployed1);
    }

    @AfterTest
    public void afterTest() throws NamingException {
        if (deployed1) {
            NucleusTestUtils.nadmin("undeploy", APP_NAME);
            deployed1 = false;
        }

        if (context != null) {
            context.close();
            context = null;
        }
    }

    private Object lookupWithFiveSecondSleep(String jndiName) throws NamingException, InterruptedException {
        long sleepTime = 5L * 1000L;
        long interval = 100L;

        while (sleepTime > 0) {
            try {
                return context.lookup(jndiName);
            }
            catch (NamingException ne) {
                sleepTime -= interval;
                if (sleepTime <= 0) {
                    throw ne;
                }

                if ((sleepTime % 1000L) == 0) {
                    System.out.println("Sleeping another " + (sleepTime / 1000) + " seconds...");
                }

                Thread.sleep(interval);
            }

        }

        throw new AssertionError("Should never get here");
    }

    @Test
    public void testInjectFromLib1IntoEjb1() throws NamingException, InterruptedException {
        Ejb1Remote ejb1 = (Ejb1Remote) lookupWithFiveSecondSleep(EJB1_JNDI_NAME);

        ejb1.isLib1HK2ServiceAvailable();
    }

    @Test
    public void testInjectFromEjb1IntoEjb1() throws NamingException, InterruptedException {
        Ejb1Remote ejb1 = (Ejb1Remote) lookupWithFiveSecondSleep(EJB1_JNDI_NAME);

        ejb1.isEjb1HK2ServiceAvailable();

    }

    @Test
    public void testInjectedLib1Ejb1War1IntoWar1() {
        String fromWar1 = NucleusTestUtils.getURL(WAR1_URL);

        Assert.assertTrue(fromWar1.contains("success"),
                "Does not contain the word success: " + fromWar1);
    }

    @Test
    public void testInjectFromLib1IntoEjb2() throws NamingException, InterruptedException {
        Ejb2Remote ejb2 = (Ejb2Remote) lookupWithFiveSecondSleep(EJB2_JNDI_NAME);

        ejb2.isLib1HK2ServiceAvailable();
    }

    @Test
    public void testInjectFromEjb1IntoEjb2() throws NamingException, InterruptedException {
        Ejb2Remote ejb2 = (Ejb2Remote) lookupWithFiveSecondSleep(EJB2_JNDI_NAME);

        ejb2.isEjb1HK2ServiceAvailable();
    }

    @Test
    public void testInjectFromEjb2IntoEjb2() throws NamingException, InterruptedException {
        Ejb2Remote ejb2 = (Ejb2Remote) lookupWithFiveSecondSleep(EJB2_JNDI_NAME);

        ejb2.isEjb2HK2ServiceAvailable();
    }

    @Test
    public void testInjectedLib1Ejb1Ejb2War2IntoWar2() {
        String fromWar2 = NucleusTestUtils.getURL(WAR2_URL);

        Assert.assertTrue(fromWar2.contains("success"),
                "Does not contain the word success: " + fromWar2);
    }
}
