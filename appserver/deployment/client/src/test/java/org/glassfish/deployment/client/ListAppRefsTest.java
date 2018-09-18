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

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tim
 */
public class ListAppRefsTest {

    public ListAppRefsTest() {
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
    public void testListAppRefsTest() {
        System.out.println("testListAppRefsTest");
        DeploymentFacility df = DeploymentFacilityFactory.getDeploymentFacility();
        ServerConnectionIdentifier sci = new ServerConnectionIdentifier();
        sci.setHostName("localhost");
        sci.setHostPort(4848); // 8080 for the REST client
        sci.setUserName("admin");
        sci.setPassword("");

        df.connect(sci);

        try {

            TargetModuleID[] results1 =
                    df._listAppRefs(new String[] {"cluster1"});
            System.out.println("TargetModuleIDs returned for default:");
            for (TargetModuleID tmid1 : results1) {
                System.out.println(tmid1.getTarget().getName() + ":" +
                        tmid1.getModuleID());
            }

            TargetModuleID[] resultsAll1 =
                    df._listAppRefs(new String[] {"cluster1"}, "all");
            System.out.println("TargetModuleIDs returned for all cluster1:");
            for (TargetModuleID tmidAll1 : resultsAll1) {
                System.out.println(tmidAll1.getTarget().getName() + ":" +
                        tmidAll1.getModuleID());
            }

            TargetModuleID[] resultsRunning1 =
                    df._listAppRefs(new String[] {"cluster1"}, "running");
            System.out.println("TargetModuleIDs returned for running cluster1:");
            for (TargetModuleID tmidRunning1 : resultsRunning1) {
               System.out.println(tmidRunning1.getTarget().getName() + ":" +
                        tmidRunning1.getModuleID());
            }

            TargetModuleID[] resultsNonRunning1 =
                    df._listAppRefs(new String[] {"cluster1"}, "non-running");
            System.out.println("TargetModuleIDs returned for nonrunning cluster1:");
            for (TargetModuleID tmidNonRunning1 : resultsNonRunning1) {
                System.out.println(tmidNonRunning1.getTarget().getName() + ":" +
                        tmidNonRunning1.getModuleID());
            }

            TargetModuleID[] results =
                    df._listAppRefs(new String[] {"server"});
            System.out.println("TargetModuleIDs returned for default:");
            for (TargetModuleID tmid : results) {
                System.out.println(tmid.getTarget().getName() + ":" +
                        tmid.getModuleID());
            }

            TargetModuleID[] resultsAll =
                    df._listAppRefs(new String[] {"server"}, "all");
            System.out.println("TargetModuleIDs returned for all:");
            for (TargetModuleID tmidAll : resultsAll) {
                System.out.println(tmidAll.getTarget().getName() + ":" +
                        tmidAll.getModuleID());
            }

            TargetModuleID[] resultsRunning =
                    df._listAppRefs(new String[] {"server"}, "running");
            System.out.println("TargetModuleIDs returned for running:");
            for (TargetModuleID tmidRunning : resultsRunning) {
                System.out.println(tmidRunning.getTarget().getName() + ":" +
                        tmidRunning.getModuleID());
            }

            TargetModuleID[] resultsNonRunning =
                    df._listAppRefs(new String[] {"server"}, "non-running");
            System.out.println("TargetModuleIDs returned for nonrunning:");
            for (TargetModuleID tmidNonRunning : resultsNonRunning) {
                System.out.println(tmidNonRunning.getTarget().getName() + ":" +
                        tmidNonRunning.getModuleID());
            }

            TargetModuleID[] resultsAllWithType =
                    df._listAppRefs(new String[] {"server"}, "all", "web");
            System.out.println("TargetModuleIDs returned for all web:");
            for (TargetModuleID tmidAllWithType : resultsAllWithType) {
                System.out.println(tmidAllWithType.getTarget().getName() + ":" +
                        tmidAllWithType.getModuleID());
            }

            TargetModuleID[] resultsRunningWithType =
                    df._listAppRefs(new String[] {"server"}, "running", "ear");
            System.out.println("TargetModuleIDs returned for running ear:");
            for (TargetModuleID tmidRunningWithType : resultsRunningWithType) {
                System.out.println(tmidRunningWithType.getTarget().getName() + ":" +
                        tmidRunningWithType.getModuleID());
            }

            TargetModuleID[] resultsNonRunningWithType =
                    df._listAppRefs(new String[] {"server"}, "non-running", "ear");
            System.out.println("TargetModuleIDs returned for nonrunning ear:");
            for (TargetModuleID tmidNonRunningWithType : resultsNonRunningWithType) {
                System.out.println(tmidNonRunningWithType.getTarget().getName() + ":" +
                        tmidNonRunningWithType.getModuleID());
            }



        } catch (Exception e) {
            fail("Failed due to exception " + e.getMessage());
        }

    }

}
