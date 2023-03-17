/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import org.glassfish.admin.rest.client.ClientWrapper;
import org.glassfish.main.itest.tools.DomainAdminRestClient;
import org.junit.jupiter.api.Test;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.glassfish.main.itest.tools.RandomGenerator.generateRandomString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Mitesh Meswani
 */
public class TokenAuthenticationITest extends SecuredRestTestBase {

    private static final String URL_SESSIONS = "/sessions";

    private static final String INVALID_TOKEN = generateRandomString();

    @Test
    public void testAuthRequired() {
        // Invalid session token
        try (TokenClient tokenClient = new TokenClient(INVALID_TOKEN)) {
            Response response = tokenClient.get(URL_DOMAIN);
            assertThat(response.getStatus(), equalTo(401));
        }

        final String token;
        try (BasicClient basicClient = new BasicClient(AUTH_USER_NAME, AUTH_PASSWORD)) {
            // Create session token
            token = getSessionToken(basicClient);
        }
        assertNotNull(token);

        // Valid session token
        try (TokenClient tokenClient = new TokenClient(token)) {
            Response response = tokenClient.get(URL_DOMAIN);
            assertThat(response.getStatus(), equalTo(200));

            // Delete session token
            response = managementClient.delete(URL_SESSIONS + "/" + token);
            assertThat(response.getStatus(), equalTo(200));

            // And try again
            response = tokenClient.get(URL_DOMAIN);
            assertThat(response.getStatus(), equalTo(401));
        }
    }


    private String getSessionToken(DomainAdminRestClient client) {
        Response response = client.post(URL_SESSIONS);
        assertThat(response.getStatus(), equalTo(200));

        Map<String, Object> extraProperties = getExtraProperties(response);
        return (String) extraProperties.get("token");
    }

    private static final class TokenClient extends DomainAdminRestClient {

        private static final String GF_REST_TOKEN_NAME = "gfresttoken";

        private final String sessionToken;

        public TokenClient(final String sessionToken) {
            super(new ClientWrapper(), managementClient.getBaseUrl(), APPLICATION_JSON);
            this.sessionToken = sessionToken;
        }

        @Override
        public Response delete(final String relativePath, final Map<String, String> queryParams) {
            return getRequestBuilder(relativePath, queryParams).cookie(GF_REST_TOKEN_NAME, sessionToken).delete();
        }

        @Override
        public Response get(final String relativePath, final Map<String, String> queryParams) {
            return getRequestBuilder(relativePath, queryParams).cookie(GF_REST_TOKEN_NAME, sessionToken).get();
        }
    }
}
