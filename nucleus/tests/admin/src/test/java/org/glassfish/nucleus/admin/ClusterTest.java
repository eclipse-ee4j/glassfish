/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.nucleus.test.tool.DomainLifecycleExtension;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.glassfish.nucleus.test.tool.NucleusTestUtils.getURL;
import static org.glassfish.nucleus.test.tool.NucleusTestUtils.nadmin;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Tom Mueller
 */
@TestMethodOrder(OrderAnnotation.class)
@ExtendWith(DomainLifecycleExtension.class)
public class ClusterTest {

    private final String tn = "QLCluster";
    private final String port1 = "55123";
    private final String port2 = "55124";
    private final String cname = "eec1";
    private final String i1name = "eein1-with-a-very-very-very-long-name";
    private final String i2name = "eein2";
    private final String i1url = "http://localhost:" + port1;
    private final String i2url = "http://localhost:" + port2;

    @Test
    @Order(1)
    public void createClusterTest() {
        // create a cluster and two instances
        assertTrue(nadmin("create-cluster", cname), "create cluster");
    }

    @Test
    @Order(2)
    public void createInstancesTest() {
        assertTrue(
            nadmin("create-local-instance", "--cluster", cname, "--systemproperties",
                        "HTTP_LISTENER_PORT=" + port1 + ":" +
                        "HTTP_SSL_LISTENER_PORT=18181:" +
                        "IIOP_SSL_LISTENER_PORT=13800:" +
                        "IIOP_LISTENER_PORT=13700:" +
                        "JMX_SYSTEM_CONNECTOR_PORT=17676:" +
                        "IIOP_SSL_MUTUALAUTH_PORT=13801:" +
                        "JMS_PROVIDER_PORT=18686:" +
                        "ASADMIN_LISTENER_PORT=14848",
                        i1name), "create instance1");

        assertTrue(
            nadmin("create-local-instance", "--cluster", cname, "--systemproperties",
                    "HTTP_LISTENER_PORT=" + port2 +
                    ":HTTP_SSL_LISTENER_PORT=28181:IIOP_SSL_LISTENER_PORT=23800:" +
                    "IIOP_LISTENER_PORT=23700:JMX_SYSTEM_CONNECTOR_PORT=27676:IIOP_SSL_MUTUALAUTH_PORT=23801:" +
                    "JMS_PROVIDER_PORT=28686:ASADMIN_LISTENER_PORT=24848",
                    i2name), "create instance2");
    }

    @Test
    @Order(3)
    public void startInstancesTest() {
        // start the instances
        assertTrue(nadmin("start-local-instance", i1name), "start instance1");
        assertTrue(nadmin("start-local-instance", i2name), "start instance2");
    }

    @Test
    @Order(4)
    public void checkClusterTest() {
        // check that the instances are there
        assertTrue(nadmin("list-instances"), "list-instances");
        assertThat(getURL(i1url), stringContainsInOrder("GlassFish Server"));
        assertThat(getURL(i2url), stringContainsInOrder("GlassFish Server"));
    }

    @Test
    @Order(5)
    public void stopInstancesTest() {
        // stop and delete the instances and cluster
        assertTrue(nadmin("stop-local-instance", "--kill", i1name), "stop instance1");
        assertTrue(nadmin("stop-local-instance", "--kill", i2name), "stop instance2");
    }

    @Test
    @Order(6)
    public void deleteInstancesTest() {
        assertTrue(nadmin("delete-local-instance", i1name), "delete instance1");
        assertTrue(nadmin("delete-local-instance", i2name), "delete instance2");
    }

    @Test
    @Order(7)
    public void deleteClusterTest() {
        assertTrue(nadmin("delete-cluster", cname), "delete cluster");
    }
}
