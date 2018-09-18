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

package org.glassfish.appclient.client.acc;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tjquinn
 */
public class AppClientContainerTest {

    private static final String[] SERVER_SPECS = new String[] {"a","a:3701","b:3700"};
//    private static final ORBEndpoint[] SERVER_ANSWERS = new ORBEndpoint[] {
//            new ORBEndpoint("a", 3700),
//            new ORBEndpoint("a", 3701),
//            new ORBEndpoint("b", 3700)
//        };

    public AppClientContainerTest() {
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

    /**
     * Test of newContainer method, of class AppClientContainer.
     */
    @Ignore
    @Test
    public void testNewContainer() {
        System.out.println("newContainer");
//        TargetServer[] targetServers = new TargetServer[] {
//            new TargetServer("localhost", 3700)
//        };
//        AppClientContainer expResult = null;
//        AppClientContainer result = null;
//        result = AppClientContainer.newContainer(targetServers);
//        assertEquals(expResult, result);
    }

//    @Test
//    public void testServerArray() {
//        try {
//            ACCOption.SERVER serverOption = ACCOption.SERVER(SERVER_SPECS);
//            assertArrayEquals(serverOption.get(), SERVER_ANSWERS);
//        } catch (ValidationException e) {
//            fail("Unexpected exception: " + e.toString());
//        }
//    }

//    @Test
//    public void testServerObjectArray() {
//        Object[] servers = new Object[] {"c", "c:3701", "c:3700"};
//        ORBEndpoint[] result = null;
//        try {
//            ACCOption.SERVER.set(servers);
//            fail("Passing array of objects to set SERVER should have failed but it worked");
//        } catch (ACCOption.TypeException ex) {
//            System.out.println("Received expected " + ex);
//        } catch (ValidationException ex) {
//            fail(ex.toString());
//        }
//    }

}
