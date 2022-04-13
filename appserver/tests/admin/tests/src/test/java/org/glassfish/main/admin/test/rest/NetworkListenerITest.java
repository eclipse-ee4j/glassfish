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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jasonlee
 */
public class NetworkListenerITest extends RestTestBase {
    private static final String URL_PROTOCOL = "/domain/configs/config/server-config/network-config/protocols/protocol";
    private static final String URL_SSL = "/domain/configs/config/server-config/network-config/protocols/protocol/http-listener-2/ssl";

    private static final String redirectProtocolName = "http-redirect";
    private static final String portUniProtocolName = "pu-protocol";

    private static final String redirectFilterName = "redirect-filter";
    private static final String finderName1 = "http-finder";
    private static final String finderName2 = "http-redirect";

    @AfterAll
    public static void cleanup() {
        Response response = managementClient.post(
            "/domain/configs/config/server-config/network-config/network-listeners/network-listener/http-listener-1",
            Map.of("protocol", "http-listener-1"));
        assertEquals(200, response.getStatus());
        response = managementClient.delete(URL_PROTOCOL + "/" + portUniProtocolName + "/delete-protocol-finder",
            Map.of("protocol", portUniProtocolName, "id", finderName1));
        assertEquals(200, response.getStatus());
        response = managementClient.delete(URL_PROTOCOL + "/" + portUniProtocolName + "/delete-protocol-finder",
            Map.of("protocol", portUniProtocolName, "id", finderName2));
        assertEquals(200, response.getStatus());
        response = managementClient.delete(
            URL_PROTOCOL + "/" + redirectProtocolName
                + "/protocol-chain-instance-handler/protocol-chain/protocol-filter/" + redirectFilterName,
            Map.of("protocol", redirectProtocolName));
        assertEquals(200, response.getStatus());
        response = managementClient.delete(URL_PROTOCOL + "/" + portUniProtocolName);
        assertEquals(200, response.getStatus());
        response = managementClient.delete(URL_PROTOCOL + "/" + redirectProtocolName);
        assertEquals(200, response.getStatus());
    }


    @Test
    public void createHttpListener() {
        Response response = managementClient.post("/domain/set", Map.of(
            "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.protocol",
            "http-listener-1"));
        assertEquals(200, response.getStatus());
        managementClient.delete(URL_PROTOCOL + "/" + portUniProtocolName);
        assertEquals(200, response.getStatus());
        managementClient.delete(URL_PROTOCOL + "/" + redirectProtocolName);
        assertEquals(200, response.getStatus());
        // asadmin commands taken from: http://www.antwerkz.com/port-unification-in-glassfish-3-part-1/
        //        asadmin create-protocol --securityenabled=false http-redirect
        //        asadmin create-protocol --securityenabled=false pu-protocol
        response = managementClient.post(URL_PROTOCOL, Map.of("securityenabled", "false", "id", redirectProtocolName));
        assertEquals(200, response.getStatus());
        response = managementClient.post(URL_PROTOCOL, Map.of("securityenabled", "false", "id", portUniProtocolName));
        assertEquals(200, response.getStatus());

        //        asadmin create-protocol-filter --protocol http-redirect --classname org.glassfish.grizzly.config.portunif.HttpRedirectFilter redirect-filter
        response = managementClient.post(URL_PROTOCOL + "/" + redirectProtocolName + "/create-protocol-filter",
            Map.of("id", redirectFilterName, "protocol", redirectProtocolName,
                "classname", "org.glassfish.grizzly.config.portunif.HttpRedirectFilter"));
        assertEquals(200, response.getStatus());

        //        asadmin create-protocol-finder --protocol pu-protocol --targetprotocol http-listener-2 --classname org.glassfish.grizzly.config.portunif.HttpProtocolFinder http-finder
        //        asadmin create-protocol-finder --protocol pu-protocol --targetprotocol http-redirect   --classname org.glassfish.grizzly.config.portunif.HttpProtocolFinder http-redirect
        response = managementClient.post(URL_PROTOCOL + "/" + portUniProtocolName + "/create-protocol-finder",
            new HashMap<String, String>() {{
                put ("id", finderName1);
                put ("protocol", portUniProtocolName);
                put ("targetprotocol", "http-listener-2");
                put ("classname", "org.glassfish.grizzly.config.portunif.HttpProtocolFinder");
            }});
        assertEquals(200, response.getStatus());
        response = managementClient.post(URL_PROTOCOL + "/" + portUniProtocolName + "/create-protocol-finder",
            new HashMap<String, String>() {{
                put ("id", finderName2);
                put ("protocol", portUniProtocolName);
                put ("targetprotocol", redirectProtocolName);
                put ("classname", "org.glassfish.grizzly.config.portunif.HttpProtocolFinder");
            }});
        assertEquals(200, response.getStatus());


        //        asadmin set configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.protocol=pu-protocol
        response = managementClient.post(
            "/domain/configs/config/server-config/network-config/network-listeners/network-listener/http-listener-1",
            Map.of("protocol", portUniProtocolName));
        assertEquals(200, response.getStatus());

        response = managementClient.get("/domain/configs/config/server-config/network-config/network-listeners/network-listener/http-listener-1/find-http-protocol");
        assertThat(response.readEntity(String.class), stringContainsInOrder("http-listener-2"));
    }

    @Test
    public void testClearingProperties() {
        Map<String, String> params = new HashMap<>() {{
            put("keyStore", "foo");
            put("trustAlgorithm", "bar");
            put("trustMaxCertLength", "15");
            put("trustStore", "baz");
        }};

        Response response = managementClient.post(URL_SSL, params);
        assertEquals(200, response.getStatus());
        response = managementClient.get(URL_SSL, params);
        Map<String, String> entity = this.getEntityValues(response);
        assertEquals(params.get("keyStore"), entity.get("keyStore"));
        assertEquals(params.get("trustAlgorithm"), entity.get("trustAlgorithm"));
        assertEquals(params.get("trustMaxCertLength"), entity.get("trustMaxCertLength"));
        assertEquals(params.get("trustStore"), entity.get("trustStore"));

        params.put("keyStore", "");
        params.put("trustAlgorithm", "");
        params.put("trustStore", "");
        response = managementClient.post(URL_SSL, params);
        assertEquals(200, response.getStatus());
        response = managementClient.get(URL_SSL, params);
        entity = this.getEntityValues(response);
        assertEquals(JSONObject.NULL, entity.get("keyStore"));
        assertEquals(JSONObject.NULL, entity.get("trustAlgorithm"));
        assertEquals(JSONObject.NULL, entity.get("trustStore"));
    }
}
