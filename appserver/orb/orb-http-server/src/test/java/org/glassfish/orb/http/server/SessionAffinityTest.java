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
import java.util.List;
import java.util.Map;

import org.glassfish.orb.http.protocol.CommonRoutes;
import org.glassfish.orb.http.protocol.EjbRoutes;
import org.glassfish.orb.http.protocol.Protocol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A stateful session bean lives in one instance's memory, so every invocation
 * quoting its id has to reach that instance. Over IIOP a reference names it
 * directly; here a cookie does, and these tests are about the cookie actually
 * being set - the part that was documented before it existed.
 */
class SessionAffinityTest {

    private static Loopback.InMemoryExchange get(String path) {
        byte[] bytes = path.getBytes(StandardCharsets.UTF_8);
        return new Loopback.InMemoryExchange("GET", bytes, null, Map.of(), new byte[0]);
    }

    private static String setCookie(Loopback.InMemoryExchange exchange) {
        List<String> values = exchange.toResponse().headers().get("Set-Cookie");
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    @Test
    @DisplayName("the affinity endpoint sets a routable cookie")
    void affinityEndpointSetsTheCookie() throws Exception {
        AffinityDispatcher dispatcher = new AffinityDispatcher(new SessionAffinity("instance-1"));
        Loopback.InMemoryExchange exchange = get(CommonRoutes.affinityPath(Protocol.CONTEXT_PATH));

        dispatcher.dispatch(exchange);

        assertEquals(Protocol.SC_NO_CONTENT, exchange.toResponse().status());
        String cookie = setCookie(exchange);
        assertNotNull(cookie, "without a cookie there is nothing for a load balancer to route on");
        assertTrue(cookie.startsWith(Protocol.AFFINITY_COOKIE + "=instance-1"),
                "the value must identify the node: " + cookie);
        assertTrue(cookie.contains("Path=" + Protocol.CONTEXT_PATH),
                "scoped to the endpoint, so it cannot collide with a deployed application's own session");
        assertTrue(cookie.contains("HttpOnly"));
    }

    @Test
    @DisplayName("the cookie is named JSESSIONID, because that is what load balancers already know")
    void theCookieUsesTheConventionalName() {
        assertEquals("JSESSIONID", Protocol.AFFINITY_COOKIE);
    }

    @Test
    void oneNodeAlwaysAnswersWithTheSameValue() throws Exception {
        AffinityDispatcher dispatcher = new AffinityDispatcher(new SessionAffinity("instance-1"));

        Loopback.InMemoryExchange first = get(CommonRoutes.affinityPath(Protocol.CONTEXT_PATH));
        Loopback.InMemoryExchange second = get(CommonRoutes.affinityPath(Protocol.CONTEXT_PATH));
        dispatcher.dispatch(first);
        dispatcher.dispatch(second);

        assertEquals(setCookie(first), setCookie(second),
                "a value that changed between calls would move the conversation between nodes");
    }

    @Test
    void differentNodesAnswerDifferently() throws Exception {
        Loopback.InMemoryExchange one = get(CommonRoutes.affinityPath(Protocol.CONTEXT_PATH));
        Loopback.InMemoryExchange two = get(CommonRoutes.affinityPath(Protocol.CONTEXT_PATH));
        new AffinityDispatcher(new SessionAffinity("instance-1")).dispatch(one);
        new AffinityDispatcher(new SessionAffinity("instance-2")).dispatch(two);

        assertNotEquals(setCookie(one), setCookie(two));
    }

    @Test
    void aGeneratedRouteIdIsStableForTheLifeOfTheNode() {
        SessionAffinity affinity = SessionAffinity.forThisNode();
        assertEquals(affinity.routeId(), affinity.routeId());
        assertNotEquals(SessionAffinity.forThisNode().routeId(), affinity.routeId());
    }

    @Test
    void theEndpointRejectsTheWrongMethodAndTheWrongPath() throws Exception {
        AffinityDispatcher dispatcher = new AffinityDispatcher(new SessionAffinity("instance-1"));

        byte[] path = CommonRoutes.affinityPath(Protocol.CONTEXT_PATH).getBytes(StandardCharsets.UTF_8);
        Loopback.InMemoryExchange posted =
                new Loopback.InMemoryExchange("POST", path, null, Map.of(), new byte[0]);
        dispatcher.dispatch(posted);
        assertEquals(Protocol.SC_BAD_REQUEST, posted.toResponse().status());

        Loopback.InMemoryExchange elsewhere = get(Protocol.CONTEXT_PATH + "/common/v1/something");
        dispatcher.dispatch(elsewhere);
        assertEquals(Protocol.SC_NOT_FOUND, elsewhere.toResponse().status());
    }

    // ---- the reason the endpoint exists ------------------------------------

    @Test
    @DisplayName("opening a stateful session pins it, even if the client never asked for affinity")
    void openAlsoSetsTheCookie() throws Exception {
        FakeContainer container = new FakeContainer();
        container.registerStateful("CounterBean", TestBeans.CounterBean::new);
        EjbDispatcher ejb = new EjbDispatcher(container, SecurityBridge.NONE, TransactionBridge.NONE,
                new org.glassfish.orb.http.protocol.JavaSerializationMarshaller(),
                new InvocationRegistry(), new SessionAffinity("instance-7"));

        byte[] path = EjbRoutes.openPath(Protocol.CONTEXT_PATH, "myapp", "mymodule", null, "CounterBean")
                .getBytes(StandardCharsets.UTF_8);
        Loopback.InMemoryExchange exchange =
                new Loopback.InMemoryExchange("POST", path, null, Map.of(), new byte[0]);

        ejb.dispatch(exchange);

        assertEquals(Protocol.SC_NO_CONTENT, exchange.toResponse().status());
        assertNotNull(exchange.toResponse().firstHeader(Protocol.H_SESSION_ID));
        String cookie = setCookie(exchange);
        assertNotNull(cookie, "the session lives on this node, so the response must say so");
        assertTrue(cookie.startsWith(Protocol.AFFINITY_COOKIE + "=instance-7"), cookie);
    }

    @Test
    @DisplayName("an ordinary invocation does not re-set it")
    void invocationsDoNotResendTheCookie() throws Exception {
        FakeContainer container = new FakeContainer();
        container.registerStateless("GreeterBean", new TestBeans.GreeterBean());
        EjbDispatcher ejb = new EjbDispatcher(container, SecurityBridge.NONE, TransactionBridge.NONE,
                new org.glassfish.orb.http.protocol.JavaSerializationMarshaller(),
                new InvocationRegistry(), new SessionAffinity("instance-7"));

        Loopback loopback = new Loopback(ejb,
                new NamingDispatcher(new HttpRoundTripTest.FakeNaming()),
                new AffinityDispatcher(new SessionAffinity("instance-7")));
        org.glassfish.orb.http.client.HttpEjbClient client =
                new org.glassfish.orb.http.client.HttpEjbClient(
                        org.glassfish.orb.http.client.ClientConfiguration
                                .builder(java.net.URI.create("http://localhost:8080" + Protocol.CONTEXT_PATH))
                                .build(),
                        loopback,
                        new org.glassfish.orb.http.protocol.JavaSerializationMarshaller(),
                        org.glassfish.orb.http.protocol.JavaSerializationMarshaller.defaultFilter());

        // Exercised through the real client so the path really is an invoke.
        assertEquals("Ada", client.createProxy(TestBeans.Greeter.class,
                new org.glassfish.orb.http.client.EjbLocator("myapp", "mymodule", null, "GreeterBean"))
                .greet("Ada", 1));

        byte[] path = EjbRoutes.invokePath(Protocol.CONTEXT_PATH, "myapp", "mymodule", null,
                "GreeterBean", EjbRoutes.ABSENT, TestBeans.Greeter.class.getName(), "greet",
                new String[] { "java.lang.String", "int" }).getBytes(StandardCharsets.UTF_8);
        Loopback.InMemoryExchange invoke =
                new Loopback.InMemoryExchange("POST", path, null, Map.of(), new byte[0]);
        ejb.dispatch(invoke);

        assertFalse(invoke.toResponse().headers().containsKey("Set-Cookie"),
                "the client already has the cookie; resending it on every call is noise");
    }
}
