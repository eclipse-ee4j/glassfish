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
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.orb.http.server.ServerExchange;
import org.glassfish.orb.http.server.TransactionBridge;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void reachesABeanRegisteredAfterTheEndpointStarted() throws Exception {
        // The registry is built before applications are deployed, so an
        // unknown path is a reason to look again, not to give up.
        Fixture fixture = new Fixture(name -> "Hello " + name, 1 << 20, false);
        FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));

        fixture.adapter().dispatch(exchange);

        assertEquals(200, exchange.status);
        assertEquals("0", exchange.trailers.get("grpc-status"));
        assertEquals("Hello Ada", fixture.responseOf(exchange.body()));
    }

    @Test
    void clearsTheRequestThreadBeforeTheCallReachesTheBean() throws Exception {
        // Request threads are pooled. A branch left on this one by an earlier
        // invocation would become this bean's transaction, so the adapter has
        // to detach before the container sets the invocation up - not after.
        List<String> events = new ArrayList<>();
        Fixture fixture = new Fixture(name -> {
            events.add("bean");
            return "Hello " + name;
        }, events);
        FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));

        fixture.adapter().dispatch(exchange);

        assertEquals(List.of("detach", "acquire", "bean"), events);
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
    void refusesACallThatIsNotGrpcWith415() throws Exception {
        for (String contentType : new String[] {null, "text/plain", "application/json",
                "application/x-www-form-urlencoded", "application/grpc-web", "application/grpcx"}) {
            List<String> events = new ArrayList<>();
            Fixture fixture = new Fixture(name -> {
                throw new AssertionError("the bean must not be reached");
            }, events);
            FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));
            exchange.contentType = contentType;

            fixture.adapter().dispatch(exchange);

            assertEquals(415, exchange.status, String.valueOf(contentType));
            assertNull(exchange.trailers.get("grpc-status"));
            assertEquals(List.of(), events, "the container was reached for " + contentType);
        }
    }

    @Test
    void acceptsTheGrpcContentTypesAClientSends() throws Exception {
        for (String contentType : new String[] {"application/grpc", "application/grpc+fory",
                "application/grpc; charset=utf-8", "Application/GRPC+proto"}) {
            Fixture fixture = new Fixture(name -> "Hello " + name);
            FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));
            exchange.contentType = contentType;

            fixture.adapter().dispatch(exchange);

            assertEquals("0", exchange.trailers.get("grpc-status"), contentType);
        }
    }

    @Test
    void refusesAMalformedFrameWithInvalidArgumentWithoutReachingTheContainer() throws Exception {
        List<byte[]> bodies = new ArrayList<>();
        bodies.add(new byte[0]);
        bodies.add(new byte[] {0});
        bodies.add(new byte[] {0, 0, 0, 0});
        Fixture probe = new Fixture(name -> "Hello " + name);
        byte[] full = probe.frame("Ada");
        byte[] trailing = new byte[full.length + 1];
        System.arraycopy(full, 0, trailing, 0, full.length);
        bodies.add(trailing);

        for (byte[] body : bodies) {
            List<String> events = new ArrayList<>();
            Fixture fixture = new Fixture(name -> {
                throw new AssertionError("the bean must not be reached");
            }, events);
            FakeExchange exchange = fixture.exchange(body);

            fixture.adapter().dispatch(exchange);

            assertEquals("3", exchange.trailers.get("grpc-status"), "body of " + body.length + " bytes");
            assertEquals(List.of(), events, "the container was reached for a body of " + body.length + " bytes");
        }
    }

    @Test
    void refusesAPayloadThatIsNotARequestMessageWithInvalidArgument() throws Exception {
        List<String> events = new ArrayList<>();
        Fixture fixture = new Fixture(name -> {
            throw new AssertionError("the bean must not be reached");
        }, events);
        byte[] garbage = {0, 0, 0, 0, 3, 1, 2, 3};
        FakeExchange exchange = fixture.exchange(garbage);

        fixture.adapter().dispatch(exchange);

        assertEquals("3", exchange.trailers.get("grpc-status"));
        assertEquals(List.of(), events);
    }

    @Test
    void doesNotAnswerForAnErrorOfTheVirtualMachine() throws Exception {
        Fixture fixture = new Fixture(name -> {
            throw new OutOfMemoryError("test");
        });
        FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));

        assertThrows(OutOfMemoryError.class, () -> fixture.adapter().dispatch(exchange));
        assertNull(exchange.trailers.get("grpc-status"));
    }

    @Test
    void stillAnswersForAnOtherErrorOfTheBean() throws Exception {
        Fixture fixture = new Fixture(name -> {
            throw new NoClassDefFoundError("gone");
        });
        FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));

        fixture.adapter().dispatch(exchange);

        assertEquals("13", exchange.trailers.get("grpc-status"));
        assertFalse(exchange.trailers.get("grpc-message").contains("gone"), exchange.trailers.toString());
    }

    @Test
    void capsTheStatusMessage() throws Exception {
        Fixture fixture = new Fixture(name -> {
            throw new IllegalStateException("x".repeat(5000));
        });
        FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));

        fixture.adapter().dispatch(exchange);

        String message = exchange.trailers.get("grpc-message");
        assertEquals("2", exchange.trailers.get("grpc-status"));
        assertTrue(message.startsWith("java.lang.IllegalStateException: xxx"), message);
        assertTrue(message.length() <= ForyGrpcServerAdapter.MAX_STATUS_MESSAGE_CHARS, "" + message.length());
    }

    @Test
    void neverCutsAStatusMessageInTheMiddleOfASurrogatePair() {
        String message = "a".repeat(ForyGrpcServerAdapter.MAX_STATUS_MESSAGE_CHARS - 1) + "\uD83D\uDE00";

        String capped = ForyGrpcServerAdapter.capped(message);

        assertEquals(ForyGrpcServerAdapter.MAX_STATUS_MESSAGE_CHARS - 1, capped.length());
        assertFalse(Character.isHighSurrogate(capped.charAt(capped.length() - 1)));
    }

    @Test
    void acceptsOnlyWellFormedGrpcContentTypes() {
        assertTrue(ForyGrpcServerAdapter.isGrpc("application/grpc"));
        assertTrue(ForyGrpcServerAdapter.isGrpc("application/grpc+proto; x=y"));
        assertFalse(ForyGrpcServerAdapter.isGrpc("application/grpc+"));
        assertFalse(ForyGrpcServerAdapter.isGrpc("application/grpc+ fory"));
        assertFalse(ForyGrpcServerAdapter.isGrpc("application/grpc/x"));
        assertFalse(ForyGrpcServerAdapter.isGrpc("application/grpcfory"));
    }

    private String statusOfABeanThatThrows(RuntimeException failure) throws Exception {
        Fixture fixture = new Fixture(name -> {
            throw failure;
        });
        FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));

        fixture.adapter().dispatch(exchange);

        lastMessage = exchange.trailers.get("grpc-message");
        return exchange.trailers.get("grpc-status");
    }

    private String lastMessage;

    @Test
    void aCallTheContainerRefusedIsPermissionDenied() throws Exception {
        assertEquals("7", statusOfABeanThatThrows(new jakarta.ejb.EJBAccessException("not in role")));
        assertFalse(lastMessage.contains("not in role"), lastMessage);
        assertEquals("7", statusOfABeanThatThrows(new jakarta.ejb.AccessLocalException("not in role")));
        assertEquals("7", statusOfABeanThatThrows(
                new IllegalStateException("wrapped", new jakarta.ejb.EJBAccessException("not in role"))));
    }

    @Test
    void aSecurityExceptionOfTheBeanItselfIsPermissionDeniedButOneInsideAnotherFailureIsNot() throws Exception {
        assertEquals("7", statusOfABeanThatThrows(new SecurityException("no")));
        assertEquals("2", statusOfABeanThatThrows(new RuntimeException("wrapped", new SecurityException("no"))));
    }

    @Test
    void aBeanThatIsNotThereIsNotFound() throws Exception {
        assertEquals("5", statusOfABeanThatThrows(new jakarta.ejb.NoSuchEJBException("gone")));
        assertEquals("5", statusOfABeanThatThrows(new jakarta.ejb.NoSuchObjectLocalException("gone")));
    }

    @Test
    void aFailureOfTheContainerInsideTheCallIsReportedInGeneralTerms() throws Exception {
        assertEquals("13", statusOfABeanThatThrows(
                new jakarta.ejb.EJBException("nested exception is: java.sql.SQLException jdbc:secret@host")));
        assertFalse(lastMessage.contains("secret"), lastMessage);
    }

    @Test
    void anExceptionOfTheBeansOwnCodeIsPassedOnWithItsClassAndMessage() throws Exception {
        assertEquals("2", statusOfABeanThatThrows(new IllegalStateException("no such customer 42")));
        assertTrue(lastMessage.contains("IllegalStateException"), lastMessage);
        assertTrue(lastMessage.contains("no such customer 42"), lastMessage);
    }

    @Test
    void aMismatchBetweenTheTargetAndTheSkeletonIsReportedInGeneralTerms() throws Exception {
        Fixture fixture = new Fixture(name -> "Hello " + name);
        fixture.adaptWithAnotherResponseModel = true;
        FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));

        fixture.adapter().dispatch(exchange);

        // The response cannot be built from what the bean returned.
        assertEquals("13", exchange.trailers.get("grpc-status"));
        assertEquals("internal error", exchange.trailers.get("grpc-message"));
    }

    @Test
    void aResponseThatCannotBeEncodedIsInternal() throws Exception {
        Fixture fixture = new Fixture(name -> "Hello " + name);
        fixture.respondWithAClassTheRuntimeDoesNotKnow = true;
        FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));

        fixture.adapter().dispatch(exchange);

        assertEquals("13", exchange.trailers.get("grpc-status"));
        assertEquals("cannot encode the response", exchange.trailers.get("grpc-message"));
    }

    @Test
    void aFailureToReleaseTheTargetIsLoggedAndDoesNotChangeTheAnswer() throws Exception {
        Fixture fixture = new Fixture(name -> "Hello " + name);
        fixture.closeFails = true;
        FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));

        fixture.adapter().dispatch(exchange);

        assertEquals("0", exchange.trailers.get("grpc-status"));
        assertEquals("Hello Ada", fixture.responseOf(exchange.body()));
    }

    @Test
    void aFailureToReleaseTheTargetAfterAFailedCallDoesNotReplaceTheFailure() throws Exception {
        Fixture fixture = new Fixture(name -> {
            throw new IllegalStateException("bean said no");
        });
        fixture.closeFails = true;
        FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));

        fixture.adapter().dispatch(exchange);

        assertEquals("2", exchange.trailers.get("grpc-status"));
        assertTrue(exchange.trailers.get("grpc-message").contains("bean said no"));
        assertFalse(exchange.trailers.get("grpc-message").contains("release"), exchange.trailers.toString());
    }

    @Test
    void aThreadThatWasInterruptedAnswersCancelledAndReadsNothing() throws Exception {
        List<String> events = new ArrayList<>();
        Fixture fixture = new Fixture(name -> "Hello " + name, events);
        FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));
        Thread.currentThread().interrupt();
        try {
            fixture.adapter().dispatch(exchange);
        } finally {
            assertTrue(Thread.interrupted());
        }

        assertEquals("1", exchange.trailers.get("grpc-status"));
        assertEquals(List.of(), events);
    }

    @Test
    void aFailureOfTheContainerIsReportedInGeneralTermsAndNotFoundForAGoneBean() throws Exception {
        Fixture fixture = new Fixture(name -> "Hello " + name);
        fixture.acquireFailure = new IllegalStateException("jdbc:secret@internal-host");
        FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));

        fixture.adapter().dispatch(exchange);

        assertEquals("13", exchange.trailers.get("grpc-status"));
        assertFalse(exchange.trailers.get("grpc-message").contains("secret"), exchange.trailers.toString());

        Fixture gone = new Fixture(name -> "Hello " + name);
        gone.acquireFailure = new org.glassfish.orb.http.server.ContainerBridge.NoSuchTargetException("gone");
        FakeExchange goneExchange = gone.exchange(gone.frame("Ada"));

        gone.adapter().dispatch(goneExchange);

        assertEquals("5", goneExchange.trailers.get("grpc-status"));
    }

    @Test
    void aBodyThatCannotBeReadIsCancelled() throws Exception {
        Fixture fixture = new Fixture(name -> "Hello " + name);
        FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));
        exchange.bodyFails = true;

        fixture.adapter().dispatch(exchange);

        assertEquals("1", exchange.trailers.get("grpc-status"));
    }

    @Test
    void answers405WithTheMethodItAccepts() throws Exception {
        Fixture fixture = new Fixture(name -> "Hello " + name);
        FakeExchange exchange = fixture.exchange(fixture.frame("Ada"));
        exchange.method = "GET";

        fixture.adapter().dispatch(exchange);

        assertEquals(405, exchange.status);
        assertEquals("POST", exchange.headers.get("Allow"));
    }

    @Test
    void percentEncodesAStatusMessageAsTheSpecificationRequires() {
        // Only bytes outside 0x20-0x7E, and the percent sign itself, are
        // encoded: a space is a legal header character and stays one.
        assertEquals("caff%C3%A8 100%25", ForyGrpcServerAdapter.percentEncode("caffè 100%"));
    }

    /** A class the generated runtime has not registered. */
    public static final class NotRegistered {
    }

    /** A registry with one method, its generated models, and the adapter under test. */
    private static final class Fixture {
        private final ForyRuntimeModelGenerator.GeneratedModels models;
        private final ForyGeneratedRuntime runtime;
        private final ForyGrpcServerAdapter adapter;
        private final Object bean;
        private Exception acquireFailure;
        private boolean closeFails;
        private boolean adaptWithAnotherResponseModel;
        private boolean respondWithAClassTheRuntimeDoesNotKnow;

        Fixture(Greeter bean) {
            this(bean, 1 << 20);
        }

        Fixture(Greeter bean, int maxMessageBytes) {
            this(bean, maxMessageBytes, true, new ArrayList<>());
        }

        Fixture(Greeter bean, List<String> events) {
            this(bean, 1 << 20, true, events);
        }

        Fixture(Greeter bean, int maxMessageBytes, boolean registeredUpFront) {
            this(bean, maxMessageBytes, registeredUpFront, new ArrayList<>());
        }

        /**
         * @param registeredUpFront false to leave the registry empty until it
         *        is asked for the path, as it is for a bean deployed after the
         *        endpoint started
         * @param events records what the adapter did, in order
         */
        Fixture(Greeter bean, int maxMessageBytes, boolean registeredUpFront, List<String> events) {
            this.bean = bean;
            models = ForyRuntimeModelGenerator.unary("generated.adapter", "greet", String.class, String.class);
            runtime = new ForyGeneratedRuntime(models, 2000, 2001);
            ForyGeneratedServiceRegistry registry = new ForyGeneratedServiceRegistry();
            ForyGeneratedSchemaAdapter real = ForyGeneratedSchemaAdapter.unary(models.request(), models.response());
            // The generated adapter, unless a test says the response cannot be made.
            ForyGrpcSchemaAdapter underTest = new ForyGrpcSchemaAdapter() {
                @Override
                public Object request(String methodPath, Object generatedRequest, Class<?> javaType) {
                    return real.request(methodPath, generatedRequest, javaType);
                }

                @Override
                public Object response(String methodPath, Object javaValue, Class<?> javaType) {
                    if (adaptWithAnotherResponseModel) {
                        throw new IllegalArgumentException("cannot construct generated.other.Response in loader@42");
                    }
                    if (respondWithAClassTheRuntimeDoesNotKnow) {
                        return new NotRegistered();
                    }
                    return real.response(methodPath, javaValue, javaType);
                }
            };
            Runnable register = () -> registry.register(PATH, PATH,
                    ForyGrpcSkeleton.of("demo.Greeter", Greeter.class), models, runtime, underTest);
            if (registeredUpFront) {
                register.run();
            } else {
                registry.onMiss(() -> {
                    registry.clear();
                    register.run();
                });
            }
            // A bridge that only says what was asked of it: the adapter is
            // expected to clear the thread, not to import anything.
            TransactionBridge transactions = (TransactionBridge) Proxy.newProxyInstance(
                    getClass().getClassLoader(), new Class<?>[] {TransactionBridge.class},
                    (proxy, method, args) -> {
                        events.add(method.getName());
                        return null;
                    });
            adapter = new ForyGrpcServerAdapter(registry, (path, exchange) -> {
                events.add("acquire");
                if (acquireFailure != null) {
                    throw acquireFailure;
                }
                return new ForyGrpcServerAdapter.Target() {
                    @Override
                    public Object value() {
                        return Fixture.this.bean;
                    }

                    @Override
                    public void close() {
                        if (closeFails) {
                            throw new IllegalStateException("cannot release: secret");
                        }
                    }
                };
            }, maxMessageBytes, transactions);
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
        private boolean bodyFails;
        private String contentType = "application/grpc+fory";

        FakeExchange(String path, byte[] requestBody) {
            this.path = path;
            this.requestBody = requestBody;
        }

        byte[] body() {
            return body;
        }

        private String method = "POST";

        @Override
        public String method() {
            return method;
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
            return "Content-Type".equalsIgnoreCase(name) ? contentType : null;
        }

        @Override
        public String queryParameter(String name) {
            return null;
        }

        @Override
        public InputStream requestBody() throws IOException {
            if (bodyFails) {
                throw new IOException("connection reset");
            }
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
