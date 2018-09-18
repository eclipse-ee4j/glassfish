/*
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

package org.glassfish.nucleus.admin.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.glassfish.admin.rest.client.utils.MarshallingUtils;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

/**
 *
 * @author jasonlee
 */
public class SystemPropertiesTest extends RestTestBase {
    public static final String URL_CONFIG_SYSTEM_PROPERTIES = "/domain/configs/config/%config%/system-properties";
    public static final String URL_CLUSTER_SYSTEM_PROPERTIES = "/domain/clusters/cluster/%clusterName%/system-properties";
    public static final String URL_INSTANCE_SYSTEM_PROPERTIES = "/domain/servers/server/%instanceName%/system-properties";
    public static final String URL_DAS_SYSTEM_PROPERTIES = URL_INSTANCE_SYSTEM_PROPERTIES.replaceAll("%instanceName%", "server");
    public static final String URL_CREATE_INSTANCE = "/domain/create-instance";
    public static final String PROP_VALUE = "${com.sun.aas.instanceRoot}/foo";

    @Test
    public void getSystemProperties() {
        Response response = get(URL_DAS_SYSTEM_PROPERTIES);
        checkStatusForSuccess(response);
        List<Map<String, String>> systemProperties = getSystemProperties(MarshallingUtils.buildMapFromDocument(response.readEntity(String.class)));
        assertNotNull(systemProperties); // This may or may not be empty, depending on whether or not other tests failed
    }

    @Test
    public void createSystemProperties() {
        final String prop1 = "property" + generateRandomString();
        final String prop2 = "property" + generateRandomString();
        Map<String, String> payload = new HashMap<String, String>() {{
            put(prop1, "value1");
            put(prop2, "value2");
        }};
        Response response = post(URL_DAS_SYSTEM_PROPERTIES, payload);
        checkStatusForSuccess(response);
        response = get(URL_DAS_SYSTEM_PROPERTIES);
        List<Map<String, String>> systemProperties = getSystemProperties(MarshallingUtils.buildMapFromDocument(response.readEntity(String.class)));
        assertNotNull(systemProperties); // This may or may not be empty, depending on whether or not other tests failed

        int testPropsFound = 0;
        for (Map<String, String> systemProperty : systemProperties) {
            String name = (String)systemProperty.get("name");
            if (prop1.equals(name) || prop2.equals(name)) {
                testPropsFound++;
            }
        }

        assertEquals(2, testPropsFound);

        response = delete(URL_DAS_SYSTEM_PROPERTIES+"/"+prop1);
        checkStatusForSuccess(response);
        response = delete(URL_DAS_SYSTEM_PROPERTIES+"/"+prop2);
        checkStatusForSuccess(response);
    }

    @Test
    public void createPropertiesWithColons() {
        final String prop1 = "property" + generateRandomString();
        Map<String, String> payload = new HashMap<String, String>() {{
            put(prop1, "http://localhost:4848");
        }};
        Response response = post(URL_DAS_SYSTEM_PROPERTIES, payload);
        checkStatusForSuccess(response);
        response = get(URL_DAS_SYSTEM_PROPERTIES);
        List<Map<String, String>> systemProperties = getSystemProperties(MarshallingUtils.buildMapFromDocument(response.readEntity(String.class)));
        assertNotNull(systemProperties); // This may or may not be empty, depending on whether or not other tests failed

        int testPropsFound = 0;
        for (Map<String, String> systemProperty : systemProperties) {
            String name = (String)systemProperty.get("name");
            if (prop1.equals(name)) {
                testPropsFound++;
            }
        }

        assertEquals(1, testPropsFound);

        response = delete(URL_DAS_SYSTEM_PROPERTIES+"/"+prop1);
        checkStatusForSuccess(response);
    }
    
    @Test
    public void testNotResolvingDasSystemProperties() {
        final String prop1 = "property" + generateRandomString();
        Map<String, String> payload = new HashMap<String, String>() {{
            put(prop1, PROP_VALUE);
        }};
        createAndTestConfigProperty(prop1, PROP_VALUE, "server-config");
        createAndTestInstanceOverride(prop1, PROP_VALUE, PROP_VALUE+"-instace", "server");
    }

    @Test()
    public void testNotResolvingDasInstanceProperties() {
        final String instanceName = "in" + generateRandomNumber();
        final String propertyName = "property" + generateRandomString();

        Response cr = post(URL_CREATE_INSTANCE, new HashMap<String, String>() {{
            put("id", instanceName);
            put("node", "localhost-domain1");
        }});
        checkStatusForSuccess(cr);
        
        createAndTestConfigProperty(propertyName, PROP_VALUE, instanceName + "-config");

        createAndTestInstanceOverride(propertyName, PROP_VALUE, PROP_VALUE + "-instance", instanceName);
    }
    
    @Test()
    public void testNotResolvingClusterProperties() {
        final String propertyName = "property" + generateRandomString();
        final String clusterName = "c" + generateRandomNumber();
        final String instanceName = clusterName + "in" + generateRandomNumber();
        ClusterTest ct = new ClusterTest();
        ct.setup();
        ct.createCluster(clusterName);
        ct.createClusterInstance(clusterName, instanceName);
        
        createAndTestConfigProperty(propertyName, PROP_VALUE, clusterName + "-config");
        createAndTestClusterOverride(propertyName, PROP_VALUE, PROP_VALUE + "-cluster", clusterName);
        createAndTestInstanceOverride(propertyName, PROP_VALUE+"-cluster", PROP_VALUE + "-instance", instanceName);
        
        ct.deleteCluster(clusterName);
    }
    
    protected void createAndTestConfigProperty(final String propertyName, final String propertyValue, String configName) {
        final String url = URL_CONFIG_SYSTEM_PROPERTIES.replaceAll("%config%", configName);
        Response response = get(url);
        Map<String, String> payload = getSystemPropertiesMap(getSystemProperties(MarshallingUtils.buildMapFromDocument(response.readEntity(String.class))));
        payload.put(propertyName, propertyValue);
        response = post(url, payload);
        checkStatusForSuccess(response);

            // Check config props
        response = get(url);
        List<Map<String, String>> systemProperties = getSystemProperties(MarshallingUtils.buildMapFromDocument(response.readEntity(String.class)));
        Map<String, String> sysProp = getSystemProperty(propertyName, systemProperties);
        assertNotNull(sysProp);
        assertEquals(sysProp.get("value"), propertyValue);
    }
    
    protected void createAndTestClusterOverride(final String propertyName, final String defaultValue, final String propertyValue, final String clusterName) {
        final String clusterSysPropsUrl = URL_CLUSTER_SYSTEM_PROPERTIES.replaceAll("%clusterName%", clusterName);
        
        Response response = get(clusterSysPropsUrl);
        List<Map<String, String>> systemProperties = getSystemProperties(MarshallingUtils.buildMapFromDocument(response.readEntity(String.class)));
        Map<String, String> sysProp = getSystemProperty(propertyName, systemProperties);
        assertNotNull(sysProp);
        assertEquals(sysProp.get("defaultValue"), defaultValue);
    
        response = post(clusterSysPropsUrl, new HashMap<String, String>() {{
            put(propertyName, propertyValue);
        }});
        checkStatusForSuccess(response);
        
        // Check updated/overriden system property
        response = get(clusterSysPropsUrl);
        systemProperties = getSystemProperties(MarshallingUtils.buildMapFromDocument(response.readEntity(String.class)));
        sysProp = getSystemProperty(propertyName, systemProperties);
        assertNotNull(sysProp);
        assertEquals(sysProp.get("value"), propertyValue);
        assertEquals(sysProp.get("defaultValue"), defaultValue);
    }
    
    protected void createAndTestInstanceOverride(final String propertyName, final String defaultValue, final String propertyValue, final String instanceName) {
        final String instanceSysPropsUrl = URL_INSTANCE_SYSTEM_PROPERTIES.replaceAll("%instanceName%", instanceName);
        
        Response response = get(instanceSysPropsUrl);
        List<Map<String, String>> systemProperties = getSystemProperties(MarshallingUtils.buildMapFromDocument(response.readEntity(String.class)));
        Map<String, String> sysProp = getSystemProperty(propertyName, systemProperties);
        assertNotNull(sysProp);
        assertEquals(sysProp.get("defaultValue"), defaultValue);
    
        response = post(instanceSysPropsUrl, new HashMap<String, String>() {{
            put(propertyName, propertyValue);
        }});
        checkStatusForSuccess(response);
        
        // Check updated/overriden system property
        response = get(instanceSysPropsUrl);
        systemProperties = getSystemProperties(MarshallingUtils.buildMapFromDocument(response.readEntity(String.class)));
        sysProp = getSystemProperty(propertyName, systemProperties);
        assertNotNull(sysProp);
        assertEquals(sysProp.get("value"), propertyValue);
        assertEquals(sysProp.get("defaultValue"), defaultValue);
    }
    
    private List<Map<String, String>> getSystemProperties(Map<String, Object> responseMap) {
        Map<String, Object> extraProperties = (Map<String, Object>)responseMap.get("extraProperties");
        return (List<Map<String, String>>)extraProperties.get("systemProperties");
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
        Map<String, String> allPropsMap = new HashMap();
        for (Map<String, String> sysProp : propsList) {
            allPropsMap.put(sysProp.get("name"),  sysProp.get("value"));
        }
        return allPropsMap;
    }
    
}
