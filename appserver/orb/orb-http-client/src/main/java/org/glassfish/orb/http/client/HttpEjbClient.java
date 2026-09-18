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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.transaction.xa.Xid;

import org.glassfish.orb.http.protocol.ChunkedOutput;
import org.glassfish.orb.http.protocol.CommonRoutes;
import org.glassfish.orb.http.protocol.ContentType;
import org.glassfish.orb.http.protocol.EjbRoutes;
import org.glassfish.orb.http.protocol.InvocationEnvelope;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Marshaller;
import org.glassfish.orb.http.protocol.Marshallers;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.TxContext;
import org.glassfish.orb.http.protocol.Xids;

/**
 * The client-side entry point: turns a remote business interface into a proxy
 * that invokes over HTTP.
 *
 * <h2>Why a proxy rather than a stub</h2>
 * The IIOP path builds a stub at runtime through
 * {@code PresentationManager.StubFactory.makeStub()} and
 * {@code StubAdapter.setDelegate()}, which is bytecode generation and drags in
 * {@code glassfish-corba-codegen} plus {@code pfl-dynamic}. Over HTTP the
 * dispatch is a {@link Proxy} and an {@link java.lang.reflect.InvocationHandler}:
 * the JDK's own facility, no code generation, nothing to ship.
 *
 * <h2>Body format</h2>
 * <pre>
 * body        := txContext params attachments
 * txContext   := raw bytes, see InvocationEnvelope
 * params      := one marshalled object per declared parameter, in order
 * attachments := one marshalled Map&lt;String, Object&gt;
 * </pre>
 * The parameter count is known from the path, so the reader never has to guess.
 */
public final class HttpEjbClient implements AutoCloseable {

    private final HttpTransport transport;
    private final ClientConfiguration config;
    /**
     * Not final: a peer may not speak the codec this client would prefer, and
     * discovering that is only possible by asking. See {@link #downgrade}.
     */
    private volatile Marshaller marshaller;

    private final ObjectInputFilter filter;

    private volatile ResponseDecoder decoder;

    private final String contextPath;

    public HttpEjbClient(ClientConfiguration config) {
        this(config, new JdkHttpTransport(config), codecFor(config),
                JavaSerializationMarshaller.defaultFilter());
    }

    /**
     * @param config the client settings
     * @return the codec to start from
     * @throws IllegalStateException if a codec was required and is not on the
     *         class path - failing here, at construction, says exactly what is
     *         missing, where discovering it on the first invocation would not
     */
    static Marshaller codecFor(ClientConfiguration config) {
        String required = config.codec();
        if (required == null) {
            return Marshallers.preferred();
        }
        return Marshallers.find(required).orElseThrow(() -> new IllegalStateException(
                "codec " + required + " was required but is not on the class path; available: "
                        + String.join(", ", Marshallers.codecs())));
    }

    public HttpEjbClient(ClientConfiguration config,
                         HttpTransport transport,
                         Marshaller marshaller,
                         ObjectInputFilter filter) {
        this.config = config;
        this.transport = transport;
        this.marshaller = marshaller;
        this.filter = filter;
        this.decoder = new ResponseDecoder(marshaller, filter, config.codec());
        this.contextPath = config.contextPath();
    }

    /**
     * @return a proxy implementing {@code viewClass} whose calls are dispatched
     *         to the bean named by {@code locator}
     */
    public <T> T createProxy(Class<T> viewClass, EjbLocator locator) {
        if (!viewClass.isInterface()) {
            throw new IllegalArgumentException("a remote view must be an interface: " + viewClass);
        }
        Object proxy = Proxy.newProxyInstance(
                viewClass.getClassLoader(),
                new Class<?>[] { viewClass },
                new HttpEjbInvocationHandler(this, viewClass, locator));
        return viewClass.cast(proxy);
    }

    /**
     * @param homeClass the EJB 2.x home interface
     * @param componentClass the component interface its create methods return
     * @param locator the bean the home belongs to
     * @return a proxy implementing {@code homeClass}
     */
    public <T> T createHomeProxy(Class<T> homeClass, Class<?> componentClass, EjbLocator locator) {
        if (!homeClass.isInterface()) {
            throw new IllegalArgumentException("a home view must be an interface: " + homeClass);
        }
        Object proxy = Proxy.newProxyInstance(
                homeClass.getClassLoader(),
                new Class<?>[] { homeClass },
                new HttpEjbHomeInvocationHandler(this, homeClass, componentClass, locator));
        return homeClass.cast(proxy);
    }

    /**
     * @param componentClass the EJB 2.x component interface
     * @param locator the reference, carrying its session if it has one
     * @param home the home it came from, so getEJBHome can answer locally
     * @return a proxy implementing {@code componentClass}
     */
    public <T> T createComponentProxy(Class<T> componentClass, EjbLocator locator, Object home) {
        Object proxy = Proxy.newProxyInstance(
                componentClass.getClassLoader(),
                new Class<?>[] { componentClass },
                new HttpEjbInvocationHandler(this, componentClass, locator, home));
        return componentClass.cast(proxy);
    }

    /**
     * Ends a stateful session, as {@code EJBObject.remove()} does.
     *
     * @param locator the session to end; must carry a session id
     */
    public void removeSession(EjbLocator locator) throws IOException {
        if (!locator.isStateful()) {
            throw new IOException("remove() needs a session; this reference has none");
        }
        URI uri = resolve(EjbRoutes.removePath(contextPath, locator.appName(), locator.moduleName(),
                locator.distinctName(), locator.beanName(),
                EjbRoutes.encodeSessionId(locator.sessionId())));
        HttpTransport.Request request =
                new HttpTransport.Request("DELETE", uri, null, null, Map.of(), null);
        try (HttpTransport.Response response = transport.exchange(request)) {
            if (response.status() != Protocol.SC_NO_CONTENT) {
                throw new IOException(ResponseDecoder.reason(response,
                        "remove failed with HTTP " + response.status()));
            }
            drain(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted while removing a session", e);
        }
    }

    /**
     * Asks the server for its routing cookie, before there is anything to be
     * sticky about.
     *
     * <p>Worth doing once per client in a cluster, and pointless against a
     * single instance. The ordering is the whole value: if the cookie only
     * appears on the response to {@code open}, then the open itself was routed
     * without one, and nothing guaranteed it reached the node the rest of the
     * conversation will reach.
     */
    public void establishAffinity() throws IOException {
        HttpTransport.Request request = new HttpTransport.Request(
                "GET", resolve(CommonRoutes.affinityPath(contextPath)), null, null, Map.of(), null);
        try (HttpTransport.Response response = transport.exchange(request)) {
            if (response.status() != Protocol.SC_NO_CONTENT) {
                throw new IOException(ResponseDecoder.reason(response,
                        "affinity request failed with HTTP " + response.status()));
            }
            drain(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted while establishing affinity", e);
        }
    }

    /**
     * Opens a stateful session, returning the session id the server minted.
     * <p>
     * The server sets its affinity cookie on this response, and the
     * transport's cookie handler returns it on every later request, so
     * invocations on the resulting locator carry the routing hint that keeps
     * them on the instance holding the session. Prefer calling
     * {@link #establishAffinity()} once beforehand: it makes the cookie exist
     * before the open itself is routed, which in a cluster is the difference
     * between the session being created on the node the rest of the
     * conversation reaches and on some other one.
     */
    public byte[] openSession(EjbLocator locator) throws IOException {
        URI uri = resolve(EjbRoutes.openPath(contextPath, locator.appName(), locator.moduleName(),
                locator.distinctName(), locator.beanName()));
        HttpTransport.Request request = new HttpTransport.Request(
                "POST", uri, null, ContentType.of(marshaller.codec(), ContentType.KIND_RESPONSE).toHeaderValue(),
                Map.of(), null);
        try (HttpTransport.Response response = transport.exchange(request)) {
            if (response.status() != Protocol.SC_NO_CONTENT) {
                throw new IOException(ResponseDecoder.reason(response,
                        "session open failed with HTTP " + response.status()));
            }
            String id = response.firstHeader(Protocol.H_SESSION_ID);
            if (id == null) {
                throw new IOException("server opened a session but sent no " + Protocol.H_SESSION_ID);
            }
            return EjbRoutes.decodeSessionId(id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted while opening a session", e);
        }
    }

    /**
     * Asks the server to abandon an in-flight invocation.
     *
     * @param interrupt whether the server should interrupt the executing
     *                  thread, or merely discard the result. On HTTP/2 a
     *                  cancelled {@link CompletableFuture} already emits
     *                  RST_STREAM, but that only says "I no longer want the
     *                  response" - it cannot ask the container to stop working,
     *                  which is why this application-level operation exists
     *                  alongside it.
     */
    public void cancel(EjbLocator locator, String invocationId, boolean interrupt) throws IOException {
        URI uri = resolve(EjbRoutes.cancelPath(contextPath, locator.appName(), locator.moduleName(),
                locator.distinctName(), locator.beanName(), invocationId, interrupt));
        HttpTransport.Request request = new HttpTransport.Request("DELETE", uri, null, null, Map.of(), null);
        try (HttpTransport.Response response = transport.exchange(request)) {
            // A cancel that arrives after completion is not an error.
            if (response.status() >= 500) {
                throw new IOException(ResponseDecoder.reason(response,
                        "cancel failed with HTTP " + response.status()));
            }
            drain(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted while cancelling", e);
        }
    }

    Object invoke(EjbLocator locator, Class<?> viewClass, Method method, Object[] args) throws Throwable {
        // Before the request is built, because building it reads the thread's
        // transaction and joining is what puts one there.
        AmbientTransaction.join(config, transport);
        HttpTransport.Request request = buildInvocation(locator, viewClass, method, args, newInvocationId());
        HttpTransport.Response response;
        try {
            response = transport.exchange(request);
            if (isUnsupportedCodec(response)) {
                // Adding a codec to this side must never break a call to a
                // server that does not have it. The server has just told us
                // what it cannot read, so drop to the codec every peer has and
                // try once more, rather than surfacing a failure the caller
                // can do nothing about.
                drain(response.body());
                downgrade();
                request = buildInvocation(locator, viewClass, method, args, newInvocationId());
                response = transport.exchange(request);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EJBException("interrupted while invoking " + method.getName(), e);
        }
        return decoder.decodeInvocationResult(response, viewClass.getClassLoader());
    }

    /**
     * @return whether the server refused the codec rather than the request
     */
    private boolean isUnsupportedCodec(HttpTransport.Response response) {
        if (response.status() != Protocol.SC_NOT_ACCEPTABLE
                || marshaller.codec().equals(ContentType.CODEC_JSER)
                || config.codec() != null) {
            // A codec that was asked for by name is not something to quietly
            // walk back: the caller wanted this encoding, and the refusal -
            // which names what the server does speak - is the useful answer.
            return false;
        }
        String reason = response.firstHeader("X-GF-Reason");
        // A version refusal also arrives as 406 and is not something a
        // different codec would fix, so the reason has to be read.
        return reason != null && reason.contains("unsupported codec");
    }

    /**
     * Falls back to the codec that is always present.
     *
     * <p>Permanent for this client rather than per call: a server that cannot
     * read a codec now will not learn it between two invocations, and paying a
     * failed round trip every time would be worse than not having the codec at
     * all.
     */
    private void downgrade() {
        Marshaller builtIn = new JavaSerializationMarshaller();
        this.marshaller = builtIn;
        this.decoder = new ResponseDecoder(builtIn, filter, config.codec());
    }

    CompletableFuture<Object> invokeAsync(EjbLocator locator, Class<?> viewClass, Method method, Object[] args) {
        AmbientTransaction.join(config, transport);
        final String invocationId = newInvocationId();
        final HttpTransport.Request request;
        try {
            request = buildInvocation(locator, viewClass, method, args, invocationId);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
        return transport.exchangeAsync(request).handle((response, error) -> {
            if (error != null) {
                throw new java.util.concurrent.CompletionException(error);
            }
            try {
                return decoder.decodeInvocationResult(response, viewClass.getClassLoader());
            } catch (Throwable t) {
                throw new java.util.concurrent.CompletionException(t);
            }
        });
    }

    private HttpTransport.Request buildInvocation(EjbLocator locator,
                                                  Class<?> viewClass,
                                                  Method method,
                                                  Object[] args,
                                                  String invocationId) throws IOException {
        Class<?>[] parameterTypes = method.getParameterTypes();
        String[] parameterTypeNames = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypeNames[i] = parameterTypes[i].getName();
        }

        String path = EjbRoutes.invokePath(contextPath,
                locator.appName(),
                locator.moduleName(),
                locator.distinctName(),
                locator.beanName(),
                EjbRoutes.encodeSessionId(locator.sessionId()),
                viewClass.getName(),
                method.getName(),
                parameterTypeNames);

        // Read the association once and use it for both the body and the
        // headers, so an interceptor changing it mid-build cannot produce an
        // invocation whose prefix and timeout describe different transactions.
        Xid transaction = ClientTransactionContext.current();
        ByteBuffer[] body = marshalArguments(args, transaction);

        Map<String, String> headers = new HashMap<>(4);
        headers.put(Protocol.H_INVOCATION_ID, invocationId);
        if (transaction != null) {
            long timeout = ClientTransactionContext.currentTimeoutSeconds();
            if (timeout > 0) {
                headers.put(Protocol.H_TXN_TIMEOUT, Long.toString(timeout));
            }
        }

        return new HttpTransport.Request(
                "POST",
                resolve(path),
                ContentType.of(marshaller.codec(), ContentType.KIND_INVOCATION).toHeaderValue(),
                ContentType.of(marshaller.codec(), ContentType.KIND_RESPONSE).toHeaderValue(),
                headers,
                body);
    }

    private ByteBuffer[] marshalArguments(Object[] args, Xid transaction) throws IOException {
        ChunkedOutput out = new ChunkedOutput();
        // TYPE_OUTFLOWED: the transaction started somewhere else and is being
        // carried into this call, which is true whether the coordinator is the
        // caller's own manager or the far end that minted it for us.
        InvocationEnvelope.writeTxContext(out, transaction == null
                ? TxContext.NONE
                : Xids.toContext(transaction, TxContext.TYPE_OUTFLOWED));
        try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
            if (args != null) {
                for (Object arg : args) {
                    writer.writeObject(arg);
                }
            }
            writer.writeObject(new HashMap<String, Object>());
            writer.flush();
        }
        return out.toByteBuffers();
    }

    private URI resolve(String path) {
        return RequestUri.build(config.baseUri(), path, null);
    }

    private static String newInvocationId() {
        return UUID.randomUUID().toString();
    }

    private static void drain(InputStream in) throws IOException {
        if (in != null) {
            in.readAllBytes();
        }
    }

    /** @return the HTTP version the last exchange actually used. */
    public String negotiatedVersion() {
        return transport.negotiatedVersion();
    }

    ObjectInputFilter deserializationFilter() {
        return filter;
    }

    @Override
    public void close() {
        transport.close();
    }
}
