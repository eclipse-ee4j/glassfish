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

import java.util.HashMap;
import java.util.Map;

import org.glassfish.main.admin.test.tool.DomainAdminRestClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Note regards HTTP 200/202 - not sure why it is happening, but the server probably needs some time
 * to propagate changes, while we request another.
 *
 * @author Mitesh Meswani
 */
@TestMethodOrder(OrderAnnotation.class)
public class MonitoringITest extends RestTestBase {

    private static final String MONITORING_RESOURCE_URL = "/domain/configs/config/server-config/monitoring-service/module-monitoring-levels";
    private static final String JDBC_CONNECTION_POOL_URL = "/domain/resources/jdbc-connection-pool";
    private static final String PING_CONNECTION_POOL_URL = "/domain/resources/ping-connection-pool";
    private static final String POOL_NAME_W_DOT = "poolNameWith.dot";

    private static DomainAdminRestClient monitoringClient;

    @BeforeAll
    public static void init() {
        monitoringClient = new DomainAdminRestClient(getBaseAdminUrl() + "/monitoring");
    }


    @AfterAll
    public static void closeResources() {
        Response responseDel = managementClient.delete(JDBC_CONNECTION_POOL_URL + '/' + POOL_NAME_W_DOT);
        assertThat(responseDel.getStatus(), anyOf(equalTo(200), equalTo(202), equalTo(404)));
        if (monitoringClient != null) {
            monitoringClient.close();
        }
    }


    @Test
    @Order(1)
    public void enableMonitoring() {
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
        Response response = managementClient.post(MONITORING_RESOURCE_URL, payLoad);
        assertThat(response.getStatus(), anyOf(equalTo(200), equalTo(202)));
    }

    /**
     * Objective - Verify that basic monitoring is working
     * Strategy - Call /monitoring/domain and assert that "server" is present as child element
     */
    @Test
    @Order(2)
    public void testBaseURL() {
        Response response = monitoringClient.get("/domain");
        assertThat(response.getStatus(), anyOf(equalTo(200), equalTo(202)));
        // monitoring/domain
        Map<String, String> entity = getChildResources(response);
        assertNotNull(entity.get("server"), entity.toString());
    }

    /**
     * Objective - Verify that invalid resources returns 404
     * Strategy - Request an invalid resource under monitoring and ensure that 404 is returned
     */
    @Test
    @Order(10)
    public void testInvalidResource() {
        Response response = monitoringClient.get("/domain/server/foo");
        assertEquals(404, response.getStatus(), "Did not receive ");
    }

    /**
     * Objective - Verify that resources with dot work
     * Strategy - create a resource with "." in name and then try to access it
     */
    @Test
    @Order(20)
    public void testDot() {
        Response responseDel = managementClient.delete(JDBC_CONNECTION_POOL_URL + '/' + POOL_NAME_W_DOT);
        assertThat(responseDel.getStatus(), equalTo(404));

        Map<String, String> payLoad = new HashMap<>() {
            {
                put("name", POOL_NAME_W_DOT);
                put("resType", "javax.sql.DataSource");
                put("datasourceClassname", "foo.bar");
            }
        };
        Response response = managementClient.post(JDBC_CONNECTION_POOL_URL, payLoad);
        assertThat(response.getStatus(), anyOf(equalTo(200), equalTo(202)));

        // Step 2- Ping the connection pool to generate some monitoring data
        Response responsePing = managementClient.get(PING_CONNECTION_POOL_URL, Map.of("id", POOL_NAME_W_DOT));
        // foo.bar is invalid ds class
        assertThat(responsePing.toString(), responsePing.getStatus(), equalTo(500));

        // Step 3 - Access monitoring tree to assert it is accessible
        Response responsePool = monitoringClient.get("/domain/server/resources/" + POOL_NAME_W_DOT);
        assertEquals(200, responsePool.getStatus());
        Map<String, String> responseEntity = getEntityValues(responsePool);
        assertThat("Monitoring data: \n" + responseEntity, responseEntity, aMapWithSize(14));
    }


    @Test
    @Order(1000)
    public void testCleanup() {
        Map<String, String> payLoad = new HashMap<>() {
            {
                put("ThreadPool", "OFF");
                put("Orb", "OFF");
                put("EjbContainer", "OFF");
                put("WebContainer", "OFF");
                put("Deployment", "OFF");
                put("TransactionService", "OFF");
                put("HttpService", "OFF");
                put("JdbcConnectionPool", "OFF");
                put("ConnectorConnectionPool", "OFF");
                put("ConnectorService", "OFF");
                put("JmsService", "OFF");
                put("Jvm", "OFF");
                put("Security", "OFF");
                put("WebServicesContainer", "OFF");
                put("Jpa", "OFF");
                put("Jersey", "OFF");
            }
        };
        Response response = managementClient.post(MONITORING_RESOURCE_URL, payLoad);
        assertThat(response.getStatus(), anyOf(equalTo(200), equalTo(202)));
    }
}
