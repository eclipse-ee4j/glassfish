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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.orb.http.protocol.NamingRoutes;
import org.glassfish.orb.http.protocol.Protocol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What a caller is told when a naming operation fails inside the server.
 */
class NamingFailureReasonTest {

    @Test
    @DisplayName("an unexpected failure names the operation, and not the cause's own text")
    void theReasonDoesNotCarryTheCause() throws Exception {
        NamingBridge naming = (NamingBridge) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { NamingBridge.class }, (proxy, method, args) -> {
                    throw new IllegalStateException("secret internal detail");
                });
        String path = NamingRoutes.path(Protocol.CONTEXT_PATH, Protocol.OP_LOOKUP, "java:global/app/Bean");
        Exchange exchange = new Exchange(NamingRoutes.methodFor(Protocol.OP_LOOKUP), path);

        new NamingDispatcher(naming).dispatch(exchange);

        assertEquals(Protocol.SC_EXCEPTION, exchange.status);
        String reason = exchange.headers.get("X-GF-Reason");
        assertTrue(reason.contains("failed"), reason);
        assertFalse(reason.contains("secret internal detail"), reason);
    }

    private static final class Exchange implements ServerExchange {

        private final String method;
        private final byte[] path;
        int status = 200;
        final Map<String, String> headers = new HashMap<>();

        Exchange(String method, String path) {
            this.method = method;
            this.path = path.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String method() {
            return method;
        }

        @Override
        public byte[] pathBytes() {
            return path;
        }

        @Override
        public int pathOffset() {
            return 0;
        }

        @Override
        public int pathLength() {
            return path.length;
        }

        @Override
        public String requestHeader(String name) {
            return null;
        }

        @Override
        public String queryParameter(String name) {
            return null;
        }

        @Override
        public InputStream requestBody() {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public void setStatus(int status) {
            this.status = status;
        }

        @Override
        public void setResponseHeader(String name, String value) {
            headers.put(name, value);
        }

        @Override
        public void writeBody(ByteBuffer[] body) {
        }

        @Override
        public String authenticatedUser() {
            return null;
        }
    }
}
