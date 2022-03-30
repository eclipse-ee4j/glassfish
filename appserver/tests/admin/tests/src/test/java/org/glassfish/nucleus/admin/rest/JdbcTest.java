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

package org.glassfish.nucleus.admin.rest;

import jakarta.ws.rs.core.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * @author jasonlee
 */
@Disabled
public class JdbcTest extends RestTestBase {
    public static final String BASE_JDBC_RESOURCE_URL = "domain/resources/jdbc-resource";
    public static final String BASE_JDBC_CONNECTION_POOL_URL = "domain/resources/jdbc-connection-pool";

    @Test
    public void testReadingPoolEntity() {
        Map<String, String> entity = getEntityValues(get(BASE_JDBC_CONNECTION_POOL_URL + "/__TimerPool"));
        assertEquals("__TimerPool", entity.get("name"));
    }

    @Test
    public void testCreateAndDeletePool() {
        String poolName = "TestPool" + generateRandomString();
        Map<String, String> params = new HashMap<>();
        params.put("name", poolName);
        params.put("datasourceClassname","org.apache.derby.jdbc.ClientDataSource");
        Response response = post(BASE_JDBC_CONNECTION_POOL_URL, params);
        assertEquals(200, response.getStatus());

        Map<String, String> entity = getEntityValues(get(BASE_JDBC_CONNECTION_POOL_URL + "/"+poolName));
        assertNotSame(0, entity.size());

        response = delete(BASE_JDBC_CONNECTION_POOL_URL+"/"+poolName, new HashMap<String, String>());
        assertEquals(200, response.getStatus());

        response = get(BASE_JDBC_CONNECTION_POOL_URL + "/" + poolName);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testCreateResourceWithBackslash() {
        String poolName = "TestPool\\" + generateRandomString();
        String encodedPoolName = poolName;
        try {
            encodedPoolName = URLEncoder.encode(poolName, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(JdbcTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        Map<String, String> params = new HashMap<>();
        params.put("name", poolName);
        params.put("poolName", "DerbyPool");

        Response response = post (BASE_JDBC_RESOURCE_URL, params);
        assertEquals(200, response.getStatus());

        Map<String, String> entity = getEntityValues(get(BASE_JDBC_CONNECTION_POOL_URL + "/" + encodedPoolName));
        assertNotSame(0, entity.size());

        response = delete("/" + encodedPoolName, new HashMap<String, String>());
        assertEquals(200, response.getStatus());

        response = get(BASE_JDBC_CONNECTION_POOL_URL + "/" + encodedPoolName);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void createDuplicateResource() {
        final String resourceName = "jdbc/__default";
        Map<String, String> params = new HashMap<>() {{
           put("id", resourceName);
           put("poolName", "DerbyPool");
        }};

        Response response = post (BASE_JDBC_RESOURCE_URL, params);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void createDuplicateConnectionPool() {
        final String poolName = "DerbyPool";
        Map<String, String> params = new HashMap<>() {{
           put("id", poolName);
           put("datasourceClassname", "org.apache.derby.jdbc.ClientDataSource");
        }};

        Response response = post (BASE_JDBC_CONNECTION_POOL_URL, params);
        assertEquals(404, response.getStatus());
    }
}
