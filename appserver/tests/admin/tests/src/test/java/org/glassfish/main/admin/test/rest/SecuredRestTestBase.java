/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.basic;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class SecuredRestTestBase extends RestTestBase {

    private static final String URL_CREATE_USER = "/domain/configs/config/server-config/security-service/auth-realm/admin-realm/create-user";

    private static final String URL_DELETE_USER = "/domain/configs/config/server-config/security-service/auth-realm/admin-realm/delete-user";

    protected static final String AUTH_USER_NAME = "dummyuser";

    protected static final String AUTH_PASSWORD = "dummypass";

    @BeforeAll
    public static void createUser() {
        // Create the new user
        Map<String, String> newUser = Map.of(
            "id", AUTH_USER_NAME,
            "groups", "asadmin",
            "authrealmname", "admin-realm",
            "AS_ADMIN_USERPASSWORD", AUTH_PASSWORD
        );
        Response response = managementClient.post(URL_CREATE_USER, newUser);
        assertThat(response.getStatus(), equalTo(200));
    }

    @AfterAll
    public static void deleteUser() {
        Response response = managementClient.delete(URL_DELETE_USER, Map.of("id", AUTH_USER_NAME));
        assertThat(response.getStatus(), equalTo(200));
    }

    protected static final class BasicClient extends DomainAdminRestClient {

        public BasicClient(String userName, String password) {
            super(createClient(userName, password), managementClient.getBaseUrl(), APPLICATION_JSON);
        }

        private static ClientWrapper createClient(String userName, String password) {
            ClientWrapper client = new ClientWrapper();
            client.register(basic(userName, password));
            return client;
        }
    }
}
