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





import jakarta.ejb.NoSuchEJBException;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.glassfish.orb.http.client.ClientConfiguration;
import org.glassfish.orb.http.client.EjbLocator;
import org.glassfish.orb.http.client.HttpEjbClient;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.RemoteEjbReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives a real client proxy through the real wire format into the real server
 * dispatch, with only the socket replaced.
 */
class HttpRoundTripTest {

    private FakeContainer container;
    private FakeNaming naming;
    private EjbDispatcher ejbDispatcher;
    private HttpEjbClient client;
    private Loopback loopback;

    @BeforeEach
    void setUp() {
        container = new FakeContainer();
        container.registerStateless("GreeterBean", new TestBeans.GreeterBean());
        container.registerStateful("CounterBean", TestBeans.CounterBean::new);

        naming = new FakeNaming();
        ejbDispatcher = new EjbDispatcher(container);
        loopback = new Loopback(ejbDispatcher, new NamingDispatcher(naming));

        ClientConfiguration config = ClientConfiguration
                .builder(URI.create("http://localhost:8080" + Protocol.CONTEXT_PATH))
                .build();
        client = new HttpEjbClient(config, loopback, new JavaSerializationMarshaller(),
                JavaSerializationMarshaller.defaultFilter());
    }

    private TestBeans.Greeter greeter() {
        return client.createProxy(TestBeans.Greeter.class,
                new EjbLocator("myapp", "mymodule", null, "GreeterBean"));
    }

    @Test
    @DisplayName("a stateless invocation carries arguments there and a result back")
    void statelessInvocation() {
        assertEquals("Ada Ada Ada", greeter().greet("Ada", 3));
    }

    @Test
    void voidMethodReturnsNothingAndDoesNotFail() {
        greeter().notifyArrival("Grace");
    }

    @Test
    @DisplayName("a structured result survives the round trip")
    void structuredResult() {
        Map<String, List<Integer>> result = greeter().cyclicFriendlyPayload(5);
        assertEquals(List.of(0, 1, 2, 3, 4), result.get("numbers"));
    }

    @Test
    @DisplayName("the application exception arrives as itself, with its cause and its state")
    void exceptionFidelity() {
        TestBeans.GreetingRefused thrown =
                assertThrows(TestBeans.GreetingRefused.class, () -> greeter().refuse());

        assertEquals("not today", thrown.getMessage());
        assertEquals("E_CLOSED", thrown.reasonCode());
        assertNotNull(thrown.getCause());
        assertInstanceOf(IllegalStateException.class, thrown.getCause());
        assertEquals("underlying cause", thrown.getCause().getMessage());
        assertTrue(thrown.getStackTrace().length > 0, "the server stack trace should survive");
    }

    @Test
    @DisplayName("a Future-returning method is dispatched without blocking the caller")
    void asynchronousMethodsReturnAFuture() throws Exception {
        // HttpEjbInvocationHandler routes these through invokeAsync rather than
        // waiting, which is the whole point of declaring them Future<V>. The
        // server unwraps the bean's Future, because a Future is not a value and
        // is not serializable.
        java.util.concurrent.Future<String> pending = greeter().greetLater("Ada");

        assertNotNull(pending);
        assertEquals("later Ada", pending.get(10, java.util.concurrent.TimeUnit.SECONDS));
        assertTrue(pending.isDone());
    }

    @Test
    @DisplayName("an unannotated unchecked exception becomes EJBException, as the container would make it")
    void systemExceptionsAreWrapped() {
        // Not cosmetic. Over IIOP the container wraps this and discards the
        // instance, so a caller's catch blocks have never been able to see the
        // original type. Letting it through would change the exception
        // contract of an application that only changed transport.
        jakarta.ejb.EJBException wrapped =
                assertThrows(jakarta.ejb.EJBException.class, () -> greeter().explode());

        assertTrue(wrapped.getMessage().contains("IllegalStateException"));
        assertNotNull(wrapped.getCausedByException(), "the original must still be reachable");
    }

    @Test
    void unknownBeanIsNotFound() {
        TestBeans.Greeter missing = client.createProxy(TestBeans.Greeter.class,
                new EjbLocator("myapp", "mymodule", null, "NoSuchBean"));
        assertThrows(NoSuchEJBException.class, () -> missing.greet("x", 1));
    }

    @Test
    @DisplayName("a stateful session is opened and then addressed by its id")
    void statefulSession() throws Exception {
        EjbLocator home = new EjbLocator("myapp", "mymodule", null, "CounterBean");
        byte[] sessionId = client.openSession(home);
        assertNotNull(sessionId);

        TestBeans.Counter counter = client.createProxy(TestBeans.Counter.class, home.withSession(sessionId));
        assertEquals(1, counter.increment());
        assertEquals(2, counter.increment());
        assertEquals(2, counter.value());

        // A second session must be a distinct instance.
        byte[] other = client.openSession(home);
        TestBeans.Counter second = client.createProxy(TestBeans.Counter.class, home.withSession(other));
        assertEquals(1, second.increment());
        assertEquals(2, counter.value());
    }

    @Test
    void openingASessionOnAStatelessBeanIsNotFound() {
        assertThrows(java.io.IOException.class, () ->
                client.openSession(new EjbLocator("myapp", "mymodule", null, "GreeterBean")));
    }

    @Test
    @DisplayName("the target is always released, including after the bean throws")
    void targetIsAlwaysReleased() {
        int before = container.released;
        greeter().greet("a", 1);
        assertEquals(before + 1, container.released);

        assertThrows(TestBeans.GreetingRefused.class, () -> greeter().refuse());
        assertEquals(before + 2, container.released);
    }

    @Test
    @DisplayName("a multi-megabyte payload survives the chunked, copy-free path")
    void largePayload() {
        String payload = "x".repeat(2 * 1024 * 1024);
        String echoed = greeter().echoLarge(payload);
        assertEquals(payload.length(), echoed.length());
        assertEquals(payload, echoed);
    }

    @Test
    @DisplayName("proxy identity is decided locally, without a remote call")
    void proxyIdentity() {
        EjbLocator locator = new EjbLocator("myapp", "mymodule", null, "GreeterBean");
        TestBeans.Greeter a = client.createProxy(TestBeans.Greeter.class, locator);
        TestBeans.Greeter b = client.createProxy(TestBeans.Greeter.class, locator);

        assertNotSame(a, b);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.toString().contains("GreeterBean"));
    }

    @Test
    void cancelIsAcceptedEvenWhenNothingIsInFlight() throws Exception {
        client.cancel(new EjbLocator("myapp", "mymodule", null, "GreeterBean"),
                "00000000-0000-0000-0000-000000000000", true);
        assertEquals(0, ejbDispatcher.registry().inFlightCount());
    }

    @Test
    @DisplayName("an invocation registers for cancellation and deregisters afterwards")
    void registryIsBalanced() {
        greeter().greet("a", 1);
        assertEquals(0, ejbDispatcher.registry().inFlightCount());
    }

    // ---- naming ----------------------------------------------------------

    @Test
    @DisplayName("a lookup returning a bean reference yields a working proxy")
    void namingLookupYieldsAProxy() throws Exception {
        naming.bindings.put("java:global/myapp/GreeterBean",
                new RemoteEjbReference("myapp", "mymodule", null, "GreeterBean",
                        TestBeans.Greeter.class.getName(), null));

        // javax.naming.Context is not AutoCloseable, so close it by hand.
        org.glassfish.orb.http.client.HttpNamingContext context = namingContext();
        try {
            Object looked = context.lookup("java:global/myapp/GreeterBean");
            assertInstanceOf(TestBeans.Greeter.class, looked);
            assertEquals("Bob", ((TestBeans.Greeter) looked).greet("Bob", 1));
        } finally {
            context.close();
        }
    }

    @Test
    void namingRoundTripsPlainValues() throws Exception {
        org.glassfish.orb.http.client.HttpNamingContext context = namingContext();
        try {
            context.bind("java:global/config/limit", 42);
            assertEquals(42, context.lookup("java:global/config/limit"));

            context.rebind("java:global/config/limit", 43);
            assertEquals(43, context.lookup("java:global/config/limit"));

            context.rename("java:global/config/limit", "java:global/config/max");
            assertEquals(43, context.lookup("java:global/config/max"));

            context.unbind("java:global/config/max");
            assertThrows(NameNotFoundException.class, () -> context.lookup("java:global/config/max"));
        } finally {
            context.close();
        }
    }

    @Test
    void namingListsBindings() throws Exception {
        naming.bindings.put("java:global/a", "one");
        naming.bindings.put("java:global/b", "two");

        org.glassfish.orb.http.client.HttpNamingContext context = namingContext();
        try {
            var enumeration = context.list("java:global");
            int count = 0;
            while (enumeration.hasMore()) {
                assertNotNull(enumeration.next().getName());
                count++;
            }
            assertEquals(2, count);
        } finally {
            context.close();
        }
    }

    private org.glassfish.orb.http.client.HttpNamingContext namingContext() {
        return new org.glassfish.orb.http.client.HttpNamingContext(
                ClientConfiguration.builder(
                        URI.create("http://localhost:8080" + Protocol.CONTEXT_PATH)).build(),
                loopback, new java.util.Hashtable<>());
    }

    /** A {@link NamingBridge} over a map. */
    static final class FakeNaming implements NamingBridge {

        final Map<String, Object> bindings = new HashMap<>();

        @Override
        public Object lookup(String name) throws NamingException {
            Object value = bindings.get(name);
            if (value == null) {
                throw new NameNotFoundException(name);
            }
            return value;
        }

        @Override
        public Object lookupLink(String name) throws NamingException {
            return lookup(name);
        }

        @Override
        public void bind(String name, Object value) {
            bindings.put(name, value);
        }

        @Override
        public void rebind(String name, Object value) {
            bindings.put(name, value);
        }

        @Override
        public void unbind(String name) {
            bindings.remove(name);
        }

        @Override
        public void rename(String oldName, String newName) throws NamingException {
            bindings.put(newName, lookup(oldName));
            bindings.remove(oldName);
        }

        @Override
        public Map<String, Object> list(String name) {
            Map<String, Object> result = new HashMap<>();
            String prefix = name.isEmpty() ? "" : name + '/';
            for (Map.Entry<String, Object> e : bindings.entrySet()) {
                if (e.getKey().startsWith(prefix)) {
                    result.put(e.getKey().substring(prefix.length()), e.getValue());
                }
            }
            return result;
        }

        @Override
        public void createSubcontext(String name) {
        }

        @Override
        public void destroySubcontext(String name) {
            bindings.remove(name);
        }
    }
}
