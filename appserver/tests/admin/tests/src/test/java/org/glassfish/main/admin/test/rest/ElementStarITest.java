/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.admin.test.rest;

import jakarta.ws.rs.core.Response;

import java.net.URISyntaxException;
import java.util.Map;

import org.glassfish.main.itest.tools.RandomGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This class tests the changes to the handling of @Element("*") instances
 *
 * @author jasonlee
 */
@Disabled("TEMPORARY FOR GLASSFISH 8 M1 - Unable to delete the temporary JMS Resource test_jms_adapter.")
public class ElementStarITest extends RestTestBase {
    private static final String URL_CREATE_INSTANCE = "/domain/create-instance";

    private String instanceName1;
    private String instanceName2;

    @BeforeEach
    public void before() {
        instanceName1 = "instance_" + RandomGenerator.generateRandomString();
        instanceName2 = "instance_" + RandomGenerator.generateRandomString();

        Response response = managementClient.post(URL_CREATE_INSTANCE,
            Map.of("id", instanceName1, "node", "localhost-domain1"));
        assertThat(response.getStatus(), equalTo(200));
        response = managementClient.post(URL_CREATE_INSTANCE, Map.of("id", instanceName2, "node", "localhost-domain1"));
        assertEquals(200, response.getStatus());
    }


    @AfterEach
    public void after() {
        Response response = managementClient.delete("/domain/servers/server/" + instanceName1 + "/delete-instance");
        assertEquals(200, response.getStatus());
        response = managementClient.delete("/domain/servers/server/" + instanceName2 + "/delete-instance");
        assertEquals(200, response.getStatus());
    }


    @Test
    public void testApplications() throws URISyntaxException {
        final String app1 = "app" + RandomGenerator.generateRandomString();
        final String app2 = "app" + RandomGenerator.generateRandomString();

        deployApp(getWar("test"), app1, app1);
        deployApp(getWar("test"), app2, app2);
        addAppRef(app1, instanceName1);
        addAppRef(app2, instanceName1);

        Response response = managementClient.get("/domain/servers/server/" + instanceName1 + "/application-ref");
        Map<String, String> children = this.getChildResources(response);
        assertThat(children, aMapWithSize(2));
    }


    /**
     * FIXME: this test may be affected by failed {@link ResourceRefITest}
     */
    @Test
    public void testResources() {
        // The DAS should already have two resource-refs (jdbc/__TimerPool and jdbc/__default)
        Response response = managementClient.get("/domain/servers/server/server/resource-ref");
        Map<String, String> children = this.getChildResources(response);
        assertThat(children, aMapWithSize(7));
    }


    @Test
    public void testLoadBalancerConfigs() {
        final String lbName = "lbconfig-" + RandomGenerator.generateRandomString();
        Response response = managementClient.post("/domain/lb-configs/lb-config/",
            Map.of("id", lbName, "target", instanceName1));
        assertEquals(200, response.getStatus());

        response = managementClient.post("/domain/lb-configs/lb-config/" + lbName + "/create-http-lb-ref",
            Map.of("id", instanceName2));
        assertEquals(200, response.getStatus());

        response = managementClient.get("/domain/lb-configs/lb-config/" + lbName + "/server-ref");
        Map<String, String> children = getChildResources(response);
        assertThat(children, aMapWithSize(1));
    }
}
