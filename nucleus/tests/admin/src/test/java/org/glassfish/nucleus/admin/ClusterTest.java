/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.nucleus.admin;

import static org.glassfish.tests.utils.NucleusTestUtils.*;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author Tom Mueller
 */
@Test(testName ="ClusterTest")
public class ClusterTest {
    
    final String tn = "QLCluster";
    final String port1 = "55123";
    final String port2 = "55124";
    final String cname = "eec1";
    final String i1name = "eein1-with-a-very-very-very-long-name";
    final String i2name = "eein2";
    String i1url = "http://localhost:" + port1;
    String i2url = "http://localhost:" + port2;

    public void createClusterTest() {
        // create a cluster and two instances
        assertTrue("create cluster", nadmin("create-cluster", cname));
    }
    
    @Test(dependsOnMethods = { "createClusterTest" })
    public void createInstancesTest() {           
        assertTrue("create instance1", nadmin("create-local-instance",
                "--cluster", cname, "--systemproperties",
                "HTTP_LISTENER_PORT=" + port1 + ":HTTP_SSL_LISTENER_PORT=18181:IIOP_SSL_LISTENER_PORT=13800:" +
                "IIOP_LISTENER_PORT=13700:JMX_SYSTEM_CONNECTOR_PORT=17676:IIOP_SSL_MUTUALAUTH_PORT=13801:" +
                "JMS_PROVIDER_PORT=18686:ASADMIN_LISTENER_PORT=14848", i1name));
        assertTrue("create instance2", nadmin("create-local-instance",
                "--cluster", cname, "--systemproperties",
                "HTTP_LISTENER_PORT=" + port2 + ":HTTP_SSL_LISTENER_PORT=28181:IIOP_SSL_LISTENER_PORT=23800:" +
                "IIOP_LISTENER_PORT=23700:JMX_SYSTEM_CONNECTOR_PORT=27676:IIOP_SSL_MUTUALAUTH_PORT=23801:" +
                "JMS_PROVIDER_PORT=28686:ASADMIN_LISTENER_PORT=24848", i2name));
    }
    
    @Test(dependsOnMethods = { "createInstancesTest" })
    public void startInstancesTest() {           
        // start the instances
        assertTrue("start instance1", nadmin("start-local-instance", i1name));
        assertTrue("start instance2", nadmin("start-local-instance", i2name));
    }
    
    @Test(dependsOnMethods = { "startInstancesTest" })
    public void checkClusterTest() {           
        // check that the instances are there
        assertTrue("list-instances", nadmin("list-instances"));
        assertTrue("getindex1", matchString("GlassFish Server", getURL(i1url)));
        assertTrue("getindex2", matchString("GlassFish Server", getURL(i2url)));
    }
    
    @Test(dependsOnMethods = { "checkClusterTest" })
    public void stopInstancesTest() {           
        // stop and delete the instances and cluster
        assertTrue("stop instance1", nadmin("stop-local-instance", "--kill", i1name));
        assertTrue("stop instance2", nadmin("stop-local-instance", "--kill", i2name));
    }
    
    @Test(dependsOnMethods = { "stopInstancesTest" })
    public void deleteInstancesTest() {
        assertTrue("delete instance1", nadmin("delete-local-instance", i1name));
        assertTrue("delete instance2", nadmin("delete-local-instance", i2name));
    }

    @Test(dependsOnMethods = { "deleteInstancesTest" })
    public void deleteClusterTest() {           
        assertTrue("delete cluster", nadmin("delete-cluster", cname));
    }
}
