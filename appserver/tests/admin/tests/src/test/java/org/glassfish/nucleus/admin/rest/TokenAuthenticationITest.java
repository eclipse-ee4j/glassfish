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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.admin.rest.client.ClientWrapper;
import org.glassfish.admin.rest.client.utils.MarshallingUtils;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Mitesh Meswani
 */
public class TokenAuthenticationITest extends RestTestBase {
    private static final String URL_DOMAIN_SESSIONS = "sessions";
    private static final String URL_CREATE_USER = "domain/configs/config/server-config/security-service/auth-realm/admin-realm/create-user";
    private static final String URL_DELETE_USER = "domain/configs/config/server-config/security-service/auth-realm/admin-realm/delete-user";
    private static final String GF_REST_TOKEN_COOKIE_NAME = "gfresttoken";
    private static final String TEST_GROUP = "newgroup";

    private static final String AUTH_USER_NAME = "dummyuser";
    private static final String AUTH_PASSWORD = "dummypass";
    private static final HttpAuthenticationFeature AUTH_DUMMY = HttpAuthenticationFeature.basic(AUTH_USER_NAME, AUTH_PASSWORD);
    private static final HttpAuthenticationFeature AUTH_NONE = HttpAuthenticationFeature.digest();
    private final DummyClient client = new DummyClient();


    @AfterEach
    public void closeClient() throws Exception {
        if (client != null) {
            client.close();
        }
    }


    @Test
    public void testTokenCreateAndDelete() {
        String token = getSessionToken(this);
        assertNotNull(token, "token");

        // Verify we can use the session token.
        Response response = getClient().target(getAddress("domain")).request().cookie(new Cookie(GF_REST_TOKEN_COOKIE_NAME, token)).get(Response.class);
        assertEquals(200, response.getStatus());

        // Delete the token
        response = getClient().target(getAddress(URL_DOMAIN_SESSIONS) + "/" + token).request().cookie(new Cookie(GF_REST_TOKEN_COOKIE_NAME, token)).delete(Response.class);
        delete(URL_DOMAIN_SESSIONS);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testAuthRequired() {
        String token = null;
        try {
            deleteUserAuthTestUser(null);

            // Verify that we can get unauthenticated access to the server
            Response response = client.get("domain");
            assertEquals(401, response.getStatus());

            // Create the new user
            Map<String, String> newUser = Map.of(
                "id", AUTH_USER_NAME,
                "groups", "asadmin",
                "authrealmname", "admin-realm",
                "AS_ADMIN_USERPASSWORD", AUTH_PASSWORD
            );
            response = post(URL_CREATE_USER, newUser);
            assertEquals(200, response.getStatus());

            // Verify that we must now authentication (response.status = 401)
            response = client.get("domain");
            assertEquals(401, response.getStatus());

            // Authenticate, get the token, then "clear" the authentication
            client.getClient().register(AUTH_DUMMY);
            token = getSessionToken(client);
            client.resetClient();

            // Build this request manually so we can pass the cookie
            response = client.getClient().target(getAddress("domain")).request().cookie(new Cookie(GF_REST_TOKEN_COOKIE_NAME, token)).get(Response.class);
            assertEquals(200, response.getStatus());
            client.resetClient();

            // Request again w/o the cookie.  This should fail.
            response = client.getClient().target(getAddress("domain")).request().get(Response.class);
            assertEquals(401, response.getStatus());
        } finally {
            deleteUserAuthTestUser(token);
        }
    }

    private String getSessionToken(RestTestBase client) {
        Response response = client.post(URL_DOMAIN_SESSIONS);
        assertEquals(200, response.getStatus());
        Map<String, ?> responseMap = MarshallingUtils.buildMapFromDocument(response.readEntity(String.class));
        Map<String, Object> extraProperties = (Map<String, Object>) responseMap.get("extraProperties");
        return (String) extraProperties.get("token");
    }

    private void deleteUserAuthTestUser(String token) {
        if (token == null) {
            Response response = delete(URL_DELETE_USER, Map.of("id", AUTH_USER_NAME));
            if (response.getStatus() == 401) {
                response = delete(URL_DELETE_USER, Map.of("id", AUTH_USER_NAME));
                assertEquals(200, response.getStatus());
            }
        } else {
            final String address = getAddress(URL_DELETE_USER);
            Response response = getClient().target(address).queryParam("id", AUTH_USER_NAME).request()
                .cookie(new Cookie(GF_REST_TOKEN_COOKIE_NAME, token)).delete(Response.class);
            assertEquals(200, response.getStatus());
        }
    }


    private static class DummyClient extends RestTestBase implements Closeable {

        Client client;

        @Override
        protected Client getClient() {
            if (client == null) {
                client = new ClientWrapper(new HashMap<String, String>(), null, null);
            }
            return client;
        }


        @Override
        protected void resetClient() {
            if (client == null) {
                return;
            }
            client.close();
            client = null;
        }


        @Override
        public void close() throws IOException {
            resetClient();
        }
    }
}
