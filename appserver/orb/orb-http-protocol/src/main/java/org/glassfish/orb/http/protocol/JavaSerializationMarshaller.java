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

package org.glassfish.orb.http.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;

/**
 * The default codec: plain Java serialization.
 * <p>
 * Chosen over a JSON binding on purpose. Remote EJB invocation is defined in
 * terms of pass-by-value with RMI semantics, and Java serialization is what
 * implements those semantics - cyclic object graphs, shared references,
 * {@code transient}, {@code writeObject}, {@code Serializable} exceptions with
 * their causes and stack traces. Payara's HTTP EJB transport uses JSON-B and
 * consequently documents that object graphs "must form trees without cyclic
 * references"; that is not a remote EJB view, it is a subset of one.
 *
 * <h2>Deserialization filtering</h2>
 * Accepting a serialized graph from the network is the classic Java
 * deserialization risk, and it is exactly the risk IIOP had too. Every reader
 * created here <em>must</em> be given an {@link ObjectInputFilter}; there is
 * no unfiltered path. {@link #defaultFilter()} supplies a conservative
 * starting point which callers are expected to narrow to the deployed
 * application's classes.
 *
 * <h3>What is bounded, and what an anonymous request can still cost</h3>
 * <b>The decisive limit first.</b> The default filter ends with {@code ;*}, so
 * every {@code Serializable} class is allowed for an anonymous caller, and the
 * array budget below only covers what the JDK reports through its filter hook:
 * arrays, and the JDK collections that call {@code checkArray} before
 * allocating. A library class on the class path whose {@code readObject}
 * allocates from a size it reads from the stream is not bounded by anything
 * here.
 *
 * <p>Within that: every reader, whatever filter it is given, also runs a
 * per-stream budget of declared array memory (length times element size, a
 * reference counting as 8, including the arrays that collections allocate while
 * being read). A collection or array allocates before it reads its elements,
 * so a limit on each array alone is not a limit: nested arrays that each pass
 * it can hold gigabytes live, which an earlier version of this filter
 * allowed. {@link #defaultFilter()} adds limits on depth, objects and bytes
 * per stream, and the transport caps the request body at {@code maxBytes}
 * with a {@link LimitedInputStream} (the filter itself is never called for a
 * string or for block data, so {@code maxbytes} alone does not bound them).
 *
 * <p>The budget counts declared arrays, not everything the JDK allocates
 * while reading them: some classes copy (immutable sets and maps, {@code
 * Vector}, {@code StringBuilder}, atomic arrays). The 32 MB budget is
 * therefore up to about 96 MB transient and 64 MB retained, which is the
 * figure to plan with; it is not scaled by a factor.
 *
 * <p>Known limits. The budget, object and depth limits are per stream, i.e.
 * per request for this codec, with no bound across concurrent requests; a
 * quota over all requests is not part of this class. A custom
 * {@code jdk.serialFilterFactory} is applied by
 * {@code ObjectInputStream.setObjectInputFilter} and may drop the budget:
 * installing one takes responsibility for the limits.
 */
public final class JavaSerializationMarshaller implements Marshaller {

    public static final String CODEC = ContentType.CODEC_JSER;

    private static final String PROPERTY_PREFIX = "org.glassfish.orb.http.filter.";

    /**
     * The declared array memory, in bytes, one <em>stream</em> may add up to
     * (default 32 MB), {@code org.glassfish.orb.http.filter.maxArrayBytes}.
     *
     * <p>The filter is asked about an array's declared length before the
     * array is allocated, and {@code maxbytes} only counts what has been read
     * so far, so a body of a few dozen bytes can declare a
     * {@code long[100_000_000]}. A bound per array is not enough either:
     * each level of a nested structure allocates before it reads the next, so
     * the bound has to be on the running total for the stream.
     */
    static final long MAX_ARRAY_BYTES = limit("maxArrayBytes", 32L * 1024 * 1024);

    /**
     * Deepest object graph accepted (default 100),
     * {@code org.glassfish.orb.http.filter.maxDepth}. 64 refused an ordinary
     * 65-node chain; 100 admits it and stays far from a stack overflow. The
     * Fory codec reads the same value.
     */
    static final long MAX_DEPTH = limit("maxDepth", 100L);

    /**
     * Objects in one stream (default 200,000),
     * {@code org.glassfish.orb.http.filter.maxRefs}. Every object read is kept
     * until the call is over, so this is what bounds the object heap one
     * request can build.
     */
    static final long MAX_REFS = limit("maxRefs", 200_000L);

    /**
     * Bytes in one request body (default 32 MB),
     * {@code org.glassfish.orb.http.filter.maxBytes}. It is the filter's
     * {@code maxbytes} and also the cap the dispatchers put on the body.
     */
    static final long MAX_BYTES = limit("maxBytes", 32L * 1024 * 1024);

    /** @return the most bytes a request or response body may have */
    public static long maxBytes() {
        return MAX_BYTES;
    }

    /**
     * The one filter {@link #defaultFilter()} returns. It is immutable and
     * thread-safe, and being a single instance lets callers use it as a cache
     * key. The array budget is not in it: it is stateful and is added around
     * every stream by the reader.
     */
    private static final ObjectInputFilter DEFAULT_FILTER = ObjectInputFilter.Config.createFilter(
            "maxdepth=" + MAX_DEPTH + ";maxrefs=" + MAX_REFS + ";maxbytes=" + MAX_BYTES + ";*");

    /**
     * Reads a positive limit from a system property. A missing, non-numeric,
     * zero or negative value is logged and replaced by the default: this runs
     * in a static initialiser, and a bad property must never make the class
     * fail to load.
     */
    static long limit(String name, long fallback) {
        String raw = System.getProperty(PROPERTY_PREFIX + name);
        if (raw == null) {
            return fallback;
        }
        try {
            long value = Long.parseLong(raw.trim());
            if (value > 0) {
                return value;
            }
        } catch (NumberFormatException e) {
            // reported below
        }
        System.getLogger(JavaSerializationMarshaller.class.getName()).log(System.Logger.Level.WARNING,
                "ignoring {0}{1}={2}: it must be a positive number; using {3}",
                PROPERTY_PREFIX, name, raw, fallback);
        return fallback;
    }

    /** @return the deepest graph the filters accept; the Fory codec uses the same bound */
    public static long maxDepth() {
        return MAX_DEPTH;
    }

    /** @return the per-stream budget of declared array memory, in bytes */
    public static long maxArrayBytes() {
        return MAX_ARRAY_BYTES;
    }

    @Override
    public String codec() {
        return CODEC;
    }

    @Override
    public ObjectWriter newWriter(OutputStream out) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(out);
        return new ObjectWriter() {
            @Override
            public void writeObject(Object o) throws IOException {
                oos.writeObject(o);
                // Drop the back-reference table between top-level objects.
                // Without this the table grows for the life of the stream,
                // and two equal-but-distinct arguments would be conflated.
                oos.reset();
            }

            @Override
            public void flush() throws IOException {
                oos.flush();
            }

            @Override
            public void close() throws IOException {
                oos.close();
            }
        };
    }

    @Override
    public ObjectReader newReader(InputStream in, ClassLoader loader, ObjectInputFilter filter) throws IOException {
        return open(in, loader, filter, ObjectInputFilter.Config.getSerialFilter(), new Budget());
    }

    /**
     * Like {@link #newReader} but charging an existing budget, for a codec that
     * reads one request as several streams.
     */
    public ObjectReader newReaderSharing(InputStream in, ClassLoader loader, ObjectInputFilter filter,
                                         Budget budget) throws IOException {
        return open(in, loader, filter, ObjectInputFilter.Config.getSerialFilter(), budget);
    }

    /**
     * @param jvmWide the JVM-wide filter ({@code jdk.serialFilter}), or
     *                {@code null} if none is configured
     */
    ObjectReader newReader(InputStream in, ClassLoader loader, ObjectInputFilter filter,
                           ObjectInputFilter jvmWide) throws IOException {
        return open(in, loader, filter, jvmWide, new Budget());
    }

    private ObjectReader open(InputStream in, ClassLoader loader, ObjectInputFilter filter,
                              ObjectInputFilter jvmWide, Budget budget) throws IOException {
        if (filter == null) {
            throw new IllegalArgumentException("a deserialization filter is required");
        }
        ClassLoader resolved = loader != null ? loader : Thread.currentThread().getContextClassLoader();
        ObjectInputStream ois = new LoaderAwareObjectInputStream(in, resolved);
        // The budget goes around whatever filter the caller passed,
        // including an application's narrowed one.
        ois.setObjectInputFilter(ObjectInputFilter.merge(budget,
                withJvmWideFilter(filter, jvmWide)));
        return new ObjectReader() {
            @Override
            public Object readObject() throws IOException, ClassNotFoundException {
                return ois.readObject();
            }

            @Override
            public void close() throws IOException {
                ois.close();
            }
        };
    }

    /**
     * A filter that rejects deeply nested or oversized graphs. It is a floor,
     * not a policy: a server should compose it with an allow-list of the
     * application's own classes.
     * <p>
     * Returns the same instance on every call; callers may use it as a cache
     * key.
     *
     * @return the shared default filter
     */
    public static ObjectInputFilter defaultFilter() {
        return DEFAULT_FILTER;
    }

    /**
     * Adds up the declared size of every array it is asked about and refuses
     * once the total passes the budget. It carries state and is not
     * thread-safe: one instance per request, used by one thread at a time. A
     * codec that reads a request as several streams shares one instance
     * between them, so the budget is per request and not per frame.
     */
    public static final class Budget implements ObjectInputFilter {

        private long declared;

        @Override
        public Status checkInput(FilterInfo info) {
            Class<?> type = info.serialClass();
            long length = info.arrayLength();
            if (length < 0 || type == null || !type.isArray()) {
                return Status.UNDECIDED;
            }
            long size = wireSize(type.getComponentType());
            if (length > MAX_ARRAY_BYTES / size) {
                return Status.REJECTED;
            }
            declared += length * size;
            return declared > MAX_ARRAY_BYTES ? Status.REJECTED : Status.UNDECIDED;
        }
    }

    /** What one element of this type costs to allocate; a reference counts as 8. */
    private static long wireSize(Class<?> component) {
        if (component == byte.class || component == boolean.class) {
            return 1;
        }
        if (component == char.class || component == short.class) {
            return 2;
        }
        if (component == int.class || component == float.class) {
            return 4;
        }
        return 8;
    }

    /**
     * Combines a filter with the JVM-wide one, so that what an operator denied
     * with {@code jdk.serialFilter} stays denied whatever filter the transport
     * brings. A class either filter rejects is rejected.
     *
     * @param filter the transport's filter
     * @return {@code filter} merged with the JVM-wide filter, or {@code filter}
     *         itself if there is none
     */
    public static ObjectInputFilter withJvmWideFilter(ObjectInputFilter filter) {
        return withJvmWideFilter(filter, ObjectInputFilter.Config.getSerialFilter());
    }

    /**
     * @param filter the transport's filter
     * @param jvmWide the JVM-wide filter, or {@code null}
     * @return the two merged; a rejection by either wins
     */
    public static ObjectInputFilter withJvmWideFilter(ObjectInputFilter filter, ObjectInputFilter jvmWide) {
        return jvmWide == null ? filter : ObjectInputFilter.merge(filter, jvmWide);
    }

    /**
     * Resolves classes through the application class loader. The default
     * {@code ObjectInputStream} resolution walks the caller's stack, which in
     * a container lands on the module that happens to be dispatching rather
     * than on the deployment that owns the class.
     */
    private static final class LoaderAwareObjectInputStream extends ObjectInputStream {

        private final ClassLoader loader;

        LoaderAwareObjectInputStream(InputStream in, ClassLoader loader) throws IOException {
            super(in);
            this.loader = loader;
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            try {
                return Class.forName(desc.getName(), false, loader);
            } catch (ClassNotFoundException e) {
                return super.resolveClass(desc);
            }
        }

        @Override
        @SuppressWarnings("deprecation") // Proxy.getProxyClass is the only way to
        // resolve a proxy class without instantiating one; ObjectInputStream's own
        // default implementation calls it too.
        protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
            Class<?>[] resolved = new Class<?>[interfaces.length];
            for (int i = 0; i < interfaces.length; i++) {
                resolved[i] = Class.forName(interfaces[i], false, loader);
            }
            try {
                return java.lang.reflect.Proxy.getProxyClass(loader, resolved);
            } catch (IllegalArgumentException e) {
                return super.resolveProxyClass(interfaces);
            }
        }
    }
}
