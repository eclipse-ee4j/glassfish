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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.glassfish.orb.http.protocol.EjbKey;
import org.glassfish.orb.http.protocol.EjbRoutes;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Protocol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Open, remove and cancel run as the caller, the way invoke does, and open
 * tells a bean that will not open a session apart from one that does not exist.
 */
class EjbSessionSecurityTest {

    private final List<String> trace = new ArrayList<>();
    private final FakeContainer container = new FakeContainer();
    private final InvocationRegistry registry = new InvocationRegistry();

    private EjbDispatcher dispatcher(SecurityBridge security) {
        container.trace = trace;
        return new EjbDispatcher(container, security, new JavaSerializationMarshaller(), registry);
    }

    private static Loopback.InMemoryExchange exchange(String method, String path, String user) {
        Loopback.InMemoryExchange exchange = new Loopback.InMemoryExchange(method,
                path.getBytes(StandardCharsets.UTF_8), null, Map.of(), new byte[0]);
        exchange.setAuthenticatedUser(user);
        return exchange;
    }

    private static String open(String bean) {
        return EjbRoutes.openPath(Protocol.CONTEXT_PATH, "myapp", "mymodule", null, bean);
    }

    private static String remove(String bean, byte[] sessionId) {
        return EjbRoutes.removePath(Protocol.CONTEXT_PATH, "myapp", "mymodule", null, bean,
                EjbRoutes.encodeSessionId(sessionId));
    }

    private static String cancel(String invocationId) {
        return EjbRoutes.cancelPath(Protocol.CONTEXT_PATH, "myapp", "mymodule", null, "CounterBean",
                invocationId, false);
    }

    private static void assertFixedReason(Loopback.InMemoryExchange exchange) {
        assertEquals(List.of("not authorized"), exchange.toResponse().headers().get("X-GF-Reason"));
    }

    // ---- open --------------------------------------------------------------

    @Test
    @DisplayName("open creates the session as the caller and clears the thread afterwards")
    void openRunsAsTheCaller() throws Exception {
        container.registerStateful("CounterBean", TestBeans.CounterBean::new);
        EjbDispatcher dispatcher = dispatcher(new RecordingSecurity(trace));
        Loopback.InMemoryExchange exchange = exchange("POST", open("CounterBean"), "alice");

        dispatcher.dispatch(exchange);

        assertEquals(Protocol.SC_NO_CONTENT, exchange.toResponse().status());
        assertNotNull(exchange.toResponse().firstHeader(Protocol.H_SESSION_ID));
        assertEquals(List.of("establish(alice)", "createSession", "clear(token-alice)"), trace);
    }

    @Test
    @DisplayName("open with a credential the realm refuses is a 403 and never reaches the container")
    void openWithARefusedCredentialIsForbidden() {
        container.registerStateful("CounterBean", TestBeans.CounterBean::new);
        EjbDispatcher dispatcher = dispatcher(new RecordingSecurity(trace));
        Loopback.InMemoryExchange exchange = exchange("POST", open("CounterBean"),
                Loopback.InMemoryExchange.REJECT);

        assertDoesNotThrow(() -> dispatcher.dispatch(exchange));

        assertEquals(Protocol.SC_FORBIDDEN, exchange.toResponse().status());
        assertFixedReason(exchange);
        assertTrue(trace.isEmpty(), "the container was reached: " + trace);
        assertNull(exchange.toResponse().firstHeader(Protocol.H_SESSION_ID));
    }

    @Test
    @DisplayName("open when establish itself refuses is a 403, and nothing is cleared that was never set")
    void openWhenEstablishRefuses() {
        container.registerStateful("CounterBean", TestBeans.CounterBean::new);
        EjbDispatcher dispatcher = dispatcher(new RefusingSecurity(trace));
        Loopback.InMemoryExchange exchange = exchange("POST", open("CounterBean"), "mallory");

        assertDoesNotThrow(() -> dispatcher.dispatch(exchange));

        assertEquals(Protocol.SC_FORBIDDEN, exchange.toResponse().status());
        assertFixedReason(exchange);
        assertEquals(List.of("establish(mallory)"), trace);
    }

    @Test
    @DisplayName("open clears the thread when the container fails")
    void openClearsOnAnyFailure() {
        container.registerStateful("CounterBean", TestBeans.CounterBean::new);
        container.failSessions = new IllegalStateException("container down");
        EjbDispatcher dispatcher = dispatcher(new RecordingSecurity(trace));
        Loopback.InMemoryExchange exchange = exchange("POST", open("CounterBean"), "alice");

        assertThrows(IllegalStateException.class, () -> dispatcher.dispatch(exchange));

        assertEquals(List.of("establish(alice)", "createSession", "clear(token-alice)"), trace);
    }

    @Test
    @DisplayName("open on a bean that is not stateful is a 500, not the 404 that means no such EJB")
    void openOnAStatelessBeanIsNotA404() {
        container.registerStateless("Odd\r\nBean", new TestBeans.GreeterBean());
        EjbDispatcher dispatcher = dispatcher(new RecordingSecurity(trace));
        Loopback.InMemoryExchange exchange = exchange("POST", open("Odd\r\nBean"), "alice");

        assertDoesNotThrow(() -> dispatcher.dispatch(exchange));

        assertEquals(Protocol.SC_EXCEPTION, exchange.toResponse().status());
        List<String> reason = exchange.toResponse().headers().get("X-GF-Reason");
        assertEquals(1, reason.size());
        assertTrue(reason.get(0).contains("not a stateful"), reason.get(0));
        assertFalse(reason.get(0).contains("\r") || reason.get(0).contains("\n"), reason.get(0));
        assertEquals(List.of("establish(alice)", "createSession", "clear(token-alice)"), trace);
    }

    @Test
    @DisplayName("open on a bean that does not exist is a 404")
    void openOnAnUnknownBeanIsNotFound() {
        EjbDispatcher dispatcher = dispatcher(new RecordingSecurity(trace));
        Loopback.InMemoryExchange exchange = exchange("POST", open("NoSuchBean"), "alice");

        assertDoesNotThrow(() -> dispatcher.dispatch(exchange));

        assertEquals(Protocol.SC_NOT_FOUND, exchange.toResponse().status());
        assertEquals(List.of("establish(alice)", "createSession", "clear(token-alice)"), trace);
    }

    // ---- remove ------------------------------------------------------------

    private byte[] existingSession() throws Exception {
        container.registerStateful("CounterBean", TestBeans.CounterBean::new);
        return container.createSession("myapp", "mymodule", null, "CounterBean");
    }

    @Test
    @DisplayName("remove ends the session as the caller and clears the thread afterwards")
    void removeRunsAsTheCaller() throws Exception {
        byte[] session = existingSession();
        EjbDispatcher dispatcher = dispatcher(new RecordingSecurity(trace));
        Loopback.InMemoryExchange exchange = exchange("DELETE", remove("CounterBean", session), "alice");

        dispatcher.dispatch(exchange);

        assertEquals(Protocol.SC_NO_CONTENT, exchange.toResponse().status());
        assertEquals(List.of("establish(alice)", "removeSession", "clear(token-alice)"), trace);
    }

    @Test
    @DisplayName("remove with a credential the realm refuses is a 403 and the session survives")
    void removeWithARefusedCredentialIsForbidden() throws Exception {
        byte[] session = existingSession();
        EjbDispatcher dispatcher = dispatcher(new RecordingSecurity(trace));
        trace.clear();
        Loopback.InMemoryExchange exchange = exchange("DELETE", remove("CounterBean", session),
                Loopback.InMemoryExchange.REJECT);

        assertDoesNotThrow(() -> dispatcher.dispatch(exchange));

        assertEquals(Protocol.SC_FORBIDDEN, exchange.toResponse().status());
        assertFixedReason(exchange);
        assertTrue(trace.isEmpty(), "the container was reached: " + trace);
        assertNotNull(container.getTargetObject(new EjbKey(1, session), null), "the session was removed");
    }

    @Test
    @DisplayName("remove clears the thread when the container fails")
    void removeClearsOnAnyFailure() throws Exception {
        byte[] session = existingSession();
        EjbDispatcher dispatcher = dispatcher(new RecordingSecurity(trace));
        trace.clear();
        container.failSessions = new IllegalStateException("container down");
        Loopback.InMemoryExchange exchange = exchange("DELETE", remove("CounterBean", session), "alice");

        assertThrows(IllegalStateException.class, () -> dispatcher.dispatch(exchange));

        assertEquals(List.of("establish(alice)", "removeSession", "clear(token-alice)"), trace);
    }

    @Test
    @DisplayName("removing a session that is already gone is still a 204, and the thread is cleared")
    void removeOfAGoneSessionClears() throws Exception {
        byte[] session = existingSession();
        EjbDispatcher dispatcher = dispatcher(new RecordingSecurity(trace));
        dispatcher.dispatch(exchange("DELETE", remove("CounterBean", session), "alice"));
        trace.clear();
        Loopback.InMemoryExchange again = exchange("DELETE", remove("CounterBean", session), "alice");

        dispatcher.dispatch(again);

        assertEquals(Protocol.SC_NO_CONTENT, again.toResponse().status());
        assertEquals("establish(alice)", trace.get(0));
        assertEquals("clear(token-alice)", trace.get(trace.size() - 1));
    }

    // ---- cancel ------------------------------------------------------------

    @Test
    @DisplayName("cancel runs as the caller and clears the thread afterwards")
    void cancelRunsAsTheCaller() throws Exception {
        EjbDispatcher dispatcher = dispatcher(new RecordingSecurity(trace));
        try (InvocationRegistry.Registration running = registry.register("inv-1")) {
            Loopback.InMemoryExchange exchange = exchange("DELETE", cancel("inv-1"), "alice");

            dispatcher.dispatch(exchange);

            assertEquals(Protocol.SC_NO_CONTENT, exchange.toResponse().status());
            assertTrue(running.isCancelled());
        }
        assertEquals(List.of("establish(alice)", "clear(token-alice)"), trace);
    }

    @Test
    @DisplayName("cancel with a credential the realm refuses is a 403 and the invocation keeps running")
    void cancelWithARefusedCredentialIsForbidden() throws Exception {
        EjbDispatcher dispatcher = dispatcher(new RecordingSecurity(trace));
        try (InvocationRegistry.Registration running = registry.register("inv-1")) {
            Loopback.InMemoryExchange exchange = exchange("DELETE", cancel("inv-1"),
                    Loopback.InMemoryExchange.REJECT);

            assertDoesNotThrow(() -> dispatcher.dispatch(exchange));

            assertEquals(Protocol.SC_FORBIDDEN, exchange.toResponse().status());
            assertFixedReason(exchange);
            assertFalse(running.isCancelled());
        }
        assertTrue(trace.isEmpty(), trace.toString());
    }

    // ---- fakes -------------------------------------------------------------

    /** Records what the dispatcher asks of the security bridge, in the order the container sees it. */
    private static final class RecordingSecurity implements SecurityBridge {
        private final List<String> events;

        RecordingSecurity(List<String> events) {
            this.events = events;
        }

        @Override
        public Object establish(String userName) {
            events.add("establish(" + userName + ")");
            return "token-" + userName;
        }

        @Override
        public void clear(Object token) {
            events.add("clear(" + token + ")");
        }
    }

    /** A realm that refuses whoever it is asked to establish; clearing is then a bug. */
    private static final class RefusingSecurity implements SecurityBridge {
        private final List<String> events;

        RefusingSecurity(List<String> events) {
            this.events = events;
        }

        @Override
        public Object establish(String userName) {
            events.add("establish(" + userName + ")");
            throw new SecurityException("realm refused " + userName);
        }

        @Override
        public void clear(Object token) {
            throw new AssertionError("nothing was established, so nothing may be cleared");
        }
    }
}
