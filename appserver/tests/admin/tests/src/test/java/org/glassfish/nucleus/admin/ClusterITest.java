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
public class ClusterITest {

    private static final String PORT1 = "55123";
    private static final String PORT2 = "55124";
    private static final String CLUSTER_NAME = "eec1";
    private static final String INSTANCE_NAME_1 = "eein1-with-a-very-very-very-long-name";
    private static final String INSTANCE_NAME_2 = "eein2";
    private static final String URL1 = "http://localhost:" + PORT1;
    private static final String URL2 = "http://localhost:" + PORT2;

    @Test
    @Order(1)
    public void createClusterTest() {
        assertTrue(nadmin("create-cluster", CLUSTER_NAME), "create cluster");
    }

    @Test
    @Order(2)
    public void createInstancesTest() {
        assertTrue(
            nadmin("create-local-instance", "--cluster", CLUSTER_NAME, "--systemproperties",
                        "HTTP_LISTENER_PORT=" + PORT1 + ":" +
                        "HTTP_SSL_LISTENER_PORT=18181:" +
                        "IIOP_SSL_LISTENER_PORT=13800:" +
                        "IIOP_LISTENER_PORT=13700:" +
                        "JMX_SYSTEM_CONNECTOR_PORT=17676:" +
                        "IIOP_SSL_MUTUALAUTH_PORT=13801:" +
                        "JMS_PROVIDER_PORT=18686:" +
                        "ASADMIN_LISTENER_PORT=14848",
                        INSTANCE_NAME_1), "create instance1");

        assertTrue(
            nadmin("create-local-instance", "--cluster", CLUSTER_NAME, "--systemproperties",
                    "HTTP_LISTENER_PORT=" + PORT2 +
                    ":HTTP_SSL_LISTENER_PORT=28181:IIOP_SSL_LISTENER_PORT=23800:" +
                    "IIOP_LISTENER_PORT=23700:JMX_SYSTEM_CONNECTOR_PORT=27676:IIOP_SSL_MUTUALAUTH_PORT=23801:" +
                    "JMS_PROVIDER_PORT=28686:ASADMIN_LISTENER_PORT=24848",
                    INSTANCE_NAME_2), "create instance2");
    }

    @Test
    @Order(3)
    public void startInstancesTest() {
        assertTrue(nadmin("start-local-instance", INSTANCE_NAME_1), "start instance1");
        assertTrue(nadmin("start-local-instance", INSTANCE_NAME_2), "start instance2");
    }

    @Test
    @Order(4)
    public void checkClusterTest() {
        assertTrue(nadmin("list-instances"), "list-instances");
        assertThat(getURL(URL1), stringContainsInOrder("GlassFish Server"));
        assertThat(getURL(URL2), stringContainsInOrder("GlassFish Server"));
    }

    @Test
    @Order(5)
    public void stopInstancesTest() {
        assertTrue(nadmin("stop-local-instance", "--kill", INSTANCE_NAME_1), "stop instance1");
        assertTrue(nadmin("stop-local-instance", "--kill", INSTANCE_NAME_2), "stop instance2");
    }

    @Test
    @Order(6)
    public void deleteInstancesTest() {
        assertTrue(nadmin("delete-local-instance", INSTANCE_NAME_1), "delete instance1");
        assertTrue(nadmin("delete-local-instance", INSTANCE_NAME_2), "delete instance2");
    }

    @Test
    @Order(7)
    public void deleteClusterTest() {
        assertTrue(nadmin("delete-cluster", CLUSTER_NAME), "delete cluster");
    }
}
