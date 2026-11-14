/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.server.ServerExchange;
import org.glassfish.orb.http.server.TransactionBridge;

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

    private static final Logger LOG = System.getLogger(ForyGrpcServerAdapter.class.getName());

    /**
     * The longest status message sent, in characters. A bean's exception can
     * carry anything, and the message travels in a header.
     */
    static final int MAX_STATUS_MESSAGE_CHARS = 512;

    private static final String GRPC_CONTENT_TYPE = "application/grpc";

    private final ForyGeneratedServiceRegistry registry;
    private final TargetProvider targets;
    private final int maxMessageBytes;
    private final TransactionBridge transactions;

    /** An adapter for a server with no transaction manager to clear. */
    public ForyGrpcServerAdapter(ForyGeneratedServiceRegistry registry,
                                 TargetProvider targets, int maxMessageBytes) {
        this(registry, targets, maxMessageBytes, TransactionBridge.NONE);
    }

    /**
     * @param transactions used only to leave the request thread with no
     *        transaction on it; this endpoint does not yet import the
     *        caller's, and a gRPC call carries none
     */
    public ForyGrpcServerAdapter(ForyGeneratedServiceRegistry registry,
                                 TargetProvider targets, int maxMessageBytes,
                                 TransactionBridge transactions) {
        if (maxMessageBytes < 0) {
            throw new IllegalArgumentException("maxMessageBytes must not be negative");
        }
        this.registry = Objects.requireNonNull(registry, "registry");
        this.targets = Objects.requireNonNull(targets, "targets");
        this.maxMessageBytes = maxMessageBytes;
        this.transactions = Objects.requireNonNull(transactions, "transactions");
    }

    public void dispatch(ServerExchange exchange) throws IOException {
        if (!"POST".equals(exchange.method())) {
            exchange.setStatus(405);
            exchange.setResponseHeader("Allow", "POST");
            return;
        }
        if (!isGrpc(exchange.requestHeader("Content-Type"))) {
            // Not a gRPC call, and in particular not something a browser can
            // send across origins without a preflight.
            exchange.setStatus(415); // Unsupported Media Type
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

        // Read and decode the request before the container is asked for
        // anything: a malformed call then costs no identity and no bean.
        Object request;
        try {
            byte[] payload = readFrame(exchange.requestBody());
            request = binding.runtime().deserialize(payload, binding.models().request());
            if (!binding.models().request().isInstance(request)) {
                throw new GrpcStatusException(STATUS_INVALID_ARGUMENT, "not a request message");
            }
        } catch (GrpcStatusException e) {
            answerWithStatus(exchange, e.status(), e.getMessage());
            return;
        } catch (IOException e) {
            LOG.log(Level.DEBUG, "cannot read the request body", e);
            answerWithStatus(exchange, STATUS_UNKNOWN, "cannot read the request");
            return;
        } catch (RuntimeException e) {
            LOG.log(Level.DEBUG, "cannot decode the request message", e);
            answerWithStatus(exchange, STATUS_INVALID_ARGUMENT, "cannot decode the request message");
            return;
        }

        try {
            // A gRPC call carries no transaction context, so it runs outside
            // one - and that has to be made true rather than assumed. Request
            // threads are pooled, and a branch that an earlier invocation left
            // on this one would otherwise become this bean's transaction, to be
            // committed or rolled back with someone else's work in it.
            transactions.detach();
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "cannot clear the transaction from the request thread", e);
            answerWithStatus(exchange, STATUS_UNKNOWN, "cannot prepare the request thread");
            return;
        }
        byte[] frame;
        try (Target target = targets.acquire(binding, path, exchange)) {
            Object response = binding.invoke(target.value(), request);
            frame = frame(binding.runtime().serialize(response));
        } catch (Throwable e) {
            if (e instanceof VirtualMachineError error) {
                // Out of memory or a broken VM: nothing here can answer for it.
                throw error;
            }
            // The call reached the bean and failed there: the client gets a
            // gRPC status, not a transport error.
            LOG.log(Level.DEBUG, "call failed: " + path, e);
            answerWithStatus(exchange, STATUS_UNKNOWN, e.toString());
            return;
        }

        exchange.setStatus(Protocol.SC_OK);
        exchange.setResponseHeader("Content-Type", GRPC_FORY_CONTENT_TYPE);
        exchange.setResponseTrailer(GRPC_STATUS, Integer.toString(STATUS_OK));
        exchange.writeBody(new ByteBuffer[] {ByteBuffer.wrap(frame)});
    }

    /** A gRPC content type is application/grpc, optionally with a +suffix or parameters. */
    static boolean isGrpc(String contentType) {
        if (contentType == null) {
            return false;
        }
        String value = contentType.trim().toLowerCase(Locale.ROOT);
        if (!value.startsWith(GRPC_CONTENT_TYPE)) {
            return false;
        }
        if (value.length() == GRPC_CONTENT_TYPE.length()) {
            return true;
        }
        char next = value.charAt(GRPC_CONTENT_TYPE.length());
        return next == '+' || next == ';';
    }

    /** A gRPC error is an HTTP 200 whose trailers carry the status. */
    private void answerWithStatus(ServerExchange exchange, int status, String message) throws IOException {
        exchange.setStatus(Protocol.SC_OK);
        exchange.setResponseHeader("Content-Type", GRPC_FORY_CONTENT_TYPE);
        exchange.setResponseTrailer(GRPC_STATUS, Integer.toString(status));
        if (message != null && !message.isEmpty()) {
            exchange.setResponseTrailer(GRPC_MESSAGE, percentEncode(capped(message)));
        }
        exchange.writeBody(new ByteBuffer[0]);
    }

    /** At most {@link #MAX_STATUS_MESSAGE_CHARS} characters, never ending in half a surrogate pair. */
    static String capped(String message) {
        if (message.length() <= MAX_STATUS_MESSAGE_CHARS) {
            return message;
        }
        int end = MAX_STATUS_MESSAGE_CHARS;
        if (Character.isHighSurrogate(message.charAt(end - 1))) {
            end--;
        }
        return message.substring(0, end);
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
            throw new GrpcStatusException(STATUS_INVALID_ARGUMENT, "missing gRPC frame");
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
        if (input.read() >= 0) {
            // A unary call is one message; whatever follows is not part of it.
            throw new GrpcStatusException(STATUS_INVALID_ARGUMENT, "unexpected data after the request message");
        }
        return payload;
    }

    private static int readByte(InputStream input) throws IOException {
        int value = input.read();
        if (value < 0) {
            throw new GrpcStatusException(STATUS_INVALID_ARGUMENT, "truncated gRPC frame header");
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

        /**
         * Obtains the target for a call that resolved to {@code binding}. A
         * provider that needs what the binding carries (see
         * {@link ForyGeneratedServiceRegistry.Binding#attachment()}) overrides
         * this, and so cannot be answered from a different generation of the
         * registry than the one the call was routed by.
         */
        default Target acquire(ForyGeneratedServiceRegistry.Binding binding, String path,
                               ServerExchange exchange) throws Exception {
            return acquire(path, exchange);
        }
    }

    public interface Target extends AutoCloseable {
        Object value();

        @Override
        void close();
    }
}
