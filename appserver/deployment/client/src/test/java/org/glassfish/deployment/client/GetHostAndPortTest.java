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
import com.sun.enterprise.util.HostAndPort;


/**
 */
public class GetHostAndPortTest {

    public GetHostAndPortTest() {
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
    public void testGetHostAndPortTest() {
        System.out.println("testGetHostAndPortTest");
        DeploymentFacility df = DeploymentFacilityFactory.getDeploymentFacility();
        ServerConnectionIdentifier sci = new ServerConnectionIdentifier();
        sci.setHostName("localhost");
        sci.setHostPort(4848); // 8080 for the REST client
        sci.setUserName("admin");
        sci.setPassword("");

        df.connect(sci);

        try {
            HostAndPort hap1 = df.getHostAndPort("server", "webapps-simple", false);
            System.out.println("Host1 returned:" + hap1.getHost());
            System.out.println("Port1 returned:" + hap1.getPort());
            HostAndPort hap2 = df.getVirtualServerHostAndPort("server", "__asadmin", false);
            System.out.println("Host2 returned:" + hap2.getHost());
            System.out.println("Port2 returned:" + hap2.getPort());
            HostAndPort hap3 = df.getHostAndPort("server", false);
            System.out.println("Host3 returned:" + hap3.getHost());
            System.out.println("Port3 returned:" + hap3.getPort());
            HostAndPort hap4 = df.getHostAndPort("foo", false);
            System.out.println("hap4:" + hap4);

        } catch (Exception e) {
            fail("Failed due to exception " + e.getMessage());
        }

    }

}
