/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.main.itest.tools.RandomGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jasonlee 2010
 * @author David Matejcek 2022
 */
public class JmsITest extends RestTestBase {
    private static final String URL_ADMIN_OBJECT_RESOURCE = "/domain/resources/admin-object-resource";
    private static final String URL_CONNECTOR_CONNECTION_POOL = "/domain/resources/connector-connection-pool";
    private static final String URL_CONNECTOR_RESOURCE = "/domain/resources/connector-resource";
    private static final String URL_JMS_HOST = "/domain/configs/config/server-config/jms-service/jms-host";
    private static final String URL_SEVER_JMS_DEST = "/domain/servers/server/server";
    private static final String DEST_TYPE = "topic";

    @Test
    public void testJmsConnectionFactories() {
        // Create connection pool
        final String poolName = "JmsConnectionFactory" + RandomGenerator.generateRandomString();
        Map<String, String> ccp_attrs = Map.of("name", poolName, "connectiondefinition",
            "jakarta.jms.ConnectionFactory", "raname", "jmsra");
        Response response = managementClient.post(URL_CONNECTOR_CONNECTION_POOL, ccp_attrs);
        assertThat(response.getStatus(), equalTo(200));

        // Check connection pool creation
        Map<String, String> pool = getEntityValues(managementClient.get(URL_CONNECTOR_CONNECTION_POOL + "/" + poolName));
        assertThat(pool, aMapWithSize(greaterThanOrEqualTo(26)));

        // Create connector resource
        String resourceName = poolName + "Resource";
        Map<String, String> cr_attrs = Map.of("name", resourceName, "poolname", poolName);
        response = managementClient.post(URL_CONNECTOR_RESOURCE, cr_attrs);
        assertThat(response.getStatus(), equalTo(200));

        // Check connector resource
        Map<String, String> resource = getEntityValues(managementClient.get(URL_CONNECTOR_RESOURCE + "/" + resourceName));
        assertThat(resource, aMapWithSize(6));

        // Edit and check ccp
        response = managementClient.post(URL_CONNECTOR_CONNECTION_POOL + "/" + poolName, Map.of("description", poolName));
        assertThat(response.getStatus(), equalTo(200));

        pool = getEntityValues(managementClient.get(URL_CONNECTOR_CONNECTION_POOL + "/" + poolName));
        assertThat(pool.get("description"), equalTo(poolName));

        // Edit and check cr
        response = managementClient.post(URL_CONNECTOR_RESOURCE + "/" + resourceName, Map.of("description", poolName));
        assertThat(response.getStatus(), equalTo(200));

        resource = getEntityValues(managementClient.get(URL_CONNECTOR_RESOURCE + "/" + resourceName));
        assertThat(pool.get("description"), equalTo(poolName));

        // Delete objects
        response = managementClient.get("/domain/servers/server/server/resource-ref/" + resourceName);
        assertEquals(200, response.getStatus());
        response = managementClient.delete(URL_CONNECTOR_CONNECTION_POOL + "/" + poolName, Map.of("cascade", "true"));
        assertThat(response.getStatus(), equalTo(200));
        response = managementClient.get("/domain/servers/server/server/resource-ref/" + resourceName);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testJmsDestinationResources() {
        final String jndiName = "jndi/" + RandomGenerator.generateRandomString();
        String encodedJndiName = URLEncoder.encode(jndiName, StandardCharsets.UTF_8);

        Map<String, String> attrs = Map.of("id", jndiName, "raname", "jmsra", "restype", "jakarta.jms.Topic");

        Response response = managementClient.post(URL_ADMIN_OBJECT_RESOURCE, attrs);
        assertThat(response.getStatus(), equalTo(200));

        Map<String, String> entity = getEntityValues(
            managementClient.get(URL_ADMIN_OBJECT_RESOURCE + "/" + encodedJndiName));
        assertThat(entity, aMapWithSize(8));

        response = managementClient.delete(URL_ADMIN_OBJECT_RESOURCE + "/" + encodedJndiName);
        assertThat(response.getStatus(), equalTo(200));
    }

    @Test
    public void testJmsPhysicalDestination() {
        final String destName = "jmsDest" + RandomGenerator.generateRandomString();
        final int maxNumMsgs = RandomGenerator.generateRandomNumber(500);
        final int consumerFlowLimit = RandomGenerator.generateRandomNumber(500);

        createJmsPhysicalDestination(destName, DEST_TYPE, URL_SEVER_JMS_DEST);

        final Map<String, String> newDest = Map.of("id", destName, "desttype", DEST_TYPE);
        Map<String, String> destProps = new HashMap<>(newDest);
        destProps.putAll(Map.of("property", "MaxNumMsgs=" + maxNumMsgs + ":ConsumerFlowLimit=" + consumerFlowLimit));

        Response response = managementClient.get(URL_SEVER_JMS_DEST + "/__get-jmsdest", newDest);
        assertThat(response.getStatus(), equalTo(200));

        response = managementClient.post(URL_SEVER_JMS_DEST + "/__update-jmsdest", destProps);
        assertThat(response.getStatus(), equalTo(200));
        response = managementClient.get(URL_SEVER_JMS_DEST + "/__get-jmsdest", newDest);
        assertThat(response.getStatus(), equalTo(200));
        Map<String, String> entity = getEntityValues(response);
        assertEquals(maxNumMsgs, entity.get("MaxNumMsgs"));
        assertEquals(consumerFlowLimit, entity.get("ConsumerFlowLimit"));

        deleteJmsPhysicalDestination(destName, URL_SEVER_JMS_DEST);
    }

    @Test
    @Disabled("Enable and fix OpenMQ - require more detailed message and probably to fix the cause:"
        + " MQJMSRA_RA4001: getJMXServiceURLList:Exception:Message=Caught exception when contacing portmapper.]]")
    public void testJmsPhysicalDestionationsWithClusters() {
        final String destName = "jmsDest" + RandomGenerator.generateRandomString();
        final String clusterName = createCluster();
        createClusterInstance(clusterName, "in1_" + clusterName);
        startCluster(clusterName);
        final String endpoint = "/domain/clusters/cluster/" + clusterName;
        try {
            createJmsPhysicalDestination(destName, "topic", endpoint);
            final Map<String, String> newDest = Map.of("id", destName, "desttype", DEST_TYPE);
            Response response = managementClient.get(endpoint + "/__get-jmsdest", newDest);
            assertThat(response.getStatus(), equalTo(200));

            response = managementClient.get(URL_SEVER_JMS_DEST + "/__get-jmsdest", newDest);
            assertThat(response.getStatus(), equalTo(200));
        } finally {
            deleteJmsPhysicalDestination(destName, endpoint);
            stopCluster(clusterName);
            deleteCluster(clusterName);
        }
    }

    @Test
    public void testJmsPing() {
        String results = managementClient.get(URL_SEVER_JMS_DEST + "/jms-ping").readEntity(String.class);
        assertThat(results, stringContainsInOrder("JMS-ping command executed successfully"));
    }

    @Test
    public void testJmsFlush() {
        Map<String, String> payload = Map.of("id", "mq.sys.dmq", "destType", "queue");
        Response response = managementClient.post(URL_SEVER_JMS_DEST + "/flush-jmsdest", payload);
        assertThat(response.getStatus(), equalTo(200));
    }

    @Test
    public void testJmsHosts() {
        final String jmsHostName = "jmshost" + RandomGenerator.generateRandomString();
        Map<String, String> newHost = Map.of("id", jmsHostName, "adminPassword", "admin", "port", "7676",
            "adminUserName", "admin", "host", "localhost");

        // Test create
        Response response = managementClient.post(URL_JMS_HOST, newHost);
        assertThat(response.getStatus(), equalTo(200));

        // Test edit
        Map<String, String> entity = getEntityValues(managementClient.get(URL_JMS_HOST + "/" + jmsHostName));
        assertThat(entity, aMapWithSize(greaterThanOrEqualTo(6)));
        assertEquals(jmsHostName, entity.get("name"));
        entity.put("port", "8686");
        response = managementClient.post(URL_JMS_HOST + "/" + jmsHostName, entity);
        assertThat(response.getStatus(), equalTo(200));
        entity = getEntityValues(managementClient.get(URL_JMS_HOST + "/" + jmsHostName));
        assertEquals("8686", entity.get("port"));

        // Test delete
        response = managementClient.delete(URL_JMS_HOST + "/" + jmsHostName);
        assertThat(response.getStatus(), equalTo(200));
    }

    public void createJmsPhysicalDestination(final String destName, final String type, final String endpoint) {
        final Map<String, String> newDest = Map.of("id", destName, "desttype", type);
        Response response = managementClient.post(endpoint + "/create-jmsdest", newDest);
        assertThat(response.getStatus(), equalTo(200));
    }

    public void deleteJmsPhysicalDestination(final String destName, final String endpoint) {
        final Map<String, String> newDest = Map.of("id", destName, "desttype", DEST_TYPE);
        Response response = managementClient.delete(endpoint + "/delete-jmsdest", newDest);
        assertThat(response.getStatus(), equalTo(200));

        response = managementClient.get(endpoint + "__get-jmsdest", newDest);
        assertThat(response.getStatus(), equalTo(404));
    }
}
