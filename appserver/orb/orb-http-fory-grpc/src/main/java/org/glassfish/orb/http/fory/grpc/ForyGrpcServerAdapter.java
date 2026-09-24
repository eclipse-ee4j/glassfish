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
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.server.ServerExchange;

/** Bridges Grizzly's HTTP exchange to a registered Fory service binding. */
public final class ForyGrpcServerAdapter {

    static final String GRPC_STATUS = "grpc-status";
    static final String GRPC_MESSAGE = "grpc-message";
    static final String GRPC_FORY_CONTENT_TYPE = "application/grpc+fory";

    /** The status codes this adapter can produce; see the gRPC status definitions. */
    static final int STATUS_OK = 0;
    static final int STATUS_UNKNOWN = 2;
    static final int STATUS_INVALID_ARGUMENT = 3;
    static final int STATUS_RESOURCE_EXHAUSTED = 8;
    static final int STATUS_UNIMPLEMENTED = 12;

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();


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
        if (!exchange.supportsResponseTrailers()) {
            // gRPC carries its status in the trailers; a transport that cannot
            // send them cannot answer a gRPC call, and pretending otherwise
            // would leave the client waiting for a status that never arrives.
            exchange.setStatus(501); // Not Implemented: this transport cannot answer gRPC
            return;
        }
        String path = new String(exchange.pathBytes(), exchange.pathOffset(), exchange.pathLength(),
                StandardCharsets.UTF_8);
        ForyGeneratedServiceRegistry.Binding binding = registry.lookup(path);
        if (binding == null) {
            // A gRPC client reads HTTP 404 as UNIMPLEMENTED.
            exchange.setStatus(Protocol.SC_NOT_FOUND);
            return;
        }

        byte[] frame;
        try (Target target = targets.acquire(path, exchange)) {
            byte[] payload = readFrame(exchange.requestBody());
            Object request = binding.runtime().deserialize(payload, binding.models().request());
            Object response = binding.invoke(target.value(), request);
            frame = frame(binding.runtime().serialize(response));
        } catch (GrpcStatusException e) {
            answerWithStatus(exchange, e.status(), e.getMessage());
            return;
        } catch (Throwable e) {
            // The call reached the bean and failed there, or the payload could
            // not be decoded: either way the client gets a gRPC status, not a
            // transport error.
            answerWithStatus(exchange, STATUS_UNKNOWN, e.toString());
            return;
        }

        exchange.setStatus(Protocol.SC_OK);
        exchange.setResponseHeader("Content-Type", GRPC_FORY_CONTENT_TYPE);
        exchange.setResponseTrailer(GRPC_STATUS, Integer.toString(STATUS_OK));
        exchange.writeBody(new ByteBuffer[] {ByteBuffer.wrap(frame)});
    }

    /** A gRPC error is an HTTP 200 whose trailers carry the status. */
    private void answerWithStatus(ServerExchange exchange, int status, String message) throws IOException {
        exchange.setStatus(Protocol.SC_OK);
        exchange.setResponseHeader("Content-Type", GRPC_FORY_CONTENT_TYPE);
        exchange.setResponseTrailer(GRPC_STATUS, Integer.toString(status));
        if (message != null && !message.isEmpty()) {
            exchange.setResponseTrailer(GRPC_MESSAGE, percentEncode(message));
        }
        exchange.writeBody(new ByteBuffer[0]);
    }

    /**
     * Percent-encodes a status message as the gRPC specification requires:
     * ASCII space to tilde stays, except the percent sign itself; everything
     * else goes out as UTF-8 bytes in %XX form.
     */
    static String percentEncode(String message) {
        StringBuilder out = new StringBuilder(message.length() + 8);
        for (byte b : message.getBytes(StandardCharsets.UTF_8)) {
            int value = b & 0xFF;
            if (value >= 0x20 && value <= 0x7E && value != '%') {
                out.append((char) value);
            } else {
                out.append('%').append(HEX[value >>> 4]).append(HEX[value & 0x0F]);
            }
        }
        return out.toString();
    }

    private byte[] readFrame(InputStream input) throws IOException {
        int flags = input.read();
        if (flags < 0) {
            throw new EOFException("missing gRPC frame");
        }
        if (flags != 0) {
            throw new GrpcStatusException(STATUS_UNIMPLEMENTED, "compressed gRPC frames are not supported");
        }
        int length = (readByte(input) << 24) | (readByte(input) << 16)
                | (readByte(input) << 8) | readByte(input);
        if (length < 0 || length > maxMessageBytes) {
            throw new GrpcStatusException(STATUS_RESOURCE_EXHAUSTED,
                    "gRPC message exceeds configured limit: " + length);
        }
        byte[] payload = input.readNBytes(length);
        if (payload.length != length) {
            throw new GrpcStatusException(STATUS_INVALID_ARGUMENT, "truncated gRPC frame");
        }
        return payload;
    }

    private static int readByte(InputStream input) throws IOException {
        int value = input.read();
        if (value < 0) {
            throw new EOFException("truncated gRPC frame header");
        }
        return value;
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

    /** Carries the gRPC status a failure should be reported with. */
    static final class GrpcStatusException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final int status;

        GrpcStatusException(int status, String message) {
            super(message);
            this.status = status;
        }

        int status() {
            return status;
        }
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
