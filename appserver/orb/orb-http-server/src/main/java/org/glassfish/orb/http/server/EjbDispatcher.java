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

import org.glassfish.orb.http.protocol.ChunkedOutput;
import org.glassfish.orb.http.protocol.ContentType;
import org.glassfish.orb.http.protocol.EjbKey;
import org.glassfish.orb.http.protocol.EjbRoutes;
import org.glassfish.orb.http.protocol.InvocationEnvelope;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Marshaller;
import org.glassfish.orb.http.protocol.PathScanner;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.ProtocolException;
import org.glassfish.orb.http.protocol.TxContext;

/**
 * Serves the EJB operations.
 * <p>
 * The dispatch is short because the hard part already exists: resolving a key
 * to a live target, switching to the deployment's class loader and releasing it
 * afterwards is {@link ContainerBridge}, which is the container's own
 * {@code EjbContainerFacade}. What is left here is decoding a path, decoding a
 * body, reflecting a method and encoding a reply.
 */
public final class EjbDispatcher {

    private final ContainerBridge container;
    private final SecurityBridge security;
    private final Marshaller marshaller;
    private final InvocationRegistry registry;
    private final SessionAffinity affinity;

    public EjbDispatcher(ContainerBridge container) {
        this(container, SecurityBridge.NONE, new JavaSerializationMarshaller(),
                new InvocationRegistry(), SessionAffinity.forThisNode());
    }

    public EjbDispatcher(ContainerBridge container,
                         SecurityBridge security,
                         Marshaller marshaller,
                         InvocationRegistry registry) {
        this(container, security, marshaller, registry, SessionAffinity.forThisNode());
    }

    public EjbDispatcher(ContainerBridge container,
                         SecurityBridge security,
                         Marshaller marshaller,
                         InvocationRegistry registry,
                         SessionAffinity affinity) {
        this.container = container;
        this.security = security;
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
        if (!marshaller.codec().equals(contentType.codec())) {
            fail(exchange, Protocol.SC_NOT_ACCEPTABLE, "unsupported codec " + contentType.codec());
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
        Object securityToken = security.establish(exchange.authenticatedUser());

        try (InvocationRegistry.Registration registration = registry.register(invocationId)) {
            Object target = null;
            try {
                Method method = resolveMethod(loader, invocation);
                Object[] args = readArguments(exchange.requestBody(), loader, method.getParameterCount());

                target = container.getTargetObject(key, invocation.viewClass());
                Object result = unwrapAsyncResult(callTarget(target, method, args));

                if (registration.isCancelled()) {
                    // The result is discarded on purpose: the caller has said
                    // it no longer wants it, and reporting success would be a lie.
                    fail(exchange, Protocol.SC_CANCELLED, "invocation cancelled");
                    return;
                }
                writeResult(exchange, result);

            } catch (ContainerBridge.NoSuchTargetException e) {
                fail(exchange, Protocol.SC_NOT_FOUND, e.getMessage());
            } catch (InvocationTargetException e) {
                // The application threw. Send the exception itself, with its
                // type, cause chain and server stack trace intact.
                writeException(exchange, e.getCause() != null ? e.getCause() : e);
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                fail(exchange, Protocol.SC_NOT_FOUND, e.toString());
            } catch (IllegalAccessException e) {
                fail(exchange, Protocol.SC_FORBIDDEN, e.toString());
            } finally {
                if (target != null) {
                    container.releaseTargetObject(target);
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
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method callable = method;
        if (!method.getDeclaringClass().isInstance(target)) {
            // The container handed back an object that implements the view
            // through a generated interface rather than the view itself.
            callable = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        }
        callable.setAccessible(true);
        return callable.invoke(target, args);
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

    private Object[] readArguments(InputStream body, ClassLoader loader, int count)
            throws IOException, ClassNotFoundException {
        TxContext tx = InvocationEnvelope.readTxContext(body);
        if (tx.isPresent()) {
            // Reserved on the wire, not yet honoured. Refusing loudly is
            // better than silently running the call outside the caller's
            // transaction and reporting success.
            throw new ProtocolException("transaction propagation is not implemented by this transport");
        }
        ObjectInputFilter filter = JavaSerializationMarshaller.defaultFilter();
        try (Marshaller.ObjectReader reader = marshaller.newReader(body, loader, filter)) {
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
        }
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
        registry.cancel(invocationId, interrupt);
        // Always 204: whether the invocation was still running is a race the
        // client cannot act on, and reporting 404 would invite a pointless retry.
        exchange.setStatus(Protocol.SC_NO_CONTENT);
    }

    // ---- responses --------------------------------------------------------

    private void writeResult(ServerExchange exchange, Object result) throws IOException {
        ByteBuffer[] body = marshal(result);
        exchange.setStatus(Protocol.SC_OK);
        exchange.setResponseHeader("Content-Type",
                ContentType.of(marshaller.codec(), ContentType.KIND_RESPONSE).toHeaderValue());
        exchange.writeBody(body);
    }

    private void writeException(ServerExchange exchange, Throwable thrown) throws IOException {
        ByteBuffer[] body;
        try {
            body = marshal(thrown);
        } catch (IOException e) {
            // The application exception is not serializable. Do not lose the
            // failure: report something that is.
            body = marshal(new java.rmi.RemoteException(
                    "server threw " + thrown.getClass().getName() + " which is not serializable: " + thrown));
        }
        exchange.setStatus(Protocol.SC_EXCEPTION);
        exchange.setResponseHeader("Content-Type",
                ContentType.of(marshaller.codec(), ContentType.KIND_EXCEPTION).toHeaderValue());
        exchange.writeBody(body);
    }

    private ByteBuffer[] marshal(Object value) throws IOException {
        ChunkedOutput out = new ChunkedOutput();
        try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
            writer.writeObject(value);
            writer.writeObject(new HashMap<String, Object>());
            writer.flush();
        }
        return out.toByteBuffers();
    }

    private static void fail(ServerExchange exchange, int status, String reason) {
        exchange.setStatus(status);
        if (reason != null) {
            // A short diagnostic header, never a marshalled body: a protocol
            // level failure must be readable by an intermediary too.
            exchange.setResponseHeader("X-GF-Reason", reason.replace('\n', ' '));
        }
    }

    /** Exposed for the naming dispatcher, which shares the codec. */
    Map<String, Object> emptyAttachments() {
        return new HashMap<>();
    }
}
