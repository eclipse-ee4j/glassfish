/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.glassfish.orb.http.server.ServerExchange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What a gRPC client sees. The status of a gRPC call travels in the trailers,
 * so every answer here is checked for the trailer as much as for the body: a
 * reply without one leaves a client waiting.
 */
class ForyGrpcServerAdapterTest {

    private static final String PATH = "/demo.Greeter/greet";

    public interface Greeter {
        String greet(String name);
    }

    @Test
    void answersAFramedReplyAndAnOkStatus() throws Exception {
        Fixture fixture = new Fixture(name -> "Hello " + name);
        FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));

        fixture.adapter().dispatch(exchange);

        assertEquals(200, exchange.status);
        assertEquals("application/grpc+fory", exchange.headers.get("Content-Type"));
        assertEquals("0", exchange.trailers.get("grpc-status"));
        assertEquals("Hello Ada", fixture.responseOf(exchange.body()));
    }

    @Test
    void reportsAFailedInvocationAsAStatusRatherThanATransportError() throws Exception {
        Fixture fixture = new Fixture(name -> {
            throw new IllegalStateException("bean said no");
        });
        FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));

        fixture.adapter().dispatch(exchange);

        // A gRPC error is an HTTP 200 whose trailers carry the status.
        assertEquals(200, exchange.status);
        assertEquals("2", exchange.trailers.get("grpc-status"));
        assertTrue(exchange.trailers.get("grpc-message").contains("bean said no"),
                exchange.trailers.get("grpc-message"));
        assertEquals(0, exchange.body().length);
    }

    @Test
    void refusesACompressedFrameWithUnimplemented() throws Exception {
        Fixture fixture = new Fixture(name -> "Hello " + name);
        byte[] compressed = fixture.frame("Ada");
        compressed[0] = 1;
        FakeExchange exchange = fixture.exchange(compressed);

        fixture.adapter().dispatch(exchange);

        assertEquals("12", exchange.trailers.get("grpc-status"));
    }

    @Test
    void refusesAFrameOverTheLimitWithResourceExhausted() throws Exception {
        Fixture fixture = new Fixture(name -> "Hello " + name, 4);
        FakeExchange exchange = fixture.exchange(fixture.frame("a name longer than four bytes"));

        fixture.adapter().dispatch(exchange);

        assertEquals("8", exchange.trailers.get("grpc-status"));
    }

    @Test
    void refusesATruncatedFrameWithInvalidArgument() throws Exception {
        Fixture fixture = new Fixture(name -> "Hello " + name);
        byte[] full = fixture.frame("Ada");
        byte[] truncated = new byte[full.length - 2];
        System.arraycopy(full, 0, truncated, 0, truncated.length);
        FakeExchange exchange = fixture.exchange(truncated);

        fixture.adapter().dispatch(exchange);

        assertEquals("3", exchange.trailers.get("grpc-status"));
    }

    @Test
    void answersAnUnknownPathWith404WhichAClientReadsAsUnimplemented() throws Exception {
        Fixture fixture = new Fixture(name -> "Hello " + name);
        FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));
        exchange.path = "/demo.Greeter/absent";

        fixture.adapter().dispatch(exchange);

        assertEquals(404, exchange.status);
        assertNull(exchange.trailers.get("grpc-status"));
    }

    @Test
    void refusesToAnswerOnATransportThatCannotSendTrailers() throws Exception {
        Fixture fixture = new Fixture(name -> {
            throw new AssertionError("the bean must not be reached");
        });
        FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));
        exchange.trailersSupported = false;

        fixture.adapter().dispatch(exchange);

        assertEquals(501, exchange.status);
    }

    @Test
    void percentEncodesAStatusMessageAsTheSpecificationRequires() {
        // Only bytes outside 0x20-0x7E, and the percent sign itself, are
        // encoded: a space is a legal header character and stays one.
        assertEquals("caff%C3%A8 100%25", ForyGrpcServerAdapter.percentEncode("caffè 100%"));
    }

    /** A registry with one method, its generated models, and the adapter under test. */
    private static final class Fixture {
        private final ForyRuntimeModelGenerator.GeneratedModels models;
        private final ForyGeneratedRuntime runtime;
        private final ForyGrpcServerAdapter adapter;
        private final Object bean;

        Fixture(Greeter bean) {
            this(bean, 1 << 20);
        }

        Fixture(Greeter bean, int maxMessageBytes) {
            this.bean = bean;
            models = ForyRuntimeModelGenerator.unary("generated.adapter", "greet", String.class, String.class);
            runtime = new ForyGeneratedRuntime(models, 2000, 2001);
            ForyGeneratedServiceRegistry registry = new ForyGeneratedServiceRegistry();
            registry.register(PATH, PATH, ForyGrpcSkeleton.of("demo.Greeter", Greeter.class), models, runtime,
                    ForyGeneratedSchemaAdapter.unary(models.request(), models.response()));
            adapter = new ForyGrpcServerAdapter(registry, (path, exchange) -> new ForyGrpcServerAdapter.Target() {
                @Override
                public Object value() {
                    return Fixture.this.bean;
                }

                @Override
                public void close() {
                }
            }, maxMessageBytes);
        }

        ForyGrpcServerAdapter adapter() {
            return adapter;
        }

        FakeExchange exchange(byte[] requestFrame) {
            return new FakeExchange(PATH, requestFrame);
        }

        /** The five-byte gRPC envelope around a serialized request model. */
        byte[] frame(String name) throws Exception {
            byte[] payload = runtime.serialize(construct(models.request(), name));
            byte[] frame = new byte[payload.length + 5];
            frame[0] = 0;
            frame[1] = (byte) (payload.length >>> 24);
            frame[2] = (byte) (payload.length >>> 16);
            frame[3] = (byte) (payload.length >>> 8);
            frame[4] = (byte) payload.length;
            System.arraycopy(payload, 0, frame, 5, payload.length);
            return frame;
        }

        String responseOf(byte[] body) throws Exception {
            byte[] payload = new byte[body.length - 5];
            System.arraycopy(body, 5, payload, 0, payload.length);
            Object response = runtime.deserialize(payload, models.response());
            return (String) response.getClass().getMethod("value").invoke(response);
        }

        private static Object construct(Class<?> model, String value) throws Exception {
            return model.getConstructor(String.class).newInstance(value);
        }
    }

    /** Records what the adapter did, including the trailers a real client needs. */
    private static final class FakeExchange implements ServerExchange {
        private String path;
        private final byte[] requestBody;
        private int status;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private final Map<String, String> trailers = new LinkedHashMap<>();
        private byte[] body = new byte[0];
        private boolean trailersSupported = true;

        FakeExchange(String path, byte[] requestBody) {
            this.path = path;
            this.requestBody = requestBody;
        }

        byte[] body() {
            return body;
        }

        @Override
        public String method() {
            return "POST";
        }

        @Override
        public byte[] pathBytes() {
            return path.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public int pathOffset() {
            return 0;
        }

        @Override
        public int pathLength() {
            return pathBytes().length;
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
            return new ByteArrayInputStream(requestBody);
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
        public boolean supportsResponseTrailers() {
            return trailersSupported;
        }

        @Override
        public void setResponseTrailer(String name, String value) {
            if (!trailersSupported) {
                throw new UnsupportedOperationException(name);
            }
            trailers.put(name, value);
        }

        @Override
        public void writeBody(ByteBuffer[] buffers) throws IOException {
            int total = 0;
            for (ByteBuffer buffer : buffers) {
                total += buffer.remaining();
            }
            byte[] out = new byte[total];
            int at = 0;
            for (ByteBuffer buffer : buffers) {
                int length = buffer.remaining();
                buffer.get(out, at, length);
                at += length;
            }
            body = out;
        }

        @Override
        public String authenticatedUser() {
            return null;
        }
    }
}
