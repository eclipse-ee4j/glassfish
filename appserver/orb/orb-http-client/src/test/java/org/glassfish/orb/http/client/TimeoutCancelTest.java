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

import jakarta.ejb.EJBException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Protocol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeoutCancelTest {

    public interface Greeter {
        String greet(String name);
    }

    /** An EJB 2.x component interface: RMI style, every method declares RemoteException. */
    public interface Cart extends jakarta.ejb.EJBObject {
        String hello() throws java.rmi.RemoteException;
    }

    public interface CartHome extends jakarta.ejb.EJBHome {
        Cart create() throws jakarta.ejb.CreateException, java.rmi.RemoteException;
    }

    /**
     * Times out on every invocation (or answers 400 to it) and accepts
     * everything else. A cancel is only ever sent asynchronously, so the
     * caller does not wait for it.
     */
    private static final class TimingOutTransport implements HttpTransport {

        final List<Request> requests = new java.util.concurrent.CopyOnWriteArrayList<>();
        final List<Request> asyncRequests = new java.util.concurrent.CopyOnWriteArrayList<>();
        final boolean badRequest;

        TimingOutTransport(boolean badRequest) {
            this.badRequest = badRequest;
        }

        @Override
        public Response exchange(Request request) throws IOException {
            requests.add(request);
            if (badRequest) {
                return new Response(Protocol.SC_BAD_REQUEST, null, Map.of("X-GF-Reason", List.of("malformed")),
                        new ByteArrayInputStream(new byte[0]));
            }
            throw new HttpTimeoutException("request timed out");
        }

        @Override
        public CompletableFuture<Response> exchangeAsync(Request request) {
            asyncRequests.add(request);
            return CompletableFuture.completedFuture(
                    new Response(Protocol.SC_NO_CONTENT, null, Map.of(), new ByteArrayInputStream(new byte[0])));
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
    @DisplayName("a call that times out asks the server to stop it, and still reports the timeout")
    void aTimedOutCallIsCancelledOnTheServer() throws Exception {
        TimingOutTransport transport = new TimingOutTransport(false);
        ClientConfiguration config = ClientConfiguration.builder(URI.create("http://localhost:8080")).build();

        try (HttpEjbClient client = new HttpEjbClient(config, transport, new JavaSerializationMarshaller(),
                JavaSerializationMarshaller.defaultFilter())) {
            EjbLocator locator = new EjbLocator("app", "module", "", "GreeterBean");
            Method greet = Greeter.class.getMethod("greet", String.class);

            assertThrows(HttpTimeoutException.class,
                    () -> client.invoke(locator, Greeter.class, greet, new Object[] { "world" }));

            assertEquals(1, transport.requests.size(), "the invocation is the only call the caller waits for");
            assertEquals(1, transport.asyncRequests.size(), "the cancel goes out without being waited for");
            HttpTransport.Request invocation = transport.requests.get(0);
            HttpTransport.Request cancel = transport.asyncRequests.get(0);
            String id = invocation.headers().get(Protocol.H_INVOCATION_ID);
            assertEquals("DELETE", cancel.method());
            assertTrue(cancel.uri().getRawPath().contains("/cancel/"), cancel.uri().toString());
            assertTrue(cancel.uri().getRawPath().contains("/" + id + "/"), cancel.uri().toString());
            assertTrue(cancel.uri().getRawPath().endsWith("/false"),
                    "the caller has stopped listening; it has not asked for the bean to be interrupted: "
                            + cancel.uri());
        }
    }

    @Test
    @DisplayName("through a proxy a timeout is an EJBException, not an UndeclaredThrowableException")
    void aTimeoutThroughAProxyIsAnEjbException() throws Exception {
        TimingOutTransport transport = new TimingOutTransport(false);
        ClientConfiguration config = ClientConfiguration.builder(URI.create("http://localhost:8080")).build();

        try (HttpEjbClient client = new HttpEjbClient(config, transport, new JavaSerializationMarshaller(),
                JavaSerializationMarshaller.defaultFilter())) {
            Greeter greeter = client.createProxy(Greeter.class, new EjbLocator("app", "module", "", "GreeterBean"));

            EJBException thrown = assertThrows(EJBException.class, () -> greeter.greet("world"));
            assertTrue(thrown.getCause() instanceof HttpTimeoutException, String.valueOf(thrown.getCause()));
        }
    }

    @Test
    @DisplayName("through a proxy a 400 from the server is an EJBException too")
    void aBadRequestThroughAProxyIsAnEjbException() throws Exception {
        TimingOutTransport transport = new TimingOutTransport(true);
        ClientConfiguration config = ClientConfiguration.builder(URI.create("http://localhost:8080")).build();

        try (HttpEjbClient client = new HttpEjbClient(config, transport, new JavaSerializationMarshaller(),
                JavaSerializationMarshaller.defaultFilter())) {
            Greeter greeter = client.createProxy(Greeter.class, new EjbLocator("app", "module", "", "GreeterBean"));

            assertThrows(EJBException.class, () -> greeter.greet("world"));
        }
    }

    private HttpEjbClient clientOver(TimingOutTransport transport) {
        ClientConfiguration config = ClientConfiguration.builder(URI.create("http://localhost:8080")).build();
        return new HttpEjbClient(config, transport, new JavaSerializationMarshaller(),
                JavaSerializationMarshaller.defaultFilter());
    }

    @Test
    @DisplayName("a timeout in a business method of a component interface is a RemoteException")
    void aComponentMethodTimeoutIsARemoteException() throws Exception {
        try (HttpEjbClient client = clientOver(new TimingOutTransport(false))) {
            Cart cart = client.createComponentProxy(Cart.class,
                    new EjbLocator("app", "module", "", "CartBean", new byte[] { 1 }), null);

            java.rmi.RemoteException thrown = assertThrows(java.rmi.RemoteException.class, cart::hello);
            assertTrue(thrown.getCause() instanceof HttpTimeoutException, String.valueOf(thrown.getCause()));
        }
    }

    @Test
    @DisplayName("a timeout in create() on an EJB 2.x home is a RemoteException")
    void aHomeCreateTimeoutIsARemoteException() throws Exception {
        try (HttpEjbClient client = clientOver(new TimingOutTransport(false))) {
            CartHome home = client.createHomeProxy(CartHome.class, Cart.class,
                    new EjbLocator("app", "module", "", "CartBean"));

            assertThrows(java.rmi.RemoteException.class, home::create);
        }
    }

    @Test
    @DisplayName("a timeout in remove() on a component is a RemoteException")
    void aRemoveTimeoutIsARemoteException() throws Exception {
        try (HttpEjbClient client = clientOver(new TimingOutTransport(false))) {
            Cart cart = client.createComponentProxy(Cart.class,
                    new EjbLocator("app", "module", "", "CartBean", new byte[] { 1 }), null);

            assertThrows(java.rmi.RemoteException.class, cart::remove);
        }
    }
}
