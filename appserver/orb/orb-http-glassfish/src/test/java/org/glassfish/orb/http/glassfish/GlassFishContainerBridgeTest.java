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

package org.glassfish.orb.http.glassfish;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;

import jakarta.ejb.CreateException;
import jakarta.ejb.NoSuchObjectLocalException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.enterprise.iiop.spi.EjbContainerFacade;
import org.glassfish.orb.http.protocol.EjbKey;
import org.glassfish.orb.http.protocol.EjbRoutes;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.server.ContainerBridge;
import org.glassfish.orb.http.server.EjbDispatcher;
import org.glassfish.orb.http.server.ServerExchange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Opening a session through the bridge, over containers that are not the
 * server's: a stand-in for {@code EjbContainerFacade} and for the descriptor
 * it reports, and a name index with a fixed answer.
 *
 * <p>What this does not cover: the real {@code StatefulSessionContainer} and
 * {@code BaseContainer} (their own {@code createSession}), the real
 * {@code EjbNameIndex} built from deployed applications, and any running
 * server. The bean here is a descriptor and a recording facade, so what is
 * proved is the bridge's own choices: which bean, which view, and which
 * failure is which.
 */
class GlassFishContainerBridgeTest {

    private static final long COUNTER = 11;
    private static final long GREETER = 12;
    private static final byte[] SESSION = { 9, 8, 7, 6 };

    private final List<String> createCalls = new ArrayList<>();
    private final List<Long> refreshes = new ArrayList<>();
    private final List<String> removeCalls = new ArrayList<>();
    private Exception removeFails;
    private final Map<Long, EjbContainerFacade> containers = new HashMap<>();
    private final GlassFishContainerBridge bridge = new GlassFishContainerBridge(containers::get);

    GlassFishContainerBridgeTest() {
        bridge.useIndex(new EjbNameIndex() {
            @Override
            public Long lookup(String appName, String moduleName, String beanName) {
                return switch (beanName) {
                    case "CounterBean", "BusinessCounterBean", "BrokenCounterBean" -> COUNTER;
                    case "GreeterBean" -> GREETER;
                    case "GoneBean" -> 99L;
                    default -> null;
                };
            }

            @Override
            public void refresh() {
                refreshes.add(1L);
            }
        });
    }

    /** A stateful bean with an EJB 2.x remote interface, so it is opened through its home. */
    private void statefulWithHome() {
        containers.put(COUNTER, facade(descriptor("CounterBean", true, true, Set.of()), null));
    }

    private EjbContainerFacade facade(EjbDescriptor descriptor, Exception createFails) {
        return (EjbContainerFacade) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { EjbContainerFacade.class }, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getEjbDescriptor":
                            return descriptor;
                        case "createSession":
                            createCalls.add(String.valueOf(args[0]));
                            if (createFails != null) {
                                throw createFails;
                            }
                            return SESSION;
                        case "removeSession":
                            removeCalls.add(Arrays.toString((byte[]) args[0]));
                            if (removeFails != null) {
                                throw removeFails;
                            }
                            return null;
                        default:
                            return other(proxy, method.getName(), args);
                    }
                });
    }

    private static EjbDescriptor descriptor(String name, boolean stateful, boolean home, Set<String> business) {
        return (EjbDescriptor) Proxy.newProxyInstance(GlassFishContainerBridgeTest.class.getClassLoader(),
                new Class<?>[] { EjbSessionDescriptor.class }, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getName":
                            return name;
                        case "isStateful":
                            return stateful;
                        case "isRemoteInterfacesSupported":
                            return home;
                        case "getRemoteBusinessClassNames":
                            return business;
                        default:
                            return other(proxy, method.getName(), args);
                    }
                });
    }

    private static Object other(Object proxy, String name, Object[] args) {
        return switch (name) {
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            case "toString" -> "stand-in";
            default -> throw new UnsupportedOperationException(name);
        };
    }

    // ---- the bridge --------------------------------------------------------

    @Test
    @DisplayName("a stateful bean opens a session through its home view and returns the container's key")
    void aStatefulBeanOpens() throws Exception {
        statefulWithHome();

        byte[] id = bridge.createSession("myapp", "mymodule", null, "CounterBean");

        assertArrayEquals(SESSION, id);
        assertEquals(List.of("null"), createCalls);
    }

    @Test
    @DisplayName("a stateful bean with only business interfaces opens through the generated name of one of them")
    void aBusinessOnlyBeanOpensThroughItsGeneratedView() throws Exception {
        containers.put(COUNTER, facade(descriptor("BusinessCounterBean", true, false,
                Set.of("test.Counter")), null));

        byte[] id = bridge.createSession("myapp", "mymodule", null, "BusinessCounterBean");

        assertArrayEquals(SESSION, id);
        assertEquals(List.of(GlassFishContainerBridge.generatedViewName("test.Counter")), createCalls);
    }

    @Test
    @DisplayName("a bean that is not stateful is refused as such, and is not reported as absent")
    void aBeanThatIsNotStatefulIsRefused() {
        containers.put(GREETER, facade(descriptor("GreeterBean", false, true, Set.of()),
                new RemoteException("GreeterBean is not a stateful session bean")));

        ContainerBridge.SessionRefusedException refused = assertThrows(
                ContainerBridge.SessionRefusedException.class,
                () -> bridge.createSession("myapp", "mymodule", null, "GreeterBean"));

        assertTrue(refused.getMessage().contains("not a stateful"), refused.getMessage());
        assertTrue(createCalls.isEmpty(), "the container was asked: " + createCalls);
    }

    @Test
    @DisplayName("a container that refuses to create the session is a refusal, not an absent bean")
    void aContainerThatRefusesIsARefusal() {
        containers.put(COUNTER, facade(descriptor("BrokenCounterBean", true, true, Set.of()),
                new CreateException("PostConstruct failed")));

        ContainerBridge.SessionRefusedException refused = assertThrows(
                ContainerBridge.SessionRefusedException.class,
                () -> bridge.createSession("myapp", "mymodule", null, "BrokenCounterBean"));

        assertTrue(refused.getMessage().contains("PostConstruct failed"), refused.getMessage());
    }

    @Test
    @DisplayName("a bean that is not deployed is absent")
    void anUnknownBeanIsAbsent() {
        assertThrows(ContainerBridge.NoSuchTargetException.class,
                () -> bridge.createSession("myapp", "mymodule", null, "NoSuchBean"));
        assertTrue(createCalls.isEmpty());
    }

    @Test
    @DisplayName("a bean whose container has gone is absent, and the index is refreshed")
    void aVanishedContainerIsAbsent() {
        assertThrows(ContainerBridge.NoSuchTargetException.class,
                () -> bridge.createSession("myapp", "mymodule", null, "GoneBean"));
        assertEquals(1, refreshes.size());
    }

    @Test
    @DisplayName("removing a session that is already gone, or never existed, is not an error")
    void removingAGoneSessionIsNotAnError() throws Exception {
        statefulWithHome();
        removeFails = new NoSuchObjectLocalException("Invalid Session Key");

        bridge.removeSession(new EjbKey(COUNTER, SESSION));

        // The container was asked, and its refusal was the answer.
        assertEquals(List.of(Arrays.toString(SESSION)), removeCalls);
    }

    @Test
    @DisplayName("a container that has no sessions to remove is reported without its own wording")
    void aBeanWithoutSessionsIsReportedPlainly() {
        containers.put(GREETER, facade(descriptor("GreeterBean", false, true, Set.of()), null));
        removeFails = new RemoteException("secret internal detail");

        ContainerBridge.NoSuchTargetException thrown = assertThrows(ContainerBridge.NoSuchTargetException.class,
                () -> bridge.removeSession(new EjbKey(GREETER, SESSION)));

        assertTrue(!thrown.getMessage().contains("secret internal detail"), thrown.getMessage());
    }

    // ---- through the dispatcher ---------------------------------------------

    @Test
    @DisplayName("open answers 204 with a session id for a stateful bean")
    void openAnswers204ForAStatefulBean() throws Exception {
        statefulWithHome();

        Exchange exchange = open("CounterBean");

        assertEquals(Protocol.SC_NO_CONTENT, exchange.status);
        assertNotNull(exchange.headers.get(Protocol.H_SESSION_ID));
        assertArrayEquals(SESSION,
                EjbRoutes.decodeSessionId(exchange.headers.get(Protocol.H_SESSION_ID).get(0)));
    }

    @Test
    @DisplayName("open answers 500, not 404, for a bean that is not stateful")
    void openAnswers500ForABeanThatIsNotStateful() throws Exception {
        containers.put(GREETER, facade(descriptor("GreeterBean", false, true, Set.of()),
                new RemoteException("not stateful")));

        Exchange exchange = open("GreeterBean");

        assertEquals(Protocol.SC_EXCEPTION, exchange.status);
        assertNull(exchange.headers.get(Protocol.H_SESSION_ID));
    }

    @Test
    @DisplayName("open answers 404 for a bean that does not exist")
    void openAnswers404ForAnUnknownBean() throws Exception {
        assertEquals(Protocol.SC_NOT_FOUND, open("NoSuchBean").status);
    }

    @Test
    @DisplayName("remove answers 204 for a session that is already gone, and does not fail the request")
    void removeAnswers204ForAGoneSession() throws Exception {
        statefulWithHome();
        removeFails = new NoSuchObjectLocalException("Invalid Session Key");

        Exchange exchange = new Exchange("DELETE", EjbRoutes.removePath(Protocol.CONTEXT_PATH,
                "myapp", "mymodule", null, "CounterBean", EjbRoutes.encodeSessionId(SESSION)));
        new EjbDispatcher(bridge).dispatch(exchange);

        assertEquals(Protocol.SC_NO_CONTENT, exchange.status);
        assertEquals(1, removeCalls.size());
    }

    private Exchange open(String bean) throws Exception {
        Exchange exchange = new Exchange("POST",
                EjbRoutes.openPath(Protocol.CONTEXT_PATH, "myapp", "mymodule", null, bean));
        new EjbDispatcher(bridge).dispatch(exchange);
        return exchange;
    }

    /** A request with no body, and a response that keeps what it is given. */
    private static final class Exchange implements ServerExchange {

        private final String method;
        private final byte[] path;

        int status = 200;
        final Map<String, List<String>> headers = new HashMap<>();

        Exchange(String method, String path) {
            this.method = method;
            this.path = path.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String method() {
            return method;
        }

        @Override
        public byte[] pathBytes() {
            return path;
        }

        @Override
        public int pathOffset() {
            return 0;
        }

        @Override
        public int pathLength() {
            return path.length;
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
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public void setStatus(int status) {
            this.status = status;
        }

        @Override
        public void setResponseHeader(String name, String value) {
            headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        }

        @Override
        public void writeBody(ByteBuffer[] body) {
        }

        @Override
        public String authenticatedUser() {
            return null;
        }
    }
}
