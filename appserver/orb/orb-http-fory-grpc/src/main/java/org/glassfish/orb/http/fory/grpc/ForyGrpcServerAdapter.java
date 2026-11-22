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
import java.util.regex.Pattern;

import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.server.ContainerBridge;
import org.glassfish.orb.http.server.ServerExchange;
import org.glassfish.orb.http.server.TransactionBridge;

/** Bridges Grizzly's HTTP exchange to a registered Fory service binding. */
public final class ForyGrpcServerAdapter {

    static final String GRPC_STATUS = "grpc-status";
    static final String GRPC_MESSAGE = "grpc-message";
    static final String GRPC_FORY_CONTENT_TYPE = "application/grpc+fory";

    /** The status codes this adapter can produce; see the gRPC status definitions. */
    static final int STATUS_OK = 0;
    static final int STATUS_CANCELLED = 1;
    static final int STATUS_UNKNOWN = 2;
    static final int STATUS_INVALID_ARGUMENT = 3;
    static final int STATUS_NOT_FOUND = 5;
    static final int STATUS_PERMISSION_DENIED = 7;
    static final int STATUS_RESOURCE_EXHAUSTED = 8;
    static final int STATUS_UNIMPLEMENTED = 12;
    static final int STATUS_INTERNAL = 13;
    static final int STATUS_UNAUTHENTICATED = 16;

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    private static final Logger LOG = System.getLogger(ForyGrpcServerAdapter.class.getName());

    /**
     * The longest status message sent, in characters. A bean's exception can
     * carry anything, and the message travels in a header.
     */
    static final int MAX_STATUS_MESSAGE_CHARS = 512;

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
        if (Thread.currentThread().isInterrupted()) {
            // Interrupted while waiting for the index: the flag stays set, and
            // nothing more is read or run for this call.
            answerWithStatus(exchange, STATUS_CANCELLED, "interrupted");
            return;
        }
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
            // The client went away, or the connection broke, mid-body.
            LOG.log(Level.DEBUG, "cannot read the request body", e);
            answerWithStatus(exchange, STATUS_CANCELLED, "cannot read the request");
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
        Target target;
        try {
            target = targets.acquire(binding, path, exchange);
        } catch (Throwable e) {
            if (e instanceof VirtualMachineError error) {
                throw error;
            }
            // The container, not the bean: the caller is told in general terms
            // and the cause goes to the log.
            if (e instanceof ContainerBridge.NoSuchTargetException) {
                LOG.log(Level.DEBUG, "no target for " + path, e);
                answerWithStatus(exchange, STATUS_NOT_FOUND, "no such bean");
            } else if (e instanceof SecurityException) {
                // What the realm raises for a credential it refused: the
                // caller's mistake, not a fault of the server, so it is not a
                // warning and it is not an internal error. The text is the
                // realm's and is not sent.
                LOG.log(Level.DEBUG, "credential refused for " + path, e);
                answerWithStatus(exchange, STATUS_UNAUTHENTICATED, "authentication failed");
            } else {
                LOG.log(Level.WARNING, "cannot reach the bean for " + path, e);
                answerWithStatus(exchange, STATUS_INTERNAL, "cannot reach the bean");
            }
            return;
        }
        Object response = null;
        Throwable failure = null;
        try {
            response = binding.invoke(target.value(), request);
        } catch (Throwable e) {
            if (e instanceof VirtualMachineError error) {
                // Out of memory or a broken VM: nothing here can answer for it.
                throw error;
            }
            failure = e;
        }
        // Releasing the target is the container's business: a failure of it is
        // logged, and neither changes the answer to a call that has run nor
        // gets its text sent to the caller.
        try {
            target.close();
        } catch (Throwable e) {
            if (e instanceof VirtualMachineError error) {
                throw error;
            }
            LOG.log(Level.WARNING, "cannot release the target of " + path, e);
        }
        if (failure != null) {
            answerWithFailure(exchange, path, binding, failure);
            return;
        }
        byte[] frame;
        try {
            frame = frame(binding.runtime().serialize(response));
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "cannot encode the response of " + path, e);
            answerWithStatus(exchange, STATUS_INTERNAL, "cannot encode the response");
            return;
        }

        exchange.setStatus(Protocol.SC_OK);
        exchange.setResponseHeader("Content-Type", GRPC_FORY_CONTENT_TYPE);
        exchange.setResponseTrailer(GRPC_STATUS, Integer.toString(STATUS_OK));
        exchange.writeBody(new ByteBuffer[] {ByteBuffer.wrap(frame)});
    }

    /**
     * Answers a call that failed. Only what the bean itself threw, and what
     * its method declares, is passed on with its text; whatever the container
     * or this machinery threw is reported in general terms, and the cause
     * goes to the log, because their messages name classes, loaders and hosts.
     */
    private void answerWithFailure(ServerExchange exchange, String path,
                                   ForyGeneratedServiceRegistry.Binding binding, Throwable failure)
            throws IOException {
        if (failure instanceof ForyGrpcCallException || failure instanceof NoSuchMethodException
                || failure instanceof Error || failure instanceof java.rmi.RemoteException
                || failure instanceof java.lang.reflect.UndeclaredThrowableException) {
            LOG.log(Level.WARNING, "call failed inside the endpoint: " + path, failure);
            answerWithStatus(exchange, STATUS_INTERNAL, "internal error");
            return;
        }
        if (causedBy(failure, ACCESS_REFUSED)) {
            LOG.log(Level.DEBUG, "call refused: " + path, failure);
            answerWithStatus(exchange, STATUS_PERMISSION_DENIED, "access denied");
            return;
        }
        if (causedBy(failure, NO_SUCH_BEAN)) {
            LOG.log(Level.DEBUG, "no such bean: " + path, failure);
            answerWithStatus(exchange, STATUS_NOT_FOUND, "no such bean");
            return;
        }
        if (failure instanceof SecurityException) {
            answerWithStatus(exchange, STATUS_PERMISSION_DENIED, failure.toString());
            return;
        }
        if (isA(failure.getClass(), "jakarta.ejb.EJBException")) {
            LOG.log(Level.WARNING, "container failure in " + path, failure);
            answerWithStatus(exchange, STATUS_INTERNAL, "internal error");
            return;
        }
        if (failure instanceof RuntimeException || binding.skeleton().declares(binding.skeletonPath(), failure)) {
            LOG.log(Level.DEBUG, "call failed: " + path, failure);
            answerWithStatus(exchange, STATUS_UNKNOWN, failure.toString());
            return;
        }
        LOG.log(Level.WARNING, "call failed with an undeclared " + failure.getClass().getName() + ": " + path,
                failure);
        answerWithStatus(exchange, STATUS_INTERNAL, "internal error");
    }

    /** The exceptions with which the container says a call was refused. */
    private static final String[] ACCESS_REFUSED = {"jakarta.ejb.EJBAccessException", "jakarta.ejb.AccessLocalException"};

    /** The ones with which it says the bean is not there. */
    private static final String[] NO_SUCH_BEAN = {"jakarta.ejb.NoSuchEJBException",
        "jakarta.ejb.NoSuchObjectLocalException"};

    /** Whether the failure, or what it was caused by, is one of the named exceptions or a subclass of one. */
    private static boolean causedBy(Throwable failure, String[] names) {
        Throwable current = failure;
        for (int depth = 0; current != null && depth < 8; depth++, current = current.getCause()) {
            for (String name : names) {
                if (isA(current.getClass(), name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isA(Class<?> type, String name) {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            if (c.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /** application/grpc, optionally with a +subtype and then parameters, and nothing else. */
    private static final Pattern GRPC_CONTENT_TYPE_FORM =
            Pattern.compile("application/grpc(\\+[a-z0-9][a-z0-9.\\-]*)?(\\s*;.*)?");

    static boolean isGrpc(String contentType) {
        return contentType != null
                && GRPC_CONTENT_TYPE_FORM.matcher(contentType.trim().toLowerCase(Locale.ROOT)).matches();
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
