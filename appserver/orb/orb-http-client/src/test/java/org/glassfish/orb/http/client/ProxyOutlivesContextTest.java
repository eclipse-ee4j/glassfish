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
import java.net.URI;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.Context;

import org.glassfish.orb.http.protocol.ChunkedOutput;
import org.glassfish.orb.http.protocol.ContentType;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Marshaller;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.RemoteEjbReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A proxy has to work after the context that produced it is closed.
 * <p>
 * Look a bean up, close the context, use the bean: that is the ordinary shape
 * of JNDI code, and an IIOP stub survives it because a stub is its own object.
 * A proxy from this transport shares the context's connection pool, and
 * closing that pool turned every proxy into one that answers "IOException:
 * closed" on first use - a trap laid for code that did nothing wrong.
 * <p>
 * Found by a bean on one server calling a bean on another, which is exactly
 * the code that writes itself this way.
 */
class ProxyOutlivesContextTest {

    public interface Greeter {
        String greet(String name);
    }

    /** Answers lookups with a reference, invocations with a result, and notices closing. */
    private static final class Recording implements HttpTransport {

        final AtomicBoolean closed = new AtomicBoolean();

        @Override
        public Response exchange(Request request) throws IOException {
            if (closed.get()) {
                throw new IOException("closed");
            }
            Marshaller marshaller = new JavaSerializationMarshaller();
            ChunkedOutput out = new ChunkedOutput();
            boolean lookup = request.uri().getPath().contains('/' + Protocol.SVC_NAMING + '/');
            try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
                if (lookup) {
                    writer.writeObject(new RemoteEjbReference("app", "module", null,
                            "GreeterBean", Greeter.class.getName(), null));
                } else {
                    writer.writeObject("hello");
                    writer.writeObject(new HashMap<String, Object>());
                }
                writer.flush();
            }
            String kind = lookup ? ContentType.KIND_VALUE : ContentType.KIND_RESPONSE;
            return new Response(Protocol.SC_OK,
                    ContentType.of(marshaller.codec(), kind).toHeaderValue(),
                    Map.of(), new ByteArrayInputStream(out.toByteArray()));
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
            closed.set(true);
        }
    }

    private static HttpNamingContext contextOver(Recording transport) {
        ClientConfiguration config = ClientConfiguration
                .builder(URI.create("http://localhost:8080" + Protocol.CONTEXT_PATH)).build();
        return new HttpNamingContext(config, transport, new Hashtable<String, String>());
    }

    @Test
    @DisplayName("a bean looked up before the context closed is still callable after")
    void aProxySurvivesTheContextThatMadeIt() throws Exception {
        Recording transport = new Recording();
        HttpNamingContext context = contextOver(transport);

        Greeter greeter = (Greeter) context.lookup(
                "java:global/app/module/GreeterBean!" + Greeter.class.getName());
        context.close();

        assertFalse(transport.closed.get(), "closing the context closed the proxy's transport");
        assertEquals("hello", greeter.greet("world"));
    }

    @Test
    @DisplayName("a context that handed out nothing still releases what it opened")
    void anUnusedContextStillCloses() throws Exception {
        Recording transport = new Recording();
        HttpNamingContext context = contextOver(transport);

        // Nothing was looked up, so nothing depends on the transport and
        // holding it open would be a leak.
        context.close();

        assertTrue(transport.closed.get());
    }

    @Test
    @DisplayName("the environment is still the context's own business")
    void theEnvironmentIsUnaffected() throws Exception {
        Recording transport = new Recording();
        Hashtable<String, String> environment = new Hashtable<>();
        environment.put(Context.PROVIDER_URL, "http://localhost:8080/glassfish-services");

        ClientConfiguration config = ClientConfiguration
                .builder(URI.create("http://localhost:8080" + Protocol.CONTEXT_PATH)).build();
        HttpNamingContext context = new HttpNamingContext(config, transport, environment);

        assertEquals("http://localhost:8080/glassfish-services",
                context.getEnvironment().get(Context.PROVIDER_URL));
        context.close();
    }
}
