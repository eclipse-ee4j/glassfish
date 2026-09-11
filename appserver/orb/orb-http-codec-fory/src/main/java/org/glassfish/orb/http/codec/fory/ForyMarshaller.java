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
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
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
     * Whether Fory may compile a serializer per type.
     * <p>
     * Off by default, and the default is about this server rather than about
     * Fory. Generated serializers are defined through a class loader Fory
     * chooses, which works on a class path and did not survive here: the type
     * being encoded lives in one bundle and Fory in another, and generation
     * fails with "Create sequential serializer failed", naming the type rather
     * than the visibility behind it. Bridging the two loaders was tried and
     * did not fix it, so the cause is not fully understood and the honest
     * default is the path that works.
     * <p>
     * The reflective path is what runs instead. It is slower than generated
     * code and still well ahead of Java serialization, which is the comparison
     * that matters for a codec adopted to be faster than it.
     * <p>
     * Set {@code org.glassfish.orb.http.codec.fory.codegen=true} to turn it on
     * where it does work - a plain client JVM, for one, where there is an
     * ordinary class path and nothing to bridge.
     */
    private static final boolean CODEGEN =
            Boolean.getBoolean("org.glassfish.orb.http.codec.fory.codegen");

    /**
     * @return whether generated serializers are in use
     */
    static boolean codeGenerationEnabled() {
        return CODEGEN;
    }

    /** A frame this codec encoded. */
    private static final byte KIND_FORY = 0;

    /**
     * A frame handed to Java serialization instead.
     * <p>
     * Throwables go this way. Fory reconstructs a plain exception faithfully
     * but loses the stack trace of an application subclass, and an exception
     * that arrives claiming to come from nowhere is worst exactly when someone
     * is trying to find out where it came from. Java serialization has a
     * special path for throwables and gets this right, and the exceptional path
     * is not where encoding speed matters.
     */
    private static final byte KIND_JAVA = 1;

    /** The codec throwables are handed to; see {@link #KIND_JAVA}. */
    private static final JavaSerializationMarshaller BUILT_IN = new JavaSerializationMarshaller();

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
                boolean java = o instanceof Throwable;
                byte[] encoded = java ? javaEncode(o) : fory.serialize(o);
                // Each object is framed by this codec rather than by the
                // library underneath: the transport writes several objects in
                // sequence, and the boundary between them has to be a property
                // of this format. The kind byte says which encoding follows.
                data.writeByte(java ? KIND_JAVA : KIND_FORY);
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
                byte kind = data.readByte();
                if (kind != KIND_FORY && kind != KIND_JAVA) {
                    throw new IOException("unknown frame kind " + kind
                            + "; the stream is not this codec's, or is out of step");
                }
                int length = data.readInt();
                if (length < 0) {
                    throw new IOException("negative frame length " + length);
                }
                byte[] encoded = data.readNBytes(length);
                if (encoded.length != length) {
                    throw new IOException("truncated frame: expected " + length
                            + " bytes but read " + encoded.length);
                }

                return kind == KIND_JAVA
                        ? javaDecode(encoded, loader, filter)
                        : fory.deserialize(encoded);
            }

            @Override
            public void close() throws IOException {
                data.close();
            }
        };
    }

    private static byte[] javaEncode(Object value) throws IOException {
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        try (Marshaller.ObjectWriter writer = BUILT_IN.newWriter(bytes)) {
            // Materialised first: the trace lives in a native structure until
            // getStackTrace is called, and writeObject only copies the field.
            materialiseStackTraces(value);
            writer.writeObject(value);
            writer.flush();
        }
        return bytes.toByteArray();
    }

    private static Object javaDecode(byte[] encoded, ClassLoader loader, ObjectInputFilter filter)
            throws IOException, ClassNotFoundException {
        try (Marshaller.ObjectReader reader =
                BUILT_IN.newReader(new java.io.ByteArrayInputStream(encoded), loader, filter)) {
            return reader.readObject();
        }
    }

    /**
     * Makes a throwable's stack trace real before it is encoded.
     *
     * <p>{@code Throwable.stackTrace} stays a shared empty sentinel until
     * {@code getStackTrace} is called; the trace lives in a native structure
     * until then. Java serialization materialises it on the way out, inside
     * {@code Throwable.writeObject}. A codec that reads the field directly sees
     * the sentinel and sends an exception with no stack trace - which arrives
     * looking like it was thrown from nowhere, exactly when someone is trying
     * to find out where it came from.
     *
     * @param value the object about to be written; anything that is not a
     *              throwable is left alone
     */
    private static void materialiseStackTraces(Object value) {
        for (Throwable t = value instanceof Throwable thrown ? thrown : null;
                t != null; t = t.getCause() == t ? null : t.getCause()) {
            t.getStackTrace();
            for (Throwable suppressed : t.getSuppressed()) {
                materialiseStackTraces(suppressed);
            }
        }
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
                .withCodegen(CODEGEN)
                .suppressClassRegistrationWarnings(true);
        if (filter != null) {
            builder = builder.withTypeChecker(new FilterBackedTypeChecker(loader, filter));
        }
        return builder.buildThreadSafeFory();
    }
}
