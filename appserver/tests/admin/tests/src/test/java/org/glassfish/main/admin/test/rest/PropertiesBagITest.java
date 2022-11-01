/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.glassfish.admin.rest.client.utils.MarshallingUtils;
import org.glassfish.main.admin.test.tool.RandomGenerator;
import org.junit.jupiter.api.Test;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.glassfish.main.admin.test.tool.RandomGenerator.generateRandomString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author jasonlee
 */
public class PropertiesBagITest extends RestTestBase {

    private static final String PROP_DOMAIN_NAME = "administrative.domain.name";
    private static final String URL_DOMAIN_PROPERTIES = "/domain/property";
    private static final String URL_JAVA_CONFIG_PROPERTIES = "/domain/configs/config/default-config/java-config/property";
    private static final String URL_SERVER_PROPERTIES = "/domain/servers/server/server/property";
    private static final String URL_DERBYPOOL_PROPERTIES = "/domain/resources/jdbc-connection-pool/DerbyPool/property";

    @Test
    public void propertyRetrieval() {
        Response response = managementClient.get(URL_DOMAIN_PROPERTIES);
        assertEquals(200, response.getStatus());
        List<Map<String, String>> properties = getProperties(response);
        assertTrue(isPropertyFound(properties, PROP_DOMAIN_NAME));
    }

    @Test
    public void javaConfigProperties() {
        createAndDeleteProperties(URL_JAVA_CONFIG_PROPERTIES);
    }

    @Test
    public void serverProperties() {
        createAndDeleteProperties(URL_SERVER_PROPERTIES);
    }

    @Test
    public void propsWithEmptyValues() {
        List<Map<String, String>> properties = new ArrayList<>();
        final String empty = "empty" + RandomGenerator.generateRandomNumber();
        final String foo = "foo" + RandomGenerator.generateRandomNumber();
        final String bar = "bar" + RandomGenerator.generateRandomNumber();
        final String abc = "abc" + RandomGenerator.generateRandomNumber();

        properties.add(createProperty(empty,""));
        properties.add(createProperty(foo,"foovalue"));
        properties.add(createProperty(bar,"barvalue"));
        createProperties(URL_DERBYPOOL_PROPERTIES, properties);
        List<Map<String, String>> newProperties = getProperties(managementClient.get(URL_DERBYPOOL_PROPERTIES));

        assertFalse(isPropertyFound(newProperties, empty));
        assertTrue(isPropertyFound(newProperties, foo));
        assertTrue(isPropertyFound(newProperties, bar));

        properties.clear();
        properties.add(createProperty(abc,"abcvalue"));
        createProperties(URL_DERBYPOOL_PROPERTIES, properties);
        newProperties = getProperties(managementClient.get(URL_DERBYPOOL_PROPERTIES));

        assertTrue(isPropertyFound(newProperties, abc));
        assertFalse(isPropertyFound(newProperties, empty));
        assertFalse(isPropertyFound(newProperties, foo));
        assertFalse(isPropertyFound(newProperties, bar));
    }

    @Test
    public void testOptimizedPropertyHandling() {
        // First, test changing one property and adding a new
        List<Map<String, String>> properties = new ArrayList<>();
        properties.add(createProperty("PortNumber","1527"));
        properties.add(createProperty("Password","APP"));
        properties.add(createProperty("User","APP"));
        properties.add(createProperty("serverName","localhost"));
        properties.add(createProperty("DatabaseName","sun-appserv-samples"));
        properties.add(createProperty("connectionAttributes",";create=false"));
        properties.add(createProperty("foo","bar","test"));
        createProperties(URL_DERBYPOOL_PROPERTIES, properties);

        List<Map<String, String>> newProperties = getProperties(managementClient.get(URL_DERBYPOOL_PROPERTIES));
        for (Map<String, String> property : newProperties) {
            if (property.get("name").equals("connectionAttributes")) {
                assertEquals(";create=false", property.get("value"));
            } else if (property.get("name").equals("foo")) {
                assertEquals("bar", property.get("value"));
                assertEquals("test", property.get("description"));
            }
        }

        // Test updating the description and value
        properties.clear();
        properties.add(createProperty("foo","bar 2","test 2"));
        createProperties(URL_DERBYPOOL_PROPERTIES, properties);

        newProperties = getProperties(managementClient.get(URL_DERBYPOOL_PROPERTIES));
        assertNotSame(1, newProperties);
        for (Map<String, String> property : newProperties) {
            if (property.get("name").equals("foo")) {
                assertEquals("bar 2", property.get("value"));
                assertEquals("test 2", property.get("description"));
            }
        }

        // Now test changing that property back and deleting the new one
        properties.clear();
        properties.add(createProperty("PortNumber","1527"));
        properties.add(createProperty("Password","APP"));
        properties.add(createProperty("User","APP"));
        properties.add(createProperty("serverName","localhost"));
        properties.add(createProperty("DatabaseName","sun-appserv-samples"));
        properties.add(createProperty("connectionAttributes",";create=true"));

        createProperties(URL_DERBYPOOL_PROPERTIES, properties);

        newProperties = getProperties(managementClient.get(URL_DERBYPOOL_PROPERTIES));
        for (Map<String, String> property : newProperties) {
            if (property.get("name").equals("connectionAttributes")) {
                assertEquals(";create=true", property.get("value"));
            } else if (property.get("name").equals("foo")) {
                fail("The property was not deleted as expected.");
            }
        }
    }

    @Test
    public void testPropertiesWithDots() {
        List<Map<String, String>> properties = new ArrayList<>();
        final String key = "some.property.with.dots." + RandomGenerator.generateRandomNumber();
        final String description = "This is the description";
        final String value = generateRandomString();
        properties.add(createProperty(key, value, description));
        createProperties(URL_DERBYPOOL_PROPERTIES, properties);

        List<Map<String, String>> newProperties = getProperties(managementClient.get(URL_DERBYPOOL_PROPERTIES));
        Map<String, String> newProp = getProperty(newProperties, key);
        assertNotNull(newProp);
        assertEquals(value, newProp.get("value"));
        assertNull(newProp.get("description"), "Descriptions are not returned at this moment: " + newProp);
    }

    // This operation is taking a REALLY long time from the console, probably due
    // to improper properties handling when create the RA config.  However, when
    // updating the config's properties, we need to verfiy that only the changed
    // properties are updated, as the broker restarts after every property is
    // saved. This test will create the jmsra config with a set of properties,
    // then update only one the object's properties, which should be a very quick,
    // inexpensive operation.
    @Test
    public void testJmsRaCreateAndUpdate() {
        List<Map<String, String>> props = new ArrayList<>(){{
           add(createProperty("AddressListBehavior", "random"));
           add(createProperty("AddressListIterations", "3"));
           add(createProperty("AdminPassword", "admin"));
           add(createProperty("AdminUserName", "admin"));
           add(createProperty("BrokerInstanceName", "imqbroker"));
           add(createProperty("BrokerPort", "7676"));
           add(createProperty("BrokerStartTimeOut", "60000"));
           add(createProperty("BrokerType", "DIRECT"));
           add(createProperty("ConnectionUrl", "mq\\://localhost\\:7676/"));
           add(createProperty("ReconnectAttempts", "3"));
           add(createProperty("ReconnectEnabled", "true"));
           add(createProperty("ReconnectInterval", "5000"));
           add(createProperty("RmiRegistryPort", "8686"));
           add(createProperty("doBind", "false"));
           add(createProperty("startRMIRegistry", "false"));
        }};
        final String propertyList = buildPropertyList(props);
        Map<String, String> attrs = new HashMap<>() {{
            put("objecttype","user");
            put("id","jmsra");
            put("threadPoolIds","thread-pool-1");
            put("property", propertyList);
        }};

        final String url = "/domain/resources/resource-adapter-config";
        managementClient.delete(url + "/jmsra");
        Response response = managementClient.post(url, attrs);
        assertEquals(200, response.getStatus());

        // Change one property value (AddressListIterations) and update the object
        props = new ArrayList<>(){{
           add(createProperty("AddressListBehavior", "random"));
           add(createProperty("AddressListIterations", "4"));
           add(createProperty("AdminPassword", "admin"));
           add(createProperty("AdminUserName", "admin"));
           add(createProperty("BrokerInstanceName", "imqbroker"));
           add(createProperty("BrokerPort", "7676"));
           add(createProperty("BrokerStartTimeOut", "60000"));
           add(createProperty("BrokerType", "DIRECT"));
           add(createProperty("ConnectionUrl", "mq\\://localhost\\:7676/"));
           add(createProperty("ReconnectAttempts", "3"));
           add(createProperty("ReconnectEnabled", "true"));
           add(createProperty("ReconnectInterval", "5000"));
           add(createProperty("RmiRegistryPort", "8686"));
           add(createProperty("doBind", "false"));
           add(createProperty("startRMIRegistry", "false"));
        }};
        createProperties(url + "/jmsra/property", props);

        managementClient.delete(url + "/jmsra");
    }

    @Test
    public void test20810() {
        Map<String, String> payload = new HashMap<>();
        payload.put("persistenceScope","session");
        payload.put("disableJreplica","false");
        payload.put("persistenceType","replicated");
        payload.put("availabilityEnabled","true");
        payload.put("persistenceFrequency","web-method");
        payload.put("persistenceStoreHealthCheckEnabled","false");
        payload.put("ssoFailoverEnabled","false");

        final String wcaUri = "/domain/configs/config/default-config/availability-service/web-container-availability";
        Response response = managementClient.post(wcaUri, payload);
        assertThat(response.getStatus(), equalTo(200));

        assertThat(managementClient.get(wcaUri).getStatus(), equalTo(200));

        response = managementClient.post(
            "/domain/configs/config/default-config/availability-service/web-container-availability/property",
            Entity.json(new JSONArray()));
        assertThat(response.getStatus(), equalTo(200));
        assertThat(managementClient.get(wcaUri).getStatus(), equalTo(200));
    }

    private String buildPropertyList(List<Map<String, String>> props) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (Map<String, String> prop : props) {
            sb.append(sep).append(prop.get("name")).append("=").append(prop.get("value"));
            sep = ":";
        }

        return sb.toString();
    }

    private void createAndDeleteProperties(String endpoint) {
        Response response = managementClient.get(endpoint);
        assertEquals(200, response.getStatus());
        assertNotNull(getProperties(response));

        List<Map<String, String>> properties = new ArrayList<>();

        for(int i = 0, max = RandomGenerator.generateRandomNumber(16); i < max; i++) {
            properties.add(
                createProperty("property_" + generateRandomString(), generateRandomString(), generateRandomString()));
        }

        createProperties(endpoint, properties);
        response = managementClient.delete(endpoint);
        assertEquals(200, response.getStatus());
    }

    private Map<String, String> createProperty(final String name, final String value) {
        return createProperty(name, value, null);
    }

    private Map<String, String> createProperty(final String name, final String value, final String description) {
        return new HashMap<>() {
            {
                put("name", name);
                put("value", value);
                if (description != null) {
                    put("description", description);
                }
            }
        };
    }

    private void createProperties(String endpoint, List<Map<String, String>> properties) {
        final String payload = buildPayload(properties);

        Response response = managementClient.post(endpoint, Entity.entity(payload, APPLICATION_JSON));
        assertEquals(200, response.getStatus());
        response = managementClient.get(endpoint);
        assertEquals(200, response.getStatus());

        // Retrieve the properties and make sure they were created.
        List<Map<String, String>> newProperties = getProperties(response);
        for (Map<String, String> property : properties) {
            String name = property.get("name");
            String value = property.get("value");
            if (value == null || value.isBlank()) {
                assertFalse(isPropertyFound(newProperties, name));
            } else {
                assertTrue(isPropertyFound(newProperties, name));
            }
        }
    }

    // Restore and verify the default domain properties
    private void restoreDomainProperties() {
        final HashMap<String, String> domainProps = new HashMap<>() {{
            put("name", PROP_DOMAIN_NAME);
            put("value", "domain1");
        }};
        Response response = managementClient.put(URL_DOMAIN_PROPERTIES,
            Entity.entity(buildPayload(List.of(domainProps)), APPLICATION_JSON));
        assertEquals(200, response.getStatus());
        response = managementClient.get(URL_DOMAIN_PROPERTIES);
        assertEquals(200, response.getStatus());
        assertTrue(isPropertyFound(getProperties(response), PROP_DOMAIN_NAME));
    }

    private String buildPayload(List<Map<String, String>> properties) {
        return MarshallingUtils.getJsonForProperties(properties);
    }

    private boolean isPropertyFound(List<Map<String, String>> properties, String name) {
        return getProperty(properties, name) != null;
    }

    private Map<String, String> getProperty(List<Map<String, String>> properties, String name) {
        Map<String, String> retval = null;
        for (Map<String,String> property : properties) {
            if (name.equals(property.get("name"))) {
                retval = property;
            }
        }

        return retval;
    }
}
