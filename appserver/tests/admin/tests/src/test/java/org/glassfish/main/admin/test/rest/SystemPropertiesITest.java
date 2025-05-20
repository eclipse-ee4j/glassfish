/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.util.List;
import java.util.Map;

import org.glassfish.main.itest.tools.RandomGenerator;
import org.junit.jupiter.api.Test;

import static org.glassfish.admin.rest.client.utils.MarshallingUtils.buildMapFromDocument;
import static org.glassfish.embeddable.GlassFishVariable.INSTANCE_ROOT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author jasonlee
 */
public class SystemPropertiesITest extends RestTestBase {
    private static final String URL_CONFIG_SYSTEM_PROPERTIES = "/domain/configs/config/%config%/system-properties";
    private static final String URL_CLUSTER_SYSTEM_PROPERTIES = "/domain/clusters/cluster/%clusterName%/system-properties";
    private static final String URL_INSTANCE_SYSTEM_PROPERTIES = "/domain/servers/server/%instanceName%/system-properties";
    private static final String URL_DAS_SYSTEM_PROPERTIES = URL_INSTANCE_SYSTEM_PROPERTIES.replaceAll("%instanceName%", "server");
    private static final String URL_CREATE_INSTANCE = "/domain/create-instance";
    private static final String PROP_VALUE = "${" + INSTANCE_ROOT.getPropertyName() + "}/foo";

    @Test
    public void getSystemProperties() {
        Response response = managementClient.get(URL_DAS_SYSTEM_PROPERTIES);
        assertEquals(200, response.getStatus());
        List<Map<String, String>> systemProperties = getSystemProperties(
            buildMapFromDocument(response.readEntity(String.class)));
        assertNotNull(systemProperties);
    }


    @Test
    public void createSystemProperties() {
        final String prop1 = "property" + RandomGenerator.generateRandomString();
        final String prop2 = "property" + RandomGenerator.generateRandomString();
        Map<String, String> payload = Map.of(prop1, "value1", prop2, "value2");
        Response response = managementClient.post(URL_DAS_SYSTEM_PROPERTIES, payload);
        assertEquals(200, response.getStatus());
        response = managementClient.get(URL_DAS_SYSTEM_PROPERTIES);
        List<Map<String, String>> systemProperties = getSystemProperties(
            buildMapFromDocument(response.readEntity(String.class)));
        assertNotNull(systemProperties);

        int testPropsFound = 0;
        for (Map<String, String> systemProperty : systemProperties) {
            String name = systemProperty.get("name");
            if (prop1.equals(name) || prop2.equals(name)) {
                testPropsFound++;
            }
        }

        assertEquals(2, testPropsFound);

        response = managementClient.delete(URL_DAS_SYSTEM_PROPERTIES+"/"+prop1);
        assertEquals(200, response.getStatus());
        response = managementClient.delete(URL_DAS_SYSTEM_PROPERTIES+"/"+prop2);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void createPropertiesWithColons() {
        final String prop1 = "property" + RandomGenerator.generateRandomString();
        Map<String, String> payload = new HashMap<>() {{
            put(prop1, "http://localhost:4848");
        }};
        Response response = managementClient.post(URL_DAS_SYSTEM_PROPERTIES, payload);
        assertEquals(200, response.getStatus());
        response = managementClient.get(URL_DAS_SYSTEM_PROPERTIES);
        List<Map<String, String>> systemProperties = getSystemProperties(
            buildMapFromDocument(response.readEntity(String.class)));
        assertNotNull(systemProperties);

        int testPropsFound = 0;
        for (Map<String, String> systemProperty : systemProperties) {
            String name = systemProperty.get("name");
            if (prop1.equals(name)) {
                testPropsFound++;
            }
        }

        assertEquals(1, testPropsFound);

        response = managementClient.delete(URL_DAS_SYSTEM_PROPERTIES + "/" + prop1);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testNotResolvingDasSystemProperties() {
        final String prop1 = "property" + RandomGenerator.generateRandomString();
        createAndTestConfigProperty(prop1, PROP_VALUE, "server-config");
        createAndTestInstanceOverride(prop1, PROP_VALUE, PROP_VALUE+"-instace", "server");
    }

    @Test
    public void testNotResolvingDasInstanceProperties() {
        final String instanceName = "in" + RandomGenerator.generateRandomNumber();
        final String propertyName = "property" + RandomGenerator.generateRandomString();

        Response response = managementClient.post(URL_CREATE_INSTANCE, Map.of("id", instanceName, "node", "localhost-domain1"));
        assertEquals(200, response.getStatus());

        createAndTestConfigProperty(propertyName, PROP_VALUE, instanceName + "-config");
        createAndTestInstanceOverride(propertyName, PROP_VALUE, PROP_VALUE + "-instance", instanceName);
    }

    @Test
    public void testNotResolvingClusterProperties() {
        final String propertyName = "property" + RandomGenerator.generateRandomString();
        final String clusterName = "cluster_" + RandomGenerator.generateRandomNumber();
        final String instanceName = clusterName + "_instance_" + RandomGenerator.generateRandomNumber();
        createCluster(clusterName);
        createClusterInstance(clusterName, instanceName);

        createAndTestConfigProperty(propertyName, PROP_VALUE, clusterName + "-config");
        createAndTestClusterOverride(propertyName, PROP_VALUE, PROP_VALUE + "-cluster", clusterName);
        createAndTestInstanceOverride(propertyName, PROP_VALUE+"-cluster", PROP_VALUE + "-instance", instanceName);

        deleteCluster(clusterName);
    }

    private void createAndTestConfigProperty(final String propertyName, final String propertyValue, String configName) {
        final String url = URL_CONFIG_SYSTEM_PROPERTIES.replaceAll("%config%", configName);
        Response response = managementClient.get(url);
        Map<String, String> payload = getSystemPropertiesMap(getSystemProperties(buildMapFromDocument(response.readEntity(String.class))));
        payload.put(propertyName, propertyValue);
        response = managementClient.post(url, payload);
        assertEquals(200, response.getStatus());

        response = managementClient.get(url);
        List<Map<String, String>> systemProperties = getSystemProperties(buildMapFromDocument(response.readEntity(String.class)));
        Map<String, String> sysProp = getSystemProperty(propertyName, systemProperties);
        assertNotNull(sysProp);
        assertEquals(propertyValue, sysProp.get("value"));
    }

    private void createAndTestClusterOverride(final String propertyName, final String defaultValue, final String propertyValue, final String clusterName) {
        final String clusterSysPropsUrl = URL_CLUSTER_SYSTEM_PROPERTIES.replaceAll("%clusterName%", clusterName);

        Response response = managementClient.get(clusterSysPropsUrl);
        List<Map<String, String>> systemProperties = getSystemProperties(buildMapFromDocument(response.readEntity(String.class)));
        Map<String, String> sysProp = getSystemProperty(propertyName, systemProperties);
        assertNotNull(sysProp);
        assertEquals(sysProp.get("defaultValue"), defaultValue);

        response = managementClient.post(clusterSysPropsUrl, Map.of(propertyName, propertyValue));
        assertEquals(200, response.getStatus());

        // Check updated/overriden system property
        response = managementClient.get(clusterSysPropsUrl);
        systemProperties = getSystemProperties(buildMapFromDocument(response.readEntity(String.class)));
        sysProp = getSystemProperty(propertyName, systemProperties);
        assertNotNull(sysProp);
        assertEquals(sysProp.get("value"), propertyValue);
        assertEquals(sysProp.get("defaultValue"), defaultValue);
    }

    private void createAndTestInstanceOverride(final String propertyName, final String defaultValue, final String propertyValue, final String instanceName) {
        final String instanceSysPropsUrl = URL_INSTANCE_SYSTEM_PROPERTIES.replaceAll("%instanceName%", instanceName);

        Response response = managementClient.get(instanceSysPropsUrl);
        List<Map<String, String>> systemProperties = getSystemProperties(buildMapFromDocument(response.readEntity(String.class)));
        Map<String, String> sysProp = getSystemProperty(propertyName, systemProperties);
        assertNotNull(sysProp);
        assertEquals(sysProp.get("defaultValue"), defaultValue);

        response = managementClient.post(instanceSysPropsUrl, Map.of(propertyName, propertyValue));
        assertEquals(200, response.getStatus());

        // Check updated/overriden system property
        response = managementClient.get(instanceSysPropsUrl);
        systemProperties = getSystemProperties(buildMapFromDocument(response.readEntity(String.class)));
        sysProp = getSystemProperty(propertyName, systemProperties);
        assertNotNull(sysProp);
        assertEquals(propertyValue, sysProp.get("value"));
        assertEquals(defaultValue, sysProp.get("defaultValue"));
    }

    private List<Map<String, String>> getSystemProperties(Map<String, ?> map) {
        Map<String, Object> extraProperties = (Map<String, Object>) map.get("extraProperties");
        if (extraProperties == null) {
            return null;
        }
        return (List<Map<String, String>>) extraProperties.get("systemProperties");
    }

    private Map<String, String> getSystemProperty(String propName, List<Map<String, String>> systemProperties) {
        for (Map<String, String> sysProp : systemProperties) {
            if (sysProp.get("name").equals(propName)) {
                return sysProp;
            }
        }

        return null;
    }

    private Map<String, String> getSystemPropertiesMap (List<Map<String, String>> propsList) {
        if (propsList == null) {
            return new HashMap<>();
        }
        Map<String, String> allPropsMap = new HashMap<>();
        for (Map<String, String> sysProp : propsList) {
            allPropsMap.put(sysProp.get("name"),  sysProp.get("value"));
        }
        return allPropsMap;
    }

}
