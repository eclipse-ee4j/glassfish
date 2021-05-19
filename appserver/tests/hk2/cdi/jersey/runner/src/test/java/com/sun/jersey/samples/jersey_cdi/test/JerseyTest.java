/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.jersey.samples.jersey_cdi.test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.tests.utils.NucleusStartStopTest;
import org.glassfish.tests.utils.NucleusTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author jwells
 *
 */
public class JerseyTest extends NucleusStartStopTest {
    private final static String SOURCE_HOME = System.getProperty("source.home", "$");
    private final static String JERSEY_WAR = "cdi/jersey/war/target/jersey-cdi.war";
    private final static String SOURCE_HOME_WAR = "/appserver/tests/hk2/" + JERSEY_WAR;
    private final static String JERSEY_WAR_APP_NAME = "jersey-cdi";

    private boolean deployed1;
    private Context context;

    @BeforeTest
    public void beforeTest() throws NamingException {
        context = new InitialContext();

        String jerseyWar = JERSEY_WAR;
        if (!SOURCE_HOME.startsWith("$")) {
            jerseyWar = SOURCE_HOME + SOURCE_HOME_WAR;
        }

        deployed1 = NucleusTestUtils.nadmin("deploy", jerseyWar);
        Assert.assertTrue(deployed1);
    }

    @AfterTest
    public void afterTest() throws NamingException {
        if (deployed1) {
            NucleusTestUtils.nadmin("undeploy", JERSEY_WAR_APP_NAME);
            deployed1 = false;
        }

        if (context != null) {
            context.close();
            context = null;
        }
    }

    @Test
    public void testJustATest() {
    }

}
