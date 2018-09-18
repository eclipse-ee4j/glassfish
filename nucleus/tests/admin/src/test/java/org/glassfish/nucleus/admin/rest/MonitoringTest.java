/*
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


import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;


/**
 *
 * @author Mitesh Meswani
 */
public class MonitoringTest extends RestTestBase {
    private static final String MONITORING_RESOURCE_URL = "/domain/configs/config/server-config/monitoring-service/module-monitoring-levels";
    private static final String JDBC_CONNECTION_POOL_URL = "/domain/resources/jdbc-connection-pool";
    private static final String PING_CONNECTION_POOL_URL = "/domain/resources/ping-connection-pool";
    private static final String CONTEXT_ROOT_MONITORING = "monitoring";

    @Override
    protected String getContextRoot() {
        return CONTEXT_ROOT_MONITORING;
    }

    @Test(enabled=false)
    public void initializeMonitoring() {
        // Enable monitoring
        String url = getManagementURL(MONITORING_RESOURCE_URL);
        Map<String, String> payLoad = new HashMap<String, String>() {
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
        checkStatusForSuccess(response);
    }

    /**
     * Objective - Verify that basic monitoring is working
     * Strategy - Call /monitoring/domain and assert that "server" is present as child element
     */
    @Test(enabled=false)
    public void testBaseURL() {
        Map<String, String> entity = getChildResources(get("/domain")); // monitoring/domain
        assertNotNull(entity.get("server"));
    }

    /**
     * Objective - Verify that resources with dot work
     * Strategy - create a resource with "." in name and then try to access it
     */
    @Test(enabled=false)
    public void testDot() {
        // Step 1- Create a resource with "."
        final String POOL_NAME = "poolNameWith.dot";

        // Clean up from leftover from previous run
        String url = getManagementURL(JDBC_CONNECTION_POOL_URL + '/' + POOL_NAME);
        delete(url);

        url = getManagementURL(JDBC_CONNECTION_POOL_URL);
        Map<String, String> payLoad = new HashMap<String, String>() {
            {
                put("name", POOL_NAME);
                put("resType", "javax.sql.DataSource");
                put("datasourceClassname", "foo.bar");
            }
        };
        Response response = post(url, payLoad);
        checkStatusForSuccess(response);


        // Step 2- Ping the connection pool to generate some monitoring data
        url = getManagementURL(PING_CONNECTION_POOL_URL);
        payLoad.clear();
        payLoad.put("id", POOL_NAME);
        get(url, payLoad);

       // Step 3 - Access monitoring tree to assert it is accessible
        response = get("/domain/server/resources/"+ POOL_NAME);
        checkStatusForSuccess(response);
        Map<String, String> responseEntity = getEntityValues(response);
        assertTrue("No Monitoring data found for pool " + POOL_NAME, responseEntity.size() > 0 );
    }

    /**
     * Objective - Verify that invalid resources returns 404
     * Strategy - Request an invalid resource under monitoring and ensure that 404 is returned
     */
    @Test(enabled=false)
    public void testInvalidResource() {
        Response response = get("/domain/server/foo");
        assertTrue("Did not receive ", response.getStatus() == javax.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode() ) ;
    }


    private String getManagementURL(String targetResourceURL) {
        return getBaseUrl() + CONTEXT_ROOT_MANAGEMENT + targetResourceURL;

    }


}
