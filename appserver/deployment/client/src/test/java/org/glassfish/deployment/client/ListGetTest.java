/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.deployment.client;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

/**
 */
public class ListGetTest {

    public ListGetTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Ignore
    @Test
    public void testListGetTest() {
        System.out.println("testListGetTest");
        DeploymentFacility df = DeploymentFacilityFactory.getDeploymentFacility();
        ServerConnectionIdentifier sci = new ServerConnectionIdentifier();
        sci.setHostName("localhost");
        sci.setHostPort(4848); // 8080 for the REST client
        sci.setUserName("admin");
        sci.setPassword("");

        df.connect(sci);

        try {
            Target[] results = df.listTargets();
            System.out.println("Targets returned:");
            for (Target tid : results) {
                System.out.println(tid.getName());
            }

            String contextRoot = df.getContextRoot("webapps-simple");
            System.out.println("Context root for webapps-simple:" + contextRoot);

            ModuleType type = df.getModuleType("webapps-simple");
            System.out.println("Module type for webapps-simple:" + type);

            ModuleType type2 = df.getModuleType("i18n-simple");
            System.out.println("Module type for i18n-simple:" + type2);

            ModuleType type3 = df.getModuleType("singleton");
            System.out.println("Module type for singleton:" + type3);

            ModuleType type4 = df.getModuleType("generic-ra");
            System.out.println("Module type for generic-ra:" + type4);

            ModuleType type5 = df.getModuleType("fooClient");
            System.out.println("Module type for fooClient:" + type5);

            ModuleType type6 = df.getModuleType("barEjb");
            System.out.println("Module type for barEjb:" + type6);

            List<String> subModuleResults = df.getSubModuleInfoForJ2EEApplication("singleton2");
            System.out.println("Sub modules returned:");
            for (String subModule : subModuleResults) {
                System.out.println(subModule);
            }

        } catch (Exception e) {
            fail("Failed due to exception " + e.getMessage());
        }

    }

}
