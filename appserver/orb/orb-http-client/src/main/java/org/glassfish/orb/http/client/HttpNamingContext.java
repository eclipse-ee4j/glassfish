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





import java.io.IOException;
import java.io.ObjectInputFilter;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;

import org.glassfish.orb.http.protocol.ChunkedOutput;
import org.glassfish.orb.http.protocol.ContentType;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Marshaller;
import org.glassfish.orb.http.protocol.Marshallers;
import org.glassfish.orb.http.protocol.NamingRoutes;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.ProtocolException;
import org.glassfish.orb.http.protocol.RemoteEjbReference;

/**
 * A JNDI {@link Context} backed by the HTTP naming service.
 * <p>
 * It is the counterpart of {@code com.sun.enterprise.naming.impl.SerialContext},
 * which reaches the server's {@code SerialContextProvider} over RMI-IIOP. The
 * nine remote operations are the same ones; only the transport differs.
 * <p>
 * When a lookup yields a {@link RemoteEjbReference} it is converted here into a
 * live proxy, so callers get back something they can invoke, exactly as they
 * would from a narrowed IIOP stub.
 */
public class HttpNamingContext implements Context {

    private final HttpEjbClient client;
    private final HttpTransport transport;
    private final ClientConfiguration config;
    private final Marshaller marshaller;
    private final ObjectInputFilter filter;
    private final Hashtable<Object, Object> environment;
    private final String contextPath;

    /**
     * Whether a proxy has been built from this context's transport. Once one
     * has, closing the context must not close the transport underneath it.
     */
    private final java.util.concurrent.atomic.AtomicBoolean lentToProxies =
            new java.util.concurrent.atomic.AtomicBoolean();

    public HttpNamingContext(ClientConfiguration config, Hashtable<?, ?> environment) {
        this(config, new JdkHttpTransport(config), environment);
    }

    public HttpNamingContext(ClientConfiguration config, HttpTransport transport, Hashtable<?, ?> environment) {
        this.config = config;
        this.transport = transport;
        this.marshaller = HttpEjbClient.codecFor(config);
        this.filter = JavaSerializationMarshaller.defaultFilter();
        this.client = new HttpEjbClient(config, transport, marshaller, filter);
        this.environment = environment == null ? new Hashtable<>() : new Hashtable<>(environment);
        this.contextPath = config.contextPath();
    }

    // ---- the nine remote operations --------------------------------------

    @Override
    public Object lookup(String name) throws NamingException {
        Object value = call(Protocol.OP_LOOKUP, name, null, ContentType.KIND_VALUE);
        return resolve(value);
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {
        call(Protocol.OP_BIND, name, obj, null);
    }

    @Override
    public void rebind(String name, Object obj) throws NamingException {
        call(Protocol.OP_REBIND, name, obj, null);
    }

    @Override
    public void unbind(String name) throws NamingException {
        call(Protocol.OP_UNBIND, name, null, null);
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
        callWithQuery(Protocol.OP_RENAME, oldName,
                NamingRoutes.PARAM_NEW_NAME + '=' + java.net.URLEncoder.encode(newName, java.nio.charset.StandardCharsets.UTF_8));
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        Object value = call(Protocol.OP_LIST, name, null, ContentType.KIND_VALUE);
        List<NameClassPair> pairs = new ArrayList<>();
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> e : map.entrySet()) {
                Object v = e.getValue();
                pairs.add(new NameClassPair(String.valueOf(e.getKey()),
                        v == null ? Object.class.getName() : v.getClass().getName()));
            }
        }
        return new ListEnumeration<>(pairs);
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        Object value = call(Protocol.OP_LIST, name, null, ContentType.KIND_VALUE);
        List<Binding> bindings = new ArrayList<>();
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> e : map.entrySet()) {
                bindings.add(new Binding(String.valueOf(e.getKey()), resolve(e.getValue())));
            }
        }
        return new ListEnumeration<>(bindings);
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {
        call(Protocol.OP_CREATE_SUBCONTEXT, name, null, null);
        return this;
    }

    @Override
    public void destroySubcontext(String name) throws NamingException {
        call(Protocol.OP_DESTROY_SUBCONTEXT, name, null, null);
    }

    @Override
    public Object lookupLink(String name) throws NamingException {
        return resolve(call(Protocol.OP_LOOKUP_LINK, name, null, ContentType.KIND_VALUE));
    }

    // ---- plumbing ---------------------------------------------------------

    /** Converts a reference the server sent into something callable. */
    private Object resolve(Object value) {
        if (value instanceof RemoteEjbReference ref) {
            try {
                // From here the client outlives this context: the proxy about
                // to be returned will use it long after close().
                lentToProxies.set(true);
                Class<?> view = Class.forName(ref.viewClassName(), false, contextClassLoader());
                EjbLocator locator = new EjbLocator(ref.appName(), ref.moduleName(),
                        ref.distinctName(), ref.beanName(), ref.sessionId());
                if (ref.isHome()) {
                    // EJB 2.x: what the client narrows and calls create() on.
                    Class<?> component =
                            Class.forName(ref.componentClassName(), false, contextClassLoader());
                    return client.createHomeProxy(view, component, locator);
                }
                return client.createProxy(view, locator);
            } catch (ClassNotFoundException e) {
                // The view interface is not on this client's classpath. Hand
                // back the reference rather than failing: the caller may only
                // want to inspect or re-bind it.
                return ref;
            }
        }
        return value;
    }

    private ClassLoader contextClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl != null ? cl : getClass().getClassLoader();
    }

    private Object call(String operation, String name, Object payload, String expectedKind) throws NamingException {
        return exchange(operation, name, null, payload, expectedKind);
    }

    private void callWithQuery(String operation, String name, String query) throws NamingException {
        exchange(operation, name, query, null, null);
    }

    private Object exchange(String operation, String name, String query, Object payload, String expectedKind)
            throws NamingException {
        try {
            String path = NamingRoutes.path(contextPath, operation, name);
            URI uri = RequestUri.build(config.baseUri(), path, query);

            ByteBuffer[] body = null;
            String contentType = null;
            if (payload != null) {
                ChunkedOutput out = new ChunkedOutput();
                try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
                    writer.writeObject(payload);
                    writer.flush();
                }
                body = out.toByteBuffers();
                contentType = ContentType.of(marshaller.codec(), ContentType.KIND_VALUE).toHeaderValue();
            }

            HttpTransport.Request request = new HttpTransport.Request(
                    NamingRoutes.methodFor(operation), uri, contentType,
                    ContentType.of(marshaller.codec(), ContentType.KIND_VALUE).toHeaderValue(),
                    Map.of(), body);

            try (HttpTransport.Response response = transport.exchange(request)) {
                if (response.status() == Protocol.SC_NOT_FOUND) {
                    throw new javax.naming.NameNotFoundException(name);
                }
                if (response.status() == Protocol.SC_FORBIDDEN) {
                    throw new javax.naming.NoPermissionException("not authorised to " + operation + ' ' + name);
                }
                if (response.status() == Protocol.SC_EXCEPTION) {
                    Object thrown = readBody(response, ContentType.KIND_EXCEPTION);
                    if (thrown instanceof NamingException ne) {
                        throw ne;
                    }
                    throw namingException("server failed to " + operation + ' ' + name,
                            thrown instanceof Throwable t ? t : null);
                }
                if (response.status() != Protocol.SC_OK && response.status() != Protocol.SC_NO_CONTENT) {
                    throw namingException("HTTP " + response.status() + " from " + operation + ' ' + name, null);
                }
                if (expectedKind == null || response.status() == Protocol.SC_NO_CONTENT) {
                    return null;
                }
                return readBody(response, expectedKind);
            }
        } catch (NamingException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw namingException("interrupted during " + operation, e);
        } catch (Exception e) {
            throw namingException(operation + ' ' + name + " failed", e);
        }
    }

    private Object readBody(HttpTransport.Response response, String expectedKind) throws IOException, ClassNotFoundException {
        if (response.contentType() == null || response.contentType().isBlank()) {
            // No body to decode. The server says why in a header when it could
            // not encode one, and reporting "missing Content-Type" instead of
            // that reason describes our disappointment rather than its failure.
            String reason = response.firstHeader("X-GF-Reason");
            throw new ProtocolException(reason == null || reason.isBlank()
                    ? "the server sent no body and no reason"
                    : reason);
        }
        ContentType type = ContentType.parse(response.contentType());
        if (!type.isVersionSupported()) {
            throw new ProtocolException("server replied with protocol version " + type.version());
        }
        try (Marshaller.ObjectReader reader =
                codecFor(type).newReader(response.body(), contextClassLoader(), filter)) {
            return reader.readObject();
        }
    }

    /**
     * Decodes with the codec the reply declares, not the one we asked for.
     *
     * <p>A lookup states its codec in {@code Accept}, which is a preference: a
     * server that does not have that codec answers in one it does have, and is
     * right to. Reading that reply with the codec we wanted does not fail
     * cleanly - a Java serialization stream header read as a length prefix is a
     * large negative number, and the error names a frame length rather than the
     * mismatch behind it.
     *
     * @param type the reply's declared type
     * @return the codec to decode with
     * @throws ProtocolException if the reply used a codec this client either
     *         does not have or was told not to accept
     */
    private Marshaller codecFor(ContentType type) throws ProtocolException {
        String replied = type.codec();
        if (marshaller.codec().equals(replied)) {
            return marshaller;
        }
        String required = config.codec();
        if (required != null) {
            throw new ProtocolException("this client requires the " + required
                    + " codec but the server replied in " + replied);
        }
        return Marshallers.find(replied).orElseThrow(() -> new ProtocolException(
                "the server replied in the " + replied + " codec, which is not on this client's class path"));
    }

    private static NamingException namingException(String message, Throwable cause) {
        NamingException e = new NamingException(message);
        if (cause != null) {
            e.initCause(cause);
        }
        return e;
    }

    // ---- Name overloads delegate to the String forms -----------------------

    @Override
    public Object lookup(Name name) throws NamingException {
        return lookup(name.toString());
    }

    @Override
    public void bind(Name name, Object obj) throws NamingException {
        bind(name.toString(), obj);
    }

    @Override
    public void rebind(Name name, Object obj) throws NamingException {
        rebind(name.toString(), obj);
    }

    @Override
    public void unbind(Name name) throws NamingException {
        unbind(name.toString());
    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
        rename(oldName.toString(), newName.toString());
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return list(name.toString());
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return listBindings(name.toString());
    }

    @Override
    public void destroySubcontext(Name name) throws NamingException {
        destroySubcontext(name.toString());
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        return createSubcontext(name.toString());
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {
        return lookupLink(name.toString());
    }

    // ---- environment and unsupported operations ---------------------------

    @Override
    public NameParser getNameParser(String name) throws NamingException {
        throw new OperationNotSupportedException("name parsing is not supported by this context");
    }

    @Override
    public NameParser getNameParser(Name name) throws NamingException {
        return getNameParser(name.toString());
    }

    @Override
    public String composeName(String name, String prefix) {
        return prefix.isEmpty() ? name : prefix + '/' + name;
    }

    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {
        Name result = (Name) prefix.clone();
        result.addAll(name);
        return result;
    }

    @Override
    public Object addToEnvironment(String propName, Object propVal) {
        return environment.put(propName, propVal);
    }

    @Override
    public Object removeFromEnvironment(String propName) {
        return environment.remove(propName);
    }

    @Override
    public Hashtable<?, ?> getEnvironment() {
        return new Hashtable<>(environment);
    }

    @Override
    public String getNameInNamespace() {
        return "";
    }

    /**
     * {@inheritDoc}
     *
     * <p>Does not close the transport once a proxy has been handed out.
     *
     * <p>Looking a bean up and then closing the context is the ordinary shape
     * of JNDI code, and an IIOP stub survives it - the stub is its own object
     * with its own lifetime. A proxy from here shares this context's
     * connection pool, so closing that pool turned every such proxy into one
     * that answers "IOException: closed" on first use, which is a trap laid
     * for correctly written code.
     *
     * <p>A context that handed out nothing still closes what it opened, so the
     * common case of a lookup that found no bean releases its resources
     * immediately.
     */
    @Override
    public void close() {
        if (!lentToProxies.get()) {
            client.close();
        }
    }

    /** A trivial {@link NamingEnumeration} over an already-materialised list. */
    private static final class ListEnumeration<T> implements NamingEnumeration<T> {

        private final java.util.Iterator<T> iterator;

        ListEnumeration(List<T> items) {
            this.iterator = items.iterator();
        }

        @Override
        public T next() {
            return iterator.next();
        }

        @Override
        public boolean hasMore() {
            return iterator.hasNext();
        }

        @Override
        public void close() {
        }

        @Override
        public boolean hasMoreElements() {
            return hasMore();
        }

        @Override
        public T nextElement() {
            return next();
        }
    }
}
