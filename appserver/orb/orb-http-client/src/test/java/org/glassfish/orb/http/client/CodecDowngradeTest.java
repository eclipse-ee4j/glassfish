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
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.glassfish.orb.http.protocol.ChunkedOutput;
import org.glassfish.orb.http.protocol.ContentType;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Marshaller;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.ProtocolException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Adding a codec to the client must not break a server that lacks it.
 */
class CodecDowngradeTest {

    /** A codec the far side is pretending not to have. */
    private static final String EXOTIC = "exotic";

    public interface Greeter {
        String greet(String name);
    }

    /**
     * Wraps the built-in encoding but announces a different token, so the
     * bytes stay readable while the negotiation is exercised.
     */
    private static final class ExoticMarshaller implements Marshaller {

        private final Marshaller delegate = new JavaSerializationMarshaller();

        @Override
        public String codec() {
            return EXOTIC;
        }

        @Override
        public ObjectWriter newWriter(OutputStream out) throws IOException {
            return delegate.newWriter(out);
        }

        @Override
        public ObjectReader newReader(InputStream in, ClassLoader loader, ObjectInputFilter filter)
                throws IOException {
            return delegate.newReader(in, loader, filter);
        }
    }

    /** Refuses the first codec it is offered, then answers normally. */
    private static final class PickyTransport implements HttpTransport {

        private final List<String> offered = new ArrayList<>();

        private final String refuses;

        PickyTransport(String refuses) {
            this.refuses = refuses;
        }

        @Override
        public Response exchange(Request request) throws IOException {
            String contentType = request.contentType();
            offered.add(contentType);
            if (contentType != null && contentType.contains(refuses)) {
                return new Response(Protocol.SC_NOT_ACCEPTABLE, null,
                        Map.of("X-GF-Reason", List.of("unsupported codec " + refuses)),
                        new ByteArrayInputStream(new byte[0]));
            }

            Marshaller marshaller = new JavaSerializationMarshaller();
            ChunkedOutput out = new ChunkedOutput();
            try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
                writer.writeObject("hello");
                writer.writeObject(new HashMap<String, Object>());
                writer.flush();
            }
            return new Response(Protocol.SC_OK,
                    ContentType.of(marshaller.codec(), ContentType.KIND_RESPONSE).toHeaderValue(),
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
        }
    }

    @Test
    @DisplayName("a server that cannot read the client's codec gets a second, readable attempt")
    void anUnsupportedCodecIsRetriedWithTheBuiltInOne() throws Throwable {
        PickyTransport transport = new PickyTransport(EXOTIC);
        ClientConfiguration config = ClientConfiguration.builder(URI.create("http://localhost:8080")).build();

        try (HttpEjbClient client = new HttpEjbClient(config, transport, new ExoticMarshaller(),
                JavaSerializationMarshaller.defaultFilter())) {
            EjbLocator locator = new EjbLocator("app", "module", "", "GreeterBean");
            Method greet = Greeter.class.getMethod("greet", String.class);

            Object result = client.invoke(locator, Greeter.class, greet, new Object[] { "world" });

            // The call succeeded despite the server refusing the first codec.
            assertEquals("hello", result);
            assertEquals(2, transport.offered.size(), "expected exactly one retry");
            assertTrue(transport.offered.get(0).contains(EXOTIC));
            assertTrue(transport.offered.get(1).contains(ContentType.CODEC_JSER));
        }
    }

    @Test
    @DisplayName("a reply in another codec is decoded with that codec, not misread")
    void aReplyInAnotherCodecIsDecodedWithIt() throws Throwable {
        // Answers in jser whatever it is offered.
        PickyTransport transport = new PickyTransport("refuses-nothing");
        ClientConfiguration config = ClientConfiguration.builder(URI.create("http://localhost:8080")).build();

        try (HttpEjbClient client = new HttpEjbClient(config, transport, new ExoticMarshaller(),
                JavaSerializationMarshaller.defaultFilter())) {
            EjbLocator locator = new EjbLocator("app", "module", "", "GreeterBean");
            Method greet = Greeter.class.getMethod("greet", String.class);

            // Decoding this with the codec we sent rather than the one declared
            // reads a Java serialization stream header as a length prefix and
            // reports a nonsense negative frame length, far from the cause.
            assertEquals("hello", client.invoke(locator, Greeter.class, greet, new Object[] { "world" }));
        }
    }

    @Test
    @DisplayName("a client that required a codec refuses a reply in another one, and says so")
    void aRequiredCodecRefusesAMismatchedReply() throws Exception {
        PickyTransport transport = new PickyTransport("refuses-nothing");
        ClientConfiguration config = ClientConfiguration.builder(URI.create("http://localhost:8080"))
                .codec(EXOTIC)
                .build();

        try (HttpEjbClient client = new HttpEjbClient(config, transport, new ExoticMarshaller(),
                JavaSerializationMarshaller.defaultFilter())) {
            EjbLocator locator = new EjbLocator("app", "module", "", "GreeterBean");
            Method greet = Greeter.class.getMethod("greet", String.class);

            ProtocolException thrown = assertThrows(ProtocolException.class,
                    () -> client.invoke(locator, Greeter.class, greet, new Object[] { "world" }));
            assertTrue(thrown.getMessage().contains(EXOTIC), thrown.getMessage());
            assertTrue(thrown.getMessage().contains(ContentType.CODEC_JSER), thrown.getMessage());
        }
    }

    @Test
    @DisplayName("a codec asked for by name is not silently walked back")
    void arequiredCodecIsNotDowngraded() throws Exception {
        PickyTransport transport = new PickyTransport(EXOTIC);
        ClientConfiguration config = ClientConfiguration.builder(URI.create("http://localhost:8080"))
                .codec(EXOTIC)
                .build();

        try (HttpEjbClient client = new HttpEjbClient(config, transport, new ExoticMarshaller(),
                JavaSerializationMarshaller.defaultFilter())) {
            EjbLocator locator = new EjbLocator("app", "module", "", "GreeterBean");
            Method greet = Greeter.class.getMethod("greet", String.class);

            // Someone who chose a codec deliberately would rather see the
            // refusal than quietly get the encoding they were moving away from.
            assertThrows(ProtocolException.class,
                    () -> client.invoke(locator, Greeter.class, greet, new Object[] { "world" }));
            assertEquals(1, transport.offered.size(), "must not retry a codec that was demanded");
        }
    }

    @Test
    @DisplayName("requiring a codec that is not installed fails at construction, naming what is")
    void aMissingRequiredCodecFailsEarly() {
        ClientConfiguration config = ClientConfiguration.builder(URI.create("http://localhost:8080"))
                .codec("not-installed")
                .build();

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> new HttpEjbClient(config));
        assertTrue(thrown.getMessage().contains("not-installed"));
        assertTrue(thrown.getMessage().contains(ContentType.CODEC_JSER),
                "the failure should say what is available: " + thrown.getMessage());
    }

    @Test
    @DisplayName("the downgrade is remembered, so the failed attempt is paid once")
    void theDowngradeIsNotRepeatedOnEveryCall() throws Throwable {
        PickyTransport transport = new PickyTransport(EXOTIC);
        ClientConfiguration config = ClientConfiguration.builder(URI.create("http://localhost:8080")).build();

        try (HttpEjbClient client = new HttpEjbClient(config, transport, new ExoticMarshaller(),
                JavaSerializationMarshaller.defaultFilter())) {
            EjbLocator locator = new EjbLocator("app", "module", "", "GreeterBean");
            Method greet = Greeter.class.getMethod("greet", String.class);

            client.invoke(locator, Greeter.class, greet, new Object[] { "world" });
            client.invoke(locator, Greeter.class, greet, new Object[] { "again" });

            // Three exchanges, not four: the second call goes straight to the
            // codec that is known to work.
            assertEquals(3, transport.offered.size());
            assertTrue(transport.offered.get(2).contains(ContentType.CODEC_JSER));
        }
    }
}
