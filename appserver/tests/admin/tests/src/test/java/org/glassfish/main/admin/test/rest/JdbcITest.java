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

import jakarta.ws.rs.core.Response;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.main.itest.tools.RandomGenerator;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author jasonlee
 */
public class JdbcITest extends RestTestBase {

    private static final String URL_DATABASE_VENDOR_NAMES = "/domain/resources/get-database-vendor-names";

    private static final String URL_JDBC_DRIVER_CLASS_NAMES = "/domain/resources/get-jdbc-driver-class-names";

    private static final String DATABASE_VENDOR_ORACLE = "ORACLE";

    @Test
    public void testReadingPoolEntity() {
        Map<String, String> entity = getEntityValues(managementClient.get(URL_JDBC_CONNECTION_POOL + "/__TimerPool"));
        assertEquals("__TimerPool", entity.get("name"));
    }


    @Test
    public void testCreateAndDeletePool() {
        String poolName = "TestPool" + RandomGenerator.generateRandomString();
        Map<String, String> params = new HashMap<>();
        params.put("name", poolName);
        params.put("datasourceClassname", "org.apache.derby.jdbc.ClientDataSource");
        Response response = managementClient.post(URL_JDBC_CONNECTION_POOL, params);
        assertEquals(200, response.getStatus());

        Map<String, String> entity = getEntityValues(managementClient.get(URL_JDBC_CONNECTION_POOL + "/" + poolName));
        assertThat(entity, aMapWithSize(greaterThan(40)));

        response = managementClient.delete(URL_JDBC_CONNECTION_POOL + "/" + poolName, Map.of());
        assertEquals(200, response.getStatus());

        response = managementClient.get(URL_JDBC_CONNECTION_POOL + "/" + poolName);
        assertEquals(404, response.getStatus());
    }


    @Test
    public void testBackslashValidation() {
        String poolName = "TestPool\\" + RandomGenerator.generateRandomString();
        String encodedPoolName = URLEncoder.encode(poolName, StandardCharsets.UTF_8);
        Map<String, String> params = new HashMap<>();
        params.put("name", poolName);
        params.put("poolName", "DerbyPool");

        Response response = managementClient.post(URL_JDBC_RESOURCE, params);
        assertEquals(500, response.getStatus());

        Response responseGet = managementClient.get(URL_JDBC_CONNECTION_POOL + "/" + encodedPoolName);
        assertEquals(500, response.getStatus());
        Map<String, String> entity = getEntityValues(responseGet);
        assertNull(entity);

        response = managementClient.delete("/" + encodedPoolName, Map.of());
        assertEquals(500, response.getStatus());

        response = managementClient.get(URL_JDBC_CONNECTION_POOL + "/" + encodedPoolName);
        assertEquals(500, response.getStatus());
    }


    @Test
    public void createDuplicateResource() {
        final String resourceName = "jdbc/__default";
        Map<String, String> params = Map.of("id", resourceName, "poolName", "DerbyPool");
        Response response = managementClient.post(URL_JDBC_RESOURCE, params);
        assertEquals(500, response.getStatus());
    }


    @Test
    public void createDuplicateConnectionPool() {
        final String poolName = "DerbyPool";
        Map<String, String> params = Map.of("id", poolName, "datasourceClassname",
            "org.apache.derby.jdbc.ClientDataSource");
        Response response = managementClient.post(URL_JDBC_CONNECTION_POOL, params);
        assertEquals(500, response.getStatus());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetDatabaseVendorNames() {
        Response response = managementClient.get(URL_DATABASE_VENDOR_NAMES);
        assertEquals(200, response.getStatus());

        Map<String, Object> extraProperties = getExtraProperties(response);
        assertNotNull(extraProperties);

        List<String> vendorNames = (List<String>) extraProperties.get("vendorNames");
        assertThat(vendorNames, not(empty()));
    }

    @Test
    public void testGetJdbcDriverClassNames() {
        List<String> driverClassNames = getJdbcResourceClassNames("java.sql.Driver");

        assertNotNull(driverClassNames);
        assertThat(driverClassNames, not(empty()));
    }

    @Test
    public void testGetDataSourceClassNames() {
        List<String> dataSourceClassNames = getJdbcResourceClassNames("javax.sql.DataSource");

        assertNotNull(dataSourceClassNames);
        assertThat(dataSourceClassNames, not(empty()));
    }

    @Test
    public void testGetXADataSourceClassNames() {
        List<String> dataSourceClassNames = getJdbcResourceClassNames("javax.sql.XADataSource");

        assertNotNull(dataSourceClassNames);
        assertThat(dataSourceClassNames, not(empty()));
    }

    @Test
    public void testGetConnectionPoolDataSourceClassNames() {
        List<String> dataSourceClassNames = getJdbcResourceClassNames("javax.sql.ConnectionPoolDataSource");

        assertNotNull(dataSourceClassNames);
        assertThat(dataSourceClassNames, not(empty()));
    }

    @SuppressWarnings("unchecked")
    private List<String> getJdbcResourceClassNames(String resourceType) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("dbVendor", DATABASE_VENDOR_ORACLE);
        queryParams.put("restype", resourceType);
        queryParams.put("introspect", "false");

        Response response = managementClient.get(URL_JDBC_DRIVER_CLASS_NAMES, queryParams);

        Map<String, Object> extraProperties = getExtraProperties(response);
        if (extraProperties == null) {
            return null;
        }
        return (List<String>) extraProperties.get("driverClassNames");
    }
}
