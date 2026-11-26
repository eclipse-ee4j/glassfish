/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.glassfish.microprofile.telemetry.rest;

import java.util.List;

import org.glassfish.jersey.uri.UriTemplate;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ServerTelemetryListenerTest {

    @Test
    void routeJoinsBasePathAndTemplatesFromRootResource() {
        // Jersey lists the matched templates from the most specific one
        List<UriTemplate> templates = List.of(new UriTemplate("/span/{name}"), new UriTemplate("/"));
        assertThat(ServerTelemetryListener.route("/app/", templates), equalTo("/app/span/{name}"));
    }

    @Test
    void routeOfSubResource() {
        List<UriTemplate> templates = List.of(new UriTemplate("{id}"), new UriTemplate("orders"));
        assertThat(ServerTelemetryListener.route("/app/api/", templates), equalTo("/app/api/orders/{id}"));
    }

    @Test
    void routeOfRootResource() {
        assertThat(ServerTelemetryListener.route("/", List.of(new UriTemplate("/"))), equalTo("/"));
        assertThat(ServerTelemetryListener.route("", List.of()), equalTo("/"));
    }
}
