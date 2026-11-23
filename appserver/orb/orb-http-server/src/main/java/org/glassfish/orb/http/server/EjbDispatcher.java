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
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.glassfish.orb.http.protocol.ChunkedOutput;
import org.glassfish.orb.http.protocol.ContentType;
import org.glassfish.orb.http.protocol.EjbKey;
import org.glassfish.orb.http.protocol.EjbRoutes;
import org.glassfish.orb.http.protocol.InvocationEnvelope;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.LimitedInputStream;
import org.glassfish.orb.http.protocol.Marshaller;
import org.glassfish.orb.http.protocol.Marshallers;
import org.glassfish.orb.http.protocol.PathScanner;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.ProtocolException;
import org.glassfish.orb.http.protocol.TxContext;
import org.glassfish.orb.http.protocol.Xids;

/**
 * Serves the EJB operations.
 * <p>
 * The dispatch is short because the hard part already exists: resolving a key
 * to a live target, switching to the deployment's class loader and releasing it
 * afterwards is {@link ContainerBridge}, which is the container's own
 * {@code EjbContainerFacade}. What is left here is decoding a path, decoding a
 * body, reflecting a method and encoding a reply.
 *
 * <p>The server accepts every codec it can find, the Fory codec included,
 * whenever its jar is present: there is no class-registration requirement and
 * no opt-in, and Fory's limits are approximate (see {@code ForyMarshaller}).
 * Every request body is capped at the transport's {@code maxBytes}.
 */
public final class EjbDispatcher {

    private final ContainerBridge container;
    private final SecurityBridge security;
    private final TransactionBridge transactions;
    private final Marshaller marshaller;
    private final InvocationRegistry registry;
    private final SessionAffinity affinity;

    /** A dispatcher with no security, no transactions and a generated route id. */
    public EjbDispatcher(ContainerBridge container) {
        this(container, SecurityBridge.NONE, TransactionBridge.NONE, Marshallers.preferred(),
                new InvocationRegistry(), SessionAffinity.forThisNode());
    }

    public EjbDispatcher(ContainerBridge container,
                         SecurityBridge security,
                         Marshaller marshaller,
                         InvocationRegistry registry) {
        this(container, security, TransactionBridge.NONE, marshaller, registry,
                SessionAffinity.forThisNode());
    }

    /**
     * @param container where invocations are dispatched
     * @param security establishes the caller's identity, or {@link SecurityBridge#NONE}
     * @param transactions imports the caller's transaction, or {@link TransactionBridge#NONE}
     * @param marshaller the codec this endpoint serves
     * @param registry tracks in-flight invocations so they can be cancelled
     * @param affinity pins stateful conversations to this node
     */
    public EjbDispatcher(ContainerBridge container,
                         SecurityBridge security,
                         TransactionBridge transactions,
                         Marshaller marshaller,
                         InvocationRegistry registry,
                         SessionAffinity affinity) {
        this.container = container;
        this.security = security;
        this.transactions = transactions;
        this.marshaller = marshaller;
        this.registry = registry;
        this.affinity = affinity;
    }

    public InvocationRegistry registry() {
        return registry;
    }

    /** Routes one request. */
    public void dispatch(ServerExchange exchange) throws IOException {
        PathScanner path;
        try {
            path = PathScanner.scan(exchange.pathBytes(), exchange.pathOffset(), exchange.pathLength());
        } catch (ProtocolException e) {
            fail(exchange, Protocol.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        // Routing compares against constants without materialising a String.
        if (!path.segmentEquals(EjbRoutes.IDX_SERVICE, Protocol.SVC_EJB)) {
            fail(exchange, Protocol.SC_NOT_FOUND, "not an EJB path");
            return;
        }
        if (!path.segmentEquals(EjbRoutes.IDX_VERSION, Protocol.VERSION_SEGMENT)) {
            fail(exchange, Protocol.SC_NOT_ACCEPTABLE, "unsupported protocol version");
            return;
        }

        try {
            if (path.segmentEquals(EjbRoutes.IDX_OPERATION, Protocol.OP_INVOKE)) {
                invoke(exchange, path);
            } else if (path.segmentEquals(EjbRoutes.IDX_OPERATION, Protocol.OP_OPEN)) {
                open(exchange, path);
            } else if (path.segmentEquals(EjbRoutes.IDX_OPERATION, Protocol.OP_REMOVE)) {
                remove(exchange, path);
            } else if (path.segmentEquals(EjbRoutes.IDX_OPERATION, Protocol.OP_CANCEL)) {
                cancel(exchange, path);
            } else {
                fail(exchange, Protocol.SC_NOT_FOUND, "unknown EJB operation");
            }
        } catch (ProtocolException e) {
            fail(exchange, Protocol.SC_BAD_REQUEST, e.getMessage());
        }
    }

    // ---- invoke ----------------------------------------------------------

    private void invoke(ServerExchange exchange, PathScanner path) throws IOException {
        if (!"POST".equals(exchange.method())) {
            fail(exchange, Protocol.SC_BAD_REQUEST, "invoke must be a POST");
            return;
        }
        ContentType contentType;
        try {
            contentType = ContentType.parse(exchange.requestHeader("Content-Type"));
        } catch (ProtocolException e) {
            fail(exchange, Protocol.SC_BAD_REQUEST, e.getMessage());
            return;
        }
        if (!contentType.isVersionSupported()) {
            fail(exchange, Protocol.SC_NOT_ACCEPTABLE, "unsupported protocol version " + contentType.version());
            return;
        }
        // A server speaks every codec it can find, not just the one it would
        // have chosen itself. That is what lets a client pick a faster codec
        // unilaterally: the two ends only have to overlap, not to agree in
        // advance. The reply is written with the codec the request used, so a
        // client never has to decode something it did not ask for.
        Marshaller codec = marshaller.codec().equals(contentType.codec())
                ? marshaller
                : Marshallers.find(contentType.codec()).orElse(null);
        if (codec == null) {
            fail(exchange, Protocol.SC_NOT_ACCEPTABLE, "unsupported codec " + contentType.codec()
                    + "; this server speaks " + marshaller.codec()
                    + ", " + String.join(", ", Marshallers.codecs()));
            return;
        }

        EjbRoutes.Invocation invocation = EjbRoutes.parseInvocation(path);
        byte[] sessionId = EjbRoutes.decodeSessionId(invocation.sessionId());

        EjbKey key;
        try {
            key = container.resolve(invocation.appName(), invocation.moduleName(),
                    invocation.distinctName(), invocation.beanName(), sessionId);
        } catch (ContainerBridge.NoSuchTargetException e) {
            fail(exchange, Protocol.SC_NOT_FOUND, e.getMessage());
            return;
        }

        ClassLoader loader = container.classLoader(key);
        String invocationId = exchange.requestHeader(Protocol.H_INVOCATION_ID);
        Object securityToken;
        try {
            securityToken = security.establish(exchange.authenticatedUser());
        } catch (SecurityException e) {
            // The credential was offered and refused. Saying so is the point:
            // running the call anonymously would answer a caller that asked to
            // be someone with the rights of no one.
            fail(exchange, Protocol.SC_FORBIDDEN, "not authorized");
            return;
        }

        try (InvocationRegistry.Registration registration = registry.register(invocationId)) {
            Object target = null;
            Method invokedMethod = null;
            Imported imported = null;
            try {
                Method method = resolveMethod(loader, invocation);
                invokedMethod = method;
                // Capped: the filter is never asked about a string or about block data,
                // so a body of one enormous string would otherwise be read whole.
                InputStream body = LimitedInputStream.bound(exchange.requestBody());
                TxContext txContext;
                Object[] args;
                try {
                    txContext = InvocationEnvelope.readTxContext(body);
                    args = readArguments(codec, body, loader, method.getParameterCount());
                } catch (ProtocolException e) {
                    // Already a statement about the protocol; dispatch answers 400.
                    throw e;
                } catch (ClassNotFoundException e) {
                    // The caller sent a class this application does not have.
                    // That is a bad request, not a missing bean.
                    registration.finish();
                    failRead(exchange, registration, "unknown class in the request body: " + e.getMessage());
                    return;
                } catch (IOException | RuntimeException | LinkageError e) {
                    if (e instanceof ExceptionInInitializerError || e instanceof NoClassDefFoundError) {
                        // A class that cannot initialise (the first time) or is
                        // known not to (every time after) is the server's
                        // problem, not a malformed body; the same rule as the
                        // naming dispatcher.
                        throw (Error) e;
                    }
                    // A body that does not decode - truncated, corrupt, refused by
                    // the filter, or written by another codec. The caller's fault,
                    // so not a 500 with a stack trace - unless the caller cancelled
                    // while it was being read, which is what interrupted the read.
                    registration.finish();
                    failRead(exchange, registration, "malformed request body: " + e.getClass().getSimpleName());
                    return;
                }

                // Import before resolving the target, so the container sees the
                // transaction while it sets the invocation up rather than after.
                imported = importTransaction(txContext, exchange, key, method);

                target = container.getTargetObject(key, invocation.viewClass());
                Object result = unwrapAsyncResult(callTarget(target, method, args));
                // Over: a cancel arriving now must not leave an interrupt on
                // this thread while the response is written.
                registration.finish();

                if (registration.isCancelled()) {
                    // The result is discarded on purpose: the caller has said
                    // it no longer wants it, and reporting success would be a lie.
                    fail(exchange, Protocol.SC_CANCELLED, "invocation cancelled");
                    return;
                }
                writeResult(codec, exchange, result);

            } catch (BadTimeoutException e) {
                fail(exchange, Protocol.SC_BAD_REQUEST, e.getMessage());
            } catch (ContainerBridge.NoSuchTargetException e) {
                registration.finish();
                fail(exchange, Protocol.SC_NOT_FOUND, e.getMessage());
            } catch (InvocationTargetException e) {
                registration.finish();
                if (registration.isCancelled()) {
                    // The caller cancelled and this is what the cancellation did
                    // to the bean - typically an InterruptedException. Reporting
                    // it as the bean's own failure would be a lie, exactly as
                    // reporting a result would be.
                    fail(exchange, Protocol.SC_CANCELLED, "invocation cancelled");
                } else {
                    // The bean threw. Whether the caller sees that exception or an
                    // EJBException is not ours to choose: the specification decides,
                    // and getting it wrong would change the exception contract of an
                    // application that was only meant to change transport.
                    Throwable thrown = e.getCause() != null ? e.getCause() : e;
                    writeException(codec, exchange, EjbExceptions.toClientException(thrown, invokedMethod));
                }
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                registration.finish();
                fail(exchange, Protocol.SC_NOT_FOUND, e.toString());
            } catch (IllegalAccessException e) {
                registration.finish();
                fail(exchange, Protocol.SC_FORBIDDEN, e.toString());
            } catch (TransactionBridge.TransactionException e) {
                exchange.setStatus(Protocol.SC_EXCEPTION);
                exchange.setResponseHeader(TransactionDispatcher.H_ERROR_CODE,
                        Integer.toString(e.errorCode()));
                exchange.setResponseHeader("X-GF-Reason",
                        String.valueOf(e.getMessage()).replace('\n', ' ').replace('\r', ' '));
            } finally {
                try {
                    if (target != null) {
                        container.releaseTargetObject(target);
                    }
                } finally {
                    // Release after the container is done with the target, and
                    // whatever happened above - including the container failing
                    // to release the target: a branch left associated with this
                    // thread would leak into whatever the pool runs next.
                    releaseTransaction(imported);
                }
            }
        } finally {
            security.clear(securityToken);
        }
    }

    private Method resolveMethod(ClassLoader loader, EjbRoutes.Invocation invocation)
            throws ClassNotFoundException, NoSuchMethodException {
        Class<?> viewClass = Class.forName(invocation.viewClass(), false, loader);
        String[] names = invocation.paramTypes();
        Class<?>[] parameterTypes = new Class<?>[names.length];
        for (int i = 0; i < names.length; i++) {
            parameterTypes[i] = resolveType(names[i], loader);
        }
        return viewClass.getMethod(invocation.methodName(), parameterTypes);
    }

    /** Resolves a parameter type name, including primitives and arrays. */
    static Class<?> resolveType(String name, ClassLoader loader) throws ClassNotFoundException {
        return switch (name) {
            case "boolean" -> boolean.class;
            case "byte" -> byte.class;
            case "char" -> char.class;
            case "short" -> short.class;
            case "int" -> int.class;
            case "long" -> long.class;
            case "float" -> float.class;
            case "double" -> double.class;
            case "void" -> void.class;
            default -> Class.forName(name, false, loader);
        };
    }

    private Object callTarget(Object target, Method method, Object[] args)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, ProtocolException {
        Method callable = method;
        if (!method.getDeclaringClass().isInstance(target)) {
            // The container handed back an object that implements the view
            // through a generated interface rather than the view itself.
            callable = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        }
        callable.setAccessible(true);
        try {
            return callable.invoke(target, args);
        } catch (IllegalArgumentException e) {
            // Method.invoke refusing the arguments it was given - a wrong
            // number or a wrong type - before the method body was entered.
            // Only this call is mapped: an IllegalArgumentException from
            // anywhere else in the dispatch is not the caller's arguments.
            throw new ProtocolException("arguments do not match the method");
        }
    }

    /**
     * Unwraps the result of an asynchronous business method.
     *
     * <p>An {@code @Asynchronous} method is declared to return {@code Future<V>},
     * but the Future is the container's promise to its <em>caller</em> - it is
     * not the value, and it is not serializable. What crosses the wire is V,
     * and the client re-wraps it, so the caller still gets a Future it never
     * had to block on.
     *
     * <p>This does block the dispatching thread on the bean's own future. That
     * is the honest cost of having no way to hand the caller a result later:
     * the alternative, answering 202 and dropping the value, would silently
     * turn every asynchronous method into a fire-and-forget one.
     *
     * @param result whatever the business method returned
     * @return the value to marshal back
     */
    private Object unwrapAsyncResult(Object result) throws InvocationTargetException {
        if (!(result instanceof Future<?> future)) {
            return result;
        }
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InvocationTargetException(e);
        } catch (ExecutionException e) {
            // The bean's failure, not ours: rethrow it as though the method had
            // thrown directly, so the caller sees its own exception.
            throw new InvocationTargetException(e.getCause() != null ? e.getCause() : e);
        }
    }

    /** The caller's transaction timeout header could not be taken at its word. */
    private static final class BadTimeoutException extends Exception {

        private static final long serialVersionUID = 1L;

        BadTimeoutException(String message) {
            super(message);
        }
    }

    /**
     * Reads the caller's remaining transaction time.
     *
     * @return the timeout in seconds, 0 when the caller sent none
     * @throws BadTimeoutException if the header is not a number of seconds a
     *         transaction manager can take: a negative or oversized value would
     *         otherwise reach {@code recreate} as the caller wrote it
     */
    private static long requestedTimeout(ServerExchange exchange) throws BadTimeoutException {
        String header = exchange.requestHeader(Protocol.H_TXN_TIMEOUT);
        if (header == null) {
            return 0;
        }
        long timeout;
        try {
            timeout = Long.parseLong(header.trim());
        } catch (NumberFormatException e) {
            throw new BadTimeoutException(Protocol.H_TXN_TIMEOUT + " must be a number of seconds");
        }
        if (timeout < 0 || timeout > Integer.MAX_VALUE) {
            throw new BadTimeoutException(Protocol.H_TXN_TIMEOUT + " is out of range: " + timeout);
        }
        return timeout;
    }

    /**
     * Imports the caller's transaction, if the invocation carries one.
     *
     * @return the branch that was imported, or null if there was none
     */
    private Imported importTransaction(TxContext txContext, ServerExchange exchange, EjbKey key, Method method)
            throws TransactionBridge.TransactionException, BadTimeoutException {
        String marker = exchange.requestHeader(Protocol.H_TXN_ROLLBACK_ONLY);
        boolean rollbackOnly = marker != null && "true".equalsIgnoreCase(marker.trim());
        if (!txContext.isPresent() && !rollbackOnly) {
            // Deliberately outside a transaction, so make that true: this
            // thread has served other requests, and one of them may have left
            // a branch on it.
            transactions.detach();
            return null;
        }
        Xid xid = Xids.fromContext(txContext);
        long timeout;
        try {
            timeout = requestedTimeout(exchange);
        } catch (BadTimeoutException e) {
            // Refusing the request must not leave the previous one's
            // association on this pooled thread. Nothing else has detached yet.
            transactions.detach();
            throw e;
        }
        if (xid == null) {
            // A rollback-only call with no branch to import: whatever a
            // previous request left on this thread must not be seen.
            transactions.detach();
        }
        try {
            if (rollbackOnly) {
                Object handle = transactions.recreateRollbackOnly(xid, timeout, key, method);
                if (handle == null) {
                    // The contract is a non-null handle. A bridge that returns
                    // none may have begun a transaction on this thread that
                    // nothing will now release, so refuse and leave it clean.
                    throw new TransactionBridge.TransactionException(
                            "the transaction bridge returned no handle for a rollback-only import",
                            XAException.XAER_RMERR);
                }
                return new Imported(xid, handle, true);
            }
            transactions.recreate(xid, timeout);
            return new Imported(xid, null, false);
        } catch (TransactionBridge.TransactionException | RuntimeException e) {
            // A manager that fails halfway may already have associated the
            // thread, and nothing will release a branch that was never
            // returned. Leave the thread with no transaction instead.
            try {
                transactions.detach();
            } catch (RuntimeException detachFailure) {
                e.addSuppressed(detachFailure);
            }
            throw e;
        }
    }

    /**
     * What an import left to be undone.
     *
     * @param xid    the caller's branch, or null if it sent none
     * @param handle what {@link TransactionBridge#recreateRollbackOnly} returned, or null for an
     *               ordinary import
     * @param rollbackOnly whether this was a rollback-only import, which is released by
     *               handle and never by xid
     */
    private record Imported(Xid xid, Object handle, boolean rollbackOnly) {
    }

    private void releaseTransaction(Imported imported) {
        if (imported == null) {
            return;
        }
        try {
            if (imported.rollbackOnly()) {
                // Whatever the bridge associated for a rollback-only call is
                // the bridge's to undo, and release(xid) is not the way: it
                // may never have associated that xid.
                transactions.releaseRollbackOnly(imported.handle());
            } else if (imported.xid() != null) {
                transactions.release(imported.xid());
            }
        } catch (TransactionBridge.TransactionException | RuntimeException e) {
            // The invocation's own outcome has already been written. Losing
            // the release is bad, but overwriting a successful reply with this
            // failure would be worse - recovery exists for exactly this. What
            // the failed release may have left on the thread is not to be
            // inherited by the next request.
            try {
                transactions.detach();
            } catch (RuntimeException detachFailure) {
                e.addSuppressed(detachFailure);
            }
            System.getLogger(EjbDispatcher.class.getName()).log(System.Logger.Level.WARNING,
                    "failed to release imported transaction "
                            + (imported.xid() == null ? "(local)" : Xids.key(imported.xid())), e);
        }
    }

    private Object[] readArguments(Marshaller codec, InputStream body, ClassLoader loader, int count)
            throws IOException, ClassNotFoundException {
        ObjectInputFilter filter = JavaSerializationMarshaller.defaultFilter();
        try (Marshaller.ObjectReader reader = codec.newReader(body, loader, filter)) {
            Object[] args = new Object[count];
            for (int i = 0; i < count; i++) {
                args[i] = reader.readObject();
            }
            // The attachments map follows the arguments; read it so the stream
            // is fully consumed even though nothing uses it yet.
            reader.readObject();
            return args;
        }
    }

    // ---- open ------------------------------------------------------------

    private void open(ServerExchange exchange, PathScanner path) throws IOException {
        if (!"POST".equals(exchange.method())) {
            fail(exchange, Protocol.SC_BAD_REQUEST, "open must be a POST");
            return;
        }
        if (path.count() != EjbRoutes.OPEN_SEGMENTS) {
            fail(exchange, Protocol.SC_BAD_REQUEST, "malformed open path");
            return;
        }
        asCaller(exchange, () -> {
            try {
                byte[] sessionId = container.createSession(
                        path.segment(EjbRoutes.IDX_APP),
                        path.segment(EjbRoutes.IDX_MODULE),
                        path.optionalSegment(EjbRoutes.IDX_DISTINCT),
                        path.segment(EjbRoutes.IDX_BEAN));
                exchange.setResponseHeader(Protocol.H_SESSION_ID, EjbRoutes.encodeSessionId(sessionId));
                // Pin the conversation here as well as at the affinity endpoint.
                // The session lives in this instance's memory, so every later
                // invocation quoting this id has to come back to this node; a
                // client that never called the affinity endpoint would otherwise
                // have nothing telling the load balancer that.
                affinity.applyTo(exchange, AffinityDispatcher.contextPathOf(path));
                exchange.setStatus(Protocol.SC_NO_CONTENT);
            } catch (ContainerBridge.NoSuchTargetException e) {
                fail(exchange, Protocol.SC_NOT_FOUND, e.getMessage());
            } catch (ContainerBridge.SessionRefusedException e) {
                // The bean is there but will not open a session. Not a 404:
                // that status means no such EJB or view, and a client acting
                // on it would look for the wrong thing.
                fail(exchange, Protocol.SC_EXCEPTION, e.getMessage());
            }
        });
    }

    // ---- remove ----------------------------------------------------------

    private void remove(ServerExchange exchange, PathScanner path) throws IOException {
        if (!"DELETE".equals(exchange.method())) {
            fail(exchange, Protocol.SC_BAD_REQUEST, "remove must be a DELETE");
            return;
        }
        if (path.count() != EjbRoutes.IDX_SESSION + 1) {
            fail(exchange, Protocol.SC_BAD_REQUEST, "malformed remove path");
            return;
        }
        asCaller(exchange, () -> {
            try {
                byte[] sessionId = EjbRoutes.decodeSessionId(path.segment(EjbRoutes.IDX_SESSION));
                if (sessionId == null) {
                    fail(exchange, Protocol.SC_BAD_REQUEST, "remove requires a session id");
                    return;
                }
                EjbKey key = container.resolve(
                        path.segment(EjbRoutes.IDX_APP),
                        path.segment(EjbRoutes.IDX_MODULE),
                        path.optionalSegment(EjbRoutes.IDX_DISTINCT),
                        path.segment(EjbRoutes.IDX_BEAN),
                        sessionId);
                container.removeSession(key);
                exchange.setStatus(Protocol.SC_NO_CONTENT);
            } catch (ContainerBridge.NoSuchTargetException e) {
                // Removing something already gone is the outcome the caller wanted.
                exchange.setStatus(Protocol.SC_NO_CONTENT);
            } catch (ProtocolException e) {
                fail(exchange, Protocol.SC_BAD_REQUEST, e.getMessage());
            }
        });
    }

    // ---- cancel ----------------------------------------------------------

    private void cancel(ServerExchange exchange, PathScanner path) throws IOException {
        if (!"DELETE".equals(exchange.method())) {
            fail(exchange, Protocol.SC_BAD_REQUEST, "cancel must be a DELETE");
            return;
        }
        int last = path.count() - 1;
        if (last < EjbRoutes.IDX_BEAN + 2) {
            fail(exchange, Protocol.SC_BAD_REQUEST, "malformed cancel path");
            return;
        }
        boolean interrupt = Boolean.parseBoolean(path.segment(last));
        String invocationId = path.segment(last - 1);
        asCaller(exchange, () -> {
            registry.cancel(invocationId, interrupt);
            // Always 204: whether the invocation was still running is a race the
            // client cannot act on, and reporting 404 would invite a pointless retry.
            exchange.setStatus(Protocol.SC_NO_CONTENT);
        });
    }

    /** What runs while the caller is the current identity. */
    @FunctionalInterface
    private interface CallerAction {
        void run() throws IOException;
    }

    /**
     * Runs {@code action} as the caller, the way {@link #invoke} does.
     *
     * <p>The container reads the identity from the thread, and a Grizzly worker
     * keeps whatever the request before it left there, so an operation that
     * reaches the container without establishing its caller runs as a stranger
     * and ignores a credential that was offered and refused. A refused
     * credential answers 403 with a fixed reason and the action never runs.
     * Whatever the action does, even throwing, the thread is cleared again.
     */
    private void asCaller(ServerExchange exchange, CallerAction action) throws IOException {
        Object securityToken;
        try {
            securityToken = security.establish(exchange.authenticatedUser());
        } catch (SecurityException e) {
            fail(exchange, Protocol.SC_FORBIDDEN, "not authorized");
            return;
        }
        try {
            action.run();
        } finally {
            security.clear(securityToken);
        }
    }

    // ---- responses --------------------------------------------------------

    private void writeResult(Marshaller codec, ServerExchange exchange, Object result) throws IOException {
        ByteBuffer[] body = marshal(codec, result);
        exchange.setStatus(Protocol.SC_OK);
        exchange.setResponseHeader("Content-Type",
                ContentType.of(codec.codec(), ContentType.KIND_RESPONSE).toHeaderValue());
        exchange.writeBody(body);
    }

    private void writeException(Marshaller codec, ServerExchange exchange, Throwable thrown) throws IOException {
        ByteBuffer[] body;
        try {
            body = marshal(codec, thrown);
        } catch (IOException e) {
            // The application exception is not serializable. Do not lose the
            // failure: report something that is.
            body = marshal(codec, new java.rmi.RemoteException(
                    "server threw " + thrown.getClass().getName() + " which is not serializable: " + thrown));
        }
        exchange.setStatus(Protocol.SC_EXCEPTION);
        exchange.setResponseHeader("Content-Type",
                ContentType.of(codec.codec(), ContentType.KIND_EXCEPTION).toHeaderValue());
        exchange.writeBody(body);
    }

    private ByteBuffer[] marshal(Marshaller codec, Object value) throws IOException {
        ChunkedOutput out = new ChunkedOutput();
        try (Marshaller.ObjectWriter writer = codec.newWriter(out)) {
            writer.writeObject(value);
            writer.writeObject(new HashMap<String, Object>());
            writer.flush();
        }
        return out.toByteBuffers();
    }

    private static void failRead(ServerExchange exchange, InvocationRegistry.Registration registration,
                                 String reason) {
        if (registration.isCancelled()) {
            fail(exchange, Protocol.SC_CANCELLED, "invocation cancelled");
        } else {
            fail(exchange, Protocol.SC_BAD_REQUEST, reason);
        }
    }

    private static void fail(ServerExchange exchange, int status, String reason) {
        exchange.setStatus(status);
        if (reason != null) {
            // A short diagnostic header, never a marshalled body: a protocol
            // level failure must be readable by an intermediary too.
            exchange.setResponseHeader("X-GF-Reason", reason.replace('\n', ' ').replace('\r', ' '));
        }
    }

    /** Exposed for the naming dispatcher, which shares the codec. */
    Map<String, Object> emptyAttachments() {
        return new HashMap<>();
    }
}
