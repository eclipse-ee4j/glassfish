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

import org.glassfish.admin.rest.client.ClientWrapper;
import org.glassfish.main.itest.tools.DomainAdminRestClient;
import org.junit.jupiter.api.Test;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class BasicAuthenticationITest extends SecuredRestTestBase {

    private static final String INVALID_USER_NAME = "invaliduser";

    private static final String INVALID_PASSWORD = "invalidpass";

    @Test
    public void testAuthRequired() {
        // Invalid credentials
        try (BasicClient basicClient = new BasicClient(INVALID_USER_NAME, INVALID_PASSWORD)) {
            Response response = basicClient.get(URL_DOMAIN);
            assertThat(response.getStatus(), equalTo(401));
        }

        // Anonymous access
        try (AnonymousClient anonymousClient = new AnonymousClient()) {
            Response response = anonymousClient.get(URL_DOMAIN);
            assertThat(response.getStatus(), equalTo(401));
        }

        // Valid credentials
        try (BasicClient basicClient = new BasicClient(AUTH_USER_NAME, AUTH_PASSWORD)) {
            Response response = basicClient.get(URL_DOMAIN);
            assertThat(response.getStatus(), equalTo(200));
        }
    }

    private static final class AnonymousClient extends DomainAdminRestClient {

        public AnonymousClient() {
            super(new ClientWrapper(), managementClient.getBaseUrl(), APPLICATION_JSON);
        }
    }
}
