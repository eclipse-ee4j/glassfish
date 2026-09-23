/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.server.ServerExchange;

/** Bridges Grizzly's HTTP exchange to a registered Fory service binding. */
public final class ForyGrpcServerAdapter {

    private final ForyGeneratedServiceRegistry registry;
    private final TargetProvider targets;
    private final int maxMessageBytes;

    public ForyGrpcServerAdapter(ForyGeneratedServiceRegistry registry,
                                 TargetProvider targets, int maxMessageBytes) {
        if (maxMessageBytes < 0) {
            throw new IllegalArgumentException("maxMessageBytes must not be negative");
        }
        this.registry = Objects.requireNonNull(registry, "registry");
        this.targets = Objects.requireNonNull(targets, "targets");
        this.maxMessageBytes = maxMessageBytes;
    }

    public void dispatch(ServerExchange exchange) throws IOException {
        if (!"POST".equals(exchange.method())) {
            exchange.setStatus(405);
            return;
        }
        String path = new String(exchange.pathBytes(), exchange.pathOffset(), exchange.pathLength(),
                java.nio.charset.StandardCharsets.UTF_8);
        ForyGeneratedServiceRegistry.Binding binding = registry.lookup(path);
        if (binding == null) {
            exchange.setStatus(Protocol.SC_NOT_FOUND);
            return;
        }
        try (Target target = targets.acquire(path, exchange)) {
            byte[] payload = readFrame(exchange.requestBody());
            Object request = binding.runtime().deserialize(payload, binding.models().request());
            Object response = binding.invoke(target.value(), request);
            byte[] encoded = binding.runtime().serialize(response);
            byte[] frame = frame(encoded);
            exchange.setStatus(Protocol.SC_OK);
            exchange.setResponseHeader("Content-Type", "application/grpc+fory");
            exchange.setResponseHeader("Content-Length", Integer.toString(frame.length));
            exchange.writeBody(new ByteBuffer[] {ByteBuffer.wrap(frame)});
        } catch (Throwable e) {
            if (e instanceof IOException io) {
                throw io;
            }
            throw new IOException("Fory gRPC invocation failed", e);
        }
    }

    private byte[] readFrame(InputStream input) throws IOException {
        int flags = input.read();
        if (flags < 0) {
            throw new EOFException("missing gRPC frame");
        }
        if (flags != 0) {
            throw new IOException("compressed gRPC frames are not supported");
        }
        int length = (input.read() << 24) | (input.read() << 16)
                | (input.read() << 8) | input.read();
        if (length < 0 || length > maxMessageBytes) {
            throw new IOException("gRPC message exceeds configured limit: " + length);
        }
        byte[] payload = input.readNBytes(length);
        if (payload.length != length) {
            throw new EOFException("truncated gRPC frame");
        }
        return payload;
    }

    private static byte[] frame(byte[] payload) {
        byte[] frame = new byte[payload.length + 5];
        frame[0] = 0;
        frame[1] = (byte) (payload.length >>> 24);
        frame[2] = (byte) (payload.length >>> 16);
        frame[3] = (byte) (payload.length >>> 8);
        frame[4] = (byte) payload.length;
        System.arraycopy(payload, 0, frame, 5, payload.length);
        return frame;
    }

    @FunctionalInterface
    public interface TargetProvider {
        Target acquire(String path, ServerExchange exchange) throws Exception;
    }

    public interface Target extends AutoCloseable {
        Object value();

        @Override
        void close();
    }
}
