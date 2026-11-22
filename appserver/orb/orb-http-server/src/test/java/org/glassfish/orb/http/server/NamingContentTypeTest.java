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


package org.glassfish.orb.http.server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.orb.http.protocol.NamingRoutes;
import org.glassfish.orb.http.protocol.Protocol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A Content-Type says how the body was written, so one that cannot be read is
 * refused rather than guessed at.
 */
class NamingContentTypeTest {

    private static int status(String contentType) throws IOException {
        Map<String, String> headers = new HashMap<>();
        if (contentType != null) {
            headers.put("Content-Type", contentType);
        }
        byte[] path = NamingRoutes.path(Protocol.CONTEXT_PATH, Protocol.OP_LIST, "x")
                .getBytes(StandardCharsets.UTF_8);
        Loopback.InMemoryExchange exchange = new Loopback.InMemoryExchange("GET", path, null, headers, new byte[0]);
        new NamingDispatcher(new HttpRoundTripTest.FakeNaming()).dispatch(exchange);
        return exchange.toResponse().status();
    }

    @Test
    @DisplayName("guard: without a Content-Type the server's own codec is used")
    void noContentTypeIsServed() throws Exception {
        assertEquals(200, status(null));
    }

    @Test
    @DisplayName("a Content-Type that is not a protocol content type is refused")
    void anUnparseableContentTypeIsRefused() throws Exception {
        assertEquals(Protocol.SC_NOT_ACCEPTABLE, status("garbage"));
        assertEquals(Protocol.SC_NOT_ACCEPTABLE, status("application/x-gf-jser; version=one"));
    }
}
