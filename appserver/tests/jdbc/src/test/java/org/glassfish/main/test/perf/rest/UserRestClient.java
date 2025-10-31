/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.perf.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.net.URI;
import java.util.List;

import org.glassfish.main.test.jdbc.pool.war.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserRestClient {

    private final WebTarget target;

    public UserRestClient(WebTarget baseUri) {
        this.target = baseUri.path("user");
    }

    public UserRestClient(URI baseUri, boolean verbose) {
        this.target = RestClientUtilities.getWebTarget(baseUri, verbose).path("user");
    }

    public void create(User user) {
        try (Response response = target.path("create").request().put(Entity.json(user))) {
            assertEquals(Status.NO_CONTENT, response.getStatusInfo().toEnum(), "response.status");
            assertFalse(response.hasEntity(), "response.hasEntity");
        }
    }

    public List<User> list() {
        try (Response response = target.path("list").request().get()) {
            assertEquals(Status.OK, response.getStatusInfo().toEnum(), "response.status");
            assertTrue(response.hasEntity(), "response.hasEntity");
            return response.readEntity(new GenericType<List<User>>() {});
        }
    }

    public long count() {
        try (Response response = target.path("count").request().get()) {
            assertEquals(Status.OK, response.getStatusInfo().toEnum(), "response.status");
            assertTrue(response.hasEntity(), "response.hasEntity");
            return response.readEntity(Long.class);
        }
    }
}
