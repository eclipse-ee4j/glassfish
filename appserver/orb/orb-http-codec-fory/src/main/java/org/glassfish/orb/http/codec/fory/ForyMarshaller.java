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


package org.glassfish.orb.http.codec.fory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.fory.Fory;
import org.apache.fory.ThreadSafeFory;
import org.apache.fory.config.CompatibleMode;
import org.apache.fory.config.Language;
import org.glassfish.orb.http.protocol.Marshaller;

/**
 * An object codec built on Apache Fory.
 * <p>
 * This class is never referenced by the transport, by an application, or by
 * any configuration file. It is reached only through
 * {@code META-INF/services}, which is what makes the claim behind this module
 * true: an application changes codec by having this jar present, and by
 * nothing else.
 * <p>
 * The encoding changes; the semantics deliberately do not. Java serialization
 * tracks object identity, closes cycles and skips {@code transient} fields,
 * and remote calls already rely on all three. Fory is configured to match
 * rather than to be fast in ways that would silently alter what an
 * application observes - see the settings in {@link #newFory}.
 */
public final class ForyMarshaller implements Marshaller {

    /** The codec token that travels in the content type. */
    public static final String CODEC = "fory";

    /**
     * Writing and reading get separate instances, and this is a security
     * property rather than tidiness.
     * <p>
     * Fory caches what it has resolved. With one shared instance, a class
     * written on the way out is already resolved by the time an attacker
     * names it on the way in, so the inbound check never runs - the guard
     * is present and bypassed. Keeping the two directions apart means the
     * reader has never been warmed by anything this process chose to send.
     * <p>
     * Keyed weakly so an application class loader stays collectable after
     * undeployment; a serializer cache must not pin a deployment in memory.
     */
    private static final Map<ClassLoader, ThreadSafeFory> WRITERS =
            Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Readers are additionally keyed by the filter in force. A filter is
     * normally one long lived object per configuration, so this stays small -
     * and it closes the second half of the same hole: a class admitted under
     * a permissive filter must not stay admitted for a stricter one.
     */
    private static final Map<ClassLoader, Map<ObjectInputFilter, ThreadSafeFory>> READERS =
            Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public String codec() {
        return CODEC;
    }

    @Override
    public int priority() {
        // Above the built-in floor, so simply being on the class path is
        // enough to be chosen.
        return 100;
    }

    @Override
    public ObjectWriter newWriter(OutputStream out) throws IOException {
        ThreadSafeFory fory = writerFor(Thread.currentThread().getContextClassLoader());
        DataOutputStream data = new DataOutputStream(out);
        return new ObjectWriter() {

            @Override
            public void writeObject(Object o) throws IOException {
                // Each object is length prefixed rather than relying on the
                // codec's own stream framing: the transport writes several
                // objects in sequence, and the boundary between them has to
                // be a property of this format, not an implementation detail
                // of the library underneath.
                byte[] encoded = fory.serialize(o);
                data.writeInt(encoded.length);
                data.write(encoded);
            }

            @Override
            public void flush() throws IOException {
                data.flush();
            }

            @Override
            public void close() throws IOException {
                data.flush();
            }
        };
    }

    @Override
    public ObjectReader newReader(InputStream in, ClassLoader loader, ObjectInputFilter filter)
            throws IOException {
        if (filter == null) {
            throw new IllegalArgumentException("a deserialization filter is required");
        }
        ThreadSafeFory fory = readerFor(loader, filter);
        DataInputStream data = new DataInputStream(in);
        return new ObjectReader() {

            @Override
            public Object readObject() throws IOException, ClassNotFoundException {
                int length = data.readInt();
                if (length < 0) {
                    throw new IOException("negative frame length " + length);
                }
                byte[] encoded = data.readNBytes(length);
                if (encoded.length != length) {
                    throw new IOException("truncated frame: expected " + length
                            + " bytes but read " + encoded.length);
                }

                return fory.deserialize(encoded);
            }

            @Override
            public void close() throws IOException {
                data.close();
            }
        };
    }

    private static ThreadSafeFory writerFor(ClassLoader loader) {
        return WRITERS.computeIfAbsent(resolve(loader), l -> newFory(l, null));
    }

    private static ThreadSafeFory readerFor(ClassLoader loader, ObjectInputFilter filter) {
        Map<ObjectInputFilter, ThreadSafeFory> byFilter = READERS.computeIfAbsent(resolve(loader),
                l -> Collections.synchronizedMap(new WeakHashMap<>()));
        return byFilter.computeIfAbsent(filter, f -> newFory(resolve(loader), f));
    }

    private static ClassLoader resolve(ClassLoader loader) {
        return loader == null ? ForyMarshaller.class.getClassLoader() : loader;
    }

    /**
     * Builds a Fory configured to preserve what remote calls already observe.
     *
     * <p>Reference tracking is on because an object graph crossing a remote
     * call may share a node or contain a cycle, and Java serialization closes
     * both; without it a shared node would arrive duplicated and a cycle would
     * not arrive at all. Compatible mode lets a field appear or disappear
     * between the two ends, which is the situation a running cluster is
     * actually in during a rolling upgrade.
     *
     * <p>Class registration is not required - an application cannot be asked
     * to enumerate its own types for a codec it never opted into - so the
     * transport's deserialization filter takes that duty instead.
     *
     * @param loader the loader that resolves incoming classes
     * @param filter the policy for inbound classes, or {@code null} for a
     *               writer, where classes come from live objects rather than
     *               from the wire and there is nothing to guard against
     * @return a thread safe Fory for that loader
     */
    private static ThreadSafeFory newFory(ClassLoader loader, ObjectInputFilter filter) {
        var builder = Fory.builder()
                .withLanguage(Language.JAVA)
                .withClassLoader(loader)
                .withRefTracking(true)
                .withCompatibleMode(CompatibleMode.COMPATIBLE)
                .requireClassRegistration(false)
                .suppressClassRegistrationWarnings(true);
        if (filter != null) {
            builder = builder.withTypeChecker(new FilterBackedTypeChecker(loader, filter));
        }
        return builder.buildThreadSafeFory();
    }
}
