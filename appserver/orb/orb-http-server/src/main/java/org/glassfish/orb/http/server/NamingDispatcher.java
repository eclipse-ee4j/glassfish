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

        Object securityToken = security.establish(exchange.authenticatedUser());
        try {
            perform(exchange, request);
        } catch (NameNotFoundException e) {
            exchange.setStatus(Protocol.SC_NOT_FOUND);
        } catch (NoPermissionException e) {
            exchange.setStatus(Protocol.SC_FORBIDDEN);
        } catch (NamingException e) {
            writeException(exchange, e);
        } catch (ClassNotFoundException e) {
            exchange.setStatus(Protocol.SC_BAD_REQUEST);
            exchange.setResponseHeader("X-GF-Reason", "cannot resolve a class in the request body");
        } finally {
            security.clear(securityToken);
        }
    }

    private void perform(ServerExchange exchange, NamingRoutes.Request request)
            throws IOException, ClassNotFoundException, NamingException {
        String name = request.jndiName();
        switch (request.operation()) {
            case Protocol.OP_LOOKUP -> writeValue(exchange, naming.lookup(name));
            case Protocol.OP_LOOKUP_LINK -> writeValue(exchange, naming.lookupLink(name));
            case Protocol.OP_LIST -> {
                Map<String, Object> bindings = naming.list(name);
                writeValue(exchange, new java.util.HashMap<>(bindings));
            }
            case Protocol.OP_BIND -> {
                naming.bind(name, readValue(exchange));
                exchange.setStatus(Protocol.SC_NO_CONTENT);
            }
            case Protocol.OP_REBIND -> {
                naming.rebind(name, readValue(exchange));
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

    private Object readValue(ServerExchange exchange) throws IOException, ClassNotFoundException {
        ObjectInputFilter filter = JavaSerializationMarshaller.defaultFilter();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (Marshaller.ObjectReader reader = marshaller.newReader(exchange.requestBody(), loader, filter)) {
            return reader.readObject();
        }
    }

    private void writeValue(ServerExchange exchange, Object value) throws IOException {
        exchange.setStatus(Protocol.SC_OK);
        exchange.setResponseHeader("Content-Type",
                ContentType.of(marshaller.codec(), ContentType.KIND_VALUE).toHeaderValue());
        exchange.writeBody(marshal(value));
    }

    private void writeException(ServerExchange exchange, Throwable thrown) throws IOException {
        exchange.setStatus(Protocol.SC_EXCEPTION);
        exchange.setResponseHeader("Content-Type",
                ContentType.of(marshaller.codec(), ContentType.KIND_EXCEPTION).toHeaderValue());
        exchange.writeBody(marshal(thrown));
    }

    private ByteBuffer[] marshal(Object value) throws IOException {
        ChunkedOutput out = new ChunkedOutput();
        try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
            writer.writeObject(value);
            writer.flush();
        }
        return out.toByteBuffers();
    }
}
