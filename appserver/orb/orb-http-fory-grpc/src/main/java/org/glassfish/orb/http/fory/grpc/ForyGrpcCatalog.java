/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.server.ServerExchange;

/**
 * Deploy-time catalog of generated Fory IDL documents.
 *
 * <p>Registration is infrequent and may happen while applications deploy;
 * reads are lock-free map lookups and allocate only the response buffer. The
 * catalog is intentionally separate from invocation dispatch so downloading an
 * IDL can never contend with the EJB hot path.</p>
 */
public final class ForyGrpcCatalog {

    public static final String PREFIX = "/.well-known/ejb/";

    private final Map<String, byte[]> idlByPath = new ConcurrentHashMap<>();

    public void register(String path, String idl) {
        if (path == null || !path.startsWith(PREFIX) || !path.endsWith(".fdl")) {
            throw new IllegalArgumentException("invalid Fory IDL path: " + path);
        }
        if (idl == null || idl.isBlank()) {
            throw new IllegalArgumentException("IDL is empty");
        }
        idlByPath.put(path, idl.getBytes(StandardCharsets.UTF_8));
    }

    public void dispatch(ServerExchange exchange) throws IOException {
        if (!"GET".equals(exchange.method()) && !"HEAD".equals(exchange.method())) {
            exchange.setStatus(405);
            return;
        }
        String path = new String(exchange.pathBytes(), exchange.pathOffset(),
                exchange.pathLength(), StandardCharsets.UTF_8);
        byte[] idl = idlByPath.get(path);
        if (idl == null) {
            exchange.setStatus(Protocol.SC_NOT_FOUND);
            return;
        }
        exchange.setStatus(Protocol.SC_OK);
        exchange.setResponseHeader("Content-Type", "text/vnd.fory.idl; charset=utf-8");
        exchange.setResponseHeader("Content-Length", Integer.toString(idl.length));
        if ("GET".equals(exchange.method())) {
            exchange.writeBody(new ByteBuffer[] {ByteBuffer.wrap(idl)});
        }
    }

    public int size() {
        return idlByPath.size();
    }
}
