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
import java.util.Map;
import jakarta.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONObject;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

/**
 *
 * @author jasonlee
 */
public class NetworkListenerTest extends RestTestBase {
    protected static final String URL_PROTOCOL = "/domain/configs/config/server-config/network-config/protocols/protocol";
    protected static final String URL_SSL = "/domain/configs/config/server-config/network-config/protocols/protocol/http-listener-2/ssl";

    @Test(enabled=false)
    public void createHttpListener() {
        final String redirectProtocolName = "http-redirect"; //protocol_" + generateRandomString();
        final String portUniProtocolName = "pu-protocol"; //protocol_" + generateRandomString();

        final String redirectFilterName = "redirect-filter"; //filter_" + generateRandomString();
        final String finderName1 = "http-finder"; //finder" + generateRandomString();
        final String finderName2 = "http-redirect"; //finder" + generateRandomString();

        try {
            Response response = post("/domain/set", new HashMap<String, String>() {{
                put("configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.protocol", "http-listener-1");
            }});
            checkStatusForSuccess(response);
            delete(URL_PROTOCOL + "/" + portUniProtocolName);
            checkStatusForSuccess(response);
            delete(URL_PROTOCOL + "/" + redirectProtocolName);
            checkStatusForSuccess(response);
// asadmin commands taken from: http://www.antwerkz.com/port-unification-in-glassfish-3-part-1/
//        asadmin create-protocol --securityenabled=false http-redirect
//        asadmin create-protocol --securityenabled=false pu-protocol
            response = post(URL_PROTOCOL, new HashMap<String, String>() {{ put ("securityenabled", "false"); put("id", redirectProtocolName); }});
            checkStatusForSuccess(response);
            response = post(URL_PROTOCOL, new HashMap<String, String>() {{ put ("securityenabled", "false"); put("id", portUniProtocolName); }});
            checkStatusForSuccess(response);

//        asadmin create-protocol-filter --protocol http-redirect --classname org.glassfish.grizzly.config.portunif.HttpRedirectFilter redirect-filter
            response = post (URL_PROTOCOL + "/" + redirectProtocolName + "/create-protocol-filter",
                new HashMap<String, String>() {{
                    put ("id", redirectFilterName);
                    put ("protocol", redirectProtocolName);
                    put ("classname", "org.glassfish.grizzly.config.portunif.HttpRedirectFilter");
                }});
            checkStatusForSuccess(response);

//        asadmin create-protocol-finder --protocol pu-protocol --targetprotocol http-listener-2 --classname org.glassfish.grizzly.config.portunif.HttpProtocolFinder http-finder
//        asadmin create-protocol-finder --protocol pu-protocol --targetprotocol http-redirect   --classname org.glassfish.grizzly.config.portunif.HttpProtocolFinder http-redirect
            response = post (URL_PROTOCOL + "/" + portUniProtocolName + "/create-protocol-finder",
                new HashMap<String, String>() {{
                    put ("id", finderName1);
                    put ("protocol", portUniProtocolName);
                    put ("targetprotocol", "http-listener-2");
                    put ("classname", "org.glassfish.grizzly.config.portunif.HttpProtocolFinder");
                }});
            checkStatusForSuccess(response);
            response = post (URL_PROTOCOL + "/" + portUniProtocolName + "/create-protocol-finder",
                new HashMap<String, String>() {{
                    put ("id", finderName2);
                    put ("protocol", portUniProtocolName);
                    put ("targetprotocol", redirectProtocolName);
                    put ("classname", "org.glassfish.grizzly.config.portunif.HttpProtocolFinder");
                }});
            checkStatusForSuccess(response);


//        asadmin set configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.protocol=pu-protocol
            response = post("/domain/configs/config/server-config/network-config/network-listeners/network-listener/http-listener-1", new HashMap<String, String>() {{
                put("protocol", portUniProtocolName);
            }});
            checkStatusForSuccess(response);

            response = get("/domain/configs/config/server-config/network-config/network-listeners/network-listener/http-listener-1/find-http-protocol");
            assertTrue(response.readEntity(String.class).contains("http-listener-2"));
        } finally {
//            ClientResponse response = post("/domain/set", new HashMap<String, String>() {{
            Response response = post("/domain/configs/config/server-config/network-config/network-listeners/network-listener/http-listener-1", new HashMap<String, String>() {{
                put("protocol", "http-listener-1");
            }});
            checkStatusForSuccess(response);
            response = delete(URL_PROTOCOL + "/" + portUniProtocolName + "/delete-protocol-finder",
                new HashMap<String, String>() {{
                    put("protocol", portUniProtocolName);
                    put("id", finderName1);
                }} );
            checkStatusForSuccess(response);
            response = delete(URL_PROTOCOL + "/" + portUniProtocolName + "/delete-protocol-finder",
                new HashMap<String, String>() {{
                    put("protocol", portUniProtocolName);
                    put("id", finderName2);
                }} );
            checkStatusForSuccess(response);
            response = delete(URL_PROTOCOL + "/" + redirectProtocolName + "/protocol-chain-instance-handler/protocol-chain/protocol-filter/" + redirectFilterName,
                    new HashMap<String, String>() {{ put("protocol", redirectProtocolName); }} );
            checkStatusForSuccess(response);
            response = delete(URL_PROTOCOL + "/" + portUniProtocolName);
            checkStatusForSuccess(response);
            response = delete(URL_PROTOCOL + "/" + redirectProtocolName);
            checkStatusForSuccess(response);
        }

    }

    @Test
    public void testClearingProperties() {
        Map<String, String> params = new HashMap<String, String>() {{
            put("keyStore", "foo");
            put("trustAlgorithm", "bar");
            put("trustMaxCertLength", "15");
            put("trustStore", "baz");
        }};

        Response response = post(URL_SSL, params);
        assertTrue(isSuccess(response));
        response = get(URL_SSL, params);
        Map<String, String> entity = this.getEntityValues(response);
        assertEquals(params.get("keyStore"), entity.get("keyStore"));
        assertEquals(params.get("trustAlgorithm"), entity.get("trustAlgorithm"));
        assertEquals(params.get("trustMaxCertLength"), entity.get("trustMaxCertLength"));
        assertEquals(params.get("trustStore"), entity.get("trustStore"));

        params.put("keyStore", "");
        params.put("trustAlgorithm", "");
        params.put("trustStore", "");
        response = post(URL_SSL, params);
        assertTrue(isSuccess(response));
        response = get(URL_SSL, params);
        entity = this.getEntityValues(response);
        assertEquals(JSONObject.NULL, entity.get("keyStore"));
        assertEquals(JSONObject.NULL, entity.get("trustAlgorithm"));
        assertEquals(JSONObject.NULL, entity.get("trustStore"));
    }
}
