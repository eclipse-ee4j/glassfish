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

package org.glassfish.orb.http.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Protocol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeoutCancelTest {

    public interface Greeter {
        String greet(String name);
    }

    /** Times out on every invocation and accepts everything else. */
    private static final class TimingOutTransport implements HttpTransport {

        final List<Request> requests = new ArrayList<>();

        @Override
        public Response exchange(Request request) throws IOException {
            requests.add(request);
            if (request.uri().getRawPath().contains("/invoke/")) {
                throw new HttpTimeoutException("request timed out");
            }
            return new Response(Protocol.SC_NO_CONTENT, null, Map.of(), new ByteArrayInputStream(new byte[0]));
        }

        @Override
        public CompletableFuture<Response> exchangeAsync(Request request) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public String negotiatedVersion() {
            return "HTTP/1.1";
        }

        @Override
        public void close() {
        }
    }

    @Test
    @DisplayName("a call that times out asks the server to stop it, and still reports the timeout")
    void aTimedOutCallIsCancelledOnTheServer() throws Exception {
        TimingOutTransport transport = new TimingOutTransport();
        ClientConfiguration config = ClientConfiguration.builder(URI.create("http://localhost:8080")).build();

        try (HttpEjbClient client = new HttpEjbClient(config, transport, new JavaSerializationMarshaller(),
                JavaSerializationMarshaller.defaultFilter())) {
            EjbLocator locator = new EjbLocator("app", "module", "", "GreeterBean");
            Method greet = Greeter.class.getMethod("greet", String.class);

            assertThrows(HttpTimeoutException.class,
                    () -> client.invoke(locator, Greeter.class, greet, new Object[] { "world" }));

            assertEquals(2, transport.requests.size(), "the invocation and then a cancel");
            HttpTransport.Request invocation = transport.requests.get(0);
            HttpTransport.Request cancel = transport.requests.get(1);
            String id = invocation.headers().get(Protocol.H_INVOCATION_ID);
            assertEquals("DELETE", cancel.method());
            assertTrue(cancel.uri().getRawPath().contains("/cancel/"), cancel.uri().toString());
            assertTrue(cancel.uri().getRawPath().contains("/" + id + "/"), cancel.uri().toString());
        }
    }
}
