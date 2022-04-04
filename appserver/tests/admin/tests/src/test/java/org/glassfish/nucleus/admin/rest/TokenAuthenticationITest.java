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

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response;

import java.util.Map;

import org.glassfish.admin.rest.client.ClientWrapper;
import org.glassfish.admin.rest.client.utils.MarshallingUtils;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.nucleus.test.tool.DomainAdminRestClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Mitesh Meswani
 */
public class TokenAuthenticationITest extends RestTestBase {
    private static final String URL_DOMAIN_SESSIONS = "/sessions";
    private static final String URL_CREATE_USER = "/domain/configs/config/server-config/security-service/auth-realm/admin-realm/create-user";
    private static final String URL_DELETE_USER = "/domain/configs/config/server-config/security-service/auth-realm/admin-realm/delete-user";
    private static final String GF_REST_TOKEN_COOKIE_NAME = "gfresttoken";

    private static final String AUTH_USER_NAME = "dummyuser";
    private static final String AUTH_PASSWORD = "dummypass";
    private static final HttpAuthenticationFeature AUTH_DUMMY = HttpAuthenticationFeature.basic(AUTH_USER_NAME, AUTH_PASSWORD);

    @AfterAll
    public static void cleanup() {
        managementClient.delete(URL_DELETE_USER, Map.of("id", AUTH_USER_NAME));
    }


    @Test
    public void testTokenCreateAndDelete() {
        String token = getSessionToken(managementClient);
        assertNotNull(token, "token");

        // Verify we can use the session token.
        Response response = managementClient.getRequestBuilder("/domain")
            .cookie(new Cookie(GF_REST_TOKEN_COOKIE_NAME, token)).get(Response.class);
        assertEquals(200, response.getStatus());

        // Delete the token
        response = managementClient.getRequestBuilder(URL_DOMAIN_SESSIONS + "/" + token)
            .cookie(new Cookie(GF_REST_TOKEN_COOKIE_NAME, token)).delete(Response.class);
        managementClient.delete(URL_DOMAIN_SESSIONS);
        assertEquals(200, response.getStatus());
    }


    @Test
    public void testAuthRequired() {
        Response delResponse = managementClient.delete(URL_DELETE_USER, Map.of("id", AUTH_USER_NAME));
        // as of gf6 any error means 500. The user doesn't exist.
        assertEquals(500, delResponse.getStatus());

        try (DummyClient client = new DummyClient()) {
            Response response = client.get("/domain");
            assertEquals(401, response.getStatus());
        }
        {
            // Create the new user
            Map<String, String> newUser = Map.of(
                "id", AUTH_USER_NAME,
                "groups", "asadmin",
                "authrealmname", "admin-realm",
                "AS_ADMIN_USERPASSWORD", AUTH_PASSWORD
                );
            Response createUserResponse = managementClient.post(URL_CREATE_USER, newUser);
            assertEquals(200, createUserResponse.getStatus());
        }
        try (AnonymousClient client = new AnonymousClient()) {
            Response response = client.getRequestBuilder("/domain").get(Response.class);
            assertEquals(401, response.getStatus());
        }
        final String token;
        try (DummyClient dummyClient = new DummyClient()) {
            token = getSessionToken(dummyClient);
        }

        try (CookieClient cookieClient = new CookieClient(token)) {
            Response response = cookieClient.get("/domain");
            assertEquals(200, response.getStatus());
        }
        try (AnonymousClient client = new AnonymousClient()) {
            Response response = client.getRequestBuilder("/domain").get(Response.class);
            assertEquals(401, response.getStatus());
        }
        try (CookieClient cookieClient = new CookieClient(token)) {
            Response response = cookieClient.delete(URL_DELETE_USER, Map.of("id", AUTH_USER_NAME));
            assertEquals(200, response.getStatus());
        }
    }


    private String getSessionToken(DomainAdminRestClient client) {
        Response response = client.post(URL_DOMAIN_SESSIONS);
        assertEquals(200, response.getStatus());
        Map<String, ?> responseMap = MarshallingUtils.buildMapFromDocument(response.readEntity(String.class));
        Map<String, Object> extraProperties = (Map<String, Object>) responseMap.get("extraProperties");
        return (String) extraProperties.get("token");
    }


    private static final class AnonymousClient extends DomainAdminRestClient {

        public AnonymousClient() {
            super(new ClientWrapper(), managementClient.getBaseUrl(), APPLICATION_JSON);
        }
    }


    private static final class DummyClient extends DomainAdminRestClient {

        public DummyClient() {
            super(createClient(), managementClient.getBaseUrl(), APPLICATION_JSON);
        }

        private static ClientWrapper createClient() {
            ClientWrapper client = new ClientWrapper();
            client.register(AUTH_DUMMY);
            return client;
        }
    }


    private static final class CookieClient extends DomainAdminRestClient {

        private final String securityCookie;

        public CookieClient(final String securityCookie) {
            super(new ClientWrapper(), managementClient.getBaseUrl(), APPLICATION_JSON);
            this.securityCookie = securityCookie;
        }

        @Override
        public Response delete(final String relativePath, final Map<String, String> queryParams) {
            return getTarget(relativePath, queryParams).request()
                .cookie(new Cookie(GF_REST_TOKEN_COOKIE_NAME, securityCookie)).delete(Response.class);
        }


        @Override
        public Response get(final String relativePath, final Map<String, String> queryParams) {
            return getTarget(relativePath, queryParams).request()
                .cookie(new Cookie(GF_REST_TOKEN_COOKIE_NAME, securityCookie)).get(Response.class);
        }

    }
}
