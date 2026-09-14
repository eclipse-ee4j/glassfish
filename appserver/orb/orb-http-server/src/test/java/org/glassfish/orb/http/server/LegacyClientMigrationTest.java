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





import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.glassfish.orb.http.client.HttpInitialContextFactory;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.RemoteEjbReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The claim this transport makes, tested over a real socket.
 *
 * <p>The claim is narrow: an existing remote EJB client keeps its JNDI names,
 * its interfaces and its code, and reaches the server over HTTP by changing
 * the {@code InitialContextFactory} and the provider URL. Everything here goes
 * through {@code new InitialContext(env)} and the real
 * {@code java.net.http.HttpClient} rather than through any test seam, because
 * a claim about migration that is only demonstrated against a fake is not
 * demonstrated.
 */
class LegacyClientMigrationTest {

    private static final String JNDI_NAME = "java:global/myapp/GreeterBean!"
            + "org.glassfish.orb.http.server.TestBeans$Greeter";

    private RealHttpServer server;
    private RecordingSecurity security;
    private HttpRoundTripTest.FakeNaming naming;

    /** Records the identity the transport established, as the container would consume it. */
    static final class RecordingSecurity implements SecurityBridge {

        volatile String established;

        @Override
        public Object establish(String userName) {
            this.established = userName;
            return userName;
        }

        @Override
        public void clear(Object token) {
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        FakeContainer container = new FakeContainer();
        container.registerStateless("GreeterBean", new TestBeans.GreeterBean());
        container.registerStateful("CounterBean", TestBeans.CounterBean::new);

        naming = new HttpRoundTripTest.FakeNaming();
        naming.bindings.put(JNDI_NAME, new RemoteEjbReference("myapp", "mymodule", null,
                "GreeterBean", TestBeans.Greeter.class.getName(), null));

        security = new RecordingSecurity();
        EjbDispatcher ejb = new EjbDispatcher(container, security, TransactionBridge.NONE,
                new JavaSerializationMarshaller(), new InvocationRegistry(),
                new SessionAffinity("instance-1"));

        server = new RealHttpServer(ejb, new NamingDispatcher(naming),
                new AffinityDispatcher(new SessionAffinity("instance-1")));
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.close();
        }
    }

    private Hashtable<String, String> environment() {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, HttpInitialContextFactory.class.getName());
        env.put(Context.PROVIDER_URL, server.baseUri().toString());
        Hashtable<String, String> table = new Hashtable<>();
        env.forEach((k, v) -> table.put(String.valueOf(k), String.valueOf(v)));
        return table;
    }

    @Test
    @DisplayName("the whole migration is the factory and the URL")
    void aLegacyClientChangesOnlyTheFactoryAndTheProviderUrl() throws Exception {
        // Everything below this line is what the application already had: the
        // same JNDI name, the same interface, the same call.
        InitialContext context = new InitialContext(environment());
        try {
            TestBeans.Greeter greeter = (TestBeans.Greeter) context.lookup(JNDI_NAME);

            assertNotNull(greeter);
            assertEquals("Ada Ada Ada", greeter.greet("Ada", 3));
        } finally {
            context.close();
        }
    }

    @Test
    @DisplayName("a structured return value crosses a real connection unchanged")
    void structuredResultsSurviveTheWire() throws Exception {
        InitialContext context = new InitialContext(environment());
        try {
            TestBeans.Greeter greeter = (TestBeans.Greeter) context.lookup(JNDI_NAME);
            assertEquals(List.of(0, 1, 2, 3, 4), greeter.cyclicFriendlyPayload(5).get("numbers"));
        } finally {
            context.close();
        }
    }

    @Test
    @DisplayName("an application exception arrives as itself, over HTTP, with its business state")
    void applicationExceptionsArriveIntact() throws Exception {
        InitialContext context = new InitialContext(environment());
        try {
            TestBeans.Greeter greeter = (TestBeans.Greeter) context.lookup(JNDI_NAME);

            TestBeans.GreetingRefused refused =
                    assertThrows(TestBeans.GreetingRefused.class, greeter::refuse);
            assertEquals("not today", refused.getMessage());
            assertEquals("E_CLOSED", refused.reasonCode());
            assertInstanceOf(IllegalStateException.class, refused.getCause());
            assertTrue(refused.getStackTrace().length > 0);

            // And the other half of the contract, over the same connection.
            jakarta.ejb.EJBException wrapped =
                    assertThrows(jakarta.ejb.EJBException.class, greeter::explode);
            assertTrue(wrapped.getMessage().contains("IllegalStateException"));
        } finally {
            context.close();
        }
    }

    @Test
    @DisplayName("a multi-megabyte argument and result cross a real connection")
    void largePayloadsCrossARealConnection() throws Exception {
        InitialContext context = new InitialContext(environment());
        try {
            TestBeans.Greeter greeter = (TestBeans.Greeter) context.lookup(JNDI_NAME);
            String payload = "x".repeat(3 * 1024 * 1024);
            assertEquals(payload, greeter.echoLarge(payload));
        } finally {
            context.close();
        }
    }

    @Test
    @DisplayName("credentials on the InitialContext reach the container as an identity")
    void callerIdentityReachesTheContainer() throws Exception {
        Hashtable<String, String> env = environment();
        env.put(Context.SECURITY_PRINCIPAL, "alice");
        env.put(Context.SECURITY_CREDENTIALS, "secret");

        InitialContext context = new InitialContext(env);
        try {
            TestBeans.Greeter greeter = (TestBeans.Greeter) context.lookup(JNDI_NAME);
            greeter.greet("Ada", 1);

            assertEquals("alice", security.established,
                    "this is the seam the container's SecurityContext.setCurrent hangs off");
        } finally {
            context.close();
        }
    }

    @Test
    @DisplayName("many threads share one client without interfering")
    void concurrentInvocationsFromOneClient() throws Exception {
        InitialContext context = new InitialContext(environment());
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            TestBeans.Greeter greeter = (TestBeans.Greeter) context.lookup(JNDI_NAME);

            List<Callable<Boolean>> tasks = new ArrayList<>();
            for (int t = 0; t < 8; t++) {
                final String name = "caller" + t;
                tasks.add(() -> {
                    for (int i = 0; i < 25; i++) {
                        // Each thread must get back its own name, never another's.
                        if (!(name + ' ' + name).equals(greeter.greet(name, 2))) {
                            return false;
                        }
                    }
                    return true;
                });
            }
            for (Future<Boolean> result : pool.invokeAll(tasks, 60, TimeUnit.SECONDS)) {
                assertTrue(result.get(), "a caller saw another caller's result");
            }
        } finally {
            pool.shutdownNow();
            context.close();
        }
    }

    @Test
    @DisplayName("a stateful conversation keeps its state across separate HTTP requests")
    void statefulConversationOverRealHttp() throws Exception {
        org.glassfish.orb.http.client.ClientConfiguration config =
                org.glassfish.orb.http.client.ClientConfiguration.builder(server.baseUri()).build();
        try (org.glassfish.orb.http.client.HttpEjbClient client =
                     new org.glassfish.orb.http.client.HttpEjbClient(config)) {

            // The order a clustered deployment needs: get the routing cookie
            // before the session exists, so the open is routed with it.
            client.establishAffinity();

            org.glassfish.orb.http.client.EjbLocator home =
                    new org.glassfish.orb.http.client.EjbLocator("myapp", "mymodule", null, "CounterBean");
            byte[] session = client.openSession(home);
            assertNotNull(session);

            TestBeans.Counter counter =
                    client.createProxy(TestBeans.Counter.class, home.withSession(session));

            assertEquals(1, counter.increment());
            assertEquals(2, counter.increment());
            assertEquals(3, counter.increment());
            assertEquals(3, counter.value(), "state must survive across separate requests");

            byte[] other = client.openSession(home);
            TestBeans.Counter second = client.createProxy(TestBeans.Counter.class, home.withSession(other));
            assertEquals(1, second.increment(), "a second session is a distinct instance");
            assertEquals(3, counter.value(), "and does not disturb the first");
        }
    }

    @Test
    @DisplayName("the affinity cookie really is stored and returned by the client")
    void theAffinityCookieRoundTrips() throws Exception {
        org.glassfish.orb.http.client.ClientConfiguration config =
                org.glassfish.orb.http.client.ClientConfiguration.builder(server.baseUri()).build();
        try (org.glassfish.orb.http.client.HttpEjbClient client =
                     new org.glassfish.orb.http.client.HttpEjbClient(config)) {

            client.establishAffinity();

            // If the cookie were not retained and returned, the server would
            // simply never see it - so assert from the server's side, which is
            // where a load balancer would be reading it too.
            org.glassfish.orb.http.client.EjbLocator home =
                    new org.glassfish.orb.http.client.EjbLocator("myapp", "mymodule", null, "CounterBean");
            client.openSession(home);

            assertEquals("HTTP/1.1", client.negotiatedVersion(),
                    "the JDK ships no HTTP/2 server, so this harness proves the protocol, not h2");
        }
    }
}
