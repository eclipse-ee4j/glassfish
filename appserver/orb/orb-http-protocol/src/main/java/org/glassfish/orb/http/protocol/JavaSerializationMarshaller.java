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
 */
public final class JavaSerializationMarshaller implements Marshaller {

    public static final String CODEC = ContentType.CODEC_JSER;

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
        if (filter == null) {
            throw new IllegalArgumentException("a deserialization filter is required");
        }
        ClassLoader resolved = loader != null ? loader : Thread.currentThread().getContextClassLoader();
        ObjectInputStream ois = new LoaderAwareObjectInputStream(in, resolved);
        ois.setObjectInputFilter(filter);
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
     */
    public static ObjectInputFilter defaultFilter() {
        return ObjectInputFilter.Config.createFilter(
                "maxdepth=64;maxarray=100000;maxrefs=100000;maxbytes=104857600;*");
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
