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
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import org.glassfish.admin.rest.client.utils.MarshallingUtils;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

/**
 * @author Mitesh Meswani
 */
public class TokenAuthenticationTest extends RestTestBase {
    private static final String URL_DOMAIN_SESSIONS = "/sessions";
    private static final String URL_CREATE_USER = "/domain/configs/config/server-config/security-service/auth-realm/admin-realm/create-user";
    private static final String URL_DELETE_USER = "/domain/configs/config/server-config/security-service/auth-realm/admin-realm/delete-user";
    private static final String GF_REST_TOKEN_COOKIE_NAME = "gfresttoken";
    private static final String TEST_GROUP = "newgroup";

    @Test
    public void testTokenCreateAndDelete() {
        deleteUserAuthTestUser(null); // just in case
        //Verify a session token got created
        String token = getSessionToken();

        // Verify we can use the session token.
        Response response = getClient().target(getAddress("/domain")).request().cookie(new Cookie(GF_REST_TOKEN_COOKIE_NAME, token)).get(Response.class);
        assertTrue(isSuccess(response));

        //Delete the token
        response = getClient().target(getAddress(URL_DOMAIN_SESSIONS) + "/" + token).request().cookie(new Cookie(GF_REST_TOKEN_COOKIE_NAME, token)).delete(Response.class);
        delete(URL_DOMAIN_SESSIONS);
        assertTrue(isSuccess(response));
    }

    @Test
    public void testAuthRequired() {
        Map<String, String> newUser = new HashMap<String, String>() {{
            put("id", AUTH_USER_NAME);
            put("groups", "asadmin");
            put("authrealmname", "admin-realm");
            put("AS_ADMIN_USERPASSWORD", AUTH_PASSWORD);
        }};
        String token = null;

        try {
            // Delete the test user if it exists
            deleteUserAuthTestUser(token);

            // Verify that we can get unauthenticated access to the server
            Response response = get("/domain");
            assertTrue(isSuccess(response));

            // Create the new user
            response = post(URL_CREATE_USER, newUser);
            assertTrue(isSuccess(response));

            // Verify that we must now authentication (response.status = 401)
            response = get("/domain");
            assertFalse(isSuccess(response));

            // Authenticate, get the token, then "clear" the authentication
            authenticate();
            token = getSessionToken();
            resetClient();

            // Build this request manually so we can pass the cookie
            response = getClient().target(getAddress("/domain")).request().cookie(new Cookie(GF_REST_TOKEN_COOKIE_NAME, token)).get(Response.class);
            assertTrue(isSuccess(response));
            resetClient();

            // Request again w/o the cookie.  This should fail.
            response = getClient().target(getAddress("/domain")).request().get(Response.class);
            assertFalse(isSuccess(response));
            authenticate();
        } finally {
            // Clean up after ourselves
            deleteUserAuthTestUser(token);
        }
    }

    protected String getSessionToken() {
        Response response = post(URL_DOMAIN_SESSIONS);
        assertTrue(isSuccess(response));
        Map<String, Object> responseMap = MarshallingUtils.buildMapFromDocument(response.readEntity(String.class));
        Map<String, Object> extraProperties = (Map<String, Object>)responseMap.get("extraProperties");
        return (String)extraProperties.get("token");
    }

    private void deleteUserAuthTestUser(String token) {
        if (token != null) {
            final String address = getAddress(URL_DELETE_USER);
            Response response = getClient().target(address).queryParam("id", AUTH_USER_NAME).request()
                    .cookie(new Cookie(GF_REST_TOKEN_COOKIE_NAME, token)).delete(Response.class);
            assertTrue(isSuccess(response));
            resetClient();
        } else {
            Response response = delete(URL_DELETE_USER, new HashMap<String, String>() {{ put("id", AUTH_USER_NAME); }});
            if (response.getStatus() == 401) {
                authenticate();
                response = delete(URL_DELETE_USER, new HashMap<String, String>() {{ put("id", AUTH_USER_NAME); }});
                assertTrue(isSuccess(response));
                resetClient();
            }
        }
    }
}
