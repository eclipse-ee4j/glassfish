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
import java.io.ObjectInputFilter;
import java.nio.ByteBuffer;
import java.util.Map;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NoPermissionException;

import org.glassfish.orb.http.protocol.ChunkedOutput;
import org.glassfish.orb.http.protocol.ContentType;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Marshaller;
import org.glassfish.orb.http.protocol.Marshallers;
import org.glassfish.orb.http.protocol.NamingRoutes;
import org.glassfish.orb.http.protocol.PathScanner;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.ProtocolException;

/** Serves the naming operations. */
public final class NamingDispatcher {

    private final NamingBridge naming;
    private final SecurityBridge security;
    private final Marshaller marshaller;

    public NamingDispatcher(NamingBridge naming) {
        this(naming, SecurityBridge.NONE, new JavaSerializationMarshaller());
    }

    public NamingDispatcher(NamingBridge naming, SecurityBridge security, Marshaller marshaller) {
        this.naming = naming;
        this.security = security;
        this.marshaller = marshaller;
    }

    public void dispatch(ServerExchange exchange) throws IOException {
        PathScanner path;
        NamingRoutes.Request request;
        try {
            path = PathScanner.scan(exchange.pathBytes(), exchange.pathOffset(), exchange.pathLength());
            request = NamingRoutes.parse(path);
        } catch (ProtocolException e) {
            exchange.setStatus(Protocol.SC_BAD_REQUEST);
            exchange.setResponseHeader("X-GF-Reason", e.getMessage());
            return;
        }

        String expectedMethod;
        try {
            expectedMethod = NamingRoutes.methodFor(request.operation());
        } catch (IllegalArgumentException e) {
            exchange.setStatus(Protocol.SC_NOT_FOUND);
            exchange.setResponseHeader("X-GF-Reason", "unknown naming operation");
            return;
        }
        if (!expectedMethod.equals(exchange.method())) {
            exchange.setStatus(Protocol.SC_BAD_REQUEST);
            exchange.setResponseHeader("X-GF-Reason",
                    request.operation() + " must be a " + expectedMethod);
            return;
        }

        Marshaller codec = codecFor(exchange);

        Object securityToken = security.establish(exchange.authenticatedUser());
        try {
            perform(codec, exchange, request);
        } catch (NameNotFoundException e) {
            exchange.setStatus(Protocol.SC_NOT_FOUND);
        } catch (NoPermissionException e) {
            exchange.setStatus(Protocol.SC_FORBIDDEN);
        } catch (NamingException e) {
            writeException(codec, exchange, e);
        } catch (ClassNotFoundException e) {
            exchange.setStatus(Protocol.SC_BAD_REQUEST);
            exchange.setResponseHeader("X-GF-Reason", "cannot resolve a class in the request body");
        } catch (RuntimeException | LinkageError e) {
            // Most often the codec refusing a value it cannot encode. Left to
            // propagate it becomes a bare 500 with no body, and the client is
            // told only that the connection ended.
            //
            // LinkageError as well as RuntimeException: an optional codec that
            // cannot initialise throws ExceptionInInitializerError, which is
            // not a RuntimeException, and letting that escape takes the
            // connection down instead of answering.
            exchange.setStatus(Protocol.SC_EXCEPTION);
            exchange.setResponseHeader("X-GF-Reason",
                    (operationLabel(request) + " failed: " + e).replace('\n', ' '));
        } finally {
            security.clear(securityToken);
        }
    }

    private void perform(Marshaller codec, ServerExchange exchange, NamingRoutes.Request request)
            throws IOException, ClassNotFoundException, NamingException {
        String name = request.jndiName();
        switch (request.operation()) {
            case Protocol.OP_LOOKUP -> writeValue(codec, exchange, naming.lookup(name));
            case Protocol.OP_LOOKUP_LINK -> writeValue(codec, exchange, naming.lookupLink(name));
            case Protocol.OP_LIST -> {
                Map<String, Object> bindings = naming.list(name);
                writeValue(codec, exchange, new java.util.HashMap<>(bindings));
            }
            case Protocol.OP_BIND -> {
                naming.bind(name, readValue(codec, exchange));
                exchange.setStatus(Protocol.SC_NO_CONTENT);
            }
            case Protocol.OP_REBIND -> {
                naming.rebind(name, readValue(codec, exchange));
                exchange.setStatus(Protocol.SC_NO_CONTENT);
            }
            case Protocol.OP_UNBIND -> {
                naming.unbind(name);
                exchange.setStatus(Protocol.SC_NO_CONTENT);
            }
            case Protocol.OP_RENAME -> {
                String newName = exchange.queryParameter(NamingRoutes.PARAM_NEW_NAME);
                if (newName == null) {
                    exchange.setStatus(Protocol.SC_BAD_REQUEST);
                    exchange.setResponseHeader("X-GF-Reason",
                            "rename requires the " + NamingRoutes.PARAM_NEW_NAME + " parameter");
                    return;
                }
                naming.rename(name, newName);
                exchange.setStatus(Protocol.SC_NO_CONTENT);
            }
            case Protocol.OP_CREATE_SUBCONTEXT -> {
                naming.createSubcontext(name);
                exchange.setStatus(Protocol.SC_NO_CONTENT);
            }
            case Protocol.OP_DESTROY_SUBCONTEXT -> {
                naming.destroySubcontext(name);
                exchange.setStatus(Protocol.SC_NO_CONTENT);
            }
            default -> {
                exchange.setStatus(Protocol.SC_NOT_FOUND);
                exchange.setResponseHeader("X-GF-Reason", "unknown naming operation");
            }
        }
    }

    /**
     * Picks the codec for this exchange.
     *
     * <p>An invocation always carries a body, so its codec is stated in the
     * content type. A naming read does not: a lookup is a GET, and the codec
     * matters for what comes back. So the request states its preference in
     * {@code Accept}, and a client that states nothing gets this server's
     * default - which is how an older client, which knew only one codec and
     * sent no preference, keeps working unchanged.
     *
     * @param exchange the request being served
     * @return the codec to use in both directions, never {@code null}
     */
    private Marshaller codecFor(ServerExchange exchange) {
        String header = exchange.requestHeader("Content-Type");
        if (header == null || header.isBlank()) {
            header = exchange.requestHeader("Accept");
        }
        if (header == null || header.isBlank()) {
            return marshaller;
        }
        try {
            ContentType requested = ContentType.parse(header);
            if (marshaller.codec().equals(requested.codec())) {
                return marshaller;
            }
            return Marshallers.find(requested.codec()).orElse(marshaller);
        } catch (ProtocolException | IllegalArgumentException e) {
            // An Accept header we cannot parse is a preference we cannot
            // honour, not a reason to refuse the lookup.
            return marshaller;
        }
    }

    private Object readValue(Marshaller codec, ServerExchange exchange)
            throws IOException, ClassNotFoundException {
        ObjectInputFilter filter = JavaSerializationMarshaller.defaultFilter();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (Marshaller.ObjectReader reader = codec.newReader(exchange.requestBody(), loader, filter)) {
            return reader.readObject();
        }
    }

    private void writeValue(Marshaller codec, ServerExchange exchange, Object value) throws IOException {
        // Marshalled before anything is committed, and this order is the whole
        // point. Setting the status first and encoding afterwards means a
        // failure to encode arrives at the client as a 200 that promised a body
        // and then ended - which the JDK's HTTP client reports as "EOF reached
        // while reading", a sentence about the connection that says nothing
        // about the bean, the codec or the value that could not be written.
        ByteBuffer[] body = marshal(codec, value);
        exchange.setStatus(Protocol.SC_OK);
        exchange.setResponseHeader("Content-Type",
                ContentType.of(codec.codec(), ContentType.KIND_VALUE).toHeaderValue());
        exchange.writeBody(body);
    }

    private void writeException(Marshaller codec, ServerExchange exchange, Throwable thrown) throws IOException {
        ByteBuffer[] body;
        try {
            body = marshal(codec, thrown);
        } catch (IOException | RuntimeException e) {
            // The failure could not be encoded. Say so in a header, which needs
            // no codec to read, rather than committing a body that is not
            // coming.
            exchange.setStatus(Protocol.SC_EXCEPTION);
            exchange.setResponseHeader("X-GF-Reason", reasonFor(thrown, e));
            return;
        }
        exchange.setStatus(Protocol.SC_EXCEPTION);
        exchange.setResponseHeader("Content-Type",
                ContentType.of(codec.codec(), ContentType.KIND_EXCEPTION).toHeaderValue());
        exchange.writeBody(body);
    }

    private static String operationLabel(NamingRoutes.Request request) {
        return request.operation() + ' ' + request.jndiName();
    }

    /**
     * @param thrown what the operation failed with
     * @param encoding why that failure could not be sent as a body
     * @return a single line naming both, safe to put in a header
     */
    private static String reasonFor(Throwable thrown, Throwable encoding) {
        return (thrown + " (and encoding it failed: " + encoding + ')').replace('\n', ' ');
    }

    private ByteBuffer[] marshal(Marshaller codec, Object value) throws IOException {
        ChunkedOutput out = new ChunkedOutput();
        try (Marshaller.ObjectWriter writer = codec.newWriter(out)) {
            writer.writeObject(value);
            writer.flush();
        }
        return out.toByteBuffers();
    }
}
