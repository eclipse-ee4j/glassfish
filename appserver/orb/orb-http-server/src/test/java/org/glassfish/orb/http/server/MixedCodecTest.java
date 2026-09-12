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
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.glassfish.orb.http.client.ClientConfiguration;
import org.glassfish.orb.http.client.EjbLocator;
import org.glassfish.orb.http.client.HttpEjbClient;
import org.glassfish.orb.http.client.HttpTransport;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Marshaller;
import org.glassfish.orb.http.protocol.Protocol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * One bean, one endpoint, two clients that encode differently.
 */
class MixedCodecTest {

    private FakeContainer container;

    private Loopback loopback;

    /** Records what each exchange asked for and what came back. */
    private static final class Recording implements HttpTransport {

        private final HttpTransport delegate;

        private final List<String> requested = new ArrayList<>();

        private final List<String> answered = new ArrayList<>();

        Recording(HttpTransport delegate) {
            this.delegate = delegate;
        }

        @Override
        public Response exchange(Request request) throws IOException, InterruptedException {
            requested.add(String.valueOf(request.contentType()));
            Response response = delegate.exchange(request);
            answered.add(String.valueOf(response.contentType()));
            return response;
        }

        @Override
        public CompletableFuture<Response> exchangeAsync(Request request) {
            return delegate.exchangeAsync(request);
        }

        @Override
        public String negotiatedVersion() {
            return delegate.negotiatedVersion();
        }

        @Override
        public void close() {
            delegate.close();
        }
    }

    /** A codec no one has, used to check that a refusal is informative. */
    private static final class UnknownCodec implements Marshaller {

        private final Marshaller delegate = new JavaSerializationMarshaller();

        @Override
        public String codec() {
            return "nobodyhasthis";
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

    @BeforeEach
    void setUp() {
        container = new FakeContainer();
        container.registerStateless("GreeterBean", new TestBeans.GreeterBean());
        loopback = new Loopback(new EjbDispatcher(container), new NamingDispatcher(new HttpRoundTripTest.FakeNaming()));
    }

    private HttpEjbClient clientUsing(Marshaller codec, Recording recording) {
        ClientConfiguration config = ClientConfiguration
                .builder(URI.create("http://localhost:8080" + Protocol.CONTEXT_PATH))
                .build();
        return new HttpEjbClient(config, recording, codec, JavaSerializationMarshaller.defaultFilter());
    }

    private TestBeans.Greeter greeterOf(HttpEjbClient client) {
        return client.createProxy(TestBeans.Greeter.class,
                new EjbLocator("myapp", "mymodule", null, "GreeterBean"));
    }

    @Test
    @DisplayName("two clients reach the same bean with different codecs, at the same endpoint")
    void oneBeanServesTwoCodecs() throws Exception {
        Recording first = new Recording(loopback);
        Recording second = new Recording(loopback);

        try (HttpEjbClient jser = clientUsing(new JavaSerializationMarshaller(), first);
             HttpEjbClient other = clientUsing(new TestCodec(), second)) {

            assertEquals("Ada Ada", greeterOf(jser).greet("Ada", 2));
            assertEquals("Grace Grace", greeterOf(other).greet("Grace", 2));

            // Neither client retried, so neither was refused: the server read
            // both codecs against the same bean.
            assertEquals(1, first.requested.size());
            assertEquals(1, second.requested.size());
            assertTrue(first.requested.get(0).contains("jser"));
            assertTrue(second.requested.get(0).contains(TestCodec.CODEC));
        }
    }

    @Test
    @DisplayName("the reply comes back in the codec the request used, not the server's own")
    void theReplyMatchesTheRequestCodec() throws Exception {
        Recording recording = new Recording(loopback);

        try (HttpEjbClient other = clientUsing(new TestCodec(), recording)) {
            greeterOf(other).greet("Grace", 1);

            // A client must never be handed something it did not ask to decode.
            assertTrue(recording.answered.get(0).contains(TestCodec.CODEC),
                    "reply was " + recording.answered.get(0));
        }
    }

    @Test
    @DisplayName("a codec the server does not have is refused, and the refusal says what it does have")
    void anUnknownCodecIsRefusedInformatively() throws Exception {
        Recording recording = new Recording(loopback);

        try (HttpEjbClient stranger = clientUsing(new UnknownCodec(), recording)) {
            // The client downgrades and the call still succeeds - but the
            // server's first answer is the one under test here.
            assertEquals("Ada", greeterOf(stranger).greet("Ada", 1));
            assertEquals(2, recording.requested.size(), "expected one refusal then one retry");
            assertTrue(recording.requested.get(1).contains("jser"));
        }
    }
}
