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

package org.glassfish.nucleus.admin.rest;


import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Mitesh Meswani
 */
@TestMethodOrder(OrderAnnotation.class)
public class MonitoringITest extends RestTestBase {
    private static final String MONITORING_RESOURCE_URL = "domain/configs/config/server-config/monitoring-service/module-monitoring-levels";
    private static final String JDBC_CONNECTION_POOL_URL = "domain/resources/jdbc-connection-pool";
    private static final String PING_CONNECTION_POOL_URL = "domain/resources/ping-connection-pool";
    private static final String CONTEXT_ROOT_MONITORING = "monitoring";

    @Test
    @Order(1)
    public void enableMonitoring() {
        String url = getManagementURL(MONITORING_RESOURCE_URL);
        Map<String, String> payLoad = new HashMap<>() {
            {
                put("ThreadPool", "HIGH");
                put("Orb", "HIGH");
                put("EjbContainer", "HIGH");
                put("WebContainer", "HIGH");
                put("Deployment", "HIGH");
                put("TransactionService", "HIGH");
                put("HttpService", "HIGH");
                put("JdbcConnectionPool", "HIGH");
                put("ConnectorConnectionPool", "HIGH");
                put("ConnectorService", "HIGH");
                put("JmsService", "HIGH");
                put("Jvm", "HIGH");
                put("Security", "HIGH");
                put("WebServicesContainer", "HIGH");
                put("Jpa", "HIGH");
                put("Jersey", "HIGH");
            }
        };
        Response response = post(url, payLoad);
        assertEquals(202, response.getStatus());
    }

    /**
     * Objective - Verify that basic monitoring is working
     * Strategy - Call /monitoring/domain and assert that "server" is present as child element
     */
    @Test
    @Order(2)
    public void testBaseURL() {
        Response response = get("domain");
        assertEquals(200, response.getStatus());
        // monitoring/domain
        Map<String, String> entity = getChildResources(response);
        assertNull(entity.get("server"), entity.toString());
    }

    /**
     * Objective - Verify that invalid resources returns 404
     * Strategy - Request an invalid resource under monitoring and ensure that 404 is returned
     */
    @Test
    @Order(10)
    public void testInvalidResource() {
        Response response = get("domain/server/foo");
        assertEquals(404, response.getStatus(), "Did not receive ");
    }

    /**
     * Objective - Verify that resources with dot work
     * Strategy - create a resource with "." in name and then try to access it
     */
    @Test
    @Order(20)
    public void testDot() {
        // Step 1- Create a resource with "."
        final String poolNameWithDot = "poolNameWith.dot";

        // Clean up from leftover from previous run
        String url = getManagementURL(JDBC_CONNECTION_POOL_URL + '/' + poolNameWithDot);
        Response responseDel = delete(url);
        assertEquals(202, responseDel.getStatus());

        url = getManagementURL(JDBC_CONNECTION_POOL_URL);
        Map<String, String> payLoad = new HashMap<>() {
            {
                put("name", poolNameWithDot);
                put("resType", "javax.sql.DataSource");
                put("datasourceClassname", "foo.bar");
            }
        };
        Response response = post(url, payLoad);
        assertEquals(202, response.getStatus());


        // Step 2- Ping the connection pool to generate some monitoring data
        url = getManagementURL(PING_CONNECTION_POOL_URL);
        Response responsePing = get(url, Map.of("id", poolNameWithDot));
        assertEquals(202, responsePing.getStatus());

        // Step 3 - Access monitoring tree to assert it is accessible
        // FIXME: As of 03.04.2022 Utils.getJerseyClient here throws exception:
        // java.lang.ClassNotFoundException: Provider for jakarta.ws.rs.client.ClientBuilder cannot be found
//        Response responsePool = get("domain/server/resources/" + poolNameWithDot);
//        assertEquals(200, responsePool.getStatus());
//        Map<String, String> responseEntity = getEntityValues(responsePool);
//        assertThat("No Monitoring data found for pool " + poolNameWithDot, responseEntity, aMapWithSize(2));
    }


    @Test
    @Order(1000)
    public void testCleanup() {
        String url = getManagementURL(MONITORING_RESOURCE_URL);
        Map<String, String> payLoad = new HashMap<>() {
            {
                put("ThreadPool", "LOW");
                put("Orb", "LOW");
                put("EjbContainer", "LOW");
                put("WebContainer", "LOW");
                put("Deployment", "LOW");
                put("TransactionService", "LOW");
                put("HttpService", "LOW");
                put("JdbcConnectionPool", "LOW");
                put("ConnectorConnectionPool", "LOW");
                put("ConnectorService", "LOW");
                put("JmsService", "LOW");
                put("Jvm", "LOW");
                put("Security", "LOW");
                put("WebServicesContainer", "LOW");
                put("Jpa", "LOW");
                put("Jersey", "LOW");
            }
        };
        Response response = post(url, payLoad);
        assertEquals(202, response.getStatus());
    }

    @Override
    protected String getContextRoot() {
        return CONTEXT_ROOT_MONITORING;
    }

    private String getManagementURL(String targetResourceURL) {
        return getBaseAdminUrl() + CONTEXT_ROOT_MANAGEMENT + targetResourceURL;
    }
}
