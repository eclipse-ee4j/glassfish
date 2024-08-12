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

import java.util.Map;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.RandomGenerator;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jasonlee
 */
public class ResourceRefITest extends RestTestBase {
    private static final String URL_CREATE_INSTANCE = "/domain/create-instance";
    private static final String URL_JDBC_RESOURCE = "/domain/resources/jdbc-resource";
    private static final String URL_RESOURCE_REF = "/domain/servers/server/server/resource-ref";

    private final String instanceName = "instance_" + RandomGenerator.generateRandomString();
    private final String jdbcResourceName = "jdbc_" + RandomGenerator.generateRandomString();

    @AfterEach
    public void cleanup() {
        Asadmin asadmin = GlassFishTestEnvironment.getAsadmin();
        asadmin.exec("delete-resource-ref", jdbcResourceName);
        asadmin.exec("delete-jdbc-resource", jdbcResourceName);
        asadmin.exec("delete-instance", instanceName);
    }


    /**
     * <ol>
     * <li>Creates resource on the additional instance
     * <li>Then creates a resource reference referring that on the server instance.
     * <li>Then deletes the resource reference from the additional instance.
     * <li>Then deletes the resource from the domain.
     * <li>Then deletes the additional instance.
     * </ol>
     *
     * @throws Exception
     */
    @Test
    public void testCreatingResourceRef() throws Exception {
        Map<String, String> newInstance = Map.of("id", instanceName, "node", "localhost-domain1");
        Response response = managementClient.post(URL_CREATE_INSTANCE, newInstance);
        assertEquals(200, response.getStatus());

        Map<String, String> jdbcResource = Map.of("id", jdbcResourceName, "connectionpoolid", "DerbyPool",
            "target", instanceName);
        response = managementClient.post(URL_JDBC_RESOURCE, jdbcResource);
        assertEquals(200, response.getStatus());

        Map<String, String> resourceRef = Map.of("id", jdbcResourceName, "target", "server");
        response = managementClient.post(URL_RESOURCE_REF, resourceRef);
        assertEquals(200, response.getStatus());
        response = managementClient.delete(
            "/domain/servers/server/" + instanceName + "/resource-ref/" + jdbcResourceName,
            Map.of("target", instanceName));
        assertEquals(200, response.getStatus());
        response = managementClient.get("/domain/servers/server/" + instanceName + "/resource-ref/" + jdbcResourceName);
        assertEquals(404, response.getStatus());

        response = managementClient.delete(URL_JDBC_RESOURCE + "/" + jdbcResourceName);
        assertEquals(200, response.getStatus());
        response = managementClient.get(URL_JDBC_RESOURCE + "/" + jdbcResourceName);
        assertEquals(404, response.getStatus());

        response = managementClient.delete("/domain/servers/server/" + instanceName + "/delete-instance");
        assertEquals(200, response.getStatus());
        response = managementClient.get("/domain/servers/server/" + instanceName);
        assertEquals(404, response.getStatus());
    }
}
