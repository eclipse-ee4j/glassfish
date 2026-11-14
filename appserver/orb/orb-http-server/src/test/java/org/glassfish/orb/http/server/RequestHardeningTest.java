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
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.naming.NamingException;

import org.glassfish.orb.http.protocol.ChunkedOutput;
import org.glassfish.orb.http.protocol.CommonRoutes;
import org.glassfish.orb.http.protocol.ContentType;
import org.glassfish.orb.http.protocol.EjbRoutes;
import org.glassfish.orb.http.protocol.InvocationEnvelope;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Marshaller;
import org.glassfish.orb.http.protocol.NamingRoutes;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.TxContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Requests that are wrong in one way or another are answered, not thrown.
 */
class RequestHardeningTest {

    private static final String INVOKE_TYPE = ContentType.of(JavaSerializationMarshaller.CODEC,
            ContentType.KIND_INVOCATION).toHeaderValue();

    private static final String[] GREET_TYPES = { String.class.getName(), int.class.getName() };

    /** A class the "application" is about to lose: see {@link #anArgumentOfAnUnknownClassIsABadRequest}. */
    static final class Marker implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    /** An asynchronous bean whose result never arrives unless something interrupts the wait. */
    interface Slow {
        Future<String> slow();
    }

    private static final class SlowBean implements Slow {

        final CountDownLatch started = new CountDownLatch(1);

        @Override
        public Future<String> slow() {
            started.countDown();
            return new CompletableFuture<>();
        }
    }

    private static byte[] body(Object... args) throws IOException {
        ChunkedOutput out = new ChunkedOutput();
        InvocationEnvelope.writeTxContext(out, TxContext.NONE);
        try (Marshaller.ObjectWriter writer = new JavaSerializationMarshaller().newWriter(out)) {
            for (Object arg : args) {
                writer.writeObject(arg);
            }
            writer.writeObject(new HashMap<String, Object>());
            writer.flush();
        }
        return out.toByteArray();
    }

    private static byte[] garbage() throws IOException {
        ChunkedOutput out = new ChunkedOutput();
        InvocationEnvelope.writeTxContext(out, TxContext.NONE);
        out.write(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 });
        return out.toByteArray();
    }

    private static Loopback.InMemoryExchange invocation(Class<?> view, String bean, String method,
                                                        String[] types, byte[] body, Map<String, String> extra) {
        String path = EjbRoutes.invokePath(Protocol.CONTEXT_PATH, "myapp", "mymodule", null,
                bean, null, view.getName(), method, types);
        Map<String, String> headers = new HashMap<>(extra);
        headers.put("Content-Type", INVOKE_TYPE);
        return new Loopback.InMemoryExchange("POST", path.getBytes(StandardCharsets.UTF_8), null, headers, body);
    }

    private static String reason(Loopback.InMemoryExchange exchange) {
        List<String> values = exchange.toResponse().headers().get("X-GF-Reason");
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private static EjbDispatcher dispatcherWithGreeter() {
        FakeContainer container = new FakeContainer();
        container.registerStateless("GreeterBean", new TestBeans.GreeterBean());
        return new EjbDispatcher(container);
    }

    // ---- EJB invocation ---------------------------------------------------

    @Test
    @DisplayName("a body that does not decode is a bad request, not an exception out of the dispatcher")
    void aCorruptBodyIsABadRequest() throws Exception {
        Loopback.InMemoryExchange exchange = invocation(TestBeans.Greeter.class, "GreeterBean", "greet",
                GREET_TYPES, garbage(), Map.of());

        dispatcherWithGreeter().dispatch(exchange);

        assertEquals(Protocol.SC_BAD_REQUEST, exchange.toResponse().status());
        assertNotNull(reason(exchange));
    }

    @Test
    @DisplayName("arguments of the wrong type are a bad request")
    void argumentsOfTheWrongTypeAreABadRequest() throws Exception {
        // greet(String, int) is sent two strings.
        Loopback.InMemoryExchange exchange = invocation(TestBeans.Greeter.class, "GreeterBean", "greet",
                GREET_TYPES, body("a", "b"), Map.of());

        dispatcherWithGreeter().dispatch(exchange);

        assertEquals(Protocol.SC_BAD_REQUEST, exchange.toResponse().status());
    }

    @Test
    @DisplayName("an argument of a class the application does not have is a bad request, not 'no such bean'")
    void anArgumentOfAnUnknownClassIsABadRequest() throws Exception {
        byte[] bytes = body(new Marker(), 1);
        // Rename the class in the stream, keeping its length: it now names a
        // class that exists nowhere.
        byte[] name = Marker.class.getName().getBytes(StandardCharsets.US_ASCII);
        int at = indexOf(bytes, name);
        assertTrue(at >= 0, "the class name should be in the stream");
        bytes[at + name.length - 1] = 'x';

        Loopback.InMemoryExchange exchange = invocation(TestBeans.Greeter.class, "GreeterBean", "greet",
                GREET_TYPES, bytes, Map.of());

        dispatcherWithGreeter().dispatch(exchange);

        assertEquals(Protocol.SC_BAD_REQUEST, exchange.toResponse().status());
        assertTrue(reason(exchange).contains("RequestHardeningTest$Marke"), reason(exchange));
    }

    private static int indexOf(byte[] haystack, byte[] needle) {
        outer:
        for (int i = 0; i + needle.length <= haystack.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    @Test
    @DisplayName("cancelling an asynchronous call answers 408 and leaves the thread uninterrupted")
    void aCancelledAsynchronousCallIsAnsweredAsCancelled() throws Exception {
        FakeContainer container = new FakeContainer();
        SlowBean bean = new SlowBean();
        container.registerStateless("SlowBean", bean);
        EjbDispatcher dispatcher = new EjbDispatcher(container);
        Loopback.InMemoryExchange exchange = invocation(Slow.class, "SlowBean", "slow", new String[0],
                body(), Map.of(Protocol.H_INVOCATION_ID, "slow-1"));

        ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "dispatch");
            t.setDaemon(true);
            return t;
        });
        try {
            // Reports whether the pooled thread is handed back interrupted.
            Callable<Boolean> dispatch = () -> {
                dispatcher.dispatch(exchange);
                return Thread.currentThread().isInterrupted();
            };
            Future<Boolean> result = pool.submit(dispatch);
            assertTrue(bean.started.await(30, TimeUnit.SECONDS), "the call should have started");

            assertTrue(dispatcher.registry().cancel("slow-1", true));

            boolean interruptedAfterwards = result.get(30, TimeUnit.SECONDS);
            assertEquals(Protocol.SC_CANCELLED, exchange.toResponse().status());
            assertFalse(interruptedAfterwards, "an interrupt meant for one request must not reach the next");
            assertEquals(0, dispatcher.registry().inFlightCount());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    @DisplayName("a second invocation with an id already in flight is refused, not allowed to take it over")
    void aDuplicateInvocationIdIsRefused() throws Exception {
        FakeContainer container = new FakeContainer();
        SlowBean bean = new SlowBean();
        container.registerStateless("SlowBean", bean);
        EjbDispatcher dispatcher = new EjbDispatcher(container);
        Map<String, String> id = Map.of(Protocol.H_INVOCATION_ID, "dup-1");
        Loopback.InMemoryExchange first = invocation(Slow.class, "SlowBean", "slow", new String[0], body(), id);
        Loopback.InMemoryExchange second = invocation(Slow.class, "SlowBean", "slow", new String[0], body(), id);

        ExecutorService pool = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "dispatch");
            t.setDaemon(true);
            return t;
        });
        try {
            Future<?> running = pool.submit(() -> {
                dispatcher.dispatch(first);
                return null;
            });
            assertTrue(bean.started.await(30, TimeUnit.SECONDS));

            // On a thread of its own, so that a dispatcher that wrongly accepts
            // it (and blocks on the slow bean) fails the test instead of hanging it.
            pool.submit(() -> {
                dispatcher.dispatch(second);
                return null;
            }).get(30, TimeUnit.SECONDS);
            assertEquals(Protocol.SC_BAD_REQUEST, second.toResponse().status());

            dispatcher.registry().cancel("dup-1", true);
            running.get(30, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
    }

    /** An exchange whose request body blocks until the reading thread is interrupted. */
    private static final class BlockingBodyExchange implements ServerExchange {

        final Loopback.InMemoryExchange delegate;
        final CountDownLatch reading = new CountDownLatch(1);

        BlockingBodyExchange(Loopback.InMemoryExchange delegate) {
            this.delegate = delegate;
        }

        @Override
        public String method() {
            return delegate.method();
        }

        @Override
        public byte[] pathBytes() {
            return delegate.pathBytes();
        }

        @Override
        public int pathOffset() {
            return delegate.pathOffset();
        }

        @Override
        public int pathLength() {
            return delegate.pathLength();
        }

        @Override
        public String requestHeader(String name) {
            return delegate.requestHeader(name);
        }

        @Override
        public String queryParameter(String name) {
            return delegate.queryParameter(name);
        }

        @Override
        public java.io.InputStream requestBody() {
            return new java.io.InputStream() {
                @Override
                public int read() throws IOException {
                    reading.countDown();
                    try {
                        Thread.sleep(60_000);
                    } catch (InterruptedException e) {
                        throw new java.io.InterruptedIOException("interrupted");
                    }
                    return -1;
                }
            };
        }

        @Override
        public void setStatus(int status) {
            delegate.setStatus(status);
        }

        @Override
        public void setResponseHeader(String name, String value) {
            delegate.setResponseHeader(name, value);
        }

        @Override
        public void writeBody(java.nio.ByteBuffer[] body) {
            delegate.writeBody(body);
        }

        @Override
        public String authenticatedUser() {
            return delegate.authenticatedUser();
        }
    }

    @Test
    @DisplayName("a cancel that interrupts the reading of the body is answered 408, not 400")
    void aCancelDuringReadingIsACancellation() throws Exception {
        EjbDispatcher dispatcher = dispatcherWithGreeter();
        BlockingBodyExchange exchange = new BlockingBodyExchange(invocation(TestBeans.Greeter.class,
                "GreeterBean", "greet", GREET_TYPES, new byte[0], Map.of(Protocol.H_INVOCATION_ID, "read-1")));

        ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "dispatch");
            t.setDaemon(true);
            return t;
        });
        try {
            Future<Boolean> result = pool.submit(() -> {
                dispatcher.dispatch(exchange);
                return Thread.currentThread().isInterrupted();
            });
            assertTrue(exchange.reading.await(30, TimeUnit.SECONDS));

            dispatcher.registry().cancel("read-1", true);

            boolean interrupted = result.get(30, TimeUnit.SECONDS);
            assertEquals(Protocol.SC_CANCELLED, exchange.delegate.toResponse().status());
            assertFalse(interrupted);
        } finally {
            pool.shutdownNow();
        }
    }

    /** A container that fails the way a bug in it would, with an IllegalArgumentException. */
    private static final class BrokenContainer implements ContainerBridge {

        private final FakeContainer delegate = new FakeContainer();

        BrokenContainer() {
            delegate.registerStateless("GreeterBean", new TestBeans.GreeterBean());
        }

        @Override
        public org.glassfish.orb.http.protocol.EjbKey resolve(String appName, String moduleName,
                String distinctName, String beanName, byte[] sessionId) throws NoSuchTargetException {
            return delegate.resolve(appName, moduleName, distinctName, beanName, sessionId);
        }

        @Override
        public Object getTargetObject(org.glassfish.orb.http.protocol.EjbKey key, String viewClassName) {
            throw new IllegalArgumentException("a bug in the container");
        }

        @Override
        public void releaseTargetObject(Object target) {
        }

        @Override
        public ClassLoader classLoader(org.glassfish.orb.http.protocol.EjbKey key) {
            return delegate.classLoader(key);
        }

        @Override
        public byte[] createSession(String appName, String moduleName, String distinctName, String beanName)
                throws NoSuchTargetException {
            return delegate.createSession(appName, moduleName, distinctName, beanName);
        }

        @Override
        public void removeSession(org.glassfish.orb.http.protocol.EjbKey key) throws NoSuchTargetException {
            delegate.removeSession(key);
        }
    }

    @Test
    @DisplayName("an IllegalArgumentException from the container is not blamed on the caller's arguments")
    void aContainerFailureIsNotReportedAsABadRequest() throws Exception {
        EjbDispatcher dispatcher = new EjbDispatcher(new BrokenContainer());
        Loopback.InMemoryExchange exchange = invocation(TestBeans.Greeter.class, "GreeterBean", "greet",
                GREET_TYPES, body("a", 1), Map.of());

        assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(exchange));
    }

    // ---- registry ---------------------------------------------------------

    @Test
    @DisplayName("an interrupt sent to a finished invocation does not stay on its thread")
    void cancelLeavesNoInterruptBehind() throws Exception {
        InvocationRegistry registry = new InvocationRegistry();
        InvocationRegistry.Registration registration = registry.register("x");

        registry.cancel("x", true);
        registration.close();

        boolean leaked = Thread.interrupted();
        assertFalse(leaked, "the pooled thread would fail the next request it runs");
    }

    // ---- naming -----------------------------------------------------------

    private static Loopback.InMemoryExchange naming(String operation, String name,
                                                    String contentType, byte[] body) {
        String path = NamingRoutes.path(Protocol.CONTEXT_PATH, operation, name);
        Map<String, String> headers = new HashMap<>();
        if (contentType != null) {
            headers.put("Content-Type", contentType);
        }
        return new Loopback.InMemoryExchange(NamingRoutes.methodFor(operation),
                path.getBytes(StandardCharsets.UTF_8), null, headers, body);
    }

    private static final String VALUE_TYPE = ContentType.of(JavaSerializationMarshaller.CODEC,
            ContentType.KIND_VALUE).toHeaderValue();

    @Test
    @DisplayName("a bind whose body does not decode is a bad request")
    void aCorruptBindBodyIsABadRequest() throws Exception {
        NamingDispatcher dispatcher = new NamingDispatcher(new HttpRoundTripTest.FakeNaming());
        Loopback.InMemoryExchange exchange = naming(Protocol.OP_BIND, "java:global/x", VALUE_TYPE,
                new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 });

        dispatcher.dispatch(exchange);

        assertEquals(Protocol.SC_BAD_REQUEST, exchange.toResponse().status());
        assertTrue(reason(exchange).contains("malformed request body"), reason(exchange));
    }

    @Test
    @DisplayName("a bind in a codec this server lacks is refused with 406, so the client can fall back")
    void aBindInAnUnknownCodecIsRefused() throws Exception {
        HttpRoundTripTest.FakeNaming naming = new HttpRoundTripTest.FakeNaming();
        NamingDispatcher dispatcher = new NamingDispatcher(naming);
        Loopback.InMemoryExchange exchange = naming(Protocol.OP_BIND, "java:global/x",
                ContentType.of("nosuch", ContentType.KIND_VALUE).toHeaderValue(), new byte[0]);

        dispatcher.dispatch(exchange);

        assertEquals(Protocol.SC_NOT_ACCEPTABLE, exchange.toResponse().status());
        assertTrue(reason(exchange).contains("unsupported codec"), reason(exchange));
        assertTrue(naming.bindings.isEmpty(), "nothing may be bound from a body that was not read");
    }

    /** A naming service that fails with a message the caller shaped. */
    private static final class FailingNaming implements NamingBridge {

        @Override
        public Object lookup(String name) {
            throw new IllegalStateException("bad\r\nX-Injected: 1");
        }

        @Override
        public Object lookupLink(String name) throws NamingException {
            return lookup(name);
        }

        @Override
        public void bind(String name, Object value) {
        }

        @Override
        public void rebind(String name, Object value) {
        }

        @Override
        public void unbind(String name) {
        }

        @Override
        public void rename(String oldName, String newName) {
        }

        @Override
        public Map<String, Object> list(String name) {
            return Map.of();
        }

        @Override
        public void createSubcontext(String name) {
        }

        @Override
        public void destroySubcontext(String name) {
        }
    }

    @Test
    @DisplayName("a failure message with a carriage return never reaches a header as one")
    void aNamingReasonCarriesNoCarriageReturn() throws Exception {
        NamingDispatcher dispatcher = new NamingDispatcher(new FailingNaming());
        Loopback.InMemoryExchange exchange = naming(Protocol.OP_LOOKUP, "java:global/x", null, new byte[0]);

        dispatcher.dispatch(exchange);

        assertEquals(Protocol.SC_EXCEPTION, exchange.toResponse().status());
        assertFalse(reason(exchange).contains("\r"), reason(exchange));
        assertFalse(reason(exchange).contains("\n"), reason(exchange));
        assertTrue(reason(exchange).contains("X-Injected"), reason(exchange));
    }

    // ---- affinity ---------------------------------------------------------

    @Test
    @DisplayName("a path segment cannot add attributes to the affinity cookie")
    void theCookiePathIsNotAttackerControlled() throws Exception {
        AffinityDispatcher dispatcher = new AffinityDispatcher(new SessionAffinity("instance-1"));
        String path = CommonRoutes.affinityPath("/a%3BDomain=evil.example");
        Loopback.InMemoryExchange exchange = new Loopback.InMemoryExchange("GET",
                path.getBytes(StandardCharsets.UTF_8), null, Map.of(), new byte[0]);

        dispatcher.dispatch(exchange);

        assertEquals(Protocol.SC_NO_CONTENT, exchange.toResponse().status());
        String cookie = exchange.toResponse().headers().get("Set-Cookie").get(0);
        assertFalse(cookie.contains("Domain"), cookie);
        assertTrue(cookie.contains("Path=" + Protocol.CONTEXT_PATH), cookie);
    }
}
